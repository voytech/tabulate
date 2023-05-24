package io.github.voytech.tabulate.core.layout.policy

import io.github.voytech.tabulate.core.layout.AbstractLayout
import io.github.voytech.tabulate.core.layout.IterableLayout
import io.github.voytech.tabulate.core.layout.LayoutProperties
import io.github.voytech.tabulate.core.layout.LayoutSpace
import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.model.EPSILON

class SimpleLayout(properties: LayoutProperties) : AbstractLayout(properties), IterableLayout {

    override fun LayoutSpace.resolveNextPosition(): Position {
        return if (properties.orientation == Orientation.HORIZONTAL) {
            Position(rightBottom.x + EPSILON, leftTop.y)
        } else {
            Position(leftTop.x, rightBottom.y + EPSILON)
        }
    }

    override fun LayoutSpace.hasSpaceLeft(): Boolean =
        maxRightBottom?.let { definedMaxRightBottom ->
            if (properties.orientation == Orientation.HORIZONTAL) {
                rightBottom.x < definedMaxRightBottom.x
            } else {
                rightBottom.y < definedMaxRightBottom.y
            }
        } ?: true

}
