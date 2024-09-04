package io.github.voytech.tabulate.components.text.operation

import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.layout.BoundaryType
import io.github.voytech.tabulate.core.layout.RenderableBoundingBox
import io.github.voytech.tabulate.core.layout.impl.SimpleLayout
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.StateAttributes
import io.github.voytech.tabulate.core.model.attributes.HeightAttribute
import io.github.voytech.tabulate.core.model.attributes.WidthAttribute
import io.github.voytech.tabulate.core.operation.HasText
import io.github.voytech.tabulate.core.operation.Operation
import io.github.voytech.tabulate.core.operation.RenderableEntity
import io.github.voytech.tabulate.ellipsis

class TextRenderableEntity internal constructor(
    val text: String,
    override val attributes: Attributes?,
    stateAttributes: StateAttributes,
) : RenderableEntity<SimpleLayout>(), HasText {

    init {
        additionalAttributes = stateAttributes.data
    }

    override val boundaryToFit = BoundaryType.BORDER

    override fun SimpleLayout.defineBoundingBox(): RenderableBoundingBox =
        getRenderableBoundingBox(
            x = getBorderRectangle().leftTop.x,
            y = getBorderRectangle().leftTop.y,
            // In case of when measure was called prior to render
            // we can take measured size of layout which was used for measuring.
            width = whenMeasured { getBorderRectangle().getWidth() } ?: getModelAttribute<WidthAttribute>()?.value,
            height = whenMeasured { getBorderRectangle().getHeight() } ?: getModelAttribute<HeightAttribute>()?.value,
            type = boundaryToFit
        )


    override val value: String
        get() = text

    override fun toString(): String {
        return "TextRenderable[${text.ellipsis()}]"
    }
}

fun interface TextOperation<CTX : RenderingContext> : Operation<CTX, TextRenderableEntity>
