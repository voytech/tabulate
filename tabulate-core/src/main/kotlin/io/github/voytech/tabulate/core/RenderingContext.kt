package io.github.voytech.tabulate.core

import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.layout.impl.NonUniformCartesianGrid
import io.github.voytech.tabulate.core.layout.impl.SizingOptions
import io.github.voytech.tabulate.core.operation.AttributedEntity
import io.github.voytech.tabulate.core.operation.RenderableEntity
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

    private lateinit var grid: NonUniformCartesianGrid
    //TODO add caching of left top column row indices by layout.

    fun createSpreadsheetGrid(defaultColumnWidth: Float, defaultRowHeight: Float) {
        grid = NonUniformCartesianGrid(defaultColumnWidth, defaultRowHeight)
    }

    fun AttributedEntity.getAbsoluteColumn(): Int = boundingBox()?.let {
        grid.getColumnIndexAtPosition(it.absoluteX, NonUniformCartesianGrid.IndexRoundMode.HALF_UP)
    } ?: 0

    fun AttributedEntity.getAbsoluteRow(): Int = boundingBox()?.let {
        grid.getRowIndexAtPosition(it.absoluteY, NonUniformCartesianGrid.IndexRoundMode.HALF_UP)
    } ?: 0

    fun AttributedEntity.trySetAndSyncAbsoluteColumnWidth(column: Int, width: Width, colSpan: Int = 1): Width? = boundingBox()?.let {
        it.width = this@RenderingContextForSpreadsheet.trySetAbsoluteColumnWidth(column, width, colSpan).switchUnitOfMeasure(it.unitsOfMeasure())
        it.width
    }

    fun AttributedEntity.syncAbsoluteColumnWidth(column: Int) = boundingBox()?.let {
        it.width = grid.getMeasuredColumnWidth(column)
    }

    private fun trySetAbsoluteColumnWidth(column: Int, width: Width, colSpan: Int): Width {
        val converted = width.switchUnitOfMeasure(grid.standardUnit.asUnitsOfMeasure())
        grid.setColumnWidth(column, converted, SizingOptions.SET_IF_GREATER)
        return grid.getMeasuredColumnWidth(column)
    }

    fun AttributedEntity.trySetAndSyncAbsoluteRowHeight(row: Int, height: Height, rowSpan: Int = 1): Height? = boundingBox()?.let {
        it.height = trySetAbsoluteRowHeight(row, height, rowSpan).switchUnitOfMeasure(it.unitsOfMeasure())
        it.height
    }

    fun AttributedEntity.syncAbsoluteRowHeight(row: Int) = boundingBox()?.let {
        it.height = grid.getMeasuredRowHeight(row)
    }

    private fun trySetAbsoluteRowHeight(row: Int, height: Height, rowSpan: Int): Height {
        val converted = height.switchUnitOfMeasure(grid.standardUnit.asUnitsOfMeasure())
        grid.setRowHeight(row, converted, SizingOptions.SET_IF_GREATER)
        return grid.getMeasuredRowHeight(row)
    }

    private fun RenderableEntity<*>.getAbsoluteLeftTopColumn(): Int = boundingBox.absoluteX.let {
        grid.getColumnIndexAtPosition(it, NonUniformCartesianGrid.IndexRoundMode.FLOOR)
    }

    private fun RenderableEntity<*>.getAbsoluteLeftTopRow(): Int = boundingBox.absoluteY.let {
        grid.getRowIndexAtPosition(it, NonUniformCartesianGrid.IndexRoundMode.FLOOR)
    }

    private fun RenderableEntity<*>.getAbsoluteRightBottomColumn(): Int = boundingBox.let {
        it.absoluteX.let<X, Int> { x ->
            val end = x + (it.width?.value ?: 0F)
            val maxEnd = it.cropBoxRightBottom.x
            val mode = NonUniformCartesianGrid.IndexRoundMode.FLOOR.takeIf { wasWidthDeclared() }
                ?: NonUniformCartesianGrid.IndexRoundMode.CEILING
            val index = grid.getColumnIndexAtPosition(end, mode)
            if (mode == NonUniformCartesianGrid.IndexRoundMode.CEILING) {
                val pos = grid.getColumnPositionAtIndex(index)
                if (pos > maxEnd) {
                    return index - 1
                }
            }
            return index
        }
    }

    private fun RenderableEntity<*>.getAbsoluteRightBottomRow(): Int = boundingBox.let {
        it.absoluteY.let<Y, Int> { y ->
            val end = y + (it.height?.value ?: 0F)
            val maxEnd = it.cropBoxRightBottom.y
            val mode = NonUniformCartesianGrid.IndexRoundMode.FLOOR.takeIf { wasHeightDeclared() }
                ?: NonUniformCartesianGrid.IndexRoundMode.CEILING
            val index = grid.getRowIndexAtPosition(end, mode)
            if (mode == NonUniformCartesianGrid.IndexRoundMode.CEILING) {
                val pos = grid.getRowPositionAtIndex(index)
                if (pos > maxEnd) {
                    return index - 1
                }
            }
            return index
        }
    }

    fun RenderableEntity<*>.checkSizeDeclarations() {
        setContextAttribute(WIDTH_DECLARED, boundingBox.width != null)
        setContextAttribute(HEIGHT_DECLARED, boundingBox.height != null)
    }

    private fun RenderableEntity<*>.wasHeightDeclared(remove: Boolean = false): Boolean =
        (if (remove) removeContextAttribute<Boolean>(HEIGHT_DECLARED)
        else getContextAttribute(HEIGHT_DECLARED)) ?: false

    private fun RenderableEntity<*>.wasWidthDeclared(remove: Boolean = false): Boolean =
        (if (remove) removeContextAttribute<Boolean>(WIDTH_DECLARED)
        else getContextAttribute(WIDTH_DECLARED)) ?: false

    fun RenderableEntity<*>.applySpreadsheetAnchor() = apply {
        removeContextAttribute<SpreadSheetAnchor>(ANCHOR)?.let { anchor ->
            if (!wasWidthDeclared(true)) {
                var width = 0F
                (anchor.leftTopColumn until anchor.rightBottomColumn).forEach {
                    width += grid.getMeasuredColumnWidth(it, 1).value
                }
                boundingBox.width = Width(width, UnitsOfMeasure.PT)
            }
            if (!wasHeightDeclared(true)) {
                var height = 0F
                (anchor.leftTopRow until anchor.rightBottomRow).forEach {
                    height += grid.getMeasuredRowHeight(it, 1).value
                }
                boundingBox.height = Height(height, UnitsOfMeasure.PT)
            }
        }
    }

    fun RenderableEntity<*>.createSpreadsheetAnchor(): SpreadSheetAnchor = SpreadSheetAnchor(
        leftTopColumn = getAbsoluteLeftTopColumn(),
        leftTopRow = getAbsoluteLeftTopRow(),
        rightBottomColumn = getAbsoluteRightBottomColumn(),
        rightBottomRow = getAbsoluteRightBottomRow()
    ).also { setContextAttribute(ANCHOR, it) }

    companion object {
        const val ANCHOR = "anchor"
        const val WIDTH_DECLARED = "width-declared"
        const val HEIGHT_DECLARED = "height-declared"
    }

}

data class SpreadSheetAnchor(
    val leftTopRow: Int,
    val leftTopColumn: Int,
    val rightBottomRow: Int,
    val rightBottomColumn: Int,
)