
import kotlinx.coroutines.yield
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.arrayTexture
import org.openrndr.draw.colorBuffer
import org.openrndr.draw.isolated
import org.openrndr.draw.loadImage
import org.openrndr.extra.color.presets.MINT_CREAM
import org.openrndr.extra.fx.patterns.Checkers
import org.openrndr.extra.minim.minim
import org.openrndr.ffmpeg.loadVideo
import org.openrndr.ffmpeg.loadVideoDevice
import org.openrndr.launch
import org.openrndr.math.Vector2
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

        val player = minim().loadFile("offline-data/0.wav")
        player.play()

        val files = File("offline-data/red/").listFiles()?.filter { it.isFile }?.sortedBy {
            it.nameWithoutExtension.toInt()
        }!!
        val at = arrayTexture(width, height, files!!.size)

        files.mapIndexed { i, it ->
            loadImage(it).copyTo(at, i)
            println(it.nameWithoutExtension)
        }

        val kinect = Kinect()
        kinect.start(32)

        var min = 0.5
        var max = 4.1

        val cb = colorBuffer(width, height)
        val ch = Checkers().apply {
            this.background = ColorRGBa.MINT_CREAM.shade(0.9)
            this.foreground = ColorRGBa.GRAY.mix(ColorRGBa.WHITE, 0.6)
        }

        ch.apply(cb, cb)

        var lastZ = 0.0


        extend {

            if (!player.isPlaying) {
                player.apply {
                    rewind()
                    play()
                }
            }

            kinect.videoTexture.update(kinect.colorWidth, kinect.colorHeight, kinect.colorFrame)

            drawer.isolated {
                drawer.translate(cb.bounds.center)
                drawer.scale(2.1)
                drawer.rotate(44.0)
                drawer.translate(-cb.bounds.center)
                drawer.image(cb)
            }

            val skeletons = kinect.skeletons.toList().filterNotNull()
            skeletons.firstOrNull { it.isTracked }?.let {

                val x = it.get3DJointX(1).toDouble()
                drawer.fill = ColorRGBa.BLACK
                drawer.text(x.toString(), 10.0, 10.0)

                if(x >= -1.1 && x <= 1.0) {
                    val z = it.get3DJointZ(1).toDouble()
                    lastZ = map(min, max, (files.size - 1).toDouble(), 0.0, z)
                }

            }?: run {
                println("lonely")
            }

            val mappedGain = map(0.0, (files.size - 1).toDouble(), 0.0, -60.0, lastZ)
            player.shiftGain(player.gain, mappedGain.toFloat(), 0)


            val mappedScale = map(0.0, (files.size - 1).toDouble(), 0.5, 0.9, lastZ.coerceAtLeast(100.0))

            drawer.translate(drawer.bounds.center)
            drawer.scale(mappedScale)
            drawer.translate(Vector2(-width / 8.0, -height / 3.6) - drawer.bounds.center)
            drawer.image(at, lastZ.toInt())
        }

    }
}

