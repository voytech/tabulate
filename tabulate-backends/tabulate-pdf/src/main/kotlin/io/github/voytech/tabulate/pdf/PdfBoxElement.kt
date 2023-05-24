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
    protected val maxHeight
        get() = resolveMaxHeight()

    protected fun x(): Float = topLeftX
    protected fun PdfBoxRenderingContext.y(): Float = (topLeftY + maxHeight).intoPdfBoxOrigin()
    protected fun resolveMaxWidth(): Int = ((boundingBox.let {
        if (it.width != null) {
            min(it.maxWidth?.value ?: 0F, it.width?.value ?: 0F)
        } else {
            it.maxWidth?.value
        }
    } ?: 0f) - paddings.width).roundToInt()

    protected fun resolveMaxHeight(): Int = ((boundingBox.let {
        if (it.height != null) {
            min(it.maxHeight?.value ?: 0F, it.height?.value ?: 0F)
        } else {
            it.maxHeight?.value
        }
    } ?: 0f) - paddings.height).roundToInt()

    protected fun applySize(measuredWidth: Float, measuredHeight: Float) {
        boundingBox.apply {
            width = width ?: (measuredWidth.asWidth() + paddings.width)
            height = height ?: (measuredHeight.asHeight() + paddings.height)
        }
    }
}