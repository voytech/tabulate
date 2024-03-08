package io.github.voytech.tabulate.pdf

import io.github.voytech.tabulate.core.layout.RenderableBoundingBox
import io.github.voytech.tabulate.core.model.asHeight
import io.github.voytech.tabulate.core.model.asWidth
import io.github.voytech.tabulate.core.model.attributes.AlignmentAttribute
import io.github.voytech.tabulate.core.operation.RenderingResult
import kotlin.math.min
import kotlin.math.roundToInt


interface PdfBoxMeasurable {
    fun measure(renderer: PdfBoxRenderingContext): RenderingResult
}

interface PdfBoxRenderable {
    fun render(renderer: PdfBoxRenderingContext): RenderingResult
}

@Suppress("MemberVisibilityCanBePrivate")
abstract class PdfBoxElement(
    protected val boundingBox: RenderableBoundingBox,
    protected val paddings: Paddings,
    protected val alignment: AlignmentAttribute? = null
) {
    protected val topLeftX = boundingBox.absoluteX.value + paddings.left
    protected val topLeftY = boundingBox.absoluteY.value + paddings.top
    protected val maxWidth
        get() = resolveMaxWidth()

    protected val bboxWidth: Int?
        get() = boundingBox.width?.let { it.value - paddings.width }?.toInt()

    protected val maxHeight
        get() = resolveMaxHeight()

    protected val bboxHeight: Int?
        get() = boundingBox.height?.let { it.value - paddings.height }?.toInt()

    protected var contentHeight: Float = 0F

    protected fun x(): Float = topLeftX
    protected fun PdfBoxRenderingContext.y(): Float = (topLeftY + maxHeight).intoPdfBoxOrigin()
    protected fun resolveMaxWidth(): Int = (boundingBox.let {
        val v = it.width
        if (v != null) {
            min(it.maxWidth.value, v.value)
        } else {
            it.maxWidth.value
        }
    } - paddings.width).roundToInt()

    protected fun resolveMaxHeight(): Int = (boundingBox.let {
        val h = it.height
        if (h != null) {
            min(it.maxHeight.value, h.value)
        } else {
            it.maxHeight.value
        }
    } - paddings.height).roundToInt()

    protected fun adjustRenderableBoundingBox(measuredWidth: Float, measuredHeight: Float) {
        contentHeight = measuredHeight
        boundingBox.apply {
            width = width ?: (measuredWidth.asWidth() + paddings.width)
            height = height ?: (measuredHeight.asHeight() + paddings.height)
        }
    }
}