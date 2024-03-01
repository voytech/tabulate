package io.github.voytech.tabulate.components.text.operation

import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.layout.LayoutBoundaryType
import io.github.voytech.tabulate.core.layout.LayoutSpace
import io.github.voytech.tabulate.core.layout.RenderableBoundingBox
import io.github.voytech.tabulate.core.layout.impl.SimpleLayout
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.StateAttributes
import io.github.voytech.tabulate.core.operation.HasText
import io.github.voytech.tabulate.core.operation.Operation
import io.github.voytech.tabulate.core.operation.Renderable

class TextRenderable internal constructor(
    val text: String,
    override val attributes: Attributes?,
    stateAttributes: StateAttributes,
) : Renderable<SimpleLayout>(), HasText {

    init {
        additionalAttributes = stateAttributes.data
    }

    override val boundaryToFit = LayoutBoundaryType.OUTER

    override fun LayoutSpace.defineBoundingBox(layout: SimpleLayout): RenderableBoundingBox = with(layout) {
        getRenderableBoundingBox(
            x = leftTop.x,
            y = leftTop.y,
            width = getExplicitWidth(),
            height = getExplicitHeight(),
            type = boundaryToFit
        )
    }

    override val value: String
        get() = text
}

fun interface TextOperation<CTX : RenderingContext> : Operation<CTX, TextRenderable>
