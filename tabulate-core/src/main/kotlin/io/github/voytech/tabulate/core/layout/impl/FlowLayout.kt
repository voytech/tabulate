package io.github.voytech.tabulate.core.layout.impl

import io.github.voytech.tabulate.core.layout.*
import io.github.voytech.tabulate.core.model.*

class FlowLayout(properties: LayoutProperties) : AbstractLayout(properties), AutonomousLayout {

    private data class Slot(var position: Position, var width: Width? = null, var height: Height? = null)

    private val slots: MutableList<Slot> = mutableListOf()

    private lateinit var localCursor: Position

    private lateinit var absoluteCursor: Position

    private var currentIndex = 0

    private lateinit var uom: UnitsOfMeasure

    override var isSpaceMeasured: Boolean = false


    override fun LayoutSpace.reset() {
        whileMeasuring { this@FlowLayout.uom = this@reset.uom }.also {
            localCursor = Position.start(uom)
            if (::absoluteCursor.isInitialized) {
                absoluteCursor = innerLeftTop
            }
            currentIndex = 0
        }
    }

    private fun LayoutSpace.currentPosition(): Position = (if (::absoluteCursor.isInitialized) absoluteCursor else currentPosition)

    private fun LayoutSpace.moveLocalCursor(pos: Position): Position {
        localCursor = pos
        slots += Slot(pos)
        currentIndex++
        return innerLeftTop + pos
    }

    override fun LayoutSpace.resolveNextPosition(): Position? {
        val computedLocCursor = currentPosition() - innerLeftTop
        return if (properties.orientation == Orientation.HORIZONTAL) {
            if (hasXSpaceLeft()) {
                moveLocalCursor(Position(computedLocCursor.x + EPSILON, localCursor.y))
            } else if (hasYSpaceLeft()) {
                moveLocalCursor(Position(X.zero(uom),computedLocCursor.y + EPSILON))
            } else null
        } else {
            if (hasYSpaceLeft()) {
                moveLocalCursor(Position(localCursor.x, computedLocCursor.y + EPSILON))
            } else if (hasXSpaceLeft()) {
                moveLocalCursor(Position(computedLocCursor.x + EPSILON,Y.zero(uom)))
            } else null
        }
    }

    override fun applyChildRectangle(box: BoundingRectangle) {
        absoluteCursor = if (properties.orientation == Orientation.HORIZONTAL) {
            val ac = if (::absoluteCursor.isInitialized) absoluteCursor.y else Y.zero(uom)
            Position(box.rightBottom.x, box.rightBottom.y.coerceAtLeast(ac))
        } else {
            val ac = if (::absoluteCursor.isInitialized) absoluteCursor.x else X.zero(uom)
            Position(box.rightBottom.x.coerceAtLeast(ac), box.rightBottom.y)
        }
    }

    private fun LayoutSpace.hasXSpaceLeft(): Boolean =
        innerMaxRightBottom?.let { definedMaxRightBottom ->
            currentPosition().x < definedMaxRightBottom.x
        } ?: true

    private fun LayoutSpace.hasYSpaceLeft(): Boolean =
        innerMaxRightBottom?.let { definedMaxRightBottom ->
            currentPosition().y < definedMaxRightBottom.y
        } ?: true

    override fun LayoutSpace.hasSpaceLeft(): Boolean = hasXSpaceLeft() || hasYSpaceLeft()

}