package com.slaviboy.matrixgesturedetector

import android.graphics.Matrix
import android.os.Build
import android.os.SystemClock
import android.view.MotionEvent
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit testing the class MatrixGestureDetector, unfortunately the method setPolyToPoly() used
 * in the detector is not included in the Robolectric framework, and the unit testing for the
 * whole class is impossible using local unit test run on the JVM, that is why the test is made
 * using instrumented test on the emulator.
 */
@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class MatrixGestureDetectorUnitTest {

    lateinit var translateEvent: ArrayList<MotionEvent>

    @Before
    @Throws(Exception::class)
    fun setUp() {
        translateEvent = arrayListOf(
            MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + 100, MotionEvent.ACTION_DOWN, 10.0f, 10.0f, 0),
            MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + 200, MotionEvent.ACTION_MOVE, 100.0f, 100.0f, 0),
            MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + 300, MotionEvent.ACTION_UP, 100.0f, 100.0f, 0)
        )
    }

    @Test
    fun MainTest() {

        val onMatrixChangeListener = mock(MatrixGestureDetector.OnMatrixChangeListener::class.java)
        val matrix = Matrix()
        val matrixGestureDetector = MatrixGestureDetector(matrix, onMatrixChangeListener)

        // simulate TouchMove event
        for (motionEvent in translateEvent) {
            matrixGestureDetector.onTouchEvent(motionEvent)
        }

        // test listener if it is called the expected number of times
        verify(onMatrixChangeListener, times(1)).onMatrixChange(matrix)
    }
}