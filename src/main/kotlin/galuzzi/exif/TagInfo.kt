package galuzzi.exif

import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

/**
 * A mapping of Exif tags/values (the result of an ExtractInfo operation).
 */
class TagInfo(val result:Result, private val tags:Map<String, Any>)
{
    enum class Result(val code:Int)
    {
        OK(1),
        UNRECOGNIZED_FORMAT(2)
    }

    val size:Int
        get()
        {
            return tags.size
        }

    operator fun get(tag:String):String?
    {
        return tags[tag] as? String
    }

    fun getInt(tag:String):Int?
    {
        val str = get(tag)
        return str?.toInt()
    }

    fun getBinary(tag:String):ByteArray?
    {
        return tags[tag] as? ByteArray
    }

    fun getTimestamp():TemporalAccessor?
    {
        DateTimeTag.values().forEach {
            try
            {
                val timestamp = getDateTime(it)
                if (timestamp != null)
                {
                    return timestamp
                }
            }
            catch (_:Exception)
            {
                // just ignore and try the next one
            }
        }
        return null
    }

    fun getDateTime(tag:DateTimeTag):TemporalAccessor?
    {
        return getDateTime(tag.tagName, tag.formatter)
    }

    fun getDateTime(tag:String, formatter:DateTimeFormatter):TemporalAccessor?
    {
        val value:String? = get(tag)
        return if (value != null && value.isNotBlank()) formatter.parse(value) else null
    }

    fun forEach(action:(tag:String, value:Any) -> Unit)
    {
        tags.forEach(action)
    }
}
