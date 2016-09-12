package galuzzi.exif

import galuzzi.file.WorkDir
import galuzzi.io.Gobbler
import galuzzi.io.extractZip
import galuzzi.io.getResource
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * @author Aaron Galuzzi (3/23/2016)
 */
class ExifTool private constructor(val process:Process)
{
    companion object
    {
        val forceExtract = true

        fun launch():ExifTool
        {
            val script = unpack()
            val process = ProcessBuilder("perl", script.toAbsolutePath().toString()).start()
            return ExifTool(process)
        }

        @Synchronized
        private fun unpack():Path
        {
            val dir = WorkDir.create(WorkDir.Type.TEMP, "kotlin-exiftool")
            val script = dir.resolve("ExifToolWrapper.pl")
            if (forceExtract || !Files.exists(script))
            {
                extractZip(getResource("exiftool-lib-10.25.zip"), dir.path)
                getResource("Codec.pm").copyInto(dir)
                getResource("ExifToolWrapper.pl").copyInto(dir) // do last
            }
            return script
        }
    }

    init
    {
        Gobbler("stderr", process.errorStream).start()
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

    fun shutdown():Unit
    {
        process.destroy()
        process.waitFor()
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
