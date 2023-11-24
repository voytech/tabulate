package io.github.voytech.tabulate.components.container.opration

import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.layout.LayoutBoundaryType
import io.github.voytech.tabulate.core.layout.RenderableBoundingBox
import io.github.voytech.tabulate.core.layout.LayoutSpace
import io.github.voytech.tabulate.core.layout.policy.FlowLayout
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.StateAttributes
import io.github.voytech.tabulate.core.operation.Renderable
import io.github.voytech.tabulate.core.operation.VoidOperation

class ContainerRenderable internal constructor(
    override val attributes: Attributes?,
    stateAttributes: StateAttributes,
) : Renderable<FlowLayout>() {

    init {
        additionalAttributes = stateAttributes.data
    }

    override val boundaryToFit = LayoutBoundaryType.OUTER

    override fun LayoutSpace.defineBoundingBox(layout: FlowLayout): RenderableBoundingBox = with(layout) {
        elementBoundingBox(
            x = leftTop.x,
            y = leftTop.y,
            width = getMeasuredSize()?.width,
            height = getMeasuredSize()?.height
        )
    }

}

fun interface ContainerOperation<CTX : RenderingContext> : VoidOperation<CTX, ContainerRenderable>
