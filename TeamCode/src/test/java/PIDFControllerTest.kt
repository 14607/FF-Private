import com.qualcomm.robotcore.util.Range
import robotuprising.lib.control.motion.PIDCoeffs
import robotuprising.lib.control.motion.PIDFController
import robotuprising.lib.util.Extensions.d
import kotlin.math.abs

object PIDFControllerTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val target = 50.0

        val iters = 10
        val times = ArrayList<Double>()

        repeat(iters) {
            var position = 0.0
            val controller = PIDFController(PIDCoeffs(0.d, 0.d, 0.d))

//            controller.setTargets(target, 0.0, 0.0)
//            controller.setBounds(1.0, -1.0)

            val timestarted = System.currentTimeMillis()
            var lastupdate = timestarted
            while (abs(position - target) > 0.003) {
                if (System.currentTimeMillis() - lastupdate > 10) {
                    position += controller.update(position, null) * Range.clip(Math.random(), 0.3, 0.7)
                    lastupdate = System.currentTimeMillis()
                }
            }
            val timetaken = (System.currentTimeMillis() - timestarted) / 1000.0
            times.add(timetaken)
        }

        println("avg: ${times.sum() / times.size}")
    }
}
