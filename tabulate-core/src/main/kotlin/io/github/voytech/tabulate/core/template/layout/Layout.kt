package io.github.voytech.tabulate.core.template.layout

import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.template.operation.AttributedContext

interface AbsolutePositionQueries {

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

interface TabularQueries : AbsolutePositionQueries {
    fun setColumnWidth(column: Int, width: Width)
    fun setRowHeight(row: Int, height: Height)
    fun getColumnWidth(column: Int,uom: UnitsOfMeasure = UnitsOfMeasure.PT): Width
    fun getRowHeight(row: Int,uom: UnitsOfMeasure = UnitsOfMeasure.PT): Height
}

interface LayoutQueries : AbsolutePositionQueries {
    fun getLayoutBoundary(): BoundingRectangle

    /**
     * Extend layout rendered space by specific [Width].
     */
    fun extend(width: Width)

    /**
     * Extend layout rendered space by specific [Height].
     */
    fun extend(height: Height)

    /**
     * Re-set layout rendered space by specific [Width].
     */
    fun setWidth(width: Width)

    /**
     * Re-set layout rendered space by specific [Height].
     */
    fun setHeight(height: Height)

}

abstract class AbstractLayoutQueries : LayoutQueries {
    protected lateinit var layout: Layout
    internal fun setLayout(layout: Layout) {
        this.layout = layout
    }

    override fun getLayoutBoundary(): BoundingRectangle = layout.boundingRectangle

}

abstract class TabularLayoutQueries : AbstractLayoutQueries(), TabularQueries

class Layout(
    val uom: UnitsOfMeasure,
    private val orientation: Orientation,
    private val leftTopCorner: Position,
    val query: AbstractLayoutQueries = DefaultLayoutQueries(),
    private val parent: Layout? = null,
    private val root: Layout? = null,
) {
    init {
        query.setLayout(this)
    }

    private var rightBottomCorner: Position = leftTopCorner

    private var nextLayoutDefaultLeftTopCorner: Position? = null

    private val renderedChildren: MutableList<Layout> = mutableListOf()

    internal var boundingRectangle: BoundingRectangle = BoundingRectangle(leftTopCorner)

    internal var activeLayout: Layout? = null

    fun newLayout(
        childQuery: AbstractLayoutQueries,
        childLeftTopCorner: Position = nextLayoutDefaultLeftTopCorner ?: leftTopCorner,
        orientation: Orientation = this@Layout.orientation,
    ): Layout = Layout(uom, orientation, childLeftTopCorner, childQuery, this, root ?: this).let {
        root?.activeLayout = it
        activeLayout = it
        it
    }


    internal fun extend(position: Position) {
        rightBottomCorner = max(rightBottomCorner, position)
        boundingRectangle = boundingRectangle.copy(rightBottom = rightBottomCorner)
    }

    internal fun setWidth(width: Width) {
        TODO("Not yet implemented")
    }

    internal fun setHeight(height: Height) {
        TODO("Not yet implemented")
    }

    internal fun extend(width: Width) = extend(Position(rightBottomCorner.x + width, rightBottomCorner.y))

    internal fun extend(x: X) = extend(Position(x, rightBottomCorner.y))

    internal fun extend(height: Height) = extend(Position(rightBottomCorner.x, rightBottomCorner.y + height))

    internal fun extend(y: Y) = extend(Position(rightBottomCorner.x, y))

    fun finish() {
        if (parent != null) {
            parent.activeLayout = null
            parent.boundingRectangle = parent.boundingRectangle + boundingRectangle
            parent.rightBottomCorner = parent.boundingRectangle.rightBottom
            parent.nextLayoutDefaultLeftTopCorner = if (orientation == Orientation.HORIZONTAL) {
                Position(parent.rightBottomCorner.x + EPSILON, parent.leftTopCorner.y)
            } else {
                Position(parent.leftTopCorner.x, parent.rightBottomCorner.y + EPSILON)
            }
            parent.renderedChildren.add(this)
            root?.activeLayout = parent
        }
    }

    //
    // context receivers
    //
    fun LayoutElementBoundingBox.applyOnLayout() {
        if (absoluteX != null) {
            extend(absoluteX + width.orZero())
        }
        if (absoluteY != null) {
            extend(absoluteY + height.orZero())
        }
    }

}

class Layouts(
    val uom: UnitsOfMeasure = UnitsOfMeasure.PT,
    orientation: Orientation = Orientation.HORIZONTAL,
    leftTopCorner: Position = Position(X(0.0f, uom), Y(0.0f, uom)),
    private var root: Layout = Layout(uom, orientation, leftTopCorner, DefaultLayoutQueries()),
) {

    fun activeLayout(): Layout = root.activeLayout ?: root

    fun <R> usingLayout(block: Layout.() -> R) = activeLayout().run(block)

}

fun interface LayoutElement {
    fun Layout.computeBoundaries(): LayoutElementBoundingBox
}

fun interface LayoutElementApply {
    fun Layout.applyBoundaries(context: LayoutElementBoundingBox)
}

data class LayoutElementBoundingBox(
    val layoutPosition: Position,
    val absoluteX: X? = null,
    val absoluteY: Y? = null,
    var width: Width? = null,
    var height: Height? = null,
) {
    fun unitsOfMeasure(): UnitsOfMeasure = layoutPosition.x.unit

    fun <E : AttributedContext> mergeInto(context: E): LayoutElementBoundingBox = apply {
        context.additionalAttributes?.put("${context.id}[boundaries]", context.boundingBox()?.merge(this) ?: this)
    }

    private fun merge(other: LayoutElementBoundingBox): LayoutElementBoundingBox = copy(
        absoluteX = other.absoluteX?.switchUnitOfMeasure(unitsOfMeasure()) ?: absoluteX,
        absoluteY = other.absoluteY?.switchUnitOfMeasure(unitsOfMeasure()) ?: absoluteY,
        width = other.width?.switchUnitOfMeasure(unitsOfMeasure()) ?: width,
        height = other.height?.switchUnitOfMeasure(unitsOfMeasure()) ?: height
    )
}

fun <E : AttributedContext> E.boundingBox(): LayoutElementBoundingBox? = getContextAttribute("$id[boundaries]")
fun <E : AttributedContext> E.dropBoundaries() = removeContextAttribute<LayoutElementBoundingBox>("$id[boundaries]")

fun AbstractLayoutQueries.elementBoundaries(x: X? = null, y: Y? = null, width: Width? = null, height: Height? = null) =
    LayoutElementBoundingBox(
        layoutPosition = getLayoutBoundary().leftTop,
        absoluteX = x?.switchUnitOfMeasure(getLayoutBoundary().leftTop.x.unit),
        absoluteY = y?.switchUnitOfMeasure(getLayoutBoundary().leftTop.x.unit),
        width = width?.switchUnitOfMeasure(getLayoutBoundary().leftTop.x.unit),
        height = height?.switchUnitOfMeasure(getLayoutBoundary().leftTop.x.unit)
    )

class DefaultLayoutQueries : AbstractLayoutQueries() {
    override fun getPosition(relativePosition: Position, targetUnit: UnitsOfMeasure): Position = Position(
        getX(relativePosition.x, targetUnit), getY(relativePosition.y, targetUnit)
    )

    override fun getX(relativeX: X, targetUnit: UnitsOfMeasure): X {
        val absoluteX = getLayoutBoundary().leftTop.x.switchUnitOfMeasure(targetUnit)
        return X(absoluteX.value + relativeX.value, targetUnit)
    }

    override fun getY(relativeY: Y, targetUnit: UnitsOfMeasure): Y {
        val absoluteY = getLayoutBoundary().leftTop.y.switchUnitOfMeasure(targetUnit)
        return Y(absoluteY.value + relativeY.value, targetUnit)
    }

    /**
     * Extend layout rendered space by specific [Width].
     */
    override fun extend(width: Width) {
        layout.extend(width)
    }

    /**
     * Extend layout rendered space by specific [Height].
     */
    override fun extend(height: Height) {
        layout.extend(height)
    }

    /**
     * Re-set layout rendered space by specific [Width].
     */
    override fun setWidth(width: Width) {
        TODO("Not yet implemented")
    }

    /**
     * Re-set layout rendered space by specific [Height].
     */
    override fun setHeight(height: Height) {
        TODO("Not yet implemented")
    }

}

class SpreadsheetQueries(
    private val defaultWidthInPt: Float = 56.25f,
    private val defaultHeightInPt: Float = 15f,
    val standardUnit: StandardUnits = StandardUnits.PT,
) : TabularQueries {

    private val rows: MutableMap<Int, PositionAndLength> = mutableMapOf()
    private val columns: MutableMap<Int, PositionAndLength> = mutableMapOf()

    data class PositionAndLength(val position: Float, val length: Float) {
        fun contains(value: Float): Boolean =
            position.compareTo(value) <= 0 && (position + length).compareTo(value) >= 0

        fun move(offset: Float) = PositionAndLength(position + offset, length)
    }

    override fun getPosition(relativePosition: Position, targetUnit: UnitsOfMeasure): Position = Position(
        getX(relativePosition.x, targetUnit), getY(relativePosition.y, targetUnit)
    )

    private fun MutableMap<Int, PositionAndLength>.findIndex(position: Float, defMeasure: Float): Int {
        var entry: PositionAndLength? = null
        return (0..Int.MAX_VALUE).find { index ->
            val pos = entry?.let { it.position + it.length + EPSILON } ?: 0.0f
            val resolved = this[index] ?: PositionAndLength(pos, defMeasure)
            entry = resolved
            resolved.contains(position)
        } ?: 0
    }

    private fun MutableMap<Int, PositionAndLength>.findPosition(index: Int, defMeasure: Float): PositionAndLength {
        return this[index] ?: run {
            val first = this[0] ?: PositionAndLength(0.0f, defMeasure)
            return (1..index).fold(first) { agg, _index ->
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
                    Y(it.position, standardUnit.asUnitsOfMeasure()).switchUnitOfMeasure(targetUnit)
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
                if (oldLength != measure) {
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

    override fun getColumnWidth(column: Int,uom: UnitsOfMeasure): Width =
        Width(columns[column]?.length ?: defaultWidthInPt  ,standardUnit.asUnitsOfMeasure()).switchUnitOfMeasure(uom)

    override fun getRowHeight(row: Int,uom: UnitsOfMeasure): Height =
        Height(rows[row]?.length ?: defaultHeightInPt ,standardUnit.asUnitsOfMeasure()).switchUnitOfMeasure(uom)

}

class TableLayoutQueries : TabularLayoutQueries() {

    private val delegate = SpreadsheetQueries()

    override fun getPosition(relativePosition: Position, targetUnit: UnitsOfMeasure): Position {
        return Position(
            getX(relativePosition.x, targetUnit),
            getY(relativePosition.y, targetUnit)
        )
    }

    override fun getX(relativeX: X, targetUnit: UnitsOfMeasure): X {
        assert(relativeX.unit == UnitsOfMeasure.NU) { "input coordinate must be expressed in tabular, numeric units" }
        val absoluteXPosition = layout.query.getLayoutBoundary().leftTop.x.switchUnitOfMeasure(targetUnit)
        val currentLayoutRelativeX = delegate.getX(relativeX, targetUnit)
        return absoluteXPosition + currentLayoutRelativeX
    }

    override fun getY(relativeY: Y, targetUnit: UnitsOfMeasure): Y {
        assert(relativeY.unit == UnitsOfMeasure.NU) { "input coordinate must be expressed in tabular, numeric units" }
        val absoluteYPosition = layout.query.getLayoutBoundary().leftTop.y.switchUnitOfMeasure(targetUnit)
        val currentLayoutRelativeY = delegate.getY(relativeY, targetUnit)
        return absoluteYPosition + currentLayoutRelativeY
    }

    override fun extend(width: Width) {
        layout.extend(width)
    }

    override fun extend(height: Height) {
        layout.extend(height)
    }

    override fun setWidth(width: Width) {
        TODO("Not yet implemented")
    }

    override fun setHeight(height: Height) {
        TODO("Not yet implemented")
    }

    override fun setColumnWidth(column: Int, width: Width) {
        delegate.setColumnWidth(column, width)
    }

    override fun setRowHeight(row: Int, height: Height) {
        delegate.setRowHeight(row, height)
    }

    override fun getColumnWidth(column: Int,uom: UnitsOfMeasure): Width = delegate.getColumnWidth(column,uom)

    override fun getRowHeight(row: Int,uom: UnitsOfMeasure): Height = delegate.getRowHeight(row,uom)

}