package io.github.voytech.tabulate.core.layout.impl

import io.github.voytech.tabulate.core.layout.*
import io.github.voytech.tabulate.core.model.*

class FlowLayout(properties: LayoutProperties) : AbstractLayout(properties), AutonomousLayout {

    private lateinit var localCursor: Position

    private lateinit var absoluteCursor: Position

    private var currentIndex = 0


    override var isMeasured: Boolean = false

    override fun onBeginLayout() = with(region) {
        localCursor = Position.start(uom)
        if (::absoluteCursor.isInitialized) {
            absoluteCursor = innerLeftTop
        }
        currentIndex = 0
    }

    /**
     * We for multi row flow layout we need separate position [absoluteCursor] for tracking next layout element.
     * [currentPosition] from Layout tracks globally allocated area in the layout and can only grow along both axis.
     * When moving to next row we need to reset one of the axis again which.
     */
    private fun Region.currentPosition(): Position =
        (if (::absoluteCursor.isInitialized) absoluteCursor else currentPosition)

    private fun Region.moveLocalCursor(pos: Position): Position {
        localCursor = pos
        currentIndex++
        return innerLeftTop + pos
    }

    override fun resolveNextPosition(): Position? = with(region) {
        val computedLocCursor = currentPosition() - innerLeftTop
        return if (properties.orientation == Orientation.HORIZONTAL) {
            if (hasXSpaceLeft()) {
                moveLocalCursor(Position(computedLocCursor.x + getHorizontalSpacing(), localCursor.y))
            } else if (hasYSpaceLeft()) {
                moveLocalCursor(Position(X.zero(uom), computedLocCursor.y + getVerticalSpacing()))
            } else null
        } else {
            if (hasYSpaceLeft()) {
                moveLocalCursor(Position(localCursor.x, computedLocCursor.y + getVerticalSpacing()))
            } else if (hasXSpaceLeft()) {
                moveLocalCursor(Position(computedLocCursor.x + getHorizontalSpacing(), Y.zero(uom)))
            } else null
        }
    }

    override fun onChildLayoutAbsorbed(boundingRectangle: BoundingRectangle) {
        absoluteCursor = if (properties.orientation == Orientation.HORIZONTAL) {
            val ac = if (::absoluteCursor.isInitialized) absoluteCursor.y else Y.zero(region.uom)
            Position(boundingRectangle.rightBottom.x, boundingRectangle.rightBottom.y.coerceAtLeast(ac))
        } else {
            val ac = if (::absoluteCursor.isInitialized) absoluteCursor.x else X.zero(region.uom)
            Position(boundingRectangle.rightBottom.x.coerceAtLeast(ac), boundingRectangle.rightBottom.y)
        }
    }

    private fun Region.hasXSpaceLeft(): Boolean = currentPosition().x < innerMaxRightBottom.x

    private fun Region.hasYSpaceLeft(): Boolean = currentPosition().y < innerMaxRightBottom.y

    override fun hasSpaceLeft(): Boolean = with(region) { hasXSpaceLeft() || hasYSpaceLeft() }

}