package galuzzi.exif

import galuzzi.file.WorkDir
import galuzzi.io.getResource
import org.testng.Assert
import org.testng.Assert.assertEquals
import org.testng.annotations.AfterTest
import org.testng.annotations.BeforeClass
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
                        getResource("example2.jpg").copyInto(dir),
                        getResource("example3.jpg").copyInto(dir))
            }
            return paths!!
        }
    }

    var tool:ExifTool = ExifTool.launch()

    @BeforeClass
    fun setup()
    {
        tool.setOption("Composite", "1")
        tool.setOption("Sort", "File")
        tool.setOption("Duplicates", "0")
        tool.setOption("PrintConv", "0")
    }

    @AfterTest
    fun teardown()
    {
        tool.shutdown()
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
                            assertEquals(trim(actual), trim(expect), "Wrong value for tag '$tag'")
                        }
                    }
                }

        val expectThumb = getResource("${basename}_thumb.jpg").load()
        val actualThumb = info.getBinary("ThumbnailImage")

        Assert.assertNotNull(actualThumb)
        assertEquals(actualThumb!!.size, expectThumb.size, "Wrong thumbnail size")
    }

    @Test(dataProvider = "images")
    fun testTimestamp(image:Path)
    {
        val expect:String = when (image.fileName.toString())
        {
            "example.jpg" -> "2003-12-14T12:01:44"
            "example2.jpg" -> "2016-09-11T10:56:23"
            "example3.jpg" -> "2016-07-23T13:35:48.314"
            else -> throw IllegalArgumentException("Unknown filename: ${image.fileName}")
        }

        val info:TagInfo = tool.extractInfo(image)
        val result = info.getTimestamp().toString().substringAfter("to ")
        assertEquals(result, expect)
    }

    fun trim(str:String?):String?
    {
        return str?.replace(Regex("^(\\d+)\\.(\\d{4}).*$"), "$1.$2")
    }
}
