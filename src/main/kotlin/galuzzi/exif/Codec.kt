package galuzzi.exif

import galuzzi.ext.closeSafely
import galuzzi.ext.readFully
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

//**********************************************************************
// Constants
//**********************************************************************

val BEGIN:Int = 0x0B1E55ED
val END:Int = 0x00FADE00

//**********************************************************************
// Enumerations
//**********************************************************************

interface Coded
{
    val code:Int
}

enum class RequestType(override val code:Int) : Coded
{
    SetOption(1),
    ClearOptions(2),
    SetTags(3),
    ExtractInfo(4),
}

enum class ResponseType(override val code:Int) : Coded
{
    OK(1),
    Error(2),
    TagInfo(3),
}

enum class ValueType(override val code:Int) : Coded
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
class Encoder(val output:OutputStream)
{
    val intBuffer:ByteBuffer = newIntBuffer()

    fun writeInt(value:Int)
    {
        intBuffer.putInt(0, value)
        output.write(intBuffer.array(), 0, 4)
    }

    fun writeByte(value:Int)
    {
        output.write(value)
    }

    fun writeChar(value:Char)
    {
        output.write(value.toInt())
    }

    fun writeString(value:String)
    {
        value.forEach { writeChar(it) }
        output.write(0)
    }

    fun writeBinary(data:ByteArray)
    {
        writeInt(data.size)
        output.write(data)
    }

    fun write(value:Coded)
    {
        writeByte(value.code)
    }

    fun writeBegin()
    {
        writeInt(BEGIN)
    }

    fun writeEnd()
    {
        writeInt(END)
    }

    fun flush()
    {
        output.flush()
    }

    fun beginRequest(type:RequestType)
    {
        writeBegin()
        write(type)
    }

    fun endRequest()
    {
        writeEnd()
        flush()
    }
}

//**********************************************************************
// Decoder
//**********************************************************************

/**
 * An decoder for reading EXIF responses from an input stream.
 */
class Decoder(val input:InputStream)
{
    val intBuffer:ByteBuffer = newIntBuffer()

    fun readInt():Int
    {
        input.readFully(intBuffer.array(), 0, 4)
        return intBuffer.getInt(0)
    }

    fun readByte():Int
    {
        val value = input.read()
        if (value < 0)
        {
            throw IOException("Failed to read single byte -- End of input stream")
        }
        return value
    }

    fun readChar():Char
    {
        return readByte().toChar()
    }

    fun readString():String
    {
        val sb = StringBuilder()
        var char = readChar()
        while (char != '\u0000')
        {
            sb.append(char)
            char = readChar()
        }
        return sb.toString()
    }

    fun readBinary():ByteArray
    {
        val length = readInt()
        val data = ByteArray(length)
        input.readFully(data, 0, length)
        return data
    }

    fun readValue():Any
    {
        val type = readByte()
        when (type)
        {
            ValueType.String.code -> return readString()
            ValueType.Binary.code -> return readBinary()
            else ->
            {
                throw corrupted("Unexpected value type: $type")
            }
        }
    }

    fun readOK()
    {
        readResponse(ResponseType.OK,
                     object : ResponseHandler
                     {
                         override fun read()
                         {
                             // nothing more to read
                         }

                         override fun onError(msg:String)
                         {
                             throw IOException("Error: $msg")
                         }
                     })
    }

    fun readTagInfo():TagInfo
    {
        val info = TagInfo()

        readResponse(ResponseType.TagInfo,
                     object : ResponseHandler
                     {
                         override fun read()
                         {
                             // Read the number of tag/value pairs
                             val count = readInt()

                             // Read each tag/value...
                             for (i in 1..count)
                             {
                                 val tag:String = readString()
                                 val value:Any = readValue()
                                 info.put(tag, value)
                             }
                         }

                         override fun onError(msg:String)
                         {
                             info.errorMessage = msg
                         }
                     })

        return info
    }

    fun readBegin()
    {
        readMagicNumber(BEGIN, "BEGIN")
    }

    fun readEnd()
    {
        readMagicNumber(END, "END")
    }

    private fun readResponse(expect:ResponseType, handler:ResponseHandler)
    {
        readBegin()
        val type = readByte()

        if (type == ResponseType.Error.code)
        {
            val message = readString()
            readEnd()
            handler.onError(message)
        }
        else if (type == expect.code)
        {
            handler.read()
            readEnd()
        }
        else
        {
            throw corrupted("Unexpected response type: $type")
        }
    }

    private fun readMagicNumber(number:Int, name:String)
    {
        val value = readInt()
        if (value != number)
        {
            throw corrupted("Wrong $name token; value was: 0x%08X".format(value))
        }
    }

    private fun corrupted(msg:String):IOException
    {
        input.closeSafely()
        return IOException("Input stream corrupted -- $msg")
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

private interface ResponseHandler
{
    fun read()

    fun onError(msg:String)
}
