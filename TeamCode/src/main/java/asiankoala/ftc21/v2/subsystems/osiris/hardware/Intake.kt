package asiankoala.ftc21.v2.subsystems.osiris.hardware

import com.qualcomm.robotcore.hardware.DcMotorSimple
import asiankoala.ftc21.v2.subsystems.osiris.motor.MotorConfig
import asiankoala.ftc21.v2.subsystems.osiris.motor.MotorControlType
import asiankoala.ftc21.v2.subsystems.osiris.motor.MotorSubsystem
import asiankoala.ftc21.v2.subsystems.osiris.motor.MotorSubsystemConfig

object Intake : MotorSubsystem(
        MotorSubsystemConfig(
                MotorConfig(
                        "intake",
                        direction = DcMotorSimple.Direction.REVERSE
                ),

                MotorControlType.OPEN_LOOP
        )
) {
    fun turnOn() {
        output = 0.8
    }

    fun turnReverse() {
        output = -0.8
    }

    fun turnOff() {
        output = 0.0
    }
}