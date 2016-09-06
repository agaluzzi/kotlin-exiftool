package galuzzi.exif

val image = ExifToolTest.getTestImagePath()
val seconds = 3

fun main(args:Array<String>)
{
    testPerformance()
    testPerformance("PrintConv", "0")
    testPerformance("FastScan", "2")
    testPerformance("Sort", "File")
    testPerformance("Composite", "1")
    testPerformance("Duplicates", "0")
    testPerformance("optimized")
}

fun testPerformance(key:String? = null, value:String? = null)
{
    val et = ExifTool.launch()

    println("---------------------------------------------------")

    if (key == "optimized")
    {
        println("optimized")
        et.setOption("PrintConv", "0")
        et.setOption("FastScan", "2")
        et.setOption("Sort", "File")
    }
    else if (key != null && value != null)
    {
        println("$key = $value")
        et.setOption(key, value)
    }
    else
    {
        println("default")
    }

    var count = 0
    val deadline = System.currentTimeMillis() + (seconds * 1000)

    while (System.currentTimeMillis() < deadline)
    {
        et.extractInfo(image)
        count++
    }

    val perSec = (count / seconds)
    println("%,d/sec".format(perSec))

    et.shutdown()
}
