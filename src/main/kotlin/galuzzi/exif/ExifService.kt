package galuzzi.exif

import java.nio.file.Path
import java.util.concurrent.*

/**
 * A multi-threaded service for submitting concurrent, asynchronous Exif tag requests.
 *
 * @author Aaron Galuzzi (9/14/2017)
 */
class ExifService private constructor(threadCount:Int, private val setup:ExifTool.() -> Unit)
{
    @Volatile
    private var queue:BlockingQueue<Request>? = LinkedTransferQueue()
    private val pendingWorkers = CountDownLatch(threadCount)
    private val workers = Array(threadCount) { i -> Worker(i) }

    companion object
    {
        fun start(threadCount:Int = Runtime.getRuntime().availableProcessors(),
                  setup:ExifTool.() -> Unit = DEFAULT_SETUP):ExifService
        {
            if (threadCount < 1) throw IllegalArgumentException("Invalid thread count: $threadCount")
            val service = ExifService(threadCount, setup)
            service.startWorkerThreads()
            return service
        }

        val DEFAULT_SETUP:ExifTool.() -> Unit = {
            setOption("PrintConv", "0")
            setOption("FastScan", "2")
            setOption("Composite", "1")
            setOption("Duplicates", "0")
            setOption("SystemTags", "0")
        }
    }

    private fun startWorkerThreads()
    {
        workers.forEach { it.start() }
        pendingWorkers.await()
    }

    fun getTags(file:Path):CompletionStage<TagInfo>
    {
        val request = Request(file)
        val queueRef = queue
        if (queueRef == null)
        {
            request.completeExceptionally(shutdownException())
        }
        else
        {
            queueRef.put(request)
        }
        return request
    }

    private fun shutdownException()
            = IllegalStateException("Exif service is shutdown")

    fun shutdown()
    {
        val queueRef = queue ?: return // already shutdown
        queue = null

        workers.forEach { it.interrupt() }

        queueRef.apply {
            var req = poll()
            while (req != null)
            {
                req.completeExceptionally(shutdownException())
                req = poll()
            }
        }
    }

    private class Request(val path:Path) : CompletableFuture<TagInfo>()

    private inner class Worker(id:Int) : Thread()
    {
        init
        {
            name = "ExifTool-$id"
            isDaemon = true
        }

        override fun run()
        {
            val exifTool = ExifTool.launch().apply(setup)
            pendingWorkers.countDown()

            try
            {
                while (!interrupted())
                {
                    // Grab the next request off the queue
                    val queueRef = queue ?: break
                    val request:Request
                    try
                    {
                        request = queueRef.take() // blocking
                    }
                    catch (e:InterruptedException)
                    {
                        break
                    }

                    // Query exiftool for tags
                    try
                    {
                        val tags = exifTool.extractInfo(request.path)
                        request.complete(tags)
                    }
                    catch (e:Exception)
                    {
                        request.completeExceptionally(e)
                    }
                }
            }
            finally
            {
                exifTool.shutdown()
            }
        }
    }
}