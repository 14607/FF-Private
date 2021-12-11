package robotuprising.ftc2021.subsystems

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.kinematics.Kinematics
import com.acmerobotics.roadrunner.kinematics.MecanumKinematics
import com.qualcomm.hardware.bosch.BNO055IMU
import com.qualcomm.hardware.bosch.BNO055IMUImpl
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder
import robotuprising.ftc2021.auto.drive.DriveConstants
import robotuprising.ftc2021.opmodes.testing.Ultrasonics
import robotuprising.ftc2021.util.BulkDataManager
import robotuprising.ftc2021.util.Globals
import robotuprising.ftc2021.util.NakiriMotor
import robotuprising.lib.hardware.AxesSigns
import robotuprising.lib.hardware.BNO055IMUUtil.remapAxes
import robotuprising.lib.math.Angle
import robotuprising.lib.math.AngleUnit
import robotuprising.lib.math.Point
import robotuprising.lib.math.Pose
import robotuprising.lib.opmode.AllianceSide
import robotuprising.lib.opmode.NakiriDashboard
import robotuprising.lib.system.Subsystem
import robotuprising.lib.util.Extensions.d
import robotuprising.lib.util.Extensions.pose
import kotlin.math.absoluteValue

class Ayame: Subsystem {

    // hardware
    private val fl = NakiriMotor("FL", true).brake.openLoopControl.reverse
    private val bl = NakiriMotor("BL", true).brake.openLoopControl.reverse
    private val fr = NakiriMotor("FR", true).brake.openLoopControl
    private val br = NakiriMotor("BR", true).brake.openLoopControl
    private val motors = listOf(fl, bl, fr, br)

    private val ultrasonics = Ultrasonics()


    // location
    var pose: Pose = Pose.DEFAULT_ANGLE
        private set

    private val imu = BulkDataManager.hwMap[BNO055IMUImpl::class.java, "imu"]
    private val headingOffset: Double
    private val rollOffset: Double
    private val pitchOffset: Double

    private val angularOrientation get() = imu.angularOrientation
    private val yaw: Angle get() = Angle(angularOrientation.firstAngle - headingOffset, AngleUnit.RAD).wrap()
//    private val roll: Angle get() = Angle(angularOrientation.secondAngle - rollOffset, AngleUnit.RAD).wrap()
//    private val pitch: Angle get() = Angle(angularOrientation.thirdAngle - pitchOffset, AngleUnit.RAD).wrap()

    // powers
    private var wheels: List<Double> = mutableListOf(0.0, 0.0, 0.0, 0.0)

    // localizer
    private var _poseEstimate = Pose2d()
    private var poseVelocity = Pose2d()
    private var lastWheelPositions = emptyList<Double>()
    private var lastExtHeading = Double.NaN

    var locationState = LocationStates.FIELD
    private var targetLocation = LocationStates.CRATER
    enum class LocationStates {
        CRATER,
        PIPES,
        FIELD,
        NONE,
    }

    fun setVectorPower(powers: Pose) {
        wheels = mutableListOf(
            powers.y + powers.x + powers.h.angle,
            powers.y - powers.x + powers.h.angle,
            powers.y - powers.x - powers.h.angle,
            powers.y + powers.x - powers.h.angle
        )
    }

    private fun setMotorPowers(frontLeft: Double, rearLeft: Double, rearRight: Double, frontRight: Double) {
        wheels = listOf(frontLeft, rearLeft, frontRight, rearRight)
    }

    private fun getWheelPositions(): List<Double> {
        return motors.map { DriveConstants.encoderTicksToInches(it.position.d) }
    }

    private fun getWheelVelocities(): List<Double> {
        return motors.map { DriveConstants.encoderTicksToInches(it.velocity.d) }
    }
    private fun getExternalHeadingVelocity(): Double {
        return imu.angularVelocity.xRotationRate.d
    }


    var startCounter = 0
    var crossCounter = 0
    private fun updatePose() {
        if(false /*locationState != LocationStates.PIPES*/) {
            val wheelPositions = getWheelPositions()
            val extHeading = yaw.angle
            if (lastWheelPositions.isNotEmpty()) {
                val wheelDeltas = wheelPositions
                        .zip(lastWheelPositions)
                        .map { it.first - it.second }
                val robotPoseDelta = MecanumKinematics.wheelToRobotVelocities(
                        wheelDeltas,
                        15.6,
                        15.6,
                        1.0
                )

                val finalHeadingDelta = Angle(extHeading - lastExtHeading, AngleUnit.RAD).wrap().abs
                _poseEstimate = Kinematics.relativeOdometryUpdate(
                        _poseEstimate,
                        Pose2d(robotPoseDelta.vec(), finalHeadingDelta)
                )
            }

            val wheelVelocities = getWheelVelocities()
            val extHeadingVel = getExternalHeadingVelocity()

            poseVelocity = MecanumKinematics.wheelToRobotVelocities(
                    wheelVelocities,
                    15.6,
                    15.6,
                    1.0
            )

            poseVelocity = Pose2d(poseVelocity.vec(), extHeadingVel)

            lastWheelPositions = wheelPositions
            lastExtHeading = extHeading


            pose = _poseEstimate.pose
            NakiriDashboard.addLine("default localization")
        } else {
            ultrasonics.update()

            if(ultrasonics.counter > 1) {
                val fwd = ultrasonics.forwardRangeReading
                val horiz = ultrasonics.horizRangeReading
                if(fwd in 20.0..765.0) {
                    NakiriDashboard["forward"] = fwd
                }

                if(horiz in 20.0..765.0) {
                    NakiriDashboard["horiz"] = horiz
                }

                val forwardNormalized = fwd * yaw.cos

                val forwardWall = 182.88
                val downWall = -182.88
                val upWall = 182.88

                val deltaX = forwardWall - fwd
                val deltaY: Double

                val xEstimate = deltaX * yaw.cos // re
                val yEstimate = if(Globals.ALLIANCE_SIDE == AllianceSide.RED) {
                    deltaY = downWall + horiz
                    deltaY * yaw.sin
                } else {
                    deltaY = upWall - horiz
                    deltaY * yaw.cos
                }

                val ultrasonicPose = Pose(Point(xEstimate, yEstimate), yaw)

                NakiriDashboard["xEstimate"] = xEstimate
                NakiriDashboard["yEstimate"] = yEstimate
                NakiriDashboard["delta x"] = deltaX
                NakiriDashboard["delta y"] = deltaY
                val crossed = (targetLocation == LocationStates.CRATER && forwardNormalized < 50) ||
                        (targetLocation == LocationStates.FIELD && forwardNormalized > 80)
                if(crossed) {
                    crossCounter++
                    pose = ultrasonicPose
                    pose.p = pose.p / 2.54
                    _poseEstimate = pose.pose2d
                    ultrasonics.stopReading()

                    locationState = if(targetLocation == LocationStates.CRATER) {
                        LocationStates.CRATER
                    } else {
                        LocationStates.FIELD
                    }

                    targetLocation = LocationStates.NONE
                }
            }

            NakiriDashboard.addLine("localizing with ultrasonics")
        }
        NakiriDashboard["pose x"] = pose.x
        NakiriDashboard["pose y"] = pose.y
        NakiriDashboard["pose h"] = pose.h.deg
        NakiriDashboard["location state"] = locationState
        NakiriDashboard["target state"] = targetLocation
        NakiriDashboard["cross counter"] = crossCounter
    }

    fun startGoingOverPipes() {
        targetLocation = if(locationState == LocationStates.FIELD) {
            LocationStates.CRATER
        } else {
            LocationStates.FIELD
        }

        locationState = LocationStates.PIPES

        ultrasonics.startReading()

        startCounter = ultrasonics.counter
    }

    // override methods
    // subsystem methods
    override fun update() {
//        if(Globals.OPMODE_TYPE == OpModeType.AUTO) {
//            updatePose()
//        }

//        ultrasonics.update()

//        updatePose()

        val absMax = wheels.map { it.absoluteValue }.maxOrNull()!!
        if (absMax > 1.0) {
            motors.forEachIndexed { i, it -> it.power = wheels[i] / absMax }
        } else {
            motors.forEachIndexed { i, it -> it.power = wheels[i] }
        }
    }

    override fun sendDashboardPacket(debugging: Boolean) {
//        ultrasonics.sendDashboardPacket(debugging)
        if (debugging) {
            NakiriDashboard.setHeader("ayame")
            NakiriDashboard["wheel powers"] = wheels
            motors.forEach { it.sendDataToDashboard() }
        }
    }

    override fun reset() {
        setVectorPower(Pose.DEFAULT_RAW)
    }

    init {
        val parameters = BNO055IMU.Parameters()
        parameters.angleUnit = BNO055IMU.AngleUnit.RADIANS
        parameters.loggingEnabled = false
        imu.initialize(parameters)
        remapAxes(imu, AxesOrder.XYZ, AxesSigns.NPN)

        val orientation = imu.angularOrientation
        headingOffset = orientation.firstAngle.d
        rollOffset = orientation.secondAngle.d
        pitchOffset = orientation.thirdAngle.d

        ultrasonics.startReading()
    }
}