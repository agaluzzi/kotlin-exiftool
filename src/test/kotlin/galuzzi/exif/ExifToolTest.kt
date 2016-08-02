package galuzzi.exif

import galuzzi.ext.isFile
import galuzzi.ext.walk
import org.testng.annotations.Test
import java.nio.file.Path
import java.nio.file.Paths

/**
 *
 *
 * *Created by **agaluzzi** on 7/28/2016*
 */
class ExifToolTest
{
    val dir:Path = Paths.get("C:\\Users\\agaluzzi\\Pictures")
    val et = ExifTool.launch()

    init
    {
        et.setOption("FastScan", "2")
        et.setOption("Composite", "1")
        et.setOption("PrintConv", "0")
        et.setOption("Duplicates", "0")
        et.setOption("Sort", "File")
        et.setOption("SystemTags", "0")
        et.setOption("RequestAll", "0")
        et.setOption("Binary", "0")

        et.setTags(arrayOf("ImageWidth",
                           "ImageHeight",
                           "DateTimeOriginal",
                           "DateTimeDigitized",
                           "FileType",
                           "ThumbnailImage",
                           "MIMEType"))
    }

    @Test
    fun testExtract()
    {
        dir.walk {
            if (it.isFile())
            {
                println("--------------------------------------------------------------------------------")
                println("$it\n")

                val info = et.extractInfo(it)

                if (info.isError())
                {
                    println("Error -> ${info.errorMessage}")
                }
                else
                {
                    info.forEachSorted { tag, value ->
                        if (value is String)
                        {
                            println("$tag = $value")
                        }
                        else if (value is ByteArray)
                        {
                            println("*** $tag = ^^^^ ${value.size} bytes ^^^^")
                        }
                        else
                        {
                            throw Exception("unknown type")
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testPerformance()
    {
        var fileCount = 0
        var binaryCount = 0
        var errorCount = 0
        var nanos = 0L

        for (i in 1..25)
        {
            println("Round $i")

            dir.walk {
                if (it.isFile())
                {
                    fileCount++
                    val start = System.nanoTime()
                    val info = et.extractInfo(it)
                    val end = System.nanoTime()
                    nanos += (end - start)

                    if (info.isError())
                    {
                        errorCount++
                    }
                    else
                    {
                        info.forEach { tag, value ->
                            if (value is ByteArray)
                            {
                                binaryCount++
                            }
                        }
                    }
                }
            }
        }

        println()
        println(" Files: $fileCount")
        println("Binary: $binaryCount")
        println("Errors: $errorCount")

        val avgNanos = nanos / fileCount
        val avgMillis = avgNanos.toDouble() / 1000000
        println("\nAverage: %.3f ms".format(avgMillis))
    }
}
