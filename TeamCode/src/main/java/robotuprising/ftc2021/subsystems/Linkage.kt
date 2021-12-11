package robotuprising.ftc2021.subsystems

import robotuprising.ftc2021.util.Globals
import robotuprising.ftc2021.util.NakiriServo
import robotuprising.lib.opmode.NakiriDashboard
import robotuprising.lib.system.Subsystem

class Linkage : Subsystem {
    private val linkageServo = NakiriServo("linkage")

    private var linkageState = LinkageStates.IN
    private enum class LinkageStates(val pos: Double) {
        IN(Globals.LINKAGE_RETRACT),
        MED(Globals.LINKAGE_MED),
        OUT(Globals.LINKAGE_EXTEND),
        TRANSFER(Globals.LINKAGE_TRANSFER)
    }

    fun retract() {
        linkageState = LinkageStates.IN
    }

    fun extendMed() {
        linkageState = LinkageStates.MED
    }

    fun extend() {
        linkageState = LinkageStates.OUT
    }

    fun extendTransfer() {
        linkageState = LinkageStates.TRANSFER
    }

    override fun update() {
        linkageServo.position = linkageState.pos
    }

    override fun sendDashboardPacket(debugging: Boolean) {
//        NakiriDashboard.setHeader("linkage")
//        NakiriDashboard["state"] = linkageState

        if (debugging) {
            NakiriDashboard["state pos"] = linkageState.pos
            NakiriDashboard["linkage transfer pos"] = Globals.LINKAGE_TRANSFER
            NakiriDashboard["linkage med pos"] = Globals.LINKAGE_MED
            NakiriDashboard["linkage custom pos"] = Globals.LINKAGE_CUSTOM
        }
    }

    override fun reset() {
        retract()
    }
}
