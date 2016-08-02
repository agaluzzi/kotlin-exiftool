package galuzzi.exif

import galuzzi.io.Gobbler
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

        input.readOK()
    }

    fun clearOptions()
    {
        output.beginRequest(RequestType.ClearOptions)
        output.endRequest()

        input.readOK()
    }

    fun setTags(tagNames:Array<String>)
    {
        output.beginRequest(RequestType.SetTags)
        output.writeInt(tagNames.size)
        tagNames.forEach { output.writeString(it) }
        output.endRequest()

        input.readOK()
    }

    fun extractInfo(file:Path):TagInfo
    {
        output.beginRequest(RequestType.ExtractInfo)
        output.writeString(file.toAbsolutePath().toString())
        output.endRequest()

        return input.readTagInfo()
    }

    fun extractInfo(file:String):TagInfo
    {
        return extractInfo(Paths.get(file))
    }
}
