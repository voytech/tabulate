package io.github.voytech.tabulate.components.image.operation

import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.StateAttributes
import io.github.voytech.tabulate.core.model.asXPosition
import io.github.voytech.tabulate.core.model.asYPosition
import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.layout.LayoutSpace
import io.github.voytech.tabulate.core.layout.RenderableBoundingBox
import io.github.voytech.tabulate.core.layout.policy.SimpleLayout
import io.github.voytech.tabulate.core.operation.HasImage
import io.github.voytech.tabulate.core.operation.Operation
import io.github.voytech.tabulate.core.operation.Renderable

class ImageRenderable internal constructor(
    val filePath: String,
    override val attributes: Attributes?,
    stateAttributes: StateAttributes,
) : Renderable<SimpleLayout>(), HasImage {

    init {
        additionalAttributes = stateAttributes.data
    }

    override fun LayoutSpace.defineBoundingBox(policy: SimpleLayout): RenderableBoundingBox = with(policy) {
        elementBoundingBox(
            x = getX(0.asXPosition(), uom),
            y = getY(0.asYPosition(), uom),
        )
    }

    override val imageUri: String
        get() = filePath
}

fun interface ImageOperation<CTX : RenderingContext> : Operation<CTX, ImageRenderable>
