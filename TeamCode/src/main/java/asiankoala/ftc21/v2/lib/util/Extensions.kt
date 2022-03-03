package asiankoala.ftc21.v2.lib.util

import com.acmerobotics.roadrunner.geometry.Pose2d
import asiankoala.ftc21.v2.lib.math.Angle
import asiankoala.ftc21.v2.lib.math.AngleUnit
import asiankoala.ftc21.v2.lib.math.Point
import asiankoala.ftc21.v2.lib.math.Pose
import kotlin.math.absoluteValue

/**
 * this is like my .vimrc but for kotlin lol
 */
object Extensions {
    val Boolean.i get() = if (this) 1 else 0
    val Boolean.d get() = this.i.toDouble()
    val Number.d get() = this.toDouble()
    val Number.i get() = this.toInt()
    val Number.l get() = this.toLong()
    val Number.f get() = this.toFloat()

    val Double.mmToIn get() = this / 25.4
    val Double.cmToIn get() = this / 2.54
    val Double.inToMM get() = this * 25.4
    val Double.inToCM get() = this * 2.54

    val List<Double>.listAbs: List<Double> get() = this.map { it.absoluteValue }
    fun <E> ArrayList<E>.deepCopy(): ArrayList<E> {
        val copy = ArrayList<E>()
        this.forEach { copy.add(it) }
        return copy
    }
    val ArrayList<Double>.average: Double get() = this.sum() / this.size

    val Pose2d.pose: Pose get() = Pose(Point(x, y), Angle(heading, AngleUnit.RAD))

    fun <E> ArrayList<E>.varargAdd(vararg e: E) {
        this.addAll(e)
    }
}