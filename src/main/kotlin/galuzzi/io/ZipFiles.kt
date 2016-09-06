package galuzzi.io

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.zip.ZipInputStream

/**
 * @author Aaron Galuzzi (9/5/2016)
 */
fun extractZip(resource:Resource, dir:Path)
{
    resource.use {
        val zip = ZipInputStream(it)
        var entry = zip.nextEntry
        while (entry != null)
        {
            val file:Path = dir.resolve(entry.name)
            if (entry.isDirectory)
            {
                Files.createDirectories(file)
            }
            else
            {
                Files.copy(zip, file, StandardCopyOption.REPLACE_EXISTING)
            }
            entry = zip.nextEntry
        }
    }
}