package io.github.voytech.tabulate.core.template.layout

import io.github.voytech.tabulate.core.model.*

interface AbsolutePositionPolicy {

    /**
     * Query for absolute position expressed in [targetUnit] by using current layout relative position.
     */
    fun getPosition(relativePosition: Position, targetUnit: UnitsOfMeasure): Position

    /**
     * Query for absolute X axis position expressed in [targetUnit] by using current layout relative X position.
     */
    fun getX(relativeX: X, targetUnit: UnitsOfMeasure): X

    /**
     * Query for absolute Y axis position expressed in [targetUnit] by using current layout relative Y position.
     */
    fun getY(relativeY: Y, targetUnit: UnitsOfMeasure): Y
}

interface GridPolicyMethods {
    fun setColumnWidth(column: Int, width: Width)
    fun setRowHeight(row: Int, height: Height)
    fun getColumnWidth(column: Int, colSpan: Int = 1, uom: UnitsOfMeasure = UnitsOfMeasure.PT): Width?
    fun getRowHeight(row: Int, rowSpan: Int = 1, uom: UnitsOfMeasure = UnitsOfMeasure.PT): Height?
    fun setOffsets(row: Int, column: Int)
}

interface LayoutPolicy {

    var isSpaceMeasured: Boolean
    /**
     * Query for absolute position expressed in [targetUnit] by using current layout relative position.
     */
    fun Layout.getPosition(relativePosition: Position, targetUnit: UnitsOfMeasure): Position

    /**
     * Query for absolute X axis position expressed in [targetUnit] by using current layout relative X position.
     */
    fun Layout.getX(relativeX: X, targetUnit: UnitsOfMeasure): X

    /**
     * Query for absolute Y axis position expressed in [targetUnit] by using current layout relative Y position.
     */
    fun Layout.getY(relativeY: Y, targetUnit: UnitsOfMeasure): Y

    /**
     * Extend layout rendered space by specific [Width].
     */
    fun Layout.extend(width: Width)

    /**
     * Extend layout rendered space by specific [Height].
     */
    fun Layout.extend(height: Height)

    fun Layout.getLayoutBoundary(): BoundingRectangle = boundingRectangle

    fun Layout.elementBoundingBox(x: X, y: Y, width: Width? = null, height: Height? = null, ): LayoutElementBoundingBox {
        val uom = getLayoutBoundary().leftTop.x.unit
        return LayoutElementBoundingBox(
            layoutPosition = getLayoutBoundary().leftTop,
            absoluteX = x.switchUnitOfMeasure(uom),
            absoluteY = y.switchUnitOfMeasure(uom),
            width = width?.switchUnitOfMeasure(uom),
            height = height?.switchUnitOfMeasure(uom)
        )
    }

}

abstract class AbstractGridLayoutPolicy(protected open val rowIndex: Int = 0, protected open val columnIndex: Int = 0) :
    LayoutPolicy, GridPolicyMethods

interface Layout {
    val uom: UnitsOfMeasure
    val leftTop: Position
    val maxRightBottom: Position?
    val boundingRectangle: BoundingRectangle
    val maxBoundingRectangle: BoundingRectangle?

    fun LayoutElementBoundingBox?.checkOverflow(): Overflow?

    fun LayoutElementBoundingBox.applyOnLayout() {
        extend(absoluteX + width.orZero())
        extend(absoluteY + height.orZero())
    }

    fun extend(position: Position)
    fun extend(width: Width)
    fun extend(x: X)
    fun extend(height: Height)
    fun extend(y: Y)
}

sealed class AbstractLayout(
    override val uom: UnitsOfMeasure,
    override val leftTop: Position,
    override val maxRightBottom: Position?,
    internal var rightBottom: Position = leftTop,
) : Layout {

    override val boundingRectangle: BoundingRectangle
        get() = BoundingRectangle(leftTop, rightBottom)

    override val maxBoundingRectangle: BoundingRectangle?
        get() = maxRightBottom?.let { BoundingRectangle(leftTop, it) }

    private fun LayoutElementBoundingBox.isXOverflow(): Boolean = maxRightBottom?.let {
        (absoluteX.value + (width.orZero().value)) > it.x.value
    } ?: false

    private fun LayoutElementBoundingBox.isYOverflow(): Boolean = maxRightBottom?.let {
        (absoluteY.value + (height.orZero().value)) > it.y.value
    } ?: false

    override fun LayoutElementBoundingBox?.checkOverflow(): Overflow? = if (this == null) null else {
        if (isXOverflow()) {
            Overflow.X
        } else if (isYOverflow()) {
            Overflow.Y
        } else null
    }

    override fun extend(position: Position) {
        rightBottom = orMax(rightBottom, position)
    }

    override fun extend(width: Width) = extend(Position(rightBottom.x + width, rightBottom.y))

    override fun extend(x: X) = extend(Position(x, rightBottom.y))

    override fun extend(height: Height) = extend(Position(rightBottom.x, rightBottom.y + height))

    override fun extend(y: Y) = extend(Position(rightBottom.x, y))

}

class DefaultLayout(
    uom: UnitsOfMeasure,
    leftTop: Position,
    maxRightBottom: Position?
) : AbstractLayout(uom, leftTop, maxRightBottom)

fun interface LayoutElement<PL: LayoutPolicy> {
    fun Layout.computeBoundingBox(policy: PL): LayoutElementBoundingBox
}

fun interface BoundingBoxModifier {
    fun Layout.alter(source: LayoutElementBoundingBox): LayoutElementBoundingBox
}

fun interface LayoutElementApply<PL: LayoutPolicy> {
    fun Layout.applyBoundingBox(context: LayoutElementBoundingBox, policy: PL, flags: BoundingBoxFlags)
}

data class LayoutElementBoundingBox(
    val layoutPosition: Position,
    val absoluteX: X,
    val absoluteY: Y,
    var width: Width? = null,
    var height: Height? = null,
) {

    fun unitsOfMeasure(): UnitsOfMeasure = layoutPosition.x.unit

    fun isDefined(): Boolean = width != null && height != null

    operator fun plus(other: LayoutElementBoundingBox): LayoutElementBoundingBox = copy(
        width = other.width?.switchUnitOfMeasure(unitsOfMeasure()) ?: width,
        height = other.height?.switchUnitOfMeasure(unitsOfMeasure()) ?: height
    )

    fun currentFlags(): BoundingBoxFlags = BoundingBoxFlags(width == null, height == null)
}

data class BoundingBoxFlags(val shouldMeasureWidth: Boolean, val shouldMeasureHeight: Boolean)

fun LayoutElementBoundingBox?.isDefined() = this?.isDefined() ?: false

enum class Overflow {
    X,
    Y
}



class DefaultLayoutPolicy : LayoutPolicy {

    override var isSpaceMeasured: Boolean = false

    override fun Layout.getPosition(relativePosition: Position, targetUnit: UnitsOfMeasure): Position = Position(
        getX(relativePosition.x, targetUnit), getY(relativePosition.y, targetUnit)
    )

    override fun Layout.getX(relativeX: X, targetUnit: UnitsOfMeasure): X {
        val absoluteX = getLayoutBoundary().leftTop.x.switchUnitOfMeasure(targetUnit)
        return X(absoluteX.value + relativeX.value, targetUnit)
    }

    override fun Layout.getY(relativeY: Y, targetUnit: UnitsOfMeasure): Y {
        val absoluteY = getLayoutBoundary().leftTop.y.switchUnitOfMeasure(targetUnit)
        return Y(absoluteY.value + relativeY.value, targetUnit)
    }

    /**
     * Extend layout rendered space by specific [Width].
     */
    override fun Layout.extend(width: Width) {
        extend(width)
    }

    /**
     * Extend layout rendered space by specific [Height].
     */
    override fun Layout.extend(height: Height) {
        extend(height)
    }

}

class SpreadsheetPolicy(
    private val defaultWidthInPt: Float = 0f,
    private val defaultHeightInPt: Float = 0f,
    val standardUnit: StandardUnits = StandardUnits.PT,
) : GridPolicyMethods, AbsolutePositionPolicy {

    private var rowIndex: Int = 0
    private var columnIndex: Int = 0
    private var firstRowPosition: PositionAndLength = PositionAndLength.zero()
    private var firstColumnPosition: PositionAndLength = PositionAndLength.zero()

    private val rows: MutableMap<Int, PositionAndLength> = mutableMapOf()
    private val columns: MutableMap<Int, PositionAndLength> = mutableMapOf()

    data class PositionAndLength(val position: Float, val length: Float) {
        fun contains(value: Float): Boolean =
            position.compareTo(value) <= 0 && (position + length).compareTo(value) >= 0

        fun move(offset: Float) = PositionAndLength(position + offset, length)

        companion object {
            fun zero(): PositionAndLength = PositionAndLength(0F, 0F)
        }
    }

    override fun getPosition(relativePosition: Position, targetUnit: UnitsOfMeasure): Position = Position(
        getX(relativePosition.x, targetUnit), getY(relativePosition.y, targetUnit)
    )

    private fun MutableMap<Int, PositionAndLength>.startingIndex(): Int = if (this === rows) {
        rowIndex
    } else {
        columnIndex
    }

    private fun MutableMap<Int, PositionAndLength>.findIndex(position: Float, defMeasure: Float): Int {
        var entry: PositionAndLength? = null
        return (startingIndex()..Int.MAX_VALUE).find { index ->
            val pos = entry?.let { it.position + it.length + EPSILON } ?: 0.0f
            val resolved = this[index] ?: PositionAndLength(pos, defMeasure)
            entry = resolved
            resolved.contains(position)
        } ?: startingIndex()
    }

    private fun MutableMap<Int, PositionAndLength>.findPosition(index: Int, defMeasure: Float): PositionAndLength {
        return this[index] ?: run {
            val first = this[startingIndex()] ?: PositionAndLength(0.0f, defMeasure)
            return (startingIndex() + 1..index).fold(first) { agg, _index ->
                this[_index] ?: PositionAndLength(agg.position + agg.length + EPSILON, defMeasure)
            }.also { this[index] = it }
        }
    }

    override fun getX(relativeX: X, targetUnit: UnitsOfMeasure): X {
        return if (relativeX.unit != UnitsOfMeasure.NU) {
            if (targetUnit == UnitsOfMeasure.NU) {
                val ptX = relativeX.switchUnitOfMeasure(standardUnit.asUnitsOfMeasure())
                val column = columns.findIndex(ptX.value, defaultWidthInPt)
                X(column.toFloat(), targetUnit)
            } else {
                relativeX.switchUnitOfMeasure(targetUnit)
            }
        } else {
            if (targetUnit != UnitsOfMeasure.NU) {
                columns.findPosition(relativeX.value.toInt(), defaultWidthInPt).let {
                    X(it.position - firstColumnPosition.position, standardUnit.asUnitsOfMeasure()).switchUnitOfMeasure(
                        targetUnit
                    )
                }
            } else relativeX
        }
    }

    override fun getY(relativeY: Y, targetUnit: UnitsOfMeasure): Y {
        return if (relativeY.unit != UnitsOfMeasure.NU) {
            if (targetUnit == UnitsOfMeasure.NU) {
                val ptX = relativeY.switchUnitOfMeasure(standardUnit.asUnitsOfMeasure())
                val row = rows.findIndex(ptX.value, defaultHeightInPt)
                Y(row.toFloat(), targetUnit)
            } else {
                relativeY.switchUnitOfMeasure(targetUnit)
            }
        } else {
            if (targetUnit != UnitsOfMeasure.NU) {
                rows.findPosition(relativeY.value.toInt(), defaultHeightInPt).let {
                    Y(it.position - firstRowPosition.position, standardUnit.asUnitsOfMeasure()).switchUnitOfMeasure(
                        targetUnit
                    )
                }
            } else relativeY
        }
    }

    private fun MutableMap<Int, PositionAndLength>.setLengthAtIndex(
        index: Int,
        length: Measure,
        defaultMeasure: Float,
    ) {
        return findPosition(index, defaultMeasure).let { posLen ->
            length.unit.switchUnitOfMeasure(length.value, standardUnit.asUnitsOfMeasure()).let { measure ->
                val oldLength = this[index]?.length ?: 0f
                if (oldLength < measure) {
                    this[index] = PositionAndLength(posLen.position, measure)
                    this.keys.forEach {
                        if (it > index) this[it] = this[it]!!.move(measure - oldLength)
                    }
                }
            }
        }
    }

    override fun setColumnWidth(column: Int, width: Width) = columns.setLengthAtIndex(column, width, defaultWidthInPt)

    override fun setRowHeight(row: Int, height: Height) = rows.setLengthAtIndex(row, height, defaultHeightInPt)

    private fun MutableMap<Int,PositionAndLength>.spannedWidth(index: Int, span: Int): MeasuredValue =
        MeasuredValue(0.until(span).sumOf { (this[index+it]?.length ?: defaultWidthInPt).toDouble() }.toFloat(), standardUnit.asUnitsOfMeasure())

    override fun getColumnWidth(column: Int, colSpan: Int, uom: UnitsOfMeasure): Width =
        columns.spannedWidth(column, colSpan).width().switchUnitOfMeasure(uom)

    override fun getRowHeight(row: Int, rowSpan: Int, uom: UnitsOfMeasure): Height =
        rows.spannedWidth(row,rowSpan).height().switchUnitOfMeasure(uom)

    override fun setOffsets(row: Int, column: Int) {
        rowIndex = row
        columnIndex = column
        firstRowPosition = rows.findPosition(rowIndex, defaultHeightInPt)
        firstColumnPosition = columns.findPosition(columnIndex, defaultWidthInPt)
    }

}

class GridLayoutPolicy : AbstractGridLayoutPolicy() {

    private val delegate = SpreadsheetPolicy()

    override var isSpaceMeasured: Boolean = false

    private val measurableWidths = mutableMapOf<Int, Boolean>()

    private val measurableHeights = mutableMapOf<Int, Boolean>()

    override fun Layout.getPosition(relativePosition: Position, targetUnit: UnitsOfMeasure): Position {
        return Position(
            getX(relativePosition.x, targetUnit),
            getY(relativePosition.y, targetUnit)
        )
    }

    override fun Layout.getX(relativeX: X, targetUnit: UnitsOfMeasure): X {
        assert(relativeX.unit == UnitsOfMeasure.NU) { "input coordinate must be expressed in ordinal numeric units" }
        //TODO getX method should be allowed ONLY to return relative position. SUMMING with absolutePosition should be encapsulated in AbstractLayoutPolicy.
        val absoluteXPosition = getLayoutBoundary().leftTop.x.switchUnitOfMeasure(targetUnit)
        val currentLayoutRelativeX = delegate.getX(relativeX, targetUnit)
        return absoluteXPosition + currentLayoutRelativeX
    }

    override fun Layout.getY(relativeY: Y, targetUnit: UnitsOfMeasure): Y {
        assert(relativeY.unit == UnitsOfMeasure.NU) { "input coordinate must be expressed in ordinal numeric units" }
        val absoluteYPosition = getLayoutBoundary().leftTop.y.switchUnitOfMeasure(targetUnit)
        val currentLayoutRelativeY = delegate.getY(relativeY, targetUnit)
        return absoluteYPosition + currentLayoutRelativeY
    }

    override fun Layout.extend(width: Width) {
        extend(width)
    }

    override fun Layout.extend(height: Height) {
        extend(height)
    }

    fun markWidthForMeasure(column: Int, measured: Boolean = false) {
        if (isSpaceMeasured) return
        measurableWidths[column] = measured
    }

    override fun setColumnWidth(column: Int, width: Width) {
        if (isSpaceMeasured) return
        delegate.setColumnWidth(column, width)
    }

    fun markHeightForMeasure(row: Int, measured: Boolean = false) {
        if (isSpaceMeasured) return
        measurableHeights[row] = measured
    }

    override fun setRowHeight(row: Int, height: Height) {
        if (isSpaceMeasured) return
        delegate.setRowHeight(row, height)
    }

    override fun getColumnWidth(column: Int, colSpan: Int, uom: UnitsOfMeasure): Width? =
        if (isSpaceMeasured || measurableWidths[column] != true) {
            delegate.getColumnWidth(column, colSpan, uom)
        } else null

    override fun getRowHeight(row: Int, rowSpan: Int, uom: UnitsOfMeasure): Height? =
        if (isSpaceMeasured || measurableHeights[row] != true) {
            delegate.getRowHeight(row, rowSpan, uom)
        } else null

    override fun setOffsets(row: Int, column: Int) {
        delegate.setOffsets(row, column)
    }

}