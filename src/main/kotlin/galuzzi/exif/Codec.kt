package galuzzi.exif

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

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
    ExtractInfo(3),
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
 * <p>
 * <i>Created by <b>agaluzzi</b> on 7/28/2016</i>
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
        value.forEach { output.write(it.toInt()) }
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

class Decoder(val input:InputStream)
{
    val intBuffer:ByteBuffer = newIntBuffer()

    fun readInt():Int
    {
        read(4, input, intBuffer.array())
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
        read(length, input, data)
        return data
    }

    fun readValue():Any
    {
        val type = readByte()
        when (type)
        {
            ValueType.String.code -> return readString()
            ValueType.Binary.code -> return readBinary()
            else -> throw IOException("Input stream corrupted -- Unexpected value type: $type")
        }
    }

    fun readResponse(expect:ResponseType, handler:() -> Unit)
    {
        readBegin()
        val type = readByte()

        if (type == ResponseType.Error.code)
        {
            val message = readString()
            readEnd()
            throw IOException("Error: $message")
        }
        else if (type == expect.code)
        {
            handler.invoke()
            readEnd()
        }
        else
        {
            throw IOException("Input stream corrupted -- Unexpected response type: $type")
        }
    }

    fun readOK()
    {
        readResponse(ResponseType.OK) {/* nothing to do */ }
    }

    fun readTagInfo():Map<String, Any>
    {
        val map = HashMap<String, Any>()

        readResponse(ResponseType.TagInfo) {

            val count = readInt()

            for (i in 1..count)
            {
                val tag:String = readString()
                val value:Any = readValue()
                map[tag] = value
            }
        }

        return map
    }

    fun readBegin()
    {
        readMagicNumber(BEGIN, "BEGIN")
    }

    fun readEnd()
    {
        readMagicNumber(END, "END")
    }

    private fun readMagicNumber(number:Int, name:String)
    {
        val value = readInt()
        if (value != number)
        {
            throw IOException("Input stream corrupted -- Wrong $name token; value was: 0x%08X".format(value))
        }
    }
}

//**********************************************************************
// Helpers
//**********************************************************************

private fun newIntBuffer():ByteBuffer
{
    return ByteBuffer.allocate(4).order(ByteOrder.nativeOrder())
}

private fun read(length:Int, input:InputStream, dst:ByteArray)
{
    var offset = 0
    var remaining = length

    while (remaining > 0)
    {
        val result = input.read(dst, offset, remaining)
        if (result < 0)
        {
            throw IOException("Failed to read $length bytes -- End of input stream")
        }
        remaining -= result
        offset += result
    }
}
