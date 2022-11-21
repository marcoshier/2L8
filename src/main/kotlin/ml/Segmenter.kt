package ml/*
import org.openrndr.application
import org.openrndr.draw.colorBuffer
import org.openrndr.extensions.Screenshots
import org.openrndr.ffmpeg.VideoPlayerConfiguration
import org.openrndr.ffmpeg.loadVideo
import org.openrndr.orml.u2net.U2Net
import org.openrndr.shape.IntRectangle
import java.io.File

fun main() = application {
    configure {
        width = 1920 / 2
        height = 1080 / 2
    }
    program {

        val vc = VideoPlayerConfiguration().apply {
            allowFrameSkipping = false
        }
        val video = loadVideo("data/videos/red.mp4", configuration = vc).apply {
            play()
        }

        val u2 = U2Net.load()
        u2.start()
        

        val cb = colorBuffer(width,height)
        val cutout = colorBuffer(width, height)

        var i = 0

        video.newFrame.listen {
            i++
            it.frame.copyTo(cb,
                sourceRectangle = it.frame.bounds.toInt(),
                targetRectangle = cb.bounds.toInt().flipped()
            )

            u2.removeBackground(cb, cutout)
            cutout.saveToFile(File("data/images/redApple/$i.png"))
        }

        extend {
            video.draw(drawer, true)

            drawer.image(cutout)

        }
    }
}
*/
