package robotuprising.koawalib.path.waypoints

import robotuprising.koawalib.math.MathUtil.d
import robotuprising.koawalib.math.MathUtil.radians
import robotuprising.koawalib.math.Point
import robotuprising.koawalib.math.Pose
import robotuprising.koawalib.path2.Waypoint

// id love for this to be a dataclass but yeah sucks to suck
open class Waypoint(
        var x: Double = 0.0,
        var y: Double = 0.0,
        val followDistance: Double = 0.0,
        val maxMoveSpeed: Double = 1.0,
        val maxTurnSpeed: Double = 1.0,
        val stop: Boolean = true,
        val isHeadingLocked: Boolean = false,
        val headingLockAngle: Double = 0.0,
        val slowDownTurnRadians: Double = 60.0.radians,
        val lowestSlowDownFromTurnError: Double = 0.4,
        val turnLookaheadDistance: Double = followDistance
) {
    constructor(x: Int, y: Int, followDistance: Int) : this(x.d, y.d, followDistance.d)
    val point = Point(x, y)

    open val copy: Waypoint get() = Waypoint(x, y, followDistance)
    fun distance(p2: Waypoint) = point.distance(p2.point)
    fun distance(p2: Pose) = point.distance(p2.point)

    override fun toString(): String {
        return String.format(
            "%.1f, %.1f, %.1f",
            x,
            y,
            followDistance,
        )
    }
}
