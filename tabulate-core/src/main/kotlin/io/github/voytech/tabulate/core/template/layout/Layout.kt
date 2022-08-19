package io.github.voytech.tabulate.core.template.layout

import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.model.attributes.MarginsAttribute
import io.github.voytech.tabulate.core.template.*
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
    fun getColumnWidth(column: Int, uom: UnitsOfMeasure = UnitsOfMeasure.PT): Width
    fun getRowHeight(row: Int, uom: UnitsOfMeasure = UnitsOfMeasure.PT): Height
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
}

abstract class AbstractLayoutQueries : LayoutQueries {

    internal lateinit var layout: Layout<*, *, *>

    override fun getLayoutBoundary(): BoundingRectangle = layout.boundingRectangle

}

abstract class TabularLayoutQueries(protected open val rowIndex: Int = 0, protected open val columnIndex: Int = 0) :
    AbstractLayoutQueries(), TabularQueries

fun <M: Model<M, C>,E : ExportTemplate<E,M,C>, C : TemplateContext<C,M>> RootNode<M,E,C>.setLayout(
    queries: AbstractLayoutQueries = DefaultLayoutQueries(),
    uom: UnitsOfMeasure = UnitsOfMeasure.PT,
    maxHeight: Height = Height.max(uom),
    maxWidth: Width = Width.max(uom),
    orientation: Orientation = Orientation.HORIZONTAL,
    leftTopCorner: Position = Position(X(0.0f, uom), Y(0.0f, uom)),
): RootLayout<M,E,C> =
    RootLayout(
        uom = uom,
        orientation = orientation,
        leftTopCorner = resolveMargins(leftTopCorner),
        query = queries,
        node = this,
        maxWidth,
        maxHeight
    ).also { layout = it }

fun <M: Model<M, C>,E : ExportTemplate<E,M,C>, C : TemplateContext<C,M>> BranchNode<M,E,C>.setLayout(
    queries: AbstractLayoutQueries,
    childLeftTopCorner: Position? = null,
    orientation: Orientation? = null,
): InnerLayout<M,E,C> = getWrappingLayout().let { wrapping ->
    InnerLayout(
        wrapping.uom,
        orientation ?: Orientation.HORIZONTAL,
        resolveMargins(childLeftTopCorner ?: wrapping.nextLayoutDefaultLeftTopCorner ?: wrapping.leftTopCorner),
        queries, this
    ).also { layout = it }
}

fun <M: Model<M, C>,E : ExportTemplate<E,M,C>, C : TemplateContext<C,M>> BranchNode<M,E,C>.createLayoutScope(
    queries: AbstractLayoutQueries,
    childLeftTopCorner: Position? = null,
    orientation: Orientation? = null,
    block: InnerLayout<M,E,C>.() -> Unit,
) = setLayout(queries, childLeftTopCorner, orientation).apply(block).finish()

private fun TreeNode<*,*,*>.resolveMargins(sourcePosition: Position): Position {
    val model = (context.model as? AttributedModelOrPart<*>)
    return model?.attributes?.get(MarginsAttribute::class.java)?.let {
        Position(it.left + sourcePosition.x, it.top + sourcePosition.y)
    } ?: sourcePosition
}

sealed class Layout<M: Model<M, C>,E : ExportTemplate<E,M,C>, C : TemplateContext<C,M>>(
    open val node: TreeNode<M,E,C>,
    val uom: UnitsOfMeasure,
    protected val orientation: Orientation,
    val leftTopCorner: Position,
    val query: AbstractLayoutQueries
) {

    internal var rightBottomCorner: Position = leftTopCorner

    internal var nextLayoutDefaultLeftTopCorner: Position? = null

    var boundingRectangle: BoundingRectangle = BoundingRectangle(leftTopCorner)

    private fun root(): RootLayout<*, *, *> = when (node) {
        is RootNode -> node.layout as RootLayout<*, *, *>
        is BranchNode -> (node as BranchNode<M, E, C>).root.layout as RootLayout<*, *, *>
    }

    private fun getMaxWidth(): Width = when (this) {
        is RootLayout -> maxWidth.orZero()
        is InnerLayout -> root().maxWidth.orZero()
    }

    private fun getMaxHeight(): Height = when (this) {
        is RootLayout -> maxHeight.orZero()
        is InnerLayout -> root().maxHeight.orZero()
    }

    private fun LayoutElementBoundingBox.isXOverflow(): Boolean =
        ((absoluteX?.value ?: 0F) + (width.orZero().value)) > getMaxWidth().value

    private fun LayoutElementBoundingBox.isYOverflow(): Boolean =
        ((absoluteY?.value ?: 0F) + (height.orZero().value)) > getMaxHeight().value

    fun LayoutElementBoundingBox?.checkRootOverflow(): Overflow? = if (this == null) null else {
        if (isXOverflow()) {
            Overflow.X
        } else if (isYOverflow()) {
            Overflow.Y
        } else null
    }

    protected abstract fun extendParent(position: Position)

    internal fun extend(position: Position): Unit = with(node.template) {
        extendParent(position)
        rightBottomCorner = orMax(rightBottomCorner, position)
        boundingRectangle = boundingRectangle.copy(rightBottom = rightBottomCorner)
    }

    internal fun extend(width: Width) = extend(Position(rightBottomCorner.x + width, rightBottomCorner.y))

    private fun extend(x: X) = extend(Position(x, rightBottomCorner.y))

    internal fun extend(height: Height) = extend(Position(rightBottomCorner.x, rightBottomCorner.y + height))

    private fun extend(y: Y) = extend(Position(rightBottomCorner.x, y))

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

class RootLayout<M: Model<M, C>,E : ExportTemplate<E, M,C>, C : TemplateContext<C,M>>(
    uom: UnitsOfMeasure,
    orientation: Orientation,
    leftTopCorner: Position,
    query: AbstractLayoutQueries = DefaultLayoutQueries(),
    node: TreeNode<M,E,C>,
    internal val maxWidth: Width? = null,
    internal val maxHeight: Height? = null,
) : Layout<M,E,C>(node, uom, orientation, leftTopCorner, query) {

    init {
        query.layout = this
    }

    override fun extendParent(position: Position) {}
}

class InnerLayout<M: Model<M, C>,E : ExportTemplate<E, M,C>, C : TemplateContext<C,M>>(
    uom: UnitsOfMeasure,
    orientation: Orientation,
    leftTopCorner: Position,
    query: AbstractLayoutQueries = DefaultLayoutQueries(),
    override val node: BranchNode<M,E,C>,
) : Layout<M,E,C>(node, uom, orientation, leftTopCorner, query) {

    init {
        query.layout = this
    }

    override fun extendParent(position: Position) {
        node.getWrappingLayout().extend(position)
    }

    internal fun finish() {
        val wrappingLayout = node.getWrappingLayout()
        wrappingLayout.nextLayoutDefaultLeftTopCorner = if (orientation == Orientation.HORIZONTAL) {
            Position(wrappingLayout.rightBottomCorner.x + EPSILON, wrappingLayout.leftTopCorner.y)
        } else {
            Position(wrappingLayout.leftTopCorner.x, wrappingLayout.rightBottomCorner.y + EPSILON)
        }
    }

}


fun interface LayoutElement {
    fun Layout<*, *, *>.computeBoundaries(): LayoutElementBoundingBox
}

fun interface LayoutElementApply {
    fun Layout<*, *, *>.applyBoundaries(context: LayoutElementBoundingBox)
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
        context.setContextAttribute("bbox", context.boundingBox()?.merge(this) ?: this)
    }

    private fun merge(other: LayoutElementBoundingBox): LayoutElementBoundingBox = copy(
        absoluteX = other.absoluteX?.switchUnitOfMeasure(unitsOfMeasure()) ?: absoluteX,
        absoluteY = other.absoluteY?.switchUnitOfMeasure(unitsOfMeasure()) ?: absoluteY,
        width = other.width?.switchUnitOfMeasure(unitsOfMeasure()) ?: width,
        height = other.height?.switchUnitOfMeasure(unitsOfMeasure()) ?: height
    )
}

enum class Overflow {
    X,
    Y
}

fun <E : AttributedContext> E.boundingBox(): LayoutElementBoundingBox? = getContextAttribute("bbox")

fun <E : AttributedContext> E.dropBoundingBox() = removeContextAttribute<LayoutElementBoundingBox>("bbox")

fun AbstractLayoutQueries.elementBoundaries(
    x: X? = null,
    y: Y? = null,
    width: Width? = null,
    height: Height? = null,
): LayoutElementBoundingBox {
    val uom = getLayoutBoundary().leftTop.x.unit
    return LayoutElementBoundingBox(
        layoutPosition = getLayoutBoundary().leftTop,
        absoluteX = x?.switchUnitOfMeasure(uom),
        absoluteY = y?.switchUnitOfMeasure(uom),
        width = width?.switchUnitOfMeasure(uom),
        height = height?.switchUnitOfMeasure(uom)
    )
}

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

}

class SpreadsheetQueries(
    private val rowIndex: Int = 0,
    private val columnIndex: Int = 0,
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

    override fun getColumnWidth(column: Int, uom: UnitsOfMeasure): Width =
        Width(columns[column]?.length ?: defaultWidthInPt, standardUnit.asUnitsOfMeasure()).switchUnitOfMeasure(uom)

    override fun getRowHeight(row: Int, uom: UnitsOfMeasure): Height =
        Height(rows[row]?.length ?: defaultHeightInPt, standardUnit.asUnitsOfMeasure()).switchUnitOfMeasure(uom)

}

class TableLayoutQueries(
    override val rowIndex: Int = 0, override val columnIndex: Int = 0,
) : TabularLayoutQueries(rowIndex, columnIndex) {

    private val delegate = SpreadsheetQueries(rowIndex, columnIndex)

    override fun getPosition(relativePosition: Position, targetUnit: UnitsOfMeasure): Position {
        return Position(
            getX(relativePosition.x, targetUnit),
            getY(relativePosition.y, targetUnit)
        )
    }

    override fun getX(relativeX: X, targetUnit: UnitsOfMeasure): X {
        assert(relativeX.unit == UnitsOfMeasure.NU) { "input coordinate must be expressed in ordinal numeric units" }
        val absoluteXPosition = getLayoutBoundary().leftTop.x.switchUnitOfMeasure(targetUnit)
        val currentLayoutRelativeX = delegate.getX(relativeX, targetUnit)
        return absoluteXPosition + currentLayoutRelativeX
    }

    override fun getY(relativeY: Y, targetUnit: UnitsOfMeasure): Y {
        assert(relativeY.unit == UnitsOfMeasure.NU) { "input coordinate must be expressed in ordinal numeric units" }
        val absoluteYPosition = getLayoutBoundary().leftTop.y.switchUnitOfMeasure(targetUnit)
        val currentLayoutRelativeY = delegate.getY(relativeY, targetUnit)
        return absoluteYPosition + currentLayoutRelativeY
    }

    override fun extend(width: Width) {
        layout.extend(width)
    }

    override fun extend(height: Height) {
        layout.extend(height)
    }

    override fun setColumnWidth(column: Int, width: Width) {
        delegate.setColumnWidth(column, width)
    }

    override fun setRowHeight(row: Int, height: Height) {
        delegate.setRowHeight(row, height)
    }

    override fun getColumnWidth(column: Int, uom: UnitsOfMeasure): Width = delegate.getColumnWidth(column, uom)

    override fun getRowHeight(row: Int, uom: UnitsOfMeasure): Height = delegate.getRowHeight(row, uom)

}