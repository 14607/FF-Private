package asiankoala.ftc21.v2.subsystems.nakiri

import com.acmerobotics.roadrunner.control.PIDCoefficients
import com.acmerobotics.roadrunner.drive.DriveSignal
import com.acmerobotics.roadrunner.drive.MecanumDrive
import com.acmerobotics.roadrunner.followers.HolonomicPIDVAFollower
import com.acmerobotics.roadrunner.followers.TrajectoryFollower
import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.trajectory.Trajectory
import com.acmerobotics.roadrunner.trajectory.TrajectoryBuilder
import com.acmerobotics.roadrunner.trajectory.constraints.*
import com.qualcomm.hardware.bosch.BNO055IMU
import com.qualcomm.hardware.bosch.BNO055IMUImpl
import com.qualcomm.robotcore.hardware.VoltageSensor
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder
import asiankoala.ftc21.v2.auto.roadrunner.drive.DriveConstants
import asiankoala.ftc21.v2.auto.roadrunner.trajectorysequence.TrajectorySequence
import asiankoala.ftc21.v2.auto.roadrunner.trajectorysequence.TrajectorySequenceBuilder
import asiankoala.ftc21.v2.auto.roadrunner.trajectorysequence.TrajectorySequenceRunner
import asiankoala.ftc21.v2.manager.BulkDataManager
import asiankoala.ftc21.v2.util.Globals
import asiankoala.ftc21.v2.hardware.nakiri.NakiriMotor
import asiankoala.ftc21.v2.lib.hardware.AxesSigns
import asiankoala.ftc21.v2.lib.hardware.BNO055IMUUtil.remapAxes
import asiankoala.ftc21.v2.lib.math.Angle
import asiankoala.ftc21.v2.lib.math.AngleUnit
import asiankoala.ftc21.v2.lib.math.Point
import asiankoala.ftc21.v2.lib.math.Pose
import asiankoala.ftc21.v2.lib.opmode.NakiriDashboard
import asiankoala.ftc21.v2.lib.system.Subsystem
import asiankoala.ftc21.v2.lib.util.Extensions.d
import java.util.*
import kotlin.math.absoluteValue

class AyameRR : MecanumDrive(DriveConstants.kV, DriveConstants.kA, DriveConstants.kStatic, DriveConstants.TRACK_WIDTH), Subsystem {

    private val fl = NakiriMotor("FL", true).brake.openLoopControl.reverse
    private val bl = NakiriMotor("BL", true).brake.openLoopControl.reverse
    private val fr = NakiriMotor("FR", true).brake.openLoopControl
    private val br = NakiriMotor("BR", true).brake.openLoopControl
    private val motors = listOf(fl, bl, fr, br)

    private val imu = BulkDataManager.hwMap[BNO055IMUImpl::class.java, "imu"]
    private val headingOffset: Double
    private val imuOffsetRead: Double get() = imu.angularOrientation.firstAngle - headingOffset

    private var wheels: List<Double> = mutableListOf(0.0, 0.0, 0.0, 0.0)

    private val trajectorySequenceRunner: TrajectorySequenceRunner
    private val follower: TrajectoryFollower
    private val batteryVoltageSensor: VoltageSensor

    private var TRANSLATIONAL_PID = PIDCoefficients(6.0, 0.0, 0.5)
    private var HEADING_PID = PIDCoefficients(5.0, 0.0, 0.5)

    private val VEL_CONSTRAINT: TrajectoryVelocityConstraint = getVelocityConstraint(DriveConstants.MAX_VEL, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH)
    private val ACCEL_CONSTRAINT: TrajectoryAccelerationConstraint = getAccelerationConstraint(DriveConstants.MAX_ACCEL)

    val isBusy: Boolean
        get() = trajectorySequenceRunner.isBusy

    fun setVectorPowers(powers: Pose) {
        wheels = mutableListOf(
                powers.y + powers.x + powers.h.angle,
                powers.y - powers.x + powers.h.angle,
                powers.y - powers.x - powers.h.angle,
                powers.y + powers.x - powers.h.angle
        )
    }

    fun setVectorPowers(x: Double, y: Double, h: Double) {
        setVectorPowers(Pose(Point(x, y), Angle(h, AngleUnit.RAW)))
    }

    private fun getVelocityConstraint(maxVel: Double, maxAngularVel: Double, trackWidth: Double): TrajectoryVelocityConstraint {
        return MinVelocityConstraint(
                Arrays.asList(
                        AngularVelocityConstraint(maxAngularVel),
                        MecanumVelocityConstraint(maxVel, trackWidth)
                )
        )
    }

    private fun getAccelerationConstraint(maxAccel: Double): TrajectoryAccelerationConstraint {
        return ProfileAccelerationConstraint(maxAccel)
    }

    // public methods
    fun trajectoryBuilder(startPose: Pose2d): TrajectoryBuilder {
        return TrajectoryBuilder(startPose, startPose.heading, VEL_CONSTRAINT, ACCEL_CONSTRAINT)
    }

    fun trajectoryBuilder(startPose: Pose2d, reversed: Boolean): TrajectoryBuilder {
        return TrajectoryBuilder(startPose, reversed, VEL_CONSTRAINT, ACCEL_CONSTRAINT)
    }

    fun trajectoryBuilder(startPose: Pose2d, startHeading: Double): TrajectoryBuilder {
        return TrajectoryBuilder(startPose, startHeading, VEL_CONSTRAINT, ACCEL_CONSTRAINT)
    }

    fun trajectorySequenceBuilder(startPose: Pose2d): TrajectorySequenceBuilder {
        return TrajectorySequenceBuilder(
                startPose,
                VEL_CONSTRAINT, ACCEL_CONSTRAINT,
                DriveConstants.MAX_ANG_VEL, DriveConstants.MAX_ANG_ACCEL
        )
    }

    fun followTrajectorySequenceAsync(trajectorySequence: TrajectorySequence) {
        trajectorySequenceRunner.followTrajectorySequenceAsync(trajectorySequence)
    }

    fun followTrajectoryAsync(trajectory: Trajectory) {
        trajectorySequenceRunner.followTrajectorySequenceAsync(
                trajectorySequenceBuilder(trajectory.start())
                        .addTrajectory(trajectory)
                        .build()
        )
    }

    override fun update() {
        if(Globals.IS_AUTO) {
            updatePoseEstimate()
            NakiriDashboard["pose x"] = poseEstimate.x
            NakiriDashboard["pose y"] = poseEstimate.y
            NakiriDashboard["pose h"] = Angle(poseEstimate.heading, AngleUnit.RAD).wrap().deg
            val signal: DriveSignal? = trajectorySequenceRunner.update(poseEstimate, poseVelocity)
            if (signal != null) setDriveSignal(signal)
        }




        val absMax = wheels.map { it.absoluteValue }.maxOrNull()!!
        if (absMax > 1.0) {
            motors.forEachIndexed { i, it -> it.power = wheels[i] / absMax }
        } else {
            motors.forEachIndexed { i, it -> it.power = wheels[i] }
        }
    }

    override fun sendDashboardPacket(debugging: Boolean) {
        if (debugging) {
            NakiriDashboard.setHeader("ayame")
            NakiriDashboard["wheel powers"] = wheels
            motors.forEach { it.sendDataToDashboard() }
        }
    }

    override fun reset() {
        setVectorPowers(Pose(AngleUnit.RAW))
    }

    // roadrunner implementation methods
    override val rawExternalHeading: Double
        get() = imuOffsetRead

    override fun getWheelPositions(): List<Double> {
        return motors.map { DriveConstants.encoderTicksToInches(it.position.d) }
    }

    override fun getWheelVelocities(): List<Double> {
        return motors.map { DriveConstants.encoderTicksToInches(it.velocity) }
    }

    override fun setMotorPowers(frontLeft: Double, rearLeft: Double, rearRight: Double, frontRight: Double) {
        wheels = mutableListOf(frontLeft, rearLeft, frontRight, rearRight)
    }

    val externalHeadingVelocity: Double
        get() = imu.angularVelocity.zRotationRate.d

    init {
        follower = HolonomicPIDVAFollower(
                TRANSLATIONAL_PID, TRANSLATIONAL_PID, HEADING_PID,
                Pose2d(0.5, 0.5, Math.toRadians(0.5)), 0.5
        )

        val parameters = BNO055IMU.Parameters()
        parameters.angleUnit = BNO055IMU.AngleUnit.RADIANS
        parameters.loggingEnabled = false
        imu.initialize(parameters)
        remapAxes(imu, AxesOrder.XYZ, AxesSigns.NPN)

        val orientation = imu.angularOrientation
        headingOffset = orientation.firstAngle.d

        batteryVoltageSensor = BulkDataManager.hwMap.voltageSensor.iterator().next()

        trajectorySequenceRunner = TrajectorySequenceRunner(follower, HEADING_PID)
    }
}