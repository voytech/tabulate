package io.github.voytech.tabulate.components.text.operation

import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.asXPosition
import io.github.voytech.tabulate.core.model.asYPosition
import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.layout.DefaultLayoutPolicy
import io.github.voytech.tabulate.core.template.layout.Layout
import io.github.voytech.tabulate.core.template.layout.LayoutElementBoundingBox
import io.github.voytech.tabulate.core.template.operation.HasText
import io.github.voytech.tabulate.core.template.operation.Operation
import io.github.voytech.tabulate.core.template.operation.RenderableContext

class TextRenderable internal constructor(val text: String, override val attributes: Attributes?) : RenderableContext<DefaultLayoutPolicy>(), HasText {

    override fun Layout.computeBoundingBox(policy: DefaultLayoutPolicy): LayoutElementBoundingBox = with(policy) {
        elementBoundingBox(
            x = getX(0.asXPosition(), uom),
            y = getY(0.asYPosition(), uom),
            width = maxBoundingRectangle?.getWidth(),
        )
    }

    override val value: String
        get() = text

}

fun interface TextOperation<CTX : RenderingContext> : Operation<CTX, TextRenderable>
