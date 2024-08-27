package io.github.voytech.tabulate.components.container.operation

import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.layout.LayoutBoundaryType
import io.github.voytech.tabulate.core.layout.RenderableBoundingBox
import io.github.voytech.tabulate.core.layout.impl.FlowLayout
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.StateAttributes
import io.github.voytech.tabulate.core.operation.RenderableEntity
import io.github.voytech.tabulate.core.operation.VoidOperation

class ContainerRenderableEntity internal constructor(
    override val attributes: Attributes?,
    stateAttributes: StateAttributes,
) : RenderableEntity<FlowLayout>() {

    init {
        additionalAttributes = stateAttributes.data
    }

    override val boundaryToFit = LayoutBoundaryType.OUTER

    override fun FlowLayout.defineBoundingBox(): RenderableBoundingBox =
        getRenderableBoundingBox(
            x = getMaxBoundingRectangle().leftTop.x,
            y = getMaxBoundingRectangle().leftTop.y,
            width = getMeasuredSize()?.width,
            height = getMeasuredSize()?.height,
            boundaryToFit
        )

}

fun interface ContainerOperation<CTX : RenderingContext> : VoidOperation<CTX, ContainerRenderableEntity>
