package io.github.voytech.tabulate.core.layout.impl

import io.github.voytech.tabulate.core.layout.AbstractLayout
import io.github.voytech.tabulate.core.layout.SequentialLayout
import io.github.voytech.tabulate.core.layout.LayoutProperties
import io.github.voytech.tabulate.core.model.Orientation
import io.github.voytech.tabulate.core.model.Position

class SimpleLayout(properties: LayoutProperties) : AbstractLayout(properties), SequentialLayout {

    override fun resolveNextPosition(): Position = with(region) {
        return if (properties.orientation == Orientation.HORIZONTAL) {
            Position(contentCursorPosition.x + getHorizontalSpacing(), contentLeftTop.y)
        } else {
            Position(contentLeftTop.x, contentCursorPosition.y + getVerticalSpacing())
        }
    }

    override fun hasSpaceLeft(): Boolean = with(region) {
        if (properties.orientation == Orientation.HORIZONTAL) {
            contentCursorPosition.x < contentRightBottom.x
        } else {
            contentCursorPosition.y < contentRightBottom.y
        }
    }

}
