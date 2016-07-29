package galuzzi.io

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

/**
 * <p>
 * <i>Created by <b>agaluzzi</b> on 7/28/2016</i>
 */
class Gobbler(val label:String, val input:InputStream) : Thread()
{
    override fun run()
    {
        val reader = BufferedReader(InputStreamReader(input))
        while (true)
        {
            val line:String = reader.readLine() ?: return
            println("[$label] $line")
        }
    }
}
