package galuzzi.exif

import java.nio.file.Files
import java.nio.file.Path

val seconds = 3

fun main(args:Array<String>)
{
    ExifToolTest.getTestImagePaths().forEach {
        println("Image size = ${Files.size(it)} bytes")
        testPerformance(it, "Warmup") {}
        testPerformance(it, "Defaults") {}
        testPerformance(it, "No Print Conversion") { setOption("PrintConv", "0") }
        testPerformance(it, "No System Tags") { setOption("SystemTags", "0") }
        testPerformance(it, "Fast Mode") { setOption("FastScan", "2") }
        testPerformance(it, "With Composite Tags") { setOption("Composite", "1") }
        testPerformance(it, "No Dups") { setOption("Duplicates", "0") }
        testPerformance(it, "** Optimized") {
            setOption("PrintConv", "0")
            setOption("SystemTags", "0")
            setOption("FastScan", "2")
            setOption("Composite", "1")
            setOption("Duplicates", "0")
        }
    }

}

fun testPerformance(path:Path, title:String, setup:ExifTool.() -> Unit)
{
    println("---------------------------------------------------")
    println("$title...")

    val tool = ExifTool.launch()
    setup.invoke(tool)

    var count = 0
    val deadline = System.currentTimeMillis() + (seconds * 1000)

    while (System.currentTimeMillis() < deadline)
    {
        tool.extractInfo(path)
        count++
    }

    val perSec = (count / seconds)
    println("%,d/sec".format(perSec))

    tool.shutdown()
}
