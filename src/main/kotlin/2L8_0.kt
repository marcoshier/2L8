
import kotlinx.coroutines.yield
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.extra.color.presets.MINT_CREAM
import org.openrndr.extra.fx.color.ColorCorrection
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

        val files = File("offline-data/red/").listFiles()?.filter { it.isFile }?.sortedBy {
            it.nameWithoutExtension.toInt()
        }!!
        val at = arrayTexture(width, height, files!!.size)

        files.mapIndexed { i, it ->
            loadImage(it).copyTo(at, i)
            println(it.nameWithoutExtension)
        }

        val player = minim().loadFile("offline-data/0.wav")
        player.play()

        val kinect = Kinect()
        kinect.start(32)

        var min = 0.6
        var max = 4.0

        val cc = ColorCorrection()
        val rt = renderTarget(width, height) {
            colorBuffer()
            depthBuffer()
        }
        val cb2 = colorBuffer(width, height)

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

            drawer.run {
                isolated {
                    translate(cb.bounds.center)
                    scale(2.1)
                    rotate(44.0)
                    translate(-cb.bounds.center)
                    image(cb)
                }
            }

            kinect.videoTexture.update(kinect.colorWidth, kinect.colorHeight, kinect.colorFrame)

            val skeletons = kinect.skeletons.toList().filterNotNull()
            skeletons.firstOrNull { it.isTracked }?.let {

                val x = it.get3DJointX(1).toDouble()

                if(x >= -1.1 && x <= 1.0) {
                    val z = it.get3DJointZ(1).toDouble()

                    if(z in min..max) {
                        lastZ = map(min, max, 1.0, 0.0, z).coerceIn(0.0, 1.0)
                    } else {
                        println("outta range $lastZ")
                    }

                    if(z == 0.0) {
                        lastZ = 0.0
                    }



                    println(z)

                }


            }?: run {
                println("lonely")
            }

            val mappedGain = map(0.0, 1.0, -12.0, 7.0, lastZ)
            player.gain = mappedGain.toFloat()

            val mappedScale = map(0.0, 1.0, 0.5, 0.9, lastZ.coerceAtLeast(0.55))

            drawer.isolatedWithTarget(rt) {
                drawer.clear(ColorRGBa.TRANSPARENT)

                drawer.translate(drawer.bounds.center)
                drawer.scale(mappedScale)
                drawer.translate(- drawer.bounds.center)
                drawer.image(at, (lastZ * files.size).toInt())

            }

            cc.apply {
                brightness = 0.0 + ((1.0 - lastZ) / 5.0)
            }
            cc.apply(rt.colorBuffer(0), cb2)
            drawer.image(cb2)
        }

    }
}

