package galuzzi.exif

import galuzzi.file.WorkDir
import galuzzi.io.getResource
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.nio.file.Path
import java.util.regex.Pattern

/**
 *
 *
 * *Created by **agaluzzi** on 7/28/2016*
 */
class ExifToolTest
{
    companion object
    {
        val excludes = setOf("Directory",
                             "FileName",
                             "FileModifyDate",
                             "FileAccessDate",
                             "FileCreateDate",
                             "FilePermissions",
                             "ThumbnailImage",
                             "DataDump",
                             "ThumbnailImage",
                             "PreviewImage")

        var paths:Array<Path>? = null

        fun getTestImagePaths():Array<Path>
        {
            if (paths == null)
            {
                val dir = WorkDir.create(WorkDir.Type.TEMP, "ExifToolTest")
                paths = arrayOf(
                        getResource("example.jpg").copyInto(dir),
                        getResource("example2.jpg").copyInto(dir))
            }
            return paths!!
        }
    }

    @DataProvider
    fun images():Array<Array<Path>>
    {
        val paths:Array<Path> = getTestImagePaths()
        return Array(paths.size, { i -> arrayOf(paths[i]) })
    }

    @Test(dataProvider = "images")
    fun testQuery(image:Path)
    {
        val tool = ExifTool.launch()
        tool.setOption("Composite", "1")
        tool.setOption("Sort", "File")
        tool.setOption("Duplicates", "0")
        tool.setOption("PrintConv", "0")

        val info:TagInfo = tool.extractInfo(image)

        val basename = image.fileName.toString().substringBeforeLast('.')

        getResource("$basename.info")
                .loadString()
                .split(Pattern.compile("\r?\n"))
                .forEach {
                    if (!it.isBlank())
                    {
                        val tag = it.substring(0, 32).trim()
                        val expect = it.substring(33).trim()
                        val actual = info[tag]?.trim()
                        if (!excludes.contains(tag))
                        {
                            Assert.assertEquals(trim(actual), trim(expect), "Wrong value for tag '$tag'")
                            println(trim(actual))
                        }
                    }
                }

        val expectThumb = getResource("${basename}_thumb.jpg").load()
        val actualThumb = info.getBinary("ThumbnailImage")

        Assert.assertNotNull(actualThumb)
        Assert.assertEquals(actualThumb!!.size, expectThumb.size, "Wrong thumbnail size")
    }

    fun trim(str:String?):String?
    {
        return str?.replace(Regex("^(\\d+)\\.(\\d{4}).*$"), "$1.$2")
    }
}
