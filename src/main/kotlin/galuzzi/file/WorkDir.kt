package galuzzi.file

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * @author Aaron Galuzzi (9/5/2016)
 */
class WorkDir private constructor(val path:Path)
{
    companion object
    {
        fun create(type:Type, name:String):WorkDir
        {
            val path = type.basedir().resolve(name)
            Files.createDirectories(path)
            return WorkDir(path)
        }
    }

    enum class Type(val systemProperty:String)
    {
        TEMP("java.io.tmpdir"),
        USER("user.home");

        fun basedir():Path
        {
            return Paths.get(System.getProperty(systemProperty))
        }
    }

    fun resolve(name:String):Path
    {
        return path.resolve(name)
    }
}