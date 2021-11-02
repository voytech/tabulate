package io.github.voytech.tabulate.template.resolvers

import io.github.voytech.tabulate.model.*
import io.github.voytech.tabulate.model.attributes.overrideAttributesLeftToRight
import io.github.voytech.tabulate.template.context.*
import io.github.voytech.tabulate.template.context.AttributedCellFactory.createAttributedCell
import io.github.voytech.tabulate.template.context.AttributedRowFactory.createAttributedRow

internal interface RowCompletionListener<T> {
    fun onAttributedCellResolved(cell: AttributedCell)
    fun onAttributedRowResolved(row: AttributedRow<T>)
    fun onAttributedRowResolved(row: AttributedRowWithCells<T>)
}

/**
 * Given requested index, [Table] model, and global custom attributes, it resolves [AttributedRowWithCells] context data with
 * effective index (effective index may differ from requested one if there are no rows matching predicate
 * - in that case - row context with next matching index is returned).
 * Additionally - while resolving - it notifies about:
 *  - computed row attributes on row,
 *  - each computed cell and its attributes,
 *  - entire row completion - that is completion of row with attributes and all row cells with its attributes.
 * @author Wojciech Mąka
 */
internal abstract class AbstractRowContextResolver<T>(
    private val tableModel: Table<T>,
    private val customAttributes: MutableMap<String, Any>,
    private val listener: RowCompletionListener<T>? = null,
) : IndexedContextResolver<T, AttributedRowWithCells<T>> {

    private fun resolveAttributedRow(tableRowIndex: RowIndex, record: IndexedValue<T>? = null): AttributedRowWithCells<T> {
        return SourceRow(
            rowIndex = tableRowIndex,
            objectIndex = record?.index,
            record = record?.value
        ).let { sourceRow ->
            val rowDefinitions = tableModel.getRows(sourceRow)
            val cellDefinitions = rowDefinitions.mergeCells()
            val rowCellAttributes = rowDefinitions.flattenCellAttributes()
            val attributedRow = createAttributedRow<T>(
                rowIndex = tableModel.getRowIndex(tableRowIndex.value),
                rowAttributes = overrideAttributesLeftToRight(
                    tableModel.rowAttributes, rowDefinitions.flattenRowAttributes()
                ),
                customAttributes = customAttributes
            ).also { listener?.onAttributedRowResolved(it) }

            val cellValues = tableModel.columns.mapIndexed { index: Int, column: ColumnDef<T> ->
                cellDefinitions.resolveCellValue(column, sourceRow)?.let { value ->
                    createAttributedCell(
                        rowIndex = tableModel.getRowIndex(tableRowIndex.value),
                        columnIndex = tableModel.getColumnIndex(column.index ?: index),
                        value = value,
                        attributes = overrideAttributesLeftToRight(
                            tableModel.cellAttributes,
                            column.cellAttributes,
                            rowCellAttributes,
                            cellDefinitions[column.id]?.cellAttributes
                        ),
                        customAttributes
                    ).also { listener?.onAttributedCellResolved(it) }
                        .let { column.id to it }
                }
            }.mapNotNull { it }.toMap()
            attributedRow.withCells(cellValues).also { listener?.onAttributedRowResolved(it) }
        }
    }

    private fun resolveRowContext(
        tableRowIndex: RowIndex,
        indexedRecord: IndexedValue<T>? = null,
    ): IndexedValue<AttributedRowWithCells<T>> {
        return IndexedValue(tableRowIndex.value, resolveAttributedRow(tableRowIndex, indexedRecord))
    }

    /**
     * Resolves indexed [AttributedRowWithCells]. Index may be equal to parameter index value, or if there are no matching predicates,
     * it may be next matching index or eventually null when no row can be resolved.
     * @param requestedIndex [RowIndex] - index requested by row iterator.
     * @author Wojciech Mąka
     */
    override fun resolve(requestedIndex: RowIndex): IndexedValue<AttributedRowWithCells<T>>? {
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
