package galuzzi.exif

import org.testng.annotations.Test
import java.nio.file.Files
import java.nio.file.Paths

/**
 *
 *
 * *Created by **agaluzzi** on 7/28/2016*
 */
class ExifToolTest
{
    @Test
    fun testSetOption()
    {
        val image = "C:\\Users\\agaluzzi\\Pictures\\test.JPG"

        val et = ExifTool.launch()
        val defaults = et.extractInfo(image)

        et.setOption("FastScan", "2")
        et.setOption("Composite", "1")
        et.setOption("Sort", "File")
        et.setOption("PrintConv", "0")
        et.setOption("Duplicates", "0")

        var binary = 0

        val info = et.extractInfo(image)
        info.entries.sortedBy { it.key }.forEach {
            val tag:String = it.key
            val value:Any = it.value
            if (value is String)
            {
                print("$tag = $value")
                if (value != defaults[tag])
                {
                    print(" *** default = " + defaults[tag])
                }
                println()
            }
            else if (value is ByteArray)
            {
                println("$tag = ${value.size} bytes *********************")
                binary++

                if( tag == "ThumbnailImage" )
                {
                    Files.write(Paths.get("C:\\Temp\\exiftool\\thumb.jpg"), value)
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
