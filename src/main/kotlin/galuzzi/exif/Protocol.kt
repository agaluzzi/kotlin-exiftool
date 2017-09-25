package galuzzi.exif

import galuzzi.exif.MagicNumber.BEGIN
import galuzzi.exif.MagicNumber.END
import galuzzi.ext.closeSafely
import galuzzi.ext.readFully
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

//**********************************************************************
// Enumerations
//**********************************************************************

private enum class MagicNumber(val value:Int)
{
    BEGIN(0x0B1E55ED),
    END(0x00FADE00)
}

private enum class RequestType(val code:Int)
{
    SetOption(1),
    ClearOptions(2),
    SetTags(3),
    ExtractInfo(4),
    Test(0xAA)
}

private enum class ResponseType(val code:Int)
{
    OK(1),
    TagInfo(2),
    Error(0xFF),
    Echo(0xAA)
}

private enum class ValueType(val code:Int)
{
    String(1),
    Binary(2),
}

//**********************************************************************
// Encoder
//**********************************************************************

/**
 * An encoder for writing EXIF requests to an output stream.
 */
internal class Encoder(private val output:OutputStream)
{
    private val intBuffer:ByteBuffer = newIntBuffer()

    private fun writeByte(value:Int)
    {
        output.write(value)
    }

    private fun writeChar(value:Char)
    {
        writeByte(value.toInt())
    }

    private fun writeInt(value:Int)
    {
        intBuffer.putInt(0, value)
        output.write(intBuffer.array(), 0, 4)
    }

    private fun writeMagicNumber(number:MagicNumber)
    {
        writeInt(number.value)
    }

    private fun writeString(value:String)
    {
        writeInt(value.length)
        value.forEach { writeChar(it) }
    }

    private fun writeBinary(value:ByteArray)
    {
        writeInt(value.size)
        output.write(value)
    }

    private fun writeRequest(type:RequestType, body:() -> Unit)
    {
        writeMagicNumber(BEGIN)
        writeByte(type.code)
        body.invoke()
        writeMagicNumber(END)
        output.flush()
    }

    fun setOption(name:String, value:String)
    {
        writeRequest(RequestType.SetOption)
        {
            writeString(name)
            writeString(value)
        }
    }

    fun clearOptions()
    {
        writeRequest(RequestType.ClearOptions)
        {
            // empty body
        }
    }

    fun setTags(tagNames:Array<String>)
    {
        writeRequest(RequestType.SetTags)
        {
            writeInt(tagNames.size)
            tagNames.forEach { writeString(it) }
        }
    }

    fun extractInfo(path:String)
    {
        writeRequest(RequestType.ExtractInfo)
        {
            writeString(path)
        }
    }

    fun test(msg:TestMessage)
    {
        writeRequest(RequestType.Test)
        {
            writeByte(msg.byte)
            writeInt(msg.int)
            writeString(msg.string)
            writeBinary(msg.binary)
        }
    }
}

//**********************************************************************
// Decoder
//**********************************************************************

/**
 * An decoder for reading EXIF responses from an input stream.
 */
internal class Decoder(private val input:InputStream)
{
    private val intBuffer:ByteBuffer = newIntBuffer()

    private fun readByte():Int
    {
        val value = input.read()
        if (value < 0)
        {
            throw IOException("End of input stream")
        }
        return value
    }

    private fun readChar():Char
    {
        return readByte().toChar()
    }

    private fun readInt():Int
    {
        input.readFully(intBuffer.array(), 0, 4)
        return intBuffer.getInt(0)
    }

    private fun readValue():Any
    {
        val type = readByte()
        return when (type)
        {
            ValueType.String.code -> readString()
            ValueType.Binary.code -> readBinary()
            else ->
            {
                corrupted("Unexpected value type: $type")
            }
        }
    }

    private fun readString():String
    {
        val len = readInt()
        val chars = CharArray(len)
        for (i in chars.indices)
        {
            chars[i] = readChar()
        }
        val str = String(chars)
        return str
    }

    private fun readBinary():ByteArray
    {
        val length = readInt()
        val data = ByteArray(length)
        input.readFully(data, 0, length)
        return data
    }

    private fun <T> readResponse(expect:ResponseType, readBody:Decoder.() -> T):T
    {
        readMagicNumber(BEGIN)
        val type = readByte()
        when (type)
        {
            ResponseType.Error.code ->
            {
                val message = readString()
                readMagicNumber(END)
                throw ExifToolException(message)
            }
            expect.code ->
            {
                val value = readBody.invoke(this)
                readMagicNumber(END)
                return value
            }
            else -> corrupted("Unexpected response type: $type")
        }
    }

    private fun readMagicNumber(number:MagicNumber)
    {
        val value = readInt()
        if (value != number.value)
        {
            corrupted("Expected %s 0x%08X, but was: 0x%08X".format(number, number.value, value))
        }
    }

    private fun corrupted(msg:String):Nothing
    {
        input.closeSafely()
        throw IOException("Input stream corrupted -- $msg")
    }

    fun readOK()
    {
        readResponse(ResponseType.OK)
        {
            // no body
        }
    }

    fun readTagInfo():TagInfo
    {
        return readResponse(ResponseType.TagInfo)
        {
            // Read the result code
            val resultCode = readByte()
            val result = when (resultCode)
            {
                TagInfo.Result.OK.code -> TagInfo.Result.OK
                TagInfo.Result.UNRECOGNIZED_FORMAT.code -> TagInfo.Result.UNRECOGNIZED_FORMAT
                else -> corrupted("Unexpected result code: $resultCode")
            }

            // Read the number of tag/value pairs
            val count = readInt()

            // Read each tag/value...
            val tags = HashMap<String, Any>()
            for (i in 1..count)
            {
                val tag:String = readString()
                val value:Any = readValue()
                tags[tag] = value
            }

            TagInfo(result, tags)
        }
    }

    fun readEcho():TestMessage
    {
        return readResponse(ResponseType.Echo)
        {
            TestMessage(readByte(),
                        readInt(),
                        readString(),
                        readBinary())
        }
    }
}

//**********************************************************************
// Helpers
//**********************************************************************

/**
 * Creates a 4-byte [ByteBuffer] for holding 32-bit integers, using the platform native byte ordering.
 */
private fun newIntBuffer():ByteBuffer
{
    return ByteBuffer.allocate(4).order(ByteOrder.nativeOrder())
}
