package org.firstinspires.ftc.teamcode.util.opmode

import com.acmerobotics.dashboard.telemetry.TelemetryPacket

class TelemAdapter : TelemetryPacket() {
    fun addData(k: String, v: Any) {
        addLine("$k: $v")
    }

    fun addSpace() {
        addLine(" ")
    }
}
