package io.github.voytech.tabulate.core.template

import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.template.layout.SpreadsheetQueries
import io.github.voytech.tabulate.core.template.layout.boundingBox
import io.github.voytech.tabulate.core.template.operation.AttributedContext

/**
 * RenderingContext is a marker interface representing entire group of classes implementing low-level rendering logic.
 * It is related to specific rendering backend implementation. Implementations of that interface should represent shared state
 * to be accessed and mutated by [ExportTemplate] managed operations.
 * @since 0.1.0
 * @author Wojciech Mąka
 */
interface RenderingContext


interface RenderingContextAttributes {
    fun getWidth(): Width
    fun getHeight(): Height
}
/**
 * RenderingContextForSpreadsheet is a base class for all rendering contexts communicating with spreadsheets. It adds
 * capabilities of tracking and accessing spreadsheet cell coordinates related to document root.
 * @since 0.*.0
 * @author Wojciech Mąka
 */
abstract class RenderingContextForSpreadsheet: RenderingContext {

    private lateinit var measures: SpreadsheetQueries
    //TODO add caching of left top column row indices by layout.

    fun setupSpreadsheetLayout(defaultColumnWidth: Float, defaultRowHeight: Float) {
        measures = SpreadsheetQueries(0,0,defaultColumnWidth,defaultRowHeight)
    }

    fun AttributedContext.getAbsoluteColumn(column: Int): Int {
        return boundingBox()?.layoutPosition?.x?.let {
            measures.getX(it, UnitsOfMeasure.NU).asColumn() + column
        } ?: 0
    }

    fun AttributedContext.getAbsoluteRow(row: Int): Int {
        return boundingBox()?.layoutPosition?.y?.let {
            measures.getY(it, UnitsOfMeasure.NU).asRow() + row
        } ?: 0
    }

    fun AttributedContext.setColumnWidth(column: Int, width: Width) {
        setAbsoluteColumnWidth(getAbsoluteColumn(column),width)
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

}
