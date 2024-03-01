package io.github.voytech.tabulate.core.layout.impl

import io.github.voytech.tabulate.core.layout.*
import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.model.EPSILON

class SimpleLayout(properties: LayoutProperties) : AbstractLayout(properties), AutonomousLayout {
    override fun LayoutSpace.reset() {

    }

    override fun LayoutSpace.resolveNextPosition(): Position {
        return if (properties.orientation == Orientation.HORIZONTAL) {
            Position(currentPosition.x + EPSILON, innerLeftTop.y)
        } else {
            Position(innerLeftTop.x, currentPosition.y + EPSILON)
        }
    }

    override fun LayoutSpace.hasSpaceLeft(): Boolean =
        if (properties.orientation == Orientation.HORIZONTAL) {
            currentPosition.x < innerMaxRightBottom.x
        } else {
            currentPosition.y < innerMaxRightBottom.y
        }

}
