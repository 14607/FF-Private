package asiankoala.ftc21.v2.subsystems.nakiri

import com.qualcomm.hardware.rev.RevColorSensorV3
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
import asiankoala.ftc21.v2.hardware.nakiri.NakiriMotor
import asiankoala.ftc21.v2.hardware.nakiri.NakiriServo
import asiankoala.ftc21.v2.manager.BulkDataManager
import asiankoala.ftc21.v2.util.*
import asiankoala.ftc21.v2.lib.opmode.NakiriDashboard
import asiankoala.ftc21.v2.lib.system.Subsystem

class Intake : Subsystem {
    private val intakeMotor = NakiriMotor("intake", false).brake.openLoopControl
    val intakePivotLeft = NakiriServo("intakeLeftPivot")
    val intakePivotRight = NakiriServo("intakeRightPivot")

    private val intakeSensor = BulkDataManager.hwMap[RevColorSensorV3::class.java, "intakeSensor"]

    private enum class IntakeStates(val power: Double) {
        ON(Globals.INTAKE_IN_POWER),
        OFF(Globals.INTAKE_NO_POWER),
        REVERSE(Globals.INTAKE_TRANSFER_POWER),
    }

    private enum class PivotStates(val leftPos: Double, val rightPos: Double) {
        IN(Globals.INTAKE_PIVOT_LEFT_IN, Globals.INTAKE_PIVOT_RIGHT_IN),
        OUT(Globals.INTAKE_PIVOT_LEFT_OUT, Globals.INTAKE_PIVOT_RIGHT_OUT)
    }

    private enum class SensorStates {
        NONE,
        MINERAL_IN
    }

    private val sensorThreshold = 40.0
    private var sensorRead = 0.0

    private var intakeState = IntakeStates.OFF
    private var pivotState = PivotStates.IN
    private var sensorState = SensorStates.NONE

    fun turnOn() {
        intakeState = IntakeStates.ON
    }

    fun turnOff() {
        intakeState = IntakeStates.OFF
    }

    fun turnReverse() {
        intakeState = IntakeStates.REVERSE
    }

    fun rotateOut() {
        if (pivotState != PivotStates.OUT)
            pivotState = PivotStates.OUT
    }

    fun rotateIn() {
        if (pivotState != PivotStates.IN)
            pivotState = PivotStates.IN
    }

    fun isMineralIn(): Boolean {
        return sensorState == SensorStates.MINERAL_IN
    }

    override fun update() {
        intakeMotor.power = intakeState.power
        intakePivotLeft.position = pivotState.leftPos
        intakePivotRight.position = pivotState.rightPos

        sensorRead = intakeSensor.getDistance(DistanceUnit.MM)
        sensorState = when {
            sensorRead < sensorThreshold -> SensorStates.MINERAL_IN
            else -> SensorStates.NONE
        }
    }

    override fun sendDashboardPacket(debugging: Boolean) {
//        NakiriDashboard.setHeader("intake")
//        NakiriDashboard["state"] = intakeState
//        NakiriDashboard["sensor state"] = sensorState
//        NakiriDashboard["sensor distance"] = sensorRead

        if (debugging) {
            NakiriDashboard["power"] = intakeMotor.power
            NakiriDashboard["pivot state"] = pivotState
            NakiriDashboard["left pos"] = pivotState.leftPos
            NakiriDashboard["right pos"] = pivotState.rightPos
            NakiriDashboard["sensor threshold"] = sensorThreshold
            intakeMotor.sendDataToDashboard()
        }
    }

    override fun reset() {
        turnOff()
        rotateIn()
    }
}