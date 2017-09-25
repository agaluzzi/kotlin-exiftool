package galuzzi.exif

import org.testng.Assert.assertEquals
import org.testng.annotations.Test

/**
 * @author Aaron Galuzzi (9/24/2017)
 */
class ProtocolTest
{
    private val tool = ExifTool.launch()

    @Test
    fun empty()
    {
        testEcho(TestMessage(0, 0, "", ByteArray(0)))
    }

    @Test
    fun limits()
    {
        testEcho(TestMessage(0xFF,
                             0xFFFFFFFF.toInt(),
                             "This is a string with special \u0000 characters !@#$%^&*()_+-=~`",
                             byteArrayOf(-128, -1, 0, 1, 127)))
    }

    @Test
    fun byteOrder()
    {
        testEcho(TestMessage(0xAB,
                             0x0A0B0C0D,
                             "...",
                             byteArrayOf(1, 2, 3, 4)))
    }

    private fun testEcho(msg:TestMessage)
    {
        val result = tool.test(msg)
        assertEquals(msg, result)
    }
}