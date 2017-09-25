package galuzzi.exif

import galuzzi.ext.exists
import galuzzi.file.WorkDir
import galuzzi.io.Gobbler
import galuzzi.io.extractZip
import galuzzi.io.getResource
import galuzzi.io.sha256
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

/**
 * The main wrapper for ExifTool.
 *
 * Each instance of this class launches a Perl process with a script that forms the interface/bridge between the JVM and
 * the Perl libraray.
 *
 * @author Aaron Galuzzi (3/23/2016)
 */
class ExifTool private constructor(private val process:Process)
{
    companion object
    {
        private val logger = LoggerFactory.getLogger("exif-tool")!!

        fun launch():ExifTool
        {
            val workingDir:Path = unpack()

            val process = ProcessBuilder("perl", "ExifToolWrapper.pl")
                    .directory(workingDir.toFile())
                    .start()

            logger.debug("Launched ExifTool Wrapper from: {}", workingDir)

            return ExifTool(process)
        }

        @Synchronized
        private fun unpack():Path
        {
            val exifToolZip = getResource("exiftool-lib-10.25.zip")
            val protocolModule = getResource("Protocol.pm")
            val wrapperScript = getResource("ExifToolWrapper.pl")

            // Compute hash of all resources to be unpacked
            val hash = sha256(exifToolZip, protocolModule, wrapperScript)

            val dir = WorkDir.create(WorkDir.Type.TEMP, "kotlin-exiftool")
            val hashFile = dir.resolve("hash")
            if (!hashFile.exists() || !Arrays.equals(Files.readAllBytes(hashFile), hash))
            {
                logger.debug("Unpacking into $dir")
                extractZip(exifToolZip, dir.path)
                protocolModule.copyInto(dir)
                wrapperScript.copyInto(dir)
                Files.write(hashFile, hash)
            }
            return dir.path
        }
    }

    init
    {
        Gobbler("stderr", process.errorStream).start()
    }

    private val output = Encoder(process.outputStream.buffered())
    private val input = Decoder(process.inputStream.buffered())

    fun setOption(name:String, value:String)
    {
        output.setOption(name, value)
        input.readOK()
    }

    fun clearOptions()
    {
        output.clearOptions()
        input.readOK()
    }

    fun setTags(tagNames:Array<String>)
    {
        output.setTags(tagNames)
        input.readOK()
    }

    fun extractInfo(file:Path):TagInfo
    {
        output.extractInfo(file.toAbsolutePath().toString())
        return input.readTagInfo()
    }

    fun extractInfo(file:String):TagInfo
    {
        return extractInfo(Paths.get(file))
    }

    internal fun test(msg:TestMessage):TestMessage
    {
        output.test(msg)
        return input.readEcho()
    }

    fun shutdown()
    {
        process.destroy()
        process.waitFor()
        logger.debug("Shutdown ExifTool Wrapper")
    }
}
