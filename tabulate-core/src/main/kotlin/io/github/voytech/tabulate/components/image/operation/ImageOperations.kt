package io.github.voytech.tabulate.components.image.operation

import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.layout.LayoutBoundaryType
import io.github.voytech.tabulate.core.layout.LayoutSpace
import io.github.voytech.tabulate.core.layout.RenderableBoundingBox
import io.github.voytech.tabulate.core.layout.policy.SimpleLayout
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.StateAttributes
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

    override val boundaryToFit = LayoutBoundaryType.OUTER

    override fun LayoutSpace.defineBoundingBox(layout: SimpleLayout): RenderableBoundingBox = with(layout) {
        getCurrentSize().let { currentSize ->
            elementBoundingBox(
                x = leftTop.x,
                y = leftTop.y,
                width = currentSize?.width.takeIf { properties.declaredWidth },
                height = currentSize?.height.takeIf { properties.declaredHeight },
                boundaryToFit
            )
        }
    }

    override val imageUri: String
        get() = filePath
}

fun interface ImageOperation<CTX : RenderingContext> : Operation<CTX, ImageRenderable>
