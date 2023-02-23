package io.github.voytech.tabulate.components.image.operation

import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.StateAttributes
import io.github.voytech.tabulate.core.model.asXPosition
import io.github.voytech.tabulate.core.model.asYPosition
import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.layout.DefaultLayoutPolicy
import io.github.voytech.tabulate.core.template.layout.Layout
import io.github.voytech.tabulate.core.template.layout.LayoutElementBoundingBox
import io.github.voytech.tabulate.core.template.operation.HasImage
import io.github.voytech.tabulate.core.template.operation.Operation
import io.github.voytech.tabulate.core.template.operation.RenderableContext

class ImageRenderable internal constructor(
    val filePath: String,
    override val attributes: Attributes?,
    stateAttributes: StateAttributes,
) : RenderableContext<DefaultLayoutPolicy>(), HasImage {

    init {
        additionalAttributes = stateAttributes.data
    }

    override fun Layout.computeBoundingBox(policy: DefaultLayoutPolicy): LayoutElementBoundingBox = with(policy) {
        elementBoundingBox(
            x = getX(0.asXPosition(), uom),
            y = getY(0.asYPosition(), uom),
        )
    }

    override val imageUri: String
        get() = filePath
}

fun interface ImageOperation<CTX : RenderingContext> : Operation<CTX, ImageRenderable>
