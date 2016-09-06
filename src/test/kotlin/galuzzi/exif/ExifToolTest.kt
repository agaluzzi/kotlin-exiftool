package galuzzi.exif

import org.testng.Assert.assertNotNull
import org.testng.annotations.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 *
 *
 * *Created by **agaluzzi** on 7/28/2016*
 */
class ExifToolTest
{
    companion object
    {
        var path:Path? = null

        fun getTestImagePath():Path
        {
            if (path == null)
            {
                val tmpdir = System.getProperty("java.io.tmpdir")
                assertNotNull(tmpdir)
                val file = Paths.get(tmpdir + File.separatorChar + "ExifToolTest.jpg")

                val resource = ClassLoader.getSystemResource("example.jpg")
                assertNotNull(resource)

                Files.write(file, resource.readBytes())
                file.toFile().deleteOnExit()
                path = file
            }
            return path!!
        }
    }

    @Test
    fun testQuery()
    {
        val tool = ExifTool.launch()

        tool.setOption("FastScan", "2")
        tool.setOption("Composite", "1")
        tool.setOption("Sort", "File")
        tool.setOption("Duplicates", "0")

        var binary = 0

        val info = tool.extractInfo(image)
        info.entries.sortedBy { it.key }.forEach {
            val tag:String = it.key
            val value:Any = it.value
            if (value is String)
            {
                print("$tag = $value")
                println()
            }
            else if (value is ByteArray)
            {
                println("$tag = ${value.size} bytes *********************")
                binary++

                if (tag == "ThumbnailImage")
                {
                    Files.write(Paths.get("C:\\Temp\\thumb.jpg"), value)
                }
            }
            else
            {
                throw Exception("unknown type")
            }
        }

        println("\nTotal: ${info.size} ($binary binary)")
    }
}
