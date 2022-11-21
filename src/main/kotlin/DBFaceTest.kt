import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.colorBuffer
import org.openrndr.ffmpeg.loadVideoDevice
import org.openrndr.math.Vector2
import org.openrndr.orml.dbface.DBFaceDetector
import org.openrndr.orml.ssd.map
import org.openrndr.shape.Rectangle

fun main() = application {
    configure {
        width = 640
        height = 480
    }
    program {
        val db = DBFaceDetector.load()
        db.start()

        val cb = colorBuffer(width, height)
        val webcam = loadVideoDevice().apply { play() }


        webcam.newFrame.listen {
            it.frame.copyTo(cb,
                sourceRectangle = it.frame.bounds.toInt(),
                targetRectangle = cb.bounds.toInt().flipped())
        }

        extend {
            webcam.draw(drawer, blind = true)
            drawer.image(cb)


            drawer.fill = null
            drawer.stroke = ColorRGBa.GREEN

            val rects = db.detectFaces(cb)
            for (rect in rects) {

                drawer.rectangle(rect.area.map(Rectangle(0.0, 0.0, 1.0, 1.0), drawer.bounds))
            }

        }
    }
}