
import kotlinx.coroutines.yield
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.arrayTexture
import org.openrndr.draw.colorBuffer
import org.openrndr.draw.loadImage
import org.openrndr.ffmpeg.loadVideo
import org.openrndr.ffmpeg.loadVideoDevice
import org.openrndr.launch
import org.openrndr.math.map
import java.io.File
import kotlin.math.max
import kotlin.math.min

fun main() = application {
    configure {
        width = 1080 / 2
        height = 1920 / 2
    }
    program {

        val files = File("offline-data/offline-data/red/").listFiles()?.filter { it.isFile }?.sortedBy {
            it.nameWithoutExtension.toInt()
        }!!.take(300)
        val at = arrayTexture(width, height, files!!.size)

        files.mapIndexed { i, it ->
            loadImage(it).copyTo(at, i)
            println(it.nameWithoutExtension)
        }

        val kinect = Kinect()
        kinect.start(32)

        var min = 0.5
        var max = 4.1

        println((files.size - 1).toDouble())


        extend {
            kinect.videoTexture.update(kinect.colorWidth, kinect.colorHeight, kinect.colorFrame)
            kinect.skeletons.firstNotNullOf {
                if(it != null && it.isTracked) {
                    val z = it.get3DJointZ(1).toDouble()


                    println("$min min    $max max") //0.5 4.1

                    val mappedDistance = map(min, max, (files.size - 1).toDouble(), 0.0, z)
                    drawer.image(at, mappedDistance.toInt())

                }
            }






        }
    }
}
