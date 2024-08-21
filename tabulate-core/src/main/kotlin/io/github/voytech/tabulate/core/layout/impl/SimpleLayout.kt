package io.github.voytech.tabulate.core.layout.impl

import io.github.voytech.tabulate.core.layout.AbstractLayout
import io.github.voytech.tabulate.core.layout.AutonomousLayout
import io.github.voytech.tabulate.core.layout.LayoutProperties
import io.github.voytech.tabulate.core.layout.Region
import io.github.voytech.tabulate.core.model.Orientation
import io.github.voytech.tabulate.core.model.Position

class SimpleLayout(properties: LayoutProperties) : AbstractLayout(properties), AutonomousLayout {
    override fun Region.reset() {}

    override fun Region.resolveNextPosition(): Position {
        return if (properties.orientation == Orientation.HORIZONTAL) {
            Position(currentPosition.x + getHorizontalSpacing(), innerLeftTop.y)
        } else {
            Position(innerLeftTop.x, currentPosition.y + getVerticalSpacing())
        }
    }

    override fun Region.hasSpaceLeft(): Boolean =
        if (properties.orientation == Orientation.HORIZONTAL) {
            currentPosition.x < innerMaxRightBottom.x
        } else {
            currentPosition.y < innerMaxRightBottom.y
        }

}
