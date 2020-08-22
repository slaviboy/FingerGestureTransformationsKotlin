package com.slaviboy.matrixgesturedetector

import android.graphics.Matrix
import android.graphics.PointF
import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import android.view.MotionEvent.*
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class MatrixGestureDetectorInstrumentedTest {

    lateinit var translateEvent: ArrayList<MotionEvent>
    lateinit var rotateEvent: ArrayList<MotionEvent>
    lateinit var scaleEvent: ArrayList<MotionEvent>

    @Before
    fun setUp() {

        val t = SystemClock.uptimeMillis()

        // simulate events that translate the matrix by (100,50)
        var x = 0f
        var y = 0f
        translateEvent = arrayListOf(
            getMotionEvent(t, t + 10, ACTION_DOWN, x, y),                        // finger1 down
            getMotionEvent(t, t + 20, ACTION_MOVE, x + 100.0f, y + 50.0f), // finger1 move
            getMotionEvent(t, t + 30, ACTION_UP, x + 100.0f, y + 50.0f)    // finger1 up
        )


        // simulated events that rotates the matrix by 90 degrees
        x = 569.70563f
        y = 236.86292f
        rotateEvent = arrayListOf(
            getMotionEvent(t, t + 40, ACTION_DOWN, x, y),                     // finger1 down
            getMotionEvent(t, t + 50, ACTION_DOWN, x, y, x + 200f, y),    // finger2 down
            getMotionEvent(t, t + 60, ACTION_MOVE, x, y, x, y + 200f),    // finger2 move
            getMotionEvent(t, t + 50, ACTION_UP, x, y, x, y + 200f),      // finger2 up
            getMotionEvent(t, t + 40, ACTION_UP, x, y)                        // finger1 up
        )


        // simulated events that scales the matrix by factor of 2
        scaleEvent = arrayListOf(
            getMotionEvent(t, t + 40, ACTION_DOWN, x, y),                     // finger1 down
            getMotionEvent(t, t + 50, ACTION_DOWN, x, y, x + 200f, y),    // finger2 down
            getMotionEvent(t, t + 60, ACTION_MOVE, x, y, x + 400f, y),    // finger2 move
            getMotionEvent(t, t + 50, ACTION_UP, x, y, x + 400f, y),      // finger2 up
            getMotionEvent(t, t + 40, ACTION_UP, x, y)                        // finger1 up
        )
    }

    @Test
    fun MatrixGestureDetectorTest() {

        // matrix with preset translation (200,100), scale 2.4, rotation 45f
        val matrix = Matrix().apply {
            postTranslate(200f, 100f)
            postScale(2.4f, 2.4f, 100f, 100f)
            postRotate(45f, 300f, 300f)
        }

        val matrixGestureDetector = MatrixGestureDetector(matrix)

        // check the preset values of the matrix, when it was initialized
        var matrixValues = FloatArray(9)
        matrixGestureDetector.matrix.getValues(matrixValues)
        assertThat(matrixGestureDetector.scale).isEqualTo(2.4f)
        assertThat(matrixGestureDetector.angle).isEqualTo(45f)
        assertThat(matrixGestureDetector.translate).isEqualTo(PointF(469.70563f, 186.86292f))
        assertThat(matrixValues).isEqualTo(floatArrayOf(1.6970563f, -1.6970563f, 469.70563f, 1.6970563f, 1.6970563f, 186.86292f, 0.0f, 0.0f, 1.0f))


        // trigger the events for the translation by (100,50)
        for (motionEvent in translateEvent) {
            matrixGestureDetector.onTouchEvent(motionEvent)
        }
        matrixValues = FloatArray(9)
        matrixGestureDetector.matrix.getValues(matrixValues)
        assertThat(matrixGestureDetector.scale).isEqualTo(2.4f)
        assertThat(matrixGestureDetector.angle).isEqualTo(45f)
        assertThat(matrixGestureDetector.translate).isEqualTo(PointF(469.70563f + 100f, 186.86292f + 50f))
        assertThat(matrixValues).isEqualTo(floatArrayOf(1.6970563f, -1.6970563f, 569.7056f, 1.6970563f, 1.6970563f, 236.86292f, 0.0f, 0.0f, 1.0f))

        // trigger the events for the rotation by 90 degrees
        for (motionEvent in rotateEvent) {
            matrixGestureDetector.onTouchEvent(motionEvent)
        }
        matrixValues = FloatArray(9)
        matrixGestureDetector.matrix.getValues(matrixValues)
        assertThat(matrixGestureDetector.scale).isEqualTo(2.4f)
        assertThat(matrixGestureDetector.angle).isEqualTo(45f + 90f)
        assertThat(matrixGestureDetector.translate).isEqualTo(PointF(469.70563f + 100f, 186.86292f + 50f))
        assertThat(matrixValues).isEqualTo(floatArrayOf(-1.6970563f, -1.6970563f, 569.7056f, 1.6970563f, -1.6970563f, 236.86292f, 0.0f, 0.0f, 1.0f))


        // trigger the events for the scale by factor of 2
        for (motionEvent in scaleEvent) {
            matrixGestureDetector.onTouchEvent(motionEvent)
        }
        matrixValues = FloatArray(9)
        matrixGestureDetector.matrix.getValues(matrixValues)
        assertThat(matrixGestureDetector.scale).isEqualTo(2.4f * 2)
        assertThat(matrixGestureDetector.angle).isEqualTo(45f + 90f)
        assertThat(matrixGestureDetector.translate).isEqualTo(PointF(469.70563f + 100f, 186.86292f + 50f))
        assertThat(matrixValues).isEqualTo(floatArrayOf(-3.3941126f, -3.3941126f, 569.7056f, 3.3941126f, -3.3941126f, 236.86292f, 0.0f, 0.0f, 1.0f))

    }

    /**
     * Generate motion event for one finger.
     * @param downTime The time (in ms) when the user originally pressed down to start a stream of position events.
     * @param eventTime The the time (in ms) when this specific event was generated.
     * @param action The kind of action being performed.
     * @param x The x coordinate for first finger.
     * @param y The y coordinate for first finger.
     */
    fun getMotionEvent(downTime: Long, eventTime: Long, action: Int, x: Float, y: Float): MotionEvent {

        val properties = PointerProperties()
        properties.id = 0
        properties.toolType = 0 // Configurator.getInstance().getToolType()

        val coordinates = PointerCoords()
        coordinates.pressure = 1f
        coordinates.size = 1f
        coordinates.x = x
        coordinates.y = y

        return obtain(
            downTime, eventTime, action, 1, arrayOf(properties), arrayOf(coordinates),
            0, 0, 1.0f, 1.0f, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0
        )
    }

    /**
     * Generate motion event for two fingers.
     * @param downTime The time (in ms) when the user originally pressed down to start a stream of position events.
     * @param eventTime The the time (in ms) when this specific event was generated.
     * @param action The kind of action being performed.
     * @param x1 The x coordinate for first finger.
     * @param y1 The y coordinate for first finger.
     * @param x2 The x coordinate for second finger.
     * @param y2 The y coordinate for second finger.
     */
    fun getMotionEvent(downTime: Long, eventTime: Long, action: Int, x1: Float, y1: Float, x2: Float, y2: Float): MotionEvent {

        val properties1 = PointerProperties()
        properties1.id = 0
        properties1.toolType = 0

        val pointerCoordinates1 = PointerCoords()
        pointerCoordinates1.pressure = 1f
        pointerCoordinates1.size = 1f
        pointerCoordinates1.x = x1
        pointerCoordinates1.y = y1

        val properties2 = PointerProperties()
        properties2.id = 1
        properties2.toolType = 0

        val pointerCoordinates2 = PointerCoords()
        pointerCoordinates2.pressure = 1f
        pointerCoordinates2.size = 1f
        pointerCoordinates2.x = x2
        pointerCoordinates2.y = y2

        return obtain(
            downTime, eventTime, action, 2, arrayOf(properties1, properties2), arrayOf(pointerCoordinates1, pointerCoordinates2),
            0, 0, 1.0f, 1.0f, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0
        )
    }
}