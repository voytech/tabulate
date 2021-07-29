package io.github.voytech.tabulate.template.resolvers

import io.github.voytech.tabulate.model.*
import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.model.attributes.alias.RowAttribute
import io.github.voytech.tabulate.model.attributes.overrideAttributesLeftToRight
import io.github.voytech.tabulate.template.context.AttributedCellFactory.createAttributedCell
import io.github.voytech.tabulate.template.context.AttributedRow
import io.github.voytech.tabulate.template.context.AttributedRowFactory.createAttributedRow
import io.github.voytech.tabulate.template.context.RowIndex
import io.github.voytech.tabulate.template.context.getColumnIndex
import io.github.voytech.tabulate.template.context.getRowIndex

/**
 * Given requested index, [Table] model, and global custom attributes, it resolves [AttributedRow] context data with
 * effective index (effective index may differ from requested one if there is no rows matching predicate matching requested index)
 * @author Wojciech Mąka
 */
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
                    createAttributedCell(
                        rowIndex = tableModel.getRowIndex(tableRowIndex.rowIndex),
                        columnIndex = tableModel.getColumnIndex(column.index ?: index),
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
            createAttributedRow(
                rowIndex = tableModel.getRowIndex(tableRowIndex.rowIndex),
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

    /**
     * Resolves indexed [AttributedRow]. Index may be equal to parameter index value, or if there are no matching predicates,
     * it may be next matching index or eventually null when no row can be resolved.
     * @param requestedIndex [RowIndex] - index requested by row iterator.
     * @author Wojciech Mąka
     */
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

    /**
     * Provides next record from data source. Resolved [IndexedValue] may wrap a value or null if there is no more
     * records left.
     * @author Wojciech Mąka
     */
    protected abstract fun getNextRecord(): IndexedValue<T>?
}
