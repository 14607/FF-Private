package robotuprising.ftc2021.util

import com.qualcomm.robotcore.hardware.HardwareMap
import robotuprising.lib.opmode.AllianceSide

object Globals {
    const val TELEOP_NAME = "AkemiTele"
    const val IS_COMP = false
    // var AUTO_END_POSE = Pose(Point.ORIGIN, Angle.EAST) // imagine having localization
    var ALLIANCE_SIDE = AllianceSide.BLUE
    lateinit var hwMap: HardwareMap // cringe!
}
