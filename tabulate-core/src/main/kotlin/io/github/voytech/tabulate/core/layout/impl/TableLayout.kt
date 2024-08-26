package io.github.voytech.tabulate.core.layout.impl

import io.github.voytech.tabulate.core.layout.*
import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.round3

enum class SizingOptions {
    SET,
    SET_IF_GREATER,
    SET_LOCKED
}

interface TableSizingMethods {
    fun setColumnWidth(column: Int, width: Width, options: SizingOptions = SizingOptions.SET)
    fun setRowHeight(row: Int, height: Height, options: SizingOptions = SizingOptions.SET)
    fun resizeColumnsToFit(width: Width)
    fun resizeRowsToFit(height: Height)
    fun getMeasuredColumnWidth(column: Int, colSpan: Int = 1, uom: UnitsOfMeasure = UnitsOfMeasure.PT): Width?
    fun getMeasuredRowHeight(row: Int, rowSpan: Int = 1, uom: UnitsOfMeasure = UnitsOfMeasure.PT): Height?
    fun startAt(row: Int, column: Int)
}

abstract class AbstractTableLayout(
    properties: LayoutProperties,
    protected open val rowIndex: Int = 0,
    protected open val columnIndex: Int = 0,
) : AbstractLayout(properties), TableSizingMethods


class NonUniformCartesianGrid(
    defaultUnscaledWidthInPt: Float = 0f,
    defaultUnscaledHeightInPt: Float = 0f,
    val standardUnit: StandardUnits = StandardUnits.PT,
) : TableSizingMethods, AbsolutePositionMethods {

    private val defaultWidthInPt: Float = defaultUnscaledWidthInPt.round3()
    private val defaultHeightInPt: Float = defaultUnscaledHeightInPt.round3()

    private var rowOffsetIndex: Int = 0
    private var columnOffsetIndex: Int = 0

    private val rows: MutableMap<Int, PositionAndLength> = mutableMapOf()
    private val columns: MutableMap<Int, PositionAndLength> = mutableMapOf()
    private val lockedRows: MutableMap<Int, Boolean> = mutableMapOf()
    private val lockedColumns: MutableMap<Int, Boolean> = mutableMapOf()

    enum class IndexRoundMode {
        HALF_UP,
        HALF_DOWN,
        CEILING,
        FLOOR
    }

    data class PositionAndLength(val position: Float, val length: Float) {
        fun indexOffsetOrNull(value: Float, mode: IndexRoundMode = IndexRoundMode.FLOOR): Int? {
            val end = (position + length).round3()
            return if (value in position..end) {
                when (mode) {
                    IndexRoundMode.FLOOR -> if (value.toInt() < end.toInt()) 0 else 1
                    IndexRoundMode.CEILING -> if (value.toInt() > position.toInt()) 1 else 0
                    IndexRoundMode.HALF_UP -> 1.takeIf { value - position >= end - value } ?: 0
                    IndexRoundMode.HALF_DOWN -> 1.takeIf { value - position > end - value } ?: 0
                }
            } else null
        }

        fun move(offset: Float) = PositionAndLength(position + offset.round3(), length)

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

    private fun MutableMap<Int, PositionAndLength>.findIndex(
        position: Float, defMeasure: Float, mode: IndexRoundMode = IndexRoundMode.FLOOR
    ): Int {
        val start = position.round3()
        var entry: PositionAndLength? = null
        for (index in 0..Int.MAX_VALUE) {
            val resolved = this[index] ?: run {
                val pos = entry?.let { it.position + it.length + DEFAULT_GAP } ?: 0.0f
                PositionAndLength(pos.round3(), defMeasure.round3())
            }
            entry = resolved
            val maybeOffset = entry.indexOffsetOrNull(start, mode)
            if (maybeOffset != null) {
                return index + maybeOffset
            } else continue
        }
        return 0
    }

    private fun MutableMap<Int, PositionAndLength>.findPosition(index: Int, defMeasure: Float): PositionAndLength =
        ofLocal(index).let { effectiveIndex ->
            val defMeasure3 = defMeasure.round3()
            return this[effectiveIndex] ?: run {
                val first = this[0] ?: PositionAndLength(0.000f, defMeasure3)
                return (1..effectiveIndex).fold(first) { agg, idx ->
                    this[idx] ?: PositionAndLength((agg.position + agg.length + DEFAULT_GAP).round3(), defMeasure3)
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

    fun getColumnIndexAtPosition(x: X, mode: IndexRoundMode): Int {
        val ptX = x.switchUnitOfMeasure(standardUnit.asUnitsOfMeasure())
        return columns.findIndex(ptX.value, defaultWidthInPt, mode)
    }

    fun getColumnPositionAtIndex(index: Int): X =
        X(columns.findPosition(index, defaultWidthInPt).position, standardUnit.asUnitsOfMeasure())

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

    fun getRowIndexAtPosition(y: Y, mode: IndexRoundMode): Int {
        val ptX = y.switchUnitOfMeasure(standardUnit.asUnitsOfMeasure())
        return rows.findIndex(ptX.value, defaultHeightInPt, mode)
    }

    fun getRowPositionAtIndex(index: Int): Y =
        Y(rows.findPosition(index, defaultHeightInPt).position, standardUnit.asUnitsOfMeasure())


    private fun <T : Measure<T>> MutableMap<Int, PositionAndLength>.setLengthAtIndex(
        index: Int, length: T, defaultMeasure: Float, setOnlyGreater: Boolean = false
    ) {
        return findPosition(index, defaultMeasure).let { posLen ->
            length.switchUnitOfMeasure(standardUnit.asUnitsOfMeasure()).let { measure ->
                val measure3 = measure.value.round3()
                val localIndex = ofLocal(index)
                val diff = measure3 - (this[localIndex]?.length ?: 0f)
                if (diff > 0 || !setOnlyGreater) {
                    this[localIndex] = PositionAndLength(posLen.position, measure3)
                    keys.forEach {
                        if (it > localIndex) this[it] = this[it]!!.move(diff)
                    }
                }
            }
        }
    }

    private fun MutableMap<Int, Boolean>.lock(index: Int) {
        this[index] = true
    }

    private fun MutableMap<Int, Boolean>.isLocked(index: Int): Boolean = this[index] ?: false

    private fun <T : Measure<T>> setLengthWithOptions(
        measures: MutableMap<Int, PositionAndLength>, locks: MutableMap<Int, Boolean>,
        index: Int, measure: T, defMeasure: Float, options: SizingOptions
    ) {
        if (!locks.isLocked(index)) {
            when (options) {
                SizingOptions.SET ->
                    measures.setLengthAtIndex(index, measure, defMeasure)

                SizingOptions.SET_IF_GREATER ->
                    measures.setLengthAtIndex(index, measure, defMeasure, true)

                SizingOptions.SET_LOCKED -> {
                    measures.setLengthAtIndex(index, measure, defMeasure); locks.lock(index)
                }
            }
        }
    }

    override fun setColumnWidth(column: Int, width: Width, options: SizingOptions) {
        setLengthWithOptions(columns, lockedColumns, column, width, defaultWidthInPt, options)
    }

    override fun setRowHeight(row: Int, height: Height, options: SizingOptions) {
        setLengthWithOptions(rows, lockedRows, row, height, defaultHeightInPt, options)
    }

    override fun resizeColumnsToFit(width: Width) {
        columns.resizeLengthsToFit(width, defaultWidthInPt, lockedColumns)
    }

    override fun resizeRowsToFit(height: Height) {
        rows.resizeLengthsToFit(height, defaultHeightInPt, lockedRows)
    }

    private fun <T : Measure<T>> MutableMap<Int, PositionAndLength>.resizeLengthsToFit(
        length: T, defaultLen: Float, locks: MutableMap<Int, Boolean>
    ) {
        val targetLength = length.switchUnitOfMeasure(standardUnit.asUnitsOfMeasure()).value
        val contentWidth = getTotalLength()
        if (targetLength > contentWidth) {
            val diff = targetLength - contentWidth
            val notLockedMeasures = filterKeys { index -> !locks.containsKey(index) }
            if (notLockedMeasures.isNotEmpty()) {
                val lengthToAdd = (diff / notLockedMeasures.size).round3()
                notLockedMeasures.keys.forEach {
                    val pos = findPosition(it, defaultLen)
                    val newLen = pos.length + lengthToAdd
                    setLengthAtIndex(it, newLen.asWidth(standardUnit.asUnitsOfMeasure()), defaultLen)
                }
            }
        }
    }

    private fun <T : Measure<T>> MutableMap<Int, PositionAndLength>.spannedMeasure(
        index: Int,
        span: Int,
        clazz: Class<T>,
        defMeasure: Float
    ): T =
        clazz.new(
            0.until(span).sumOf { (this[ofLocal(index + it)]?.length ?: defMeasure).toDouble() }.toFloat(),
            standardUnit.asUnitsOfMeasure()
        )

    override fun getMeasuredColumnWidth(column: Int, colSpan: Int, uom: UnitsOfMeasure): Width =
        columns.spannedMeasure(column, colSpan, Width::class.java, defaultWidthInPt).switchUnitOfMeasure(uom)

    override fun getMeasuredRowHeight(row: Int, rowSpan: Int, uom: UnitsOfMeasure): Height =
        rows.spannedMeasure(row, rowSpan, Height::class.java, defaultHeightInPt).switchUnitOfMeasure(uom)

    internal fun isRowLocked(row: Int): Boolean = lockedRows[row] ?: false

    internal fun isColumnLocked(column: Int): Boolean = lockedColumns[column] ?: false

    private fun getWidth(): Width =
        Width(columns.getTotalLength(), standardUnit.asUnitsOfMeasure())

    private fun getHeight(): Height =
        Height(rows.getTotalLength(), standardUnit.asUnitsOfMeasure())

    private fun MutableMap<Int, PositionAndLength>.getTotalLength(): Float =
        values.sumOf { it.length.toDouble() }.toFloat()


    fun getSize(): Size = Size(getWidth(), getHeight())

    override fun startAt(row: Int, column: Int) {
        rowOffsetIndex = row
        columnOffsetIndex = column
    }

}

class TableLayout(properties: LayoutProperties) : AbstractTableLayout(properties) {

    private val delegate = NonUniformCartesianGrid()

    override fun getPosition(relativePosition: Position, targetUnit: UnitsOfMeasure): Position {
        return Position(
            getX(relativePosition.x, targetUnit),
            getY(relativePosition.y, targetUnit)
        )
    }

    override fun getX(relativeX: X, targetUnit: UnitsOfMeasure): X {
        assert(relativeX.unit == UnitsOfMeasure.NU) { "input coordinate must be expressed in ordinal numeric units" }
        //TODO getX method should be allowed ONLY to return relative position. SUMMING with absolutePosition should be encapsulated in AbstractLayoutPolicy.
        val absoluteXPosition = getActiveRectangle().leftTop.x.switchUnitOfMeasure(targetUnit)
        val currentLayoutRelativeX = delegate.getX(relativeX, targetUnit)
        return absoluteXPosition + currentLayoutRelativeX
    }

    override fun getY(relativeY: Y, targetUnit: UnitsOfMeasure): Y {
        assert(relativeY.unit == UnitsOfMeasure.NU) { "input coordinate must be expressed in ordinal numeric units" }
        val absoluteYPosition = getActiveRectangle().leftTop.y.switchUnitOfMeasure(targetUnit)
        val currentLayoutRelativeY = delegate.getY(relativeY, targetUnit)
        return absoluteYPosition + currentLayoutRelativeY
    }

    override fun getCurrentContentSize(): Size = delegate.getSize()

    fun getAbsoluteColumnPosition(columnIndex: Int): X =
        getX(columnIndex.asX(), uom)

    fun getAbsoluteRowPosition(rowIndex: Int): Y =
        getY(rowIndex.asY(), uom)

    override fun setColumnWidth(column: Int, width: Width, options: SizingOptions) {
        whileMeasuring {
            delegate.setColumnWidth(column, width, options)
        }
    }

    override fun setRowHeight(row: Int, height: Height, options: SizingOptions) {
        whileMeasuring {
            delegate.setRowHeight(row, height, options)
        }
    }

    override fun resizeColumnsToFit(width: Width) {
        whileMeasuring {
            delegate.resizeColumnsToFit(width)
        }
    }

    override fun resizeRowsToFit(height: Height) {
        whileMeasuring {
            delegate.resizeRowsToFit(height)
        }
    }

    override fun getMeasuredColumnWidth(column: Int, colSpan: Int, uom: UnitsOfMeasure): Width? =
        if (isMeasured || delegate.isColumnLocked(column)) {
            delegate.getMeasuredColumnWidth(column, colSpan, uom)
        } else null

    override fun getMeasuredRowHeight(row: Int, rowSpan: Int, uom: UnitsOfMeasure): Height? =
        if (isMeasured || delegate.isRowLocked(row)) {
            delegate.getMeasuredRowHeight(row, rowSpan, uom)
        } else null

    fun getCurrentRowHeight(row: Int, rowSpan: Int, uom: UnitsOfMeasure): Height =
        delegate.getMeasuredRowHeight(row, rowSpan, uom)

    override fun startAt(row: Int, column: Int) {
        delegate.startAt(row, column)
    }

}