package galuzzi.ext

/**
 * Extensions to standard Java file system classes.
 */

import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Stream

fun Path.exists():Boolean
{
    return Files.exists(this)
}

fun Path.isDirectory():Boolean
{
    return Files.isDirectory(this)
}

fun Path.isFile():Boolean
{
    return !Files.isDirectory(this)
}

fun Path.size():Long
{
    return Files.size(this)
}

fun Path.walk(action:(path:Path) -> Unit, maxDepth:Int = Int.MAX_VALUE)
{
    val stream:Stream<Path> = Files.walk(this, maxDepth)
    try
    {
        stream.forEach(action)
    }
    finally
    {
        stream.closeSafely()
    }
}

fun Path.getExtension():String
{
    val filename = this.fileName?.toString()
    return filename?.substringAfterLast('.')?.toLowerCase() ?: ""
}
