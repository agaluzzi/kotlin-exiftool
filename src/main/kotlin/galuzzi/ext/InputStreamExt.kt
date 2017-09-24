package galuzzi.ext

/**
 * Extensions to standard Java I/O classes.
 */

import java.io.IOException
import java.io.InputStream

/**
 * Reads exactly some number of bytes from an [InputStream] into a [ByteArray].
 */
fun InputStream.readFully(dst:ByteArray, off:Int, len:Int)
{
    var offset = off
    var remaining = len

    while (remaining > 0)
    {
        val result = this.read(dst, offset, remaining)
        if (result < 0)
        {
            throw IOException("Failed to read exactly $len bytes -- End of input stream; Missing $remaining bytes")
        }
        remaining -= result
        offset += result
    }
}
