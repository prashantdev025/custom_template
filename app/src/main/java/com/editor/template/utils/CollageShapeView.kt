package com.editor.template.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.scale
import com.editor.template.R

@SuppressLint("ClickableViewAccessibility")
class CollageShapeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    companion object {
        private var activeShape: CollageShapeView? = null
        private var gestureInProgress = false
    }

    private val matrixValues = Matrix()
    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())

    private var lastX = 0f
    private var lastY = 0f

    private var currentScale = 1f
    private var minScale = 1f
    private var maxScale = 4f

    private var drawableWidth = 0f
    private var drawableHeight = 0f

    private var originalMask: Bitmap? = null
    private var scaledMask: Bitmap? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var onShapeLongClick: (() -> Unit)? = null
    fun setOnShapeLongClickListener(listener: () -> Unit) {
        onShapeLongClick = listener
    }

    private var onShapeClick: (() -> Unit)? = null
    fun setOnShapeClickListener(listener: () -> Unit) {
        onShapeClick = listener
    }

    init {
        scaleType = ScaleType.MATRIX
        setLayerType(LAYER_TYPE_SOFTWARE, null)

        attrs?.let {
            val ta = context.obtainStyledAttributes(it, R.styleable.CollageShapeView)

            val drawableRes = ta.getResourceId(
                R.styleable.CollageShapeView_shapeDrawable,
                0
            )

            if (drawableRes != 0) {
                originalMask = drawableToBitmapInt(drawableRes)
            }

            ta.recycle()
        }

        setOnTouchListener { _, event ->

            gestureDetector.onTouchEvent(event)

            when (event.actionMasked) {

                MotionEvent.ACTION_DOWN -> {

                    if (gestureInProgress && activeShape != this) {
                        return@setOnTouchListener false
                    }

                    activeShape = this
                    gestureInProgress = true

                    lastX = event.x
                    lastY = event.y
                }

                MotionEvent.ACTION_MOVE -> {

                    if (activeShape != this) return@setOnTouchListener true

                    scaleDetector.onTouchEvent(event)

                    if (!scaleDetector.isInProgress) {

                        val dx = event.x - lastX
                        val dy = event.y - lastY

                        matrixValues.postTranslate(dx, dy)

                        fixTranslation()

                        imageMatrix = matrixValues

                        lastX = event.x
                        lastY = event.y
                    }
                }

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {

                    if (activeShape == this) {
                        activeShape = null
                        gestureInProgress = false
                    }
                }
            }

            scaleDetector.onTouchEvent(event)

            true
        }
    }

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            fitImageToView()
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            onShapeClick?.invoke()
            return super.onSingleTapConfirmed(e)
        }

        override fun onLongPress(e: MotionEvent) {
            onShapeLongClick?.invoke()
            super.onLongPress(e)
        }
    }
    )

    fun moveLeft() {
        imageMatrix.postTranslate(-20f, 0f)
        imageMatrix = imageMatrix
        invalidate()
    }

    fun moveRight() {
        imageMatrix.postTranslate(20f, 0f)
        imageMatrix = imageMatrix
        invalidate()
    }

    fun moveTop() {
        imageMatrix.postTranslate(0f, -20f)
        imageMatrix = imageMatrix
        invalidate()
    }

    fun moveBottom() {
        imageMatrix.postTranslate(0f, 20f)
        imageMatrix = imageMatrix
        invalidate()
    }

    fun zoomIn() {
        imageMatrix.postScale(1.1f, 1.1f, width / 2f, height / 2f)
        imageMatrix = imageMatrix
        invalidate()
    }

    fun zoomOut() {
        imageMatrix.postScale(0.9f, 0.9f, width / 2f, height / 2f)
        imageMatrix = imageMatrix
        invalidate()
    }

    fun rotate() {
        imageMatrix.postRotate(90f, width / 2f, height / 2f)
        imageMatrix = imageMatrix
        invalidate()
    }

    fun flipHorizontal() {
        imageMatrix.postScale(-1f, 1f, width / 2f, height / 2f)
        imageMatrix = imageMatrix
        invalidate()
    }

    fun flipVertical() {
        imageMatrix.postScale(1f, -1f, width / 2f, height / 2f)
        imageMatrix = imageMatrix
        invalidate()
    }

    fun setShapeResource(resId: Int) {
        originalMask = drawableToBitmapInt(resId)
        scaledMask = null
        invalidate()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        post {
            fitImageToView()
        }
    }

    fun fitImageToView() {

        drawable ?: return

        drawableWidth = drawable.intrinsicWidth.toFloat()
        drawableHeight = drawable.intrinsicHeight.toFloat()

        if (drawableWidth <= 0 || drawableHeight <= 0 || width == 0 || height == 0) return

        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()

        val scale = maxOf(
            viewWidth / drawableWidth,
            viewHeight / drawableHeight
        )

        minScale = scale
        currentScale = scale

        val dx = (viewWidth - drawableWidth * scale) / 2f
        val dy = (viewHeight - drawableHeight * scale) / 2f

        matrixValues.reset()
        matrixValues.postScale(scale, scale)
        matrixValues.postTranslate(dx, dy)

        imageMatrix = matrixValues
    }

    private fun fixTranslation() {

        val values = FloatArray(9)
        matrixValues.getValues(values)

        val transX = values[Matrix.MTRANS_X]
        val transY = values[Matrix.MTRANS_Y]

        val scaledWidth = drawableWidth * currentScale
        val scaledHeight = drawableHeight * currentScale

        if (scaledWidth > width) {
            val minX = width - scaledWidth
            values[Matrix.MTRANS_X] = transX.coerceIn(minX, 0f)
        }

        if (scaledHeight > height) {
            val minY = height - scaledHeight
            values[Matrix.MTRANS_Y] = transY.coerceIn(minY, 0f)
        }

        matrixValues.setValues(values)
    }

    private fun drawableToBitmapInt(drawableRes: Int): Bitmap {

        val drawable = AppCompatResources.getDrawable(context, drawableRes)!!

        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)

        drawable.setBounds(
            0,
            0,
            canvas.width,
            canvas.height
        )

        drawable.draw(canvas)

        return bitmap
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        originalMask?.let {
            scaledMask = Bitmap.createScaledBitmap(it, w, h, true)
        }
    }

    override fun onDraw(canvas: Canvas) {
        val saveLayer = canvas.saveLayer(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            null
        )

        scaledMask?.let { canvas.drawColor(Color.WHITE) }

        super.onDraw(canvas)

        scaledMask?.let {
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
            canvas.drawBitmap(
                it,
                0f,
                0f,
                paint
            )
            paint.xfermode = null
        }
        canvas.restoreToCount(saveLayer)
    }

    fun setShapeDrawable(drawable: Drawable) {
        originalMask = drawableToBitmap(drawable)

        post {
            if (width > 0 && height > 0 && originalMask != null) {
                scaledMask = originalMask!!.scale(width, height)
                invalidate()
            }
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {

        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 500
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 500

        val bitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)

        drawable.setBounds(
            0,
            0,
            canvas.width,
            canvas.height
        )

        drawable.draw(canvas)

        return bitmap
    }

    inner class ScaleListener :
        ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val factor = detector.scaleFactor
            val newScale = currentScale * factor

            if (newScale in minScale..maxScale) {

                currentScale = newScale

                matrixValues.postScale(
                    factor,
                    factor,
                    detector.focusX,
                    detector.focusY
                )

                fixTranslation()

                imageMatrix = matrixValues
            }
            return true
        }
    }
}