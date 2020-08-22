/*
 * Copyright (C) 2020 Stanislav Georgiev
 * https://github.com/slaviboy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.slaviboy.matrixgesturedetector

import android.graphics.Matrix
import android.graphics.PointF
import android.view.MotionEvent

/**
 * Gesture detection using a transformation matrix, the changes according to
 * gestures made by the user, supported gestures are Move, Scale and Rotate.
 * All transformations are then applied to a matrix, that can be used to
 * transform array with coordinates as float array, paths or canvas elements.
 * @param matrix main matrix that holds all the gesture transformation: rotation, scale and translation
 * @param listener listener with callback, triggered when new transformations are done
 */
open class MatrixGestureDetector(var matrix: Matrix = Matrix(), listener: OnMatrixChangeListener? = null) {

    internal var pointerIndex: Int                  // index showing the first pair of source and distance values for setPolyToPoly()
    internal var tempMatrix: Matrix                 // temp matrix with applied finger gesture fro current event, that is then concat with the main matrix
    internal var source: FloatArray                 // array with the 4 coordinates from the two fingers set on TOUCH_DOWN event
    internal var distance: FloatArray               // array with the 4 coordinates from the two fingers set on TOUCH_MOVE event
    lateinit var listener: OnMatrixChangeListener   // listener object with method onMatrixChange(), called when the main matrix is changed

    var scale: Float                                // current scale factor used same for x and y directions
    var angle: Float                                // current rotational angle in degrees
    var translate: PointF                           // current translate values for x and y directions

    init {

        if (listener != null) {
            this.listener = listener
        }

        scale = 0f
        angle = 0f
        translate = PointF()
        pointerIndex = 0
        tempMatrix = Matrix()
        source = FloatArray(4)
        distance = FloatArray(4)
        setTransformations()
    }

    /**
     * Method called in order to apply transformation made using finger gestures to the
     * matrix using motion event used in the views, to detect touch events.
     * @param event motion event from the view
     */
    fun onTouchEvent(event: MotionEvent) {

        // allow only two fingers
        if (event.pointerCount > 2) {
            return
        }

        when (event.actionMasked) {

            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {

                for (i in 0 until event.pointerCount) {
                    val id = event.getPointerId(i)
                    source[id * 2] = event.getX(i)
                    source[id * 2 + 1] = event.getY(i)
                }

                pointerIndex = 0
            }

            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    val id = event.getPointerId(i)
                    distance[id * 2] = event.getX(i)
                    distance[id * 2 + 1] = event.getY(i)
                }

                // use poly to poly to detect transformations
                tempMatrix.setPolyToPoly(source, pointerIndex, distance, pointerIndex, event.pointerCount)
                matrix.postConcat(tempMatrix)

                if (::listener.isInitialized) {
                    listener.onMatrixChange(matrix)
                }

                System.arraycopy(distance, 0, source, 0, distance.size)
            }

             MotionEvent.ACTION_POINTER_UP -> {

                val index = event.actionIndex
                if (event.getPointerId(index) == 0) pointerIndex = 2
            }
        }

        // set the transformations after matrix update
        setTransformations()
    }

    /**
     * Get the transformations: translate, scale and rotation from the
     * current transformation matrix values.
     */
    fun setTransformations() {
        val points = FloatArray(9)
        matrix.getValues(points)

        val scaleX: Float = points[Matrix.MSCALE_X]
        val skewX: Float = points[Matrix.MSKEW_X]
        val skewY: Float = points[Matrix.MSKEW_Y]
        val translateX: Float = points[Matrix.MTRANS_X]
        val translateY: Float = points[Matrix.MTRANS_Y]

        // set translate
        translate.x = translateX
        translate.y = translateY

        // set scale
        scale = Math.sqrt(scaleX * scaleX + skewY * skewY.toDouble()).toFloat()

        // set rotation as angle in degrees
        angle = -(Math.atan2(skewX.toDouble(), scaleX.toDouble()) * (180.0 / Math.PI)).toFloat()
    }

    /**
     * Listener triggered when matrix is changed when new gesture is detected from
     * the user. The first argument is the matrix with the updated transformations.
     */
    interface OnMatrixChangeListener {
        fun onMatrixChange(matrix: Matrix)
    }
}