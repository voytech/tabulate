package io.github.voytech.tabulate.components.text.operation

import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.asXPosition
import io.github.voytech.tabulate.core.model.asYPosition
import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.layout.Layout
import io.github.voytech.tabulate.core.template.layout.LayoutElement
import io.github.voytech.tabulate.core.template.layout.LayoutElementBoundingBox
import io.github.voytech.tabulate.core.template.layout.elementBoundaries
import io.github.voytech.tabulate.core.template.operation.AttributedContext
import io.github.voytech.tabulate.core.template.operation.HasText
import io.github.voytech.tabulate.core.template.operation.Operation

class TextRenderable internal constructor(val text: String, override val attributes: Attributes?) : AttributedContext(),
    LayoutElement, HasText {
    override fun Layout<*, *, *>.computeBoundaries(): LayoutElementBoundingBox = query.elementBoundaries(
        x = query.getX(0.asXPosition(), uom),
        y = query.getY(0.asYPosition(), uom),
        width = query.layout.maxBoundingRectangle?.getWidth(),
    )

    override val value: String
        get() = text
}

fun interface RenderTextOperation<CTX : RenderingContext> : Operation<CTX, TextRenderable>
