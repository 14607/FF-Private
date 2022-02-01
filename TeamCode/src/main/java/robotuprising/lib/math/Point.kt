package robotuprising.lib.math

import robotuprising.lib.math.MathUtil.clip
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

data class Point(
    val x: Double = 0.0,
    val y: Double = 0.0
) {
    val hypot = hypot(x, y)
    val atan2 = Angle(atan2(y, x), AngleUnit.RAD)
    val dbNormalize = Point(y, -x)
    val copy= Point(x, y)

    operator fun plus(p: Point) = Point(x + p.x, y + p.y)
    operator fun minus(p: Point) = Point(x - p.x, y - p.y)
    operator fun times(n: Double) = Point(x * n, y * n)
    operator fun div(n: Double) = Point(x / n, y / n)
    operator fun unaryMinus() = this.times(-1.0)

    fun clip(n: Double) = Point(x.clip(n), y.clip(n))
    fun distance(p: Point) = minus(p).hypot
    fun rotate(angle: Double) = Point(
        x * cos(angle) - y * sin(angle),
        x * sin(angle) + y * cos(angle)
    )

    override fun toString() = String.format("%.2f, %.2f", x, y)
}
