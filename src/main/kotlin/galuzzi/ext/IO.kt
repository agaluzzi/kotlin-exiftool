package galuzzi.ext

/**
 * Extensions to standard Java I/O classes.
 */

import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.InputStream

private val logger = LoggerFactory.getLogger("galuzzi.ext.IO")

/**
 * Closes a resource, catching and logging any exceptions.
 */
fun AutoCloseable.closeSafely()
{
    try
    {
        this.close()
    }
    catch(e:Exception)
    {
        logger.error("Error closing {}", this, e)
    }
}

/**
 * Reads exactly some number of bytes from an [InputStream] into a [ByteArray].
 */
fun InputStream.readFully(dst:ByteArray, off:Int, len:Int)
{
    var offset = off
    var remaining = len

    while (remaining > 0)
    {
        val result = this.read(dst, offset, remaining)
        if (result < 0)
        {
            throw IOException("Failed to read exactly $len bytes -- End of input stream; Missing $remaining bytes")
        }
        remaining -= result
        offset += result
    }
}
