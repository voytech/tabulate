package io.github.voytech.tabulate.components.container.opration

import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.layout.LayoutBoundaryType
import io.github.voytech.tabulate.core.layout.RenderableBoundingBox
import io.github.voytech.tabulate.core.layout.Region
import io.github.voytech.tabulate.core.layout.impl.FlowLayout
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

    override fun defineBoundingBox(layout: FlowLayout): RenderableBoundingBox = with(layout) {
        getRenderableBoundingBox(
            x = getMaxBoundingRectangle().leftTop.x,
            y = getMaxBoundingRectangle().leftTop.y,
            width = getMeasuredSize()?.width,
            height = getMeasuredSize()?.height,
            boundaryToFit
        )
    }

}

fun interface ContainerOperation<CTX : RenderingContext> : VoidOperation<CTX, ContainerRenderable>
