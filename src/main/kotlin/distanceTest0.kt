import kotlinx.coroutines.yield
import org.openrndr.application
import org.openrndr.draw.arrayTexture
import org.openrndr.draw.loadImage
import org.openrndr.launch
import org.openrndr.math.map
import java.io.File

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

        files.mapIndexed { i, it ->
            loadImage(it).copyTo(at, i)
            println(it.nameWithoutExtension)
        }

        extend {

            val mappedDistance = map(0.0, width * 1.0, 0.0, 678.0, mouse.position.x)
            drawer.image(at, mappedDistance.toInt())

        }
    }
}