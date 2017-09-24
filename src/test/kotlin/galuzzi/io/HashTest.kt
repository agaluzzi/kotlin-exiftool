package galuzzi.io

import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import java.io.ByteArrayInputStream

/**
 * @author Aaron Galuzzi (9/17/2017)
 */
class HashTest
{
    @Test
    fun sha256_inputStream()
    {
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                     toHex(sha256(ByteArrayInputStream(kotlin.ByteArray(0)))))

        assertEquals("a5181065dae9437211283f370f7ad850be92e13d3de4c6661f1b5aefd0318b49",
                     toHex(sha256(ByteArrayInputStream("Digest me...".toByteArray()))))
    }

    @Test
    fun sha256_resource()
    {
        assertEquals("8a9d04b92d0de5836c59ede8ae421235488e4031e893e07b1fe7e4b78f6a9901",
                     toHex(sha256(getResource("example.jpg"))))
    }
}