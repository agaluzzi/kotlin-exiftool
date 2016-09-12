package galuzzi.exif

import galuzzi.file.WorkDir
import galuzzi.io.Gobbler
import galuzzi.io.getResource
import org.testng.Assert.assertEquals
import java.io.BufferedInputStream
import java.io.BufferedOutputStream

fun main(args:Array<String>)
{
    val dir = WorkDir.create(WorkDir.Type.TEMP, "CodecTest")
    val path = getResource("CodecTest.pl").copyInto(dir)
    getResource("Codec.pm").copyInto(dir)

    val cmd = arrayListOf("perl", path.toString())

    val proc = ProcessBuilder(cmd).directory(dir.path.toFile()).start()
    val stdin:BufferedOutputStream = proc.outputStream.buffered()
    val stdout:BufferedInputStream = proc.inputStream.buffered()
    val stderr:BufferedInputStream = proc.errorStream.buffered()

    Gobbler("stderr", stderr).start()

    val encoder = Encoder(stdin)
    val decoder = Decoder(stdout)

    val intVal:Int = 0x03FF4278
    val byteVal:Int = 0x42
    val strVal:String = "Hello\nto the world!!\r\n"
    val binVal:ByteArray = byteArrayOf(1, 2, 3, 4, 5, 6, 7)

    encoder.writeBegin()
    encoder.writeInt(intVal)
    encoder.writeByte(byteVal)
    encoder.writeString(strVal)
    encoder.writeBinary(binVal)
    encoder.writeEnd()
    encoder.flush()
    println("Wrote Message")

    decoder.readBegin()
    assertEquals(decoder.readInt(), intVal)
    assertEquals(decoder.readByte(), byteVal)
    assertEquals(decoder.readString(), strVal)
    assertEquals(decoder.readBinary(), binVal)
    decoder.readEnd()
    println("Read Message")

}


