package robotuprising.ftc2021.auto.drive.advanced

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import com.acmerobotics.roadrunner.util.Angle.normDelta
import com.qualcomm.robotcore.eventloop.opmode.Disabled
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import kotlin.Throws

/**
 * This opmode demonstrates how one can augment driver control by following Road Runner arbitrary
 * Road Runner trajectories at any time during teleop. This really isn't recommended at all. This is
 * not what Trajectories are meant for. A path follower is more suited for this scenario. This
 * sample primarily serves as a demo showcasing Road Runner's capabilities.
 *
 *
 * This bot starts in driver controlled mode by default. The player is able to drive the bot around
 * like any teleop opmode. However, if one of the select buttons are pressed, the bot will switch
 * to automatic control and run to specified location on its own.
 *
 *
 * If A is pressed, the bot will generate a splineTo() trajectory on the fly and follow it to
 * targetA (x: 45, y: 45, heading: 90deg).
 *
 *
 * If B is pressed, the bot will generate a lineTo() trajectory on the fly and follow it to
 * targetB (x: -15, y: 25, heading: whatever the heading is when you press B).
 *
 *
 * If Y is pressed, the bot will turn to face 45 degrees, no matter its position on the field.
 *
 *
 * Pressing X will cancel trajectory following and switch control to the driver. The bot will also
 * cede control to the driver once trajectory following is done.
 *
 *
 * The following may be a little off with this method as the trajectory follower and turn
 * function assume the bot starts at rest.
 *
 *
 * This sample utilizes the SampleMecanumDriveCancelable.java class.
 */
@Disabled
@TeleOp(group = "advanced")
class TeleOpAugmentedDriving : LinearOpMode() {
    // Define 2 states, drive control or automatic control
    enum class Mode {
        DRIVER_CONTROL, AUTOMATIC_CONTROL
    }

    var currentMode = Mode.DRIVER_CONTROL

    // The coordinates we want the bot to automatically go to when we press the A button
    var targetAVector = Vector2d(45.0, 45.0)

    // The heading we want the bot to end on for targetA
    var targetAHeading = Math.toRadians(90.0)

    // The location we want the bot to automatically go to when we press the B button
    var targetBVector = Vector2d(-15.0, 25.0)

    // The angle we want to align to when we press Y
    var targetAngle = Math.toRadians(45.0)
    @Throws(InterruptedException::class)
    override fun runOpMode() {
        // Initialize custom cancelable SampleMecanumDrive class
        val drive = SampleMecanumDriveCancelable(hardwareMap)

        // We want to turn off velocity control for teleop
        // Velocity control per wheel is not necessary outside of motion profiled auto
        drive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER)

        // Retrieve our pose from the PoseStorage.currentPose static field
        // See AutoTransferPose.java for further details
        drive.poseEstimate = PoseStorage.currentPose
        waitForStart()
        if (isStopRequested) return
        while (opModeIsActive() && !isStopRequested) {
            // Update the drive class
            drive.update()

            // Read pose
            val poseEstimate = drive.poseEstimate

            // Print pose to telemetry
            telemetry.addData("mode", currentMode)
            telemetry.addData("x", poseEstimate.x)
            telemetry.addData("y", poseEstimate.y)
            telemetry.addData("heading", poseEstimate.heading)
            telemetry.update()
            when (currentMode) {
                Mode.DRIVER_CONTROL -> {
                    drive.setWeightedDrivePower(
                        Pose2d(
                            (-gamepad1.left_stick_y).toDouble(),
                            (-gamepad1.left_stick_x).toDouble(),
                            (-gamepad1.right_stick_x).toDouble()
                        )
                    )
                    if (gamepad1.a) {
                        // If the A button is pressed on gamepad1, we generate a splineTo()
                        // trajectory on the fly and follow it
                        // We switch the state to AUTOMATIC_CONTROL
                        val traj1 = drive.trajectoryBuilder(poseEstimate)
                            .splineTo(targetAVector, targetAHeading)
                            .build()
                        drive.followTrajectoryAsync(traj1)
                        currentMode = Mode.AUTOMATIC_CONTROL
                    } else if (gamepad1.b) {
                        // If the B button is pressed on gamepad1, we generate a lineTo()
                        // trajectory on the fly and follow it
                        // We switch the state to AUTOMATIC_CONTROL
                        val traj1 = drive.trajectoryBuilder(poseEstimate)
                            .lineTo(targetBVector)
                            .build()
                        drive.followTrajectoryAsync(traj1)
                        currentMode = Mode.AUTOMATIC_CONTROL
                    } else if (gamepad1.y) {
                        // If Y is pressed, we turn the bot to the specified angle to reach
                        // targetAngle (by default, 45 degrees)
                        drive.turnAsync(normDelta(targetAngle - poseEstimate.heading))
                        currentMode = Mode.AUTOMATIC_CONTROL
                    }
                }
                Mode.AUTOMATIC_CONTROL -> {
                    // If x is pressed, we break out of the automatic following
                    if (gamepad1.x) {
                        drive.cancelFollowing()
                        currentMode = Mode.DRIVER_CONTROL
                    }

                    // If drive finishes its task, cede control to the driver
                    if (!drive.isBusy) {
                        currentMode = Mode.DRIVER_CONTROL
                    }
                }
            }
        }
    }
}