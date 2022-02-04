package robotuprising.ftc2021.subsystems.osiris.hardware

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import robotuprising.ftc2021.subsystems.osiris.motor.*
import robotuprising.ftc2021.util.*

object Slides : ZeroableMotorSubsystem(
        MotorSubsystemConfig(
                MotorConfig(
                        "slides",
                        zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT,
                        direction = DcMotorSimple.Direction.REVERSE
                ),

                controlType = MotorControlType.MOTION_PROFILE,

                ticksPerUnit = 20.8333,
                gearRatio = 1.0,

                kP = 0.2,
                kD = 0.007,

                maxVelocity = 84.0,
                maxAcceleration = 64.0,

                positionEpsilon = 1.0,
                homePositionToDisable = 0.0,

                kStatic = 0.03,
//                kD = 0.00005,


                postZeroedValue = Constants.slideHomeValue
        )
) {
    // target is in continuous form
    fun setSlideInches(target: Double) {
        generateAndFollowMotionProfile(position, target)
    }

    fun setSlideLockTarget(target: Double) {
        setControllerTarget(target)
    }
}