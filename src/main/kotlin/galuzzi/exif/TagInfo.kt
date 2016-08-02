package galuzzi.exif

import java.util.*

/**
 * A collection of EXIF tag information
 */
class TagInfo
{
    private val map = HashMap<String, Any>()

    val size:Int
        get()
        {
            return map.size
        }

    var errorMessage:String? = null

    fun isError():Boolean
    {
        return errorMessage != null
    }

    fun put(tag:String, value:Any)
    {
        map[tag] = value
    }

    operator fun get(tag:String):String?
    {
        return map[tag] as? String
    }

    fun getInt(tag:String):Int?
    {
        val str = get(tag)
        return str?.toInt()
    }

    fun getBinary(tag:String):ByteArray?
    {
        return map[tag] as? ByteArray
    }

    fun forEach(action:(tag:String, value:Any) -> Unit)
    {
        map.forEach(action)
    }

    fun forEachSorted(action:(tag:String, value:Any) -> Unit)
    {
        map.entries.sortedBy { it.key }
                .forEach { action.invoke(it.key, it.value) }
    }
}
