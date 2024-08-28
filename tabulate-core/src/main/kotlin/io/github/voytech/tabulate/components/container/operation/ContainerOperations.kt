package io.github.voytech.tabulate.components.container.operation

import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.layout.BoundaryType
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

    //This means we are going to fit the bounding box to the border of the container
    //This renderable may be rendered within following area: border>padding>content
    override val boundaryToFit = BoundaryType.BORDER

    override fun FlowLayout.defineBoundingBox(): RenderableBoundingBox =
        getRenderableBoundingBox(
            x = getBorderRectangle().leftTop.x,
            y = getBorderRectangle().leftTop.y,
            width = whenMeasured { getBorderRectangle().getWidth() },
            height = whenMeasured { getBorderRectangle().getHeight() },
            boundaryToFit
        )

}

fun interface ContainerOperation<CTX : RenderingContext> : VoidOperation<CTX, ContainerRenderableEntity>
