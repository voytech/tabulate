package io.github.voytech.tabulate.components.image.operation

import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.layout.LayoutBoundaryType
import io.github.voytech.tabulate.core.layout.RenderableBoundingBox
import io.github.voytech.tabulate.core.layout.impl.SimpleLayout
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.StateAttributes
import io.github.voytech.tabulate.core.model.attributes.HeightAttribute
import io.github.voytech.tabulate.core.model.attributes.WidthAttribute
import io.github.voytech.tabulate.core.operation.HasImage
import io.github.voytech.tabulate.core.operation.Operation
import io.github.voytech.tabulate.core.operation.RenderableEntity

class ImageRenderableEntity internal constructor(
    val filePath: String,
    override val attributes: Attributes?,
    stateAttributes: StateAttributes,
) : RenderableEntity<SimpleLayout>(), HasImage {

    init {
        additionalAttributes = stateAttributes.data
    }

    override val boundaryToFit = LayoutBoundaryType.OUTER

    override fun SimpleLayout.defineBoundingBox(): RenderableBoundingBox =
        getRenderableBoundingBox(
            x = getMaxBoundingRectangle().leftTop.x,
            y = getMaxBoundingRectangle().leftTop.y,
            width = getModelAttribute<WidthAttribute>()?.value,
            height = getModelAttribute<HeightAttribute>()?.value,
            boundaryToFit
        )


    override val imageUri: String
        get() = filePath
}

fun interface ImageOperation<CTX : RenderingContext> : Operation<CTX, ImageRenderableEntity>
