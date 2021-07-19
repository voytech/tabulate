package io.github.voytech.tabulate.template.resolvers

import io.github.voytech.tabulate.model.*
import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.model.attributes.alias.RowAttribute
import io.github.voytech.tabulate.model.attributes.overrideAttributesLeftToRight
import io.github.voytech.tabulate.template.context.AttributedRow
import io.github.voytech.tabulate.template.context.RowIndex
import io.github.voytech.tabulate.template.context.createAttributedCell
import io.github.voytech.tabulate.template.context.createAttributedRow

abstract class AbstractRowContextResolver<T>(
    private val tableModel: Table<T>,
    private val customAttributes: MutableMap<String, Any>
) :
    IndexedContextResolver<T, AttributedRow<T>> {


    private fun computeCells(rowDefinitions: Set<RowDef<T>>): Map<ColumnKey<T>, CellDef<T>> {
        return rowDefinitions.mapNotNull { row -> row.cells }.fold(mapOf()) { acc, m -> acc + m }
    }

    private fun computeRowLevelCellAttributes(rowDefinitions: Set<RowDef<T>>): Set<CellAttribute> {
        return overrideAttributesLeftToRight(*(rowDefinitions.mapNotNull { i -> i.cellAttributes }.toTypedArray()))
    }

    private fun computeRowAttributes(rowDefinitions: Set<RowDef<T>>): Set<RowAttribute> {
        return rowDefinitions.mapNotNull { attribs -> attribs.rowAttributes }
            .fold(setOf()) { acc, r -> acc + r }
    }

    private fun resolveAttributedRow(tableRowIndex: RowIndex, record: IndexedValue<T>? = null): AttributedRow<T> {
        return SourceRow(
            rowIndex = tableRowIndex,
            objectIndex = record?.index,
            record = record?.value
        ).let { sourceRow ->
            val rowDefinitions = tableModel.getRows(sourceRow)
            val cellDefinitions = computeCells(rowDefinitions)
            val rowCellAttributes = computeRowLevelCellAttributes(rowDefinitions)
            val cellValues = tableModel.columns.mapIndexed { index: Int, column: ColumnDef<T> ->
                cellDefinitions.resolveCellValue(column, sourceRow)?.let { value ->
                    tableModel.createAttributedCell(
                        rowIndex = tableRowIndex.rowIndex,
                        columnIndex = column.index ?: index,
                        value = value,
                        attributes = overrideAttributesLeftToRight(
                            tableModel.cellAttributes,
                            column.cellAttributes,
                            rowCellAttributes,
                            cellDefinitions[column.id]?.cellAttributes
                        ),
                        customAttributes
                    ).let { Pair(column.id, it) }
                }
            }.mapNotNull { it }.toMap()
            tableModel.createAttributedRow(
                rowIndex = tableRowIndex.rowIndex,
                rowAttributes = computeRowAttributes(rowDefinitions),
                cells = cellValues,
                customAttributes = customAttributes
            )
        }
    }

    private fun resolveRowContext(
        tableRowIndex: RowIndex,
        indexedRecord: IndexedValue<T>? = null,
    ): IndexedValue<AttributedRow<T>> {
        return IndexedValue(tableRowIndex.rowIndex, resolveAttributedRow(tableRowIndex, indexedRecord))
    }

    override fun resolve(requestedIndex: RowIndex): IndexedValue<AttributedRow<T>>? {
        return if (tableModel.hasCustomRows(SourceRow(requestedIndex))) {
            resolveRowContext(requestedIndex)
        } else {
            getNextRecord().let {
                if (it != null) {
                    resolveRowContext(requestedIndex, it)
                } else {
                    tableModel.getNextCustomRowIndex(requestedIndex)
                        ?.let { nextIndexDef ->
                            resolveRowContext(requestedIndex + nextIndexDef)
                        }
                }
            }
        }
    }

    protected abstract fun getNextRecord(): IndexedValue<T>?
}
