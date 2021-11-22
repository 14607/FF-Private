package robotuprising.lib.control.motion

import com.acmerobotics.roadrunner.util.NanoClock
import com.acmerobotics.roadrunner.util.epsilonEquals
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

/**
 * Constructor for [dep]. [kV], [kA], and [kStatic] are designed for DC motor feedforward
 * control (the most common kind of feedforward in FTC). [kF] provides a custom feedforward term for other plants.
 *
 * @param pid traditional PID coefficients
 * @param kV feedforward velocity gain
 * @param kA feedforward acceleration gain
 * @param kStatic additive feedforward constant
 * @param kF custom feedforward that depends on position and/or velocity (e.g., a gravity term for arms)
 * @param clock clock
 */
class dep(
    private val pid: dep2,
    private val kV: Double = 0.0,
    private val kA: Double = 0.0,
    private val kStatic: Double = 0.0,
    private val kF: (Double, Double?) -> Double = { _, _ -> 0.0 },
    private val clock: NanoClock = NanoClock.system()
) {
    private var errorSum: Double = 0.0
    private var lastUpdateTimestamp: Double = Double.NaN

    private var inputBounded: Boolean = false
    private var minInput: Double = 0.0
    private var maxInput: Double = 0.0

    private var outputBounded: Boolean = false
    private var minOutput: Double = 0.0
    private var maxOutput: Double = 0.0

    /**
     * Target position (that is, the controller setpoint).
     */
    var targetPosition: Double = 0.0

    /**
     * Target velocity.
     */
    var targetVelocity: Double = 0.0

    /**
     * Target acceleration.
     */
    var targetAcceleration: Double = 0.0

    /**
     * Error computed in the last call to [update].
     */
    var lastError: Double = 0.0
        private set

    /**
     * Sets bound on the input of the controller. The min and max values are considered modularly-equivalent (that is,
     * the input wraps around).
     *
     * @param min minimum input
     * @param max maximum input
     */
    fun setInputBounds(min: Double, max: Double) {
        if (min < max) {
            inputBounded = true
            minInput = min
            maxInput = max
        }
    }

    /**
     * Sets bounds on the output of the controller.
     *
     * @param min minimum output
     * @param max maximum output
     */
    fun setOutputBounds(min: Double, max: Double) {
        if (min < max) {
            outputBounded = true
            minOutput = min
            maxOutput = max
        }
    }

    private fun getPositionError(measuredPosition: Double): Double {
        var error = targetPosition - measuredPosition
        if (inputBounded) {
            val inputRange = maxInput - minInput
            while (error.absoluteValue > inputRange / 2.0) {
                error -= sign(error) * inputRange
            }
        }
        return error
    }

    /**
     * Run a single iteration of the controller.
     *
     * @param measuredPosition measured position (feedback)
     * @param measuredVelocity measured velocity
     */
    fun update(
        measuredPosition: Double,
        measuredVelocity: Double? = null
    ): Double {
        val currentTimestamp = clock.seconds()
        val error = getPositionError(measuredPosition)
        return if (lastUpdateTimestamp.isNaN()) {
            lastError = error
            lastUpdateTimestamp = currentTimestamp
            0.0
        } else {
            val dt = currentTimestamp - lastUpdateTimestamp
            errorSum += 0.5 * (error + lastError) * dt
            val errorDeriv = (error - lastError) / dt

            lastError = error
            lastUpdateTimestamp = currentTimestamp

            val baseOutput = pid.kp * error + pid.ki * errorSum +
                pid.kd * (measuredVelocity?.let { targetVelocity - it } ?: errorDeriv) +
                kV * targetVelocity + kA * targetAcceleration + kF(measuredPosition, measuredVelocity)
            val output = if (baseOutput epsilonEquals 0.0) 0.0 else baseOutput + sign(baseOutput) * kStatic

            if (outputBounded) {
                max(minOutput, min(output, maxOutput))
            } else {
                output
            }
        }
    }

    /**
     * Reset the controller's integral sum.
     */
    fun reset() {
        errorSum = 0.0
        lastError = 0.0
        lastUpdateTimestamp = Double.NaN
    }
}