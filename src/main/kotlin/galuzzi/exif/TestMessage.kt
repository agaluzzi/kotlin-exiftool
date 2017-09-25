package galuzzi.exif

import java.util.*

/**
 * @author Aaron Galuzzi (9/24/2017)
 */
data class TestMessage(val byte:Int,
                       val int:Int,
                       val string:String,
                       val binary:ByteArray)
{
    override fun equals(other:Any?):Boolean
    {
        if (this === other) return true
        if (other !is TestMessage) return false

        if (byte != other.byte) return false
        if (int != other.int) return false
        if (string != other.string) return false
        if (!Arrays.equals(binary, other.binary)) return false

        return true
    }

    override fun hashCode():Int
    {
        var result = byte
        result = 31 * result + int
        result = 31 * result + string.hashCode()
        result = 31 * result + Arrays.hashCode(binary)
        return result
    }
}