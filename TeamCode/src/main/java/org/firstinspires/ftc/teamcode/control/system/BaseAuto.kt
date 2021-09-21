package org.firstinspires.ftc.teamcode.control.system

import com.qualcomm.robotcore.eventloop.opmode.Disabled
import org.firstinspires.ftc.teamcode.control.path.Path

@Disabled
@Deprecated("need to have a working version for akemi")
abstract class BaseAuto : BaseOpMode() {
    abstract fun initialPath(): Path

    lateinit var pathCache: Path

    lateinit var x: DoubleArray
    lateinit var y: DoubleArray

    override fun onInit() {
        pathCache = initialPath()

        x = DoubleArray(pathCache.waypoints.size)
        y = DoubleArray(pathCache.waypoints.size)
        for ((index, e) in pathCache.waypoints.withIndex()) {
            x[index] = e.p.y
            y[index] = -e.p.x
        }
    }

    override fun onInitLoop() {
        updateDashboardPath()
    }

    override fun onLoop() {
//        if (pathCache.isFinished) {
//            azusa.driveTrain.setZeroPowers()
//            requestOpModeStop()
//        }

//        pathCache.update(azusa)
        updateDashboardPath()
    }

    override fun onStop() {
//        Globals.AUTO_END_POSE = azusa.currPose.copy
    }

    private fun updateDashboardPath() {
        akemiTelemetry.fieldOverlay()
            .setStroke("black")
            .strokePolyline(x, y)
    }
}
