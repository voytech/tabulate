package io.github.voytech.tabulate.components.text.operation

import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.layout.*
import io.github.voytech.tabulate.core.layout.impl.SimpleLayout
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.StateAttributes
import io.github.voytech.tabulate.core.model.attributes.HeightAttribute
import io.github.voytech.tabulate.core.model.attributes.WidthAttribute
import io.github.voytech.tabulate.core.operation.HasText
import io.github.voytech.tabulate.core.operation.Operation
import io.github.voytech.tabulate.core.operation.Renderable
import io.github.voytech.tabulate.core.operation.RenderingStatus
import io.github.voytech.tabulate.ellipsis

class TextRenderable internal constructor(
    val text: String,
    override val attributes: Attributes?,
    stateAttributes: StateAttributes,
) : Renderable<SimpleLayout>(), ApplyLayoutElement<SimpleLayout>, HasText {

    init {
        additionalAttributes = stateAttributes.data
    }

    override val boundaryToFit = LayoutBoundaryType.OUTER

    override fun Region.defineBoundingBox(layout: SimpleLayout): RenderableBoundingBox = with(layout) {
        getRenderableBoundingBox(
            x = leftTop.x,
            y = leftTop.y,
            // In case of when measure was called prior to render
            // we can take measured size of layout which was used for measuring.
            width = getMeasuredSize()?.width ?:  getModelAttribute<WidthAttribute>()?.value,
            height = getMeasuredSize()?.height ?: getModelAttribute<HeightAttribute>()?.value,
            type = boundaryToFit
        )
    }

    override fun Region.applyBoundingBox(
        bbox: RenderableBoundingBox,
        layout: SimpleLayout,
        status: RenderingStatus
    ) = with(layout) {
        allocateRectangle(bbox)
    }

    override val value: String
        get() = text

    override fun toString(): String {
        return "TextRenderable[${text.ellipsis()}]"
    }
}

fun interface TextOperation<CTX : RenderingContext> : Operation<CTX, TextRenderable>
