package io.github.voytech.tabulate.components.container.opration

import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.layout.RenderableBoundingBox
import io.github.voytech.tabulate.core.layout.LayoutSpace
import io.github.voytech.tabulate.core.layout.policy.FlowLayout
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.asXPosition
import io.github.voytech.tabulate.core.model.asYPosition
import io.github.voytech.tabulate.core.operation.Operation
import io.github.voytech.tabulate.core.operation.Renderable
import io.github.voytech.tabulate.core.operation.VoidOperation

class ContainerRenderable internal constructor(
    override val attributes: Attributes?,
) : Renderable<FlowLayout>() {

    override fun LayoutSpace.defineBoundingBox(policy: FlowLayout): RenderableBoundingBox = with(policy) {
        elementBoundingBox(
            x = getX(0.asXPosition(), uom),
            y = getY(0.asYPosition(), uom),
            width = policy.getMeasuredSize()?.width,
            height = policy.getMeasuredSize()?.height
        )
    }

}

fun interface ContainerOperation<CTX : RenderingContext> : VoidOperation<CTX, ContainerRenderable>
