import org.openrndr.application
import org.openrndr.extra.minim.minim

fun main() = application {
    configure {
        width = 1080 / 2
        height = 1920 / 2
    }
    program {
        val minim = minim()
        val player = minim.loadFile("offline-data/0.wav")
        player.play()


        extend {
            if (!player.isPlaying) {
                player.apply {
                    rewind()
                    play()
                }
            }
        }
    }
}