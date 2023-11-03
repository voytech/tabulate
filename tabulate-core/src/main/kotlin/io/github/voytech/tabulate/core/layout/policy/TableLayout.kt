package io.github.voytech.tabulate.core.layout.policy

import io.github.voytech.tabulate.core.layout.*
import io.github.voytech.tabulate.core.model.*

interface TablePolicyMethods {
    fun setColumnWidth(column: Int, width: Width)
    fun setRowHeight(row: Int, height: Height)
    fun getColumnWidth(column: Int, colSpan: Int = 1, uom: UnitsOfMeasure = UnitsOfMeasure.PT): Width?
    fun getRowHeight(row: Int, rowSpan: Int = 1, uom: UnitsOfMeasure = UnitsOfMeasure.PT): Height?
    fun setOffsets(row: Int, column: Int)
}

abstract class AbstractTableLayout(
    properties: LayoutProperties,
    protected open val rowIndex: Int = 0,
    protected open val columnIndex: Int = 0,
) : AbstractLayout(properties), TablePolicyMethods


class SpreadsheetPolicy(
    private val defaultWidthInPt: Float = 0f,
    private val defaultHeightInPt: Float = 0f,
    val standardUnit: StandardUnits = StandardUnits.PT,
) : TablePolicyMethods, AbsolutePositionMethods {

    private var rowOffsetIndex: Int = 0
    private var columnOffsetIndex: Int = 0

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

    private fun MutableMap<Int, PositionAndLength>.offsetIndex(): Int = if (this === rows) {
        rowOffsetIndex
    } else {
        columnOffsetIndex
    }

    private fun MutableMap<Int, PositionAndLength>.ofLocal(index: Int): Int = index - offsetIndex()

    private fun MutableMap<Int, PositionAndLength>.findIndex(position: Float, defMeasure: Float): Int {
        var entry: PositionAndLength? = null
        return (0..Int.MAX_VALUE).find { index ->
            val pos = entry?.let { it.position + it.length + EPSILON } ?: 0.0f
            val resolved = this[index] ?: PositionAndLength(pos, defMeasure)
            entry = resolved
            resolved.contains(position)
        } ?: 0
    }

    private fun MutableMap<Int, PositionAndLength>.findPosition(index: Int, defMeasure: Float): PositionAndLength =
        ofLocal(index).let { effectiveIndex ->
            return this[effectiveIndex] ?: run {
                val first = this[0] ?: PositionAndLength(0.0f, defMeasure)
                return (1..effectiveIndex).fold(first) { agg, idx ->
                    this[idx] ?: PositionAndLength(agg.position + agg.length + EPSILON, defMeasure)
                }.also { this[effectiveIndex] = it }
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
                    X(it.position, standardUnit.asUnitsOfMeasure()).switchUnitOfMeasure(targetUnit)
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
                    Y(it.position, standardUnit.asUnitsOfMeasure()).switchUnitOfMeasure(
                        targetUnit
                    )
                }
            } else relativeY
        }
    }

    private fun <T : Measure<T>> MutableMap<Int, PositionAndLength>.setLengthAtIndex(
        index: Int,
        length: T,
        defaultMeasure: Float,
    ) {
        return findPosition(index, defaultMeasure).let { posLen ->
            length.switchUnitOfMeasure(standardUnit.asUnitsOfMeasure()).let { measure ->
                val localIndex = ofLocal(index)
                val oldLength = this[localIndex]?.length ?: 0f
                if (oldLength < measure.value) {
                    this[localIndex] = PositionAndLength(posLen.position, measure.value)
                    this.keys.forEach {
                        if (it > localIndex) this[it] = this[it]!!.move(measure.value - oldLength)
                    }
                }
            }
        }
    }

    override fun setColumnWidth(column: Int, width: Width) = columns.setLengthAtIndex(column, width, defaultWidthInPt)

    override fun setRowHeight(row: Int, height: Height) = rows.setLengthAtIndex(row, height, defaultHeightInPt)

    private fun <T : Measure<T>> MutableMap<Int, PositionAndLength>.spannedMeasure(
        index: Int,
        span: Int,
        clazz: Class<T>
    ): T =
        clazz.new(
            0.until(span).sumOf { (this[ofLocal(index + it)]?.length ?: defaultWidthInPt).toDouble() }.toFloat(),
            standardUnit.asUnitsOfMeasure()
        )

    override fun getColumnWidth(column: Int, colSpan: Int, uom: UnitsOfMeasure): Width =
        columns.spannedMeasure(column, colSpan, Width::class.java).switchUnitOfMeasure(uom)

    override fun getRowHeight(row: Int, rowSpan: Int, uom: UnitsOfMeasure): Height =
        rows.spannedMeasure(row, rowSpan, Height::class.java).switchUnitOfMeasure(uom)

    private fun getWidth(): Width =
        Width(columns.values.sumOf { it.length.toDouble() }.toFloat(), standardUnit.asUnitsOfMeasure())

    private fun getHeight(): Height =
        Height(rows.values.sumOf { it.length.toDouble() }.toFloat(), standardUnit.asUnitsOfMeasure())

    fun getSize(): Size = Size(getWidth(), getHeight())

    override fun setOffsets(row: Int, column: Int) {
        rowOffsetIndex = row
        columnOffsetIndex = column
    }

}

class TableLayout(properties: LayoutProperties) : AbstractTableLayout(properties) {

    private val delegate = SpreadsheetPolicy()

    private val measurableWidths = mutableMapOf<Int, Boolean>()

    private val measurableHeights = mutableMapOf<Int, Boolean>()

    override fun LayoutSpace.getPosition(relativePosition: Position, targetUnit: UnitsOfMeasure): Position {
        return Position(
            getX(relativePosition.x, targetUnit),
            getY(relativePosition.y, targetUnit)
        )
    }

    override fun LayoutSpace.getX(relativeX: X, targetUnit: UnitsOfMeasure): X {
        assert(relativeX.unit == UnitsOfMeasure.NU) { "input coordinate must be expressed in ordinal numeric units" }
        //TODO getX method should be allowed ONLY to return relative position. SUMMING with absolutePosition should be encapsulated in AbstractLayoutPolicy.
        val absoluteXPosition = getActiveRectangle().leftTop.x.switchUnitOfMeasure(targetUnit)
        val currentLayoutRelativeX = delegate.getX(relativeX, targetUnit)
        return absoluteXPosition + currentLayoutRelativeX
    }

    override fun LayoutSpace.getY(relativeY: Y, targetUnit: UnitsOfMeasure): Y {
        assert(relativeY.unit == UnitsOfMeasure.NU) { "input coordinate must be expressed in ordinal numeric units" }
        val absoluteYPosition = getActiveRectangle().leftTop.y.switchUnitOfMeasure(targetUnit)
        val currentLayoutRelativeY = delegate.getY(relativeY, targetUnit)
        return absoluteYPosition + currentLayoutRelativeY
    }

    override fun getCurrentContentSize(): Size = delegate.getSize()

    fun LayoutSpace.getAbsoluteColumnPosition(columnIndex: Int): X =
        getX(columnIndex.asXPosition(), uom)

    fun LayoutSpace.getAbsoluteRowPosition(rowIndex: Int): Y =
        getY(rowIndex.asYPosition(), uom)

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