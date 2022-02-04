package robotuprising.ftc2021.auto.pp

import com.qualcomm.robotcore.util.ElapsedTime
import robotuprising.ftc2021.auto.pp.math.Line
import robotuprising.ftc2021.subsystems.osiris.hardware.Odometry
import robotuprising.lib.control.auto.waypoints.StopWaypoint
import robotuprising.lib.control.auto.waypoints.Waypoint
import robotuprising.lib.math.AngleUnit
import robotuprising.lib.math.MathUtil
import robotuprising.lib.math.Pose
import kotlin.collections.ArrayList

class PurePursuitPath(val waypoints: ArrayList<Waypoint>) {
    private val DEAD_MAN_SWITCH = 2000

    private var currPoint = 0
    private var interrupting = false
    private val timeUntilDeadMan = ElapsedTime()

    fun update(): Pose {
        val position = Odometry.currentPosition
        val velocity = Odometry.relVelocity

        if(interrupting) {
            val advance = (waypoints[currPoint].action as Subroutines.ArrivalInterruptSubroutine).runCycle()

            if(advance) {
                interrupting = false
            } else {
                return Pose(AngleUnit.RAW)
            }
        }

        var jumpToNextSegment: Boolean
        do {
            jumpToNextSegment = false
            val target = waypoints[currPoint + 1]

            if(target is StopWaypoint && timeUntilDeadMan.milliseconds() > DEAD_MAN_SWITCH) {
                jumpToNextSegment = true
            } else if(target !is StopWaypoint || velocity.hypot > 1.0) {
                timeUntilDeadMan.reset()
            }
            if(target is StopWaypoint) {
                if(position.distance(target) < target.allowedPositionError) {
                    jumpToNextSegment = true
                }
            } else {
                if(position.distance(target) < target.followDistance) {
                    jumpToNextSegment = true
                }
            }

            var action = waypoints[currPoint + 1].action
            if(action is Subroutines.RepeatedSubroutine) {
                if(action.runLoop()) {
                    jumpToNextSegment = true
                }
            }

            if(jumpToNextSegment) {
                currPoint++
                action = waypoints[currPoint].action

                if(action is Subroutines.OnceOffSubroutine) {
                    action.runOnce()
                }

                if(action is Subroutines.ArrivalInterruptSubroutine) {
                    interrupting = true
                    this.update()
                    return Pose(AngleUnit.RAW)

                }
            }

        } while(jumpToNextSegment && currPoint < waypoints.size - 1)
        if(finished) return Pose(AngleUnit.RAW)

        val target = waypoints[currPoint + 1]

        return if(target is StopWaypoint && position.distance(target) < target.followDistance) {
            MecanumPurePursuitController.goToPosition(
                    position,
                    velocity,
                    target
            )
        } else {
            trackToLine(
                    position,
                    velocity,
                    waypoints[currPoint],
                    waypoints[currPoint + 1]
            )
        }
    }

    private fun trackToLine(robotPosition: Pose, robotVelocity: Pose, start: Waypoint, mid: Waypoint): Pose {
        val currSegment = Line(start.p, mid.p)
        val center = currSegment.nearestLinePoint(robotPosition.p)

        val intersection = MathUtil.circleLineIntersection(
                start.p, mid.p, center, mid.followDistance
        )

        val target = Waypoint(intersection.x, intersection.y, mid.followDistance, mid.action)
        return MecanumPurePursuitController.goToPosition(
                robotPosition,
                robotVelocity,
                target
        )
    }

    val finished get() = currPoint >= waypoints.size - 1
}