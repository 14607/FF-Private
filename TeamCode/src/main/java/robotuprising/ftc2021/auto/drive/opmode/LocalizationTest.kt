// package robotuprising.ftc2021.auto.drive.opmode
//
// import com.acmerobotics.roadrunner.geometry.Pose2d
// import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
// import com.qualcomm.robotcore.eventloop.opmode.TeleOp
// import com.qualcomm.robotcore.hardware.DcMotor
// import robotuprising.ftc2021.auto.drive.SampleMecanumDrive
// import kotlin.Throws
//
// /**
// * This is a simple teleop routine for testing localization. Drive the robot around like a normal
// * teleop routine and make sure the robot's estimated pose matches the robot's actual pose (slight
// * errors are not out of the ordinary, especially with sudden drive motions). The goal of this
// * exercise is to ascertain whether the localizer has been configured properly (note: the pure
// * encoder localizer heading may be significantly off if the track width has not been tuned).
// */
// @TeleOp(group = "drive")
// class LocalizationTest : LinearOpMode() {
//    @Throws(InterruptedException::class)
//    override fun runOpMode() {
//        val drive = SampleMecanumDrive(hardwareMap)
//        drive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER)
//        waitForStart()
//        while (!isStopRequested) {
//            drive.setWeightedDrivePower(
//                Pose2d(
//                    (-gamepad1.left_stick_y).toDouble(),
//                    (-gamepad1.left_stick_x).toDouble(),
//                    (-gamepad1.right_stick_x).toDouble()
//                )
//            )
//            drive.update()
//            val poseEstimate = drive.poseEstimate
//            telemetry.addData("x", poseEstimate.x)
//            telemetry.addData("y", poseEstimate.y)
//            telemetry.addData("heading", poseEstimate.heading)
//            telemetry.update()
//        }
//    }
// }
