package robotuprising.ftc2021.statemachines

import robotuprising.ftc2021.subsystems.osiris.hardware.*
import robotuprising.lib.system.statemachine.StateMachineBuilder

// STATIC TURRET MOVEMENT
object ReadyForDepositStateMachine : StateMachineI<ReadyForDepositStateMachine.States>() {
    enum class States {
        EXTEND_SLIDES,
        MOVE_ARM_AND_OUTTAKE,
    }

    override val stateMachine = StateMachineBuilder<States>()
            .state(States.EXTEND_SLIDES)
            .onEnter { Slides.setSlideInches(34.0) }
            .transitionTimed(0.5)

            .state(States.MOVE_ARM_AND_OUTTAKE)
            .onEnter(Arm::depositHigh)
            .onEnter(Outtake::depositHigh)
            .transitionTimed(2.0)
            .build()

}