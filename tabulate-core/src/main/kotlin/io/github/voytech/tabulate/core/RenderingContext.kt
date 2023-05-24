package io.github.voytech.tabulate.core

import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.layout.policy.SpreadsheetPolicy
import io.github.voytech.tabulate.core.operation.AttributedContext
import io.github.voytech.tabulate.core.operation.Renderable
import io.github.voytech.tabulate.core.operation.boundingBox

/**
 * RenderingContext is a marker interface representing entire group of classes implementing low-level rendering logic.
 * It is related to specific rendering backend implementation. Implementations of that interface should represent shared state
 * to be accessed and mutated by [AbstractModel] and its managed rendering/measuring operations.
 * @since 0.1.0
 * @author Wojciech Mąka
 */
interface RenderingContext

interface HavingViewportSize {
    fun getWidth(): Width
    fun getHeight(): Height
}

/**
 * [RenderingContextForSpreadsheet] is a base class for all rendering contexts communicating with 3rd party spreadsheet apis. It adds
 * capabilities of tracking and accessing spreadsheet cell coordinates related to root spreadsheet-like document. Also tracks
 * and provides column widths and row heights for nested layouts offsets calculations.
 * @since 0.*.0
 * @author Wojciech Mąka
 */
abstract class RenderingContextForSpreadsheet : RenderingContext {

    private lateinit var measures: SpreadsheetPolicy
    //TODO add caching of left top column row indices by layout.

    fun setupSpreadsheetLayout(defaultColumnWidth: Float, defaultRowHeight: Float) {
        measures = SpreadsheetPolicy(defaultColumnWidth, defaultRowHeight)
    }

    fun AttributedContext.getAbsoluteColumn(column: Int): Int = boundingBox()?.layoutPosition?.x?.let {
        measures.getX(it, UnitsOfMeasure.NU).asColumn() + column
    } ?: 0

    fun AttributedContext.getAbsoluteRow(row: Int): Int = boundingBox()?.layoutPosition?.y?.let {
        measures.getY(it, UnitsOfMeasure.NU).asRow() + row
    } ?: 0

    fun AttributedContext.setColumnWidth(column: Int, width: Width) {
        setAbsoluteColumnWidth(getAbsoluteColumn(column), width)
    }

    fun AttributedContext.setRowHeight(row: Int, height: Height) {
        setAbsoluteRowHeight(getAbsoluteRow(row), height)
    }

    fun AttributedContext.setAbsoluteColumnWidth(column: Int, width: Width) {
        val converted = width.switchUnitOfMeasure(measures.standardUnit.asUnitsOfMeasure())
        measures.setColumnWidth(column, converted)
        boundingBox()?.let {
            it.width = width.switchUnitOfMeasure(it.unitsOfMeasure())
        }
    }

    fun AttributedContext.setAbsoluteRowHeight(row: Int, height: Height) {
        val converted = height.switchUnitOfMeasure(measures.standardUnit.asUnitsOfMeasure())
        measures.setRowHeight(row, converted)
        boundingBox()?.let {
            it.height = height.switchUnitOfMeasure(it.unitsOfMeasure())
        }
    }

    fun Renderable<*>.getAbsoluteLeftTopColumn(): Int = boundingBox.absoluteX.let {
        measures.getX(it, UnitsOfMeasure.NU).asColumn()
    }

    fun Renderable<*>.getAbsoluteLeftTopRow(): Int = boundingBox.absoluteY.let {
        measures.getY(it, UnitsOfMeasure.NU).asRow()
    }

    fun Renderable<*>.getAbsoluteRightBottomColumn(): Int = boundingBox.let {
        it.absoluteX.let { x -> measures.getX(x + (it.width?.value ?: 0F), UnitsOfMeasure.NU).asColumn() }
    }

    fun Renderable<*>.getAbsoluteRightBottomRow(): Int = boundingBox.let {
        it.absoluteY.let { y -> measures.getY(y + (it.height?.value ?: 0F), UnitsOfMeasure.NU).asRow() }
    }

    fun Renderable<*>.createSpreadsheetAnchor(): SpreadSheetAnchor = SpreadSheetAnchor(
        leftTopColumn = getAbsoluteLeftTopColumn(),
        leftTopRow = getAbsoluteLeftTopRow(),
        rightBottomColumn = getAbsoluteRightBottomColumn(),
        rightBottomRow = getAbsoluteRightBottomRow()
    )

}

data class SpreadSheetAnchor(
    val leftTopRow: Int,
    val leftTopColumn: Int,
    val rightBottomRow: Int,
    val rightBottomColumn: Int,
)