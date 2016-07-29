package galuzzi.exif

import galuzzi.io.Gobbler
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths

/**
 * @author Aaron Galuzzi (3/23/2016)
 */
class ExifTool private constructor(process:Process)
{
    companion object
    {
        fun launch():ExifTool
        {
            // TODO: Unpack Perl script and ExifTool library

            val cmd = arrayListOf("perl",
                                  "C:\\Projects\\agaluzzi\\kotlin-exiftool\\src\\main\\resources\\ExifToolWrapper.pl")

            val process = ProcessBuilder(cmd).start()

            Gobbler("stderr", process.errorStream).start()

            return ExifTool(process)
        }
    }

    val output = Encoder(process.outputStream.buffered())
    val input = Decoder(process.inputStream.buffered())

    fun setOption(name:String, value:String)
    {
        output.beginRequest(RequestType.SetOption)
        output.writeString(name)
        output.writeString(value)
        output.endRequest()

        readOK()
    }

    fun clearOptions()
    {
        output.beginRequest(RequestType.ClearOptions)
        output.endRequest()

        readOK()
    }

    fun extractInfo(file:Path):Map<String, Any>
    {
        output.beginRequest(RequestType.ExtractInfo)
        output.writeString(file.toAbsolutePath().toString())
        output.endRequest()

        return input.readTagInfo()
    }

    fun extractInfo(file:String):Map<String, Any>
    {
        return extractInfo(Paths.get(file))
    }

    private fun readOK()
    {
        input.readBegin()
        val type = input.readByte()
        val msg:String
        when (type)
        {
            ResponseType.OK.code -> msg = ""

            ResponseType.Error.code -> msg = input.readString()

            else -> throw IOException("Unexpected response type: $type")
        }
        input.readEnd()

        if (type != ResponseType.OK.code)
        {
            throw Exception("Error: $msg")
        }
    }
}

interface ExifHandler
{
    fun string(tag:String, value:String)

    fun binary(tag:String, value:ByteArray)

    fun error(message:String)
}


