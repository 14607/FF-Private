package robotuprising.ftc2021.v2.opmodes.junk.examples

import com.qualcomm.robotcore.eventloop.opmode.OpMode

class exOpMode : OpMode() {
    private var bIntake: exIntake? = null
    override fun init() {
        bIntake = exIntake(hardwareMap)
    }

    override fun loop() {
        bIntake!!.turnIntakeOn()
        bIntake!!.distanceSensorReading
    }
}