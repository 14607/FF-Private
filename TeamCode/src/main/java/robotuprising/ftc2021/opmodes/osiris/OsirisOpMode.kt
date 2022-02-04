package robotuprising.ftc2021.opmodes.osiris

import robotuprising.ftc2021.manager.*
import robotuprising.ftc2021.subsystems.osiris.*
import robotuprising.ftc2021.subsystems.osiris.hardware.*
import robotuprising.ftc2021.subsystems.osiris.hardware.vision.BlueWebcam
import robotuprising.ftc2021.subsystems.osiris.hardware.vision.RedWebcam
import robotuprising.lib.opmode.AllianceSide
import robotuprising.lib.system.BaseOpMode

abstract class OsirisOpMode : BaseOpMode() {
//    private val turretLimitSwitch = TurretLimitSwitch
//    private val slideLimitSwitch = SlideLimitSwitch

//    private val spinner = Spinner
//
//    private val redWebcam = RedWebcam
//    private val blueWebcam = BlueWebcam

    open fun register() {
        SubsystemManager.registerSubsystems(
                Ghost,

                Intake,
                LoadingSensor,

                Outtake,
                Indexer,
                Arm,

                Turret,

                Slides

        )
    }

    override fun mInit() {
        SubsystemManager.clearAll()
        register()
        SubsystemManager.initAll()

        Turret.setTurretLockAngle(180.0)
        Slides.setSlideInches(0.0)
    }

    override fun mInitLoop() {
        SubsystemManager.periodic()
        SubsystemManager.initServos()
        StateMachineManager.periodic()
    }

    override fun mStart() {

    }

    override fun mLoop() {
        SubsystemManager.periodic()
        StateMachineManager.periodic()
    }

    override fun mStop() {
        SubsystemManager.stopAll()
        StateMachineManager.stop()
    }
}