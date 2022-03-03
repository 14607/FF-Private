package asiankoala.ftc21.v2.hardware.osiris

import com.qualcomm.robotcore.hardware.CRServo
import asiankoala.ftc21.v2.manager.BulkDataManager
import asiankoala.ftc21.v2.lib.math.MathUtil.epsilonNotEqual

class OsirisCRServo(name: String) {
    private val servo = BulkDataManager.hwMap[CRServo::class.java, name]

    var power: Double = 0.0
        set(value) {
            if(field epsilonNotEqual value) {
                servo.power = value
                field = value
            }
        }
}