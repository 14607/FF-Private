package robotuprising.ftc2021.v2.util

import com.acmerobotics.dashboard.config.Config
import robotuprising.ftc2021.v2.subsystems.nakiri.vision.Webcam
import robotuprising.lib.opmode.AllianceSide

@Config
object Globals {
    var ALLIANCE_SIDE = AllianceSide.BLUE
    var IS_AUTO = false
    var CUP_LOCATION = Webcam.CupStates.LEFT

    const val LINKAGE_RETRACT = 1.0
    const val LINKAGE_MED = 0.80
    const val LINKAGE_EXTEND = 0.5
    const val LINKAGE_TRANSFER = 0.82
    const val LINKAGE_EXTEND_MIDDLE = 0.65

    const val OUTTAKE_LEFT_IN = 0.07
    const val OUTTAKE_RIGHT_IN = 0.45

    const val OUTTAKE_LEFT_OUT = 0.495
    const val OUTTAKE_RIGHT_OUT = 0.02

    const val OUTTAKE_LEFT_MED = 0.24
    const val OUTTAKE_RIGHT_MED = 0.28

    const val INTAKE_PIVOT_LEFT_OUT = 0.83
    const val INTAKE_PIVOT_RIGHT_OUT = 0.02

    const val INTAKE_PIVOT_LEFT_IN = 0.08
    const val INTAKE_PIVOT_RIGHT_IN = 0.74

    const val INTAKE_IN_POWER = 1.0
    const val INTAKE_TRANSFER_POWER = -0.75
    const val INTAKE_NO_POWER = 0.0

    const val LIFT_LOW = 160
    const val LIFT_MEDIUM = 500
    const val LIFT_HIGH = 1000

    val MASTER_MAPPINGS = listOf("FL", "FR", "BR", "BL")
    val SLAVE_MAPPINGS = listOf("intake", "liftLeft", "liftRight", "duck")
}