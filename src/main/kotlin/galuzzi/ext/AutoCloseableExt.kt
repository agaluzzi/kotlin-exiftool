package galuzzi.ext

import org.slf4j.LoggerFactory

/**
 * @author Aaron Galuzzi (9/24/2017)
 */
private val logger = LoggerFactory.getLogger("galuzzi.ext")

/**
 * Closes a resource, catching and logging any exceptions.
 */
fun AutoCloseable.closeSafely()
{
    try
    {
        this.close()
    }
    catch (e:Exception)
    {
        logger.error("Error closing {}", this, e)
    }
}