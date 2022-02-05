package robotuprising.ftc2021.statemachines.blue

import robotuprising.ftc2021.statemachines.StateMachineI
import robotuprising.ftc2021.subsystems.osiris.hardware.Spinner
import robotuprising.ftc2021.util.Constants
import robotuprising.lib.system.statemachine.StateMachine
import robotuprising.lib.system.statemachine.StateMachineBuilder

object DuckBlueStateMachine : StateMachineI<DuckBlueStateMachine.States>() {
    enum class States {
        SPIN
    }

    override val stateMachine: StateMachine<States> = StateMachineBuilder<States>()
            .state(States.SPIN)
            .onEnter { Spinner.setPower(Constants.duckSpeed) }
            .onExit { Spinner.setPower(0.0) }
            .transitionTimed(2.3)
            .build()
}