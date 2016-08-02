package galuzzi.ext

/**
 * Extensions to standard Java file system classes.
 */

import java.nio.file.Files
import java.nio.file.Path

fun Path.isDirectory():Boolean
{
    return Files.isDirectory(this)
}

fun Path.isFile():Boolean
{
    return !Files.isDirectory(this)
}

fun Path.list(action:(path:Path) -> Unit)
{
    Files.list(this).forEach(action)
}

fun Path.walk(action:(path:Path) -> Unit)
{
    Files.walk(this).forEach(action)
}
