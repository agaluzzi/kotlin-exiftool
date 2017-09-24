package galuzzi.io

import java.io.InputStream
import java.security.MessageDigest

/**
 * Some utilities for computing hashes.
 *
 * @author Aaron Galuzzi (9/17/2017)
 */

private fun newSHA256()
        = MessageDigest.getInstance("SHA-256")

fun sha256(vararg resources:Resource):ByteArray
{
    val digest = newSHA256()
    resources.forEach { resource ->
        digest.update(sha256(resource))
    }
    return digest.digest()
}

fun sha256(resource:Resource):ByteArray
{
    return resource.use { sha256(it) }
}

fun sha256(stream:InputStream):ByteArray
{
    val digest = newSHA256()

    val buffer = ByteArray(4096)
    var length = stream.read(buffer)
    while (length >= 0)
    {
        digest.update(buffer, 0, length)
        length = stream.read(buffer)
    }

    return digest.digest()
}

fun toHex(bytes:ByteArray):String
{
    return bytes.map { b -> b.toInt() and 0xFF }
            .map { i -> Integer.toHexString(i) }
            .map { s -> s.padStart(2, '0') }
            .joinToString(separator = "")
}