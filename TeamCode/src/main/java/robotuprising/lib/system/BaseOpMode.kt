package robotuprising.lib.system

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeManagerImpl
import robotuprising.lib.debug.Debuggable
import robotuprising.lib.opmode.AllianceSide
import robotuprising.lib.opmode.OpModeStatus
import robotuprising.lib.opmode.OpModeType

abstract class BaseOpMode : LinearOpMode() {
    private val opModeStatus: OpModeStatus
        get() = when {
            isStopRequested -> OpModeStatus.STOP
            isStarted -> OpModeStatus.LOOP
            else -> OpModeStatus.INIT_LOOP
        }

    private var hasStarted = false
    private var prevLoopTime = System.currentTimeMillis()

    private var opModeType: OpModeType = OpModeType.AUTO
    private var debugging = false

    private var allianceSide = AllianceSide.BLUE

    override fun runOpMode() {
        val manager = (internalOpModeServices as OpModeManagerImpl)

        debugging = javaClass.isAnnotationPresent(Debuggable::class.java)
        opModeType = if (javaClass.isAnnotationPresent(Autonomous::class.java)) {
            OpModeType.AUTO
        } else {
            OpModeType.TELEOP
        }

        m_init()

        mainLoop@ while (true) {
            when (opModeStatus) {
                OpModeStatus.INIT_LOOP -> {
                    m_init_loop()
                }

                OpModeStatus.LOOP -> {
                    if (hasStarted) {
                        val dt = System.currentTimeMillis() - prevLoopTime
                        telemetry.addData("loop ms", dt)
                        prevLoopTime = System.currentTimeMillis()
                        m_loop()
                    } else {
                        m_start()
                        hasStarted = true
                    }
                }

                OpModeStatus.STOP -> {
                    break@mainLoop
                }
            }

            telemetry.update()
        }

        m_stop()
        if (opModeType == OpModeType.AUTO && is_comp) {
            manager.initActiveOpMode("NakiriAuto")
        }
    }

    abstract val is_comp: Boolean
    abstract fun m_init()
    open fun m_init_loop() {}
    open fun m_start() {}
    abstract fun m_loop()
    open fun m_stop() {}
}
