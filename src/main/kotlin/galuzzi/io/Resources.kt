package galuzzi.io

import galuzzi.file.WorkDir
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URL
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

/**
 * @author Aaron Galuzzi (9/5/2016)
 */
abstract class Resource
{
    abstract fun getName():String

    abstract protected fun open():InputStream

    fun <T> use(block:(InputStream) -> T):T
    {
        return open().use { block.invoke(it) }
    }

    open fun load():ByteArray
    {
        val buffer = ByteArrayOutputStream()
        use {
            it.copyTo(buffer)
        }
        return buffer.toByteArray()
    }

    open fun loadString(charset:Charset):String
    {
        return load().toString(charset)
    }

    fun loadString():String
    {
        return loadString(Charset.defaultCharset())
    }

    open fun copyTo(file:Path)
    {
        use {
            Files.copy(it, file, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    fun copyInto(dir:Path):Path
    {
        val file = dir.resolve(getName())
        copyTo(file)
        return file
    }

    fun copyInto(dir:WorkDir):Path
    {
        return copyInto(dir.path)
    }
}

fun getResource(name:String):Resource
{
    val url:URL? = ClassLoader.getSystemResource(name)
    if (url != null)
    {
        return URLResource(url)
    }

    val path = Paths.get(name)
    if (Files.exists(path))
    {
        return FileResource(path)
    }

    throw MissingResourceException(name, null, null)
}

private class URLResource(val url:URL):Resource()
{
    override fun getName():String
    {
        return url.path.substringAfterLast('/')
    }

    override fun open():InputStream
    {
        return url.openStream()
    }
}

private class FileResource(val path:Path):Resource()
{
    override fun getName():String
    {
        return path.fileName.toString()
    }

    override fun open():InputStream
    {
        return Files.newInputStream(path)
    }

    override fun load():ByteArray
    {
        return Files.readAllBytes(path)
    }

    override fun copyTo(file:Path)
    {
        Files.copy(path, file, StandardCopyOption.REPLACE_EXISTING)
    }
}