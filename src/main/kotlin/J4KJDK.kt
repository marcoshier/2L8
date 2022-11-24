import edu.ufl.digitalworlds.j4k.DepthMap
import edu.ufl.digitalworlds.j4k.J4KSDK
import edu.ufl.digitalworlds.j4k.Skeleton
import edu.ufl.digitalworlds.j4k.VideoFrame
import org.openrndr.application
import kotlin.math.max
import kotlin.math.min


fun main() = application {
    configure {
        width = 768
        height = 576
    }

    program {
        val kinect = Kinect()
        kinect.start(32)

        var min = 1000.0
        var max = 0.0

        extend {

            kinect.videoTexture.update(kinect.colorWidth, kinect.colorHeight, kinect.colorFrame)
            kinect.skeletons.forEach {
                if(it != null && it.isTracked) {
                    min = min(it.get3DJointZ(1).toDouble(), min)
                    max = max(it.get3DJointZ(1).toDouble(), max)

                    println("$min min    $max max") //0.5 4.1
                }
            }


            /*for(sk in kinect.skeletons.filterNotNull()) {
                val jointDistance = sk.get3DJointZ(1)
                println(jointDistance)
            }*/
        }
    }
}

class Kinect(val videoTexture: VideoFrame = VideoFrame()): J4KSDK() {
    override fun onDepthFrameEvent(
        depth_frame: ShortArray?,
        player_index: ByteArray?,
        XYZ: FloatArray?,
        UV: FloatArray?
    ) {
    }


    override fun onSkeletonFrameEvent(
        skeleton_tracked: BooleanArray?,
        joint_position: FloatArray?,
        joint_orientation: FloatArray?,
        joint_status: ByteArray?
    ) {
        val skeletons: Array<Skeleton?> = arrayOfNulls(maxNumberOfSkeletons)
        for (i in 0 until maxNumberOfSkeletons)  {
            skeletons[i] = Skeleton.getSkeleton(i, skeleton_tracked, joint_position, joint_orientation, joint_status, this)
        }
    }


    override fun onColorFrameEvent(data: ByteArray?) {
        videoTexture.update(colorWidth, colorHeight, data)
    }


}