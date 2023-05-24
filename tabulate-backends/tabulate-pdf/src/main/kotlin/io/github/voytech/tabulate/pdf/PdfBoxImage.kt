package io.github.voytech.tabulate.pdf

import io.github.voytech.tabulate.core.layout.RenderableBoundingBox
import io.github.voytech.tabulate.core.model.attributes.AlignmentAttribute
import io.github.voytech.tabulate.core.operation.Ok
import io.github.voytech.tabulate.core.operation.RenderingResult
import io.github.voytech.tabulate.core.operation.asResult
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject

class PdfBoxImage(
    private val image: PDImageXObject,
    boundingBox: RenderableBoundingBox,
    paddings: Paddings,
    alignment: AlignmentAttribute? = null
) : PdfBoxElement(boundingBox, paddings, alignment), PdfBoxRenderable, PdfBoxMeasurable {

    private var measured = false

    override fun measure(renderer: PdfBoxRenderingContext): RenderingResult {
        if (!boundingBox.isDefined()) {
            applySize(image.width.toFloat(), image.height.toFloat())
        }
        measured = true
        return Ok.asResult()
    }

    override fun render(renderer: PdfBoxRenderingContext): RenderingResult {
        if (!measured) {
            measure(renderer)
        }
        with(renderer) {
            image.showImage(x(), y(), maxWidth.toFloat(), maxHeight.toFloat())
        }
        return Ok.asResult()
    }
}