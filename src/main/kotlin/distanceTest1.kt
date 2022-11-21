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
        width = 1920 / 2
        height = 1080 / 2
    }
    program {

        val files = File("data/images/redApple/").listFiles()?.filter { it.isFile }?.sortedBy {
            it.nameWithoutExtension.toInt()
        }
        val at = arrayTexture(width, height, files!!.size)

        val webcam = loadVideoDevice().apply { play() }
        val cb = colorBuffer(width, height)

        val bp = BlazePoseDetector.load()

        webcam.newFrame.listen {
            it.frame.copyTo(cb,
                sourceRectangle = it.frame.bounds.toInt(),
                targetRectangle = cb.bounds.toInt().flipped())
        }

        files.mapIndexed { i, it ->
            loadImage(it).copyTo(at, i)
            println(it.nameWithoutExtension)
        }

        var min = 1000.0
        var max = 0.0



        extend {
            webcam.draw(drawer, true)

            val regions = bp.detect(cb)
            if (regions.isNotEmpty()) {
                computeRoi(regions[0])
                val z = regions[0].rectangle.area

                min = min(z, min)
                max = max(z, max)

                val mappedDistance = map(min, max, 678.0, 0.0, z)
                drawer.image(at, mappedDistance.toInt())

                println(z)

                drawer.fill = null
                drawer.stroke = ColorRGBa.GREEN
                drawer.rectangle(regions[0].rectangle)
            } else {
                println("empty")
            }


        }
    }
}