package io.github.voytech.tabulate.core.template

import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.template.layout.SpreadsheetQueries
import io.github.voytech.tabulate.core.template.layout.boundaries
import io.github.voytech.tabulate.core.template.operation.AttributedContext

/**
 * RenderingContext is a marker interface representing entire group of classes implementing low-level context rendering logic.
 * It is related to specific rendering backend implementation.
 * @since 0.1.0
 */
interface RenderingContext

abstract class RenderingContextForSpreadsheet: RenderingContext {

    private lateinit var measures: SpreadsheetQueries
    //TODO add caching of left top column row indices by layout.

    fun setupSpreadsheetLayout(defaultColumnWidth: Float, defaultRowHeight: Float) {
        measures = SpreadsheetQueries(defaultColumnWidth,defaultRowHeight)
    }

    fun AttributedContext<*>.getAbsoluteColumn(column: Int): Int {
        return boundaries()?.layoutPosition?.x?.let {
            measures.getX(it, UnitsOfMeasure.NU).asColumn() + column
        } ?: 0
    }

    fun AttributedContext<*>.getAbsoluteRow(row: Int): Int {
        return boundaries()?.layoutPosition?.y?.let {
            measures.getY(it, UnitsOfMeasure.NU).asRow() + row
        } ?: 0
    }

    fun AttributedContext<*>.setColumnWidth(column: Int, width: Width) {
        setAbsoluteColumnWidth(getAbsoluteColumn(column),width)
    }

    fun AttributedContext<*>.setRowHeight(row: Int, height: Height) {
        setAbsoluteRowHeight(getAbsoluteRow(row), height)
    }

    fun AttributedContext<*>.setAbsoluteColumnWidth(column: Int, width: Width) {
        val converted = width.switchUnitOfMeasure(measures.standardUnit.asUnitsOfMeasure())
        measures.setColumnWidth(column, converted)
        boundaries()?.let {
            it.width = width.switchUnitOfMeasure(it.unitsOfMeasure())
        }
    }

    fun AttributedContext<*>.setAbsoluteRowHeight(row: Int, height: Height) {
        val converted = height.switchUnitOfMeasure(measures.standardUnit.asUnitsOfMeasure())
        measures.setRowHeight(row, converted)
        boundaries()?.let {
            it.height = height.switchUnitOfMeasure(it.unitsOfMeasure())
        }
    }

}
