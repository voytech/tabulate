package io.github.voytech.tabulate.components.image.operation

import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.asXPosition
import io.github.voytech.tabulate.core.model.asYPosition
import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.layout.Layout
import io.github.voytech.tabulate.core.template.layout.LayoutElementBoundingBox
import io.github.voytech.tabulate.core.template.layout.elementBoundingBox
import io.github.voytech.tabulate.core.template.operation.Operation
import io.github.voytech.tabulate.core.template.operation.RenderableContext

class ImageRenderable internal constructor(val filePath: String, override val attributes: Attributes?) : RenderableContext() {
    override fun Layout.computeBoundingBox(): LayoutElementBoundingBox = policy.elementBoundingBox(
        x = policy.getX(0.asXPosition(), uom),
        y = policy.getY(0.asYPosition(), uom),
    )
}

fun interface ImageOperation<CTX : RenderingContext> : Operation<CTX, ImageRenderable>
