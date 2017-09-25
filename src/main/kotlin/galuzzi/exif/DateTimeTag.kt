package galuzzi.exif

import java.time.format.DateTimeFormatter

/**
 * Some common date/time tags, in order of preference for determining the original timestamp.
 *
 * @author Aaron Galuzzi (9/12/2017)
 */
enum class DateTimeTag(val tagName:String, pattern:String)
{
    SUB_SEC_DATE_TIME_ORIGINAL("SubSecDateTimeOriginal", "uuuu:MM:dd HH:mm:ss.SSS"),
    DATE_TIME_ORIGINAL("DateTimeOriginal", "uuuu:MM:dd HH:mm:ss"),
    SUB_SEC_CREATE_DATE("SubSecCreateDate", "uuuu:MM:dd HH:mm:ss.SSS"),
    CREATE_DATE("CreateDate", "uuuu:MM:dd HH:mm:ss"),
    DATE_CREATED("DateCreated", "uuuu:MM:dd HH:mm:ss"),
    GPS_DATE_TIME("GPSDateTime", "uuuu:MM:dd HH:mm:ss.SSX");

    val formatter:DateTimeFormatter = DateTimeFormatter.ofPattern(pattern)
}