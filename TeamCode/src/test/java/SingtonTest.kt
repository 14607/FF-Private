import robotuprising.ftc2021.hardware.subsystems.Ayame

object SingtonTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val m_homura = Ayame
        m_homura.setFromMecanumPowers(MecanumPowers())
    }
}
