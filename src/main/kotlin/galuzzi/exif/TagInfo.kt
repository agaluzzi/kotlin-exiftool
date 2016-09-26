package galuzzi.exif

import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
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

    fun getDateTime(tag:String, format:DateTimeFormatter):TemporalAccessor?
    {
        val str:String? = get(tag)
        return if (str != null && str.isNotEmpty()) format.parse(str) else null
    }

    fun getDateTimeOriginal():TemporalAccessor?
    {
        var ts:TemporalAccessor? = getDateTime("SubSecDateTimeOriginal", DATE_TIME_SUBSEC_FORMAT)
        if (ts == null)
        {
            ts = getDateTime("DateTimeOriginal", DATE_TIME_FORMAT)
        }
        return ts
    }

    fun getCreateDate():TemporalAccessor?
    {
        var ts:TemporalAccessor? = getDateTime("SubSecCreateDate", DATE_TIME_SUBSEC_FORMAT)
        if (ts == null)
        {
            ts = getDateTime("CreateDate", DATE_TIME_FORMAT)
        }
        return ts
    }

    fun getTimestamp():TemporalAccessor?
    {
        var ts:TemporalAccessor? = getDateTimeOriginal()
        if (ts == null)
        {
            ts = getCreateDate()
        }
        return ts
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

    companion object
    {
        val DATE_TIME_FORMAT:DateTimeFormatter = DateTimeFormatter.ofPattern("uuuu:MM:dd HH:mm:ss")
        val DATE_TIME_SUBSEC_FORMAT:DateTimeFormatter = DateTimeFormatter.ofPattern("uuuu:MM:dd HH:mm:ss.SSS")
    }
}
