package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.api.builder.AttributeSetTransformer
import io.github.voytech.tabulate.api.builder.AttributeTransformerContainer
import io.github.voytech.tabulate.model.attributes.CellAttribute
import io.github.voytech.tabulate.model.attributes.ColumnAttribute
import io.github.voytech.tabulate.model.attributes.RowAttribute
import io.github.voytech.tabulate.model.attributes.TableAttribute

@Suppress("UNCHECKED_CAST")
internal class AttributeDelegatingExportOperations<T>(
    private val attributeOperationsContainer: AttributesOperationsContainer<T>,
    private val exposedExportOperations: ExposedContextExportOperations<T>,
    private val enableAttributeSetCaching: Boolean = true
) : TableExportOperations<T>  {

    inner class SortedTableAttributeSetTransformer: AttributeSetTransformer<TableAttribute<*>> {
        override fun transform(input: Set<TableAttribute<*>>): Set<TableAttribute<*>> =
            input.toSortedSet(compareBy {
                attributeOperationsContainer.getTableAttributeOperation(it.javaClass)?.priority() ?: 0
            })
    }

    inner class SortedColumnAttributeSetTransformer: AttributeSetTransformer<ColumnAttribute<*>> {
        override fun transform(input: Set<ColumnAttribute<*>>): Set<ColumnAttribute<*>> =
            input.toSortedSet(compareBy {
                attributeOperationsContainer.getColumnAttributeOperation(it.javaClass)?.priority() ?: 0
            })
    }

    inner class SortedRowAttributeSetTransformer: AttributeSetTransformer<RowAttribute<*>> {
        override fun transform(input: Set<RowAttribute<*>>): Set<RowAttribute<*>> =
            input.toSortedSet(compareBy {
                attributeOperationsContainer.getRowAttributeOperation(it.javaClass)?.priority() ?: 0
            })
    }
    inner class SortedCellAttributeSetTransformer: AttributeSetTransformer<CellAttribute<*>> {
        override fun transform(input: Set<CellAttribute<*>>): Set<CellAttribute<*>> =
            input.sortedBy {
                attributeOperationsContainer.getCellAttributeOperation(it.javaClass)?.priority() ?: 0
            }.toSet()
    }

    internal fun createAttributeTransformerContainer() : AttributeTransformerContainer {
        return AttributeTransformerContainer().also {
            it.set(TableAttribute::class.java, listOf(SortedTableAttributeSetTransformer()))
            it.set(ColumnAttribute::class.java, listOf(SortedColumnAttributeSetTransformer()))
            it.set(RowAttribute::class.java, listOf(SortedRowAttributeSetTransformer()))
            it.set(CellAttribute::class.java, listOf(SortedCellAttributeSetTransformer()))
        }
    }

    override fun createTable(context: AttributedTable) {
        with(context.crop()) {
            return exposedExportOperations.createTable(this).also {
                context.attributes?.forEach { tableAttribute ->
                    attributeOperationsContainer.getTableAttributeOperation(tableAttribute.javaClass)?.renderAttribute(this, tableAttribute)
                }
            }
        }
    }

    override fun beginRow(context: AttributedRow<T>) {
        with(context.crop()) {
            var operationRendered = false
            if (!context.attributes.isNullOrEmpty()) {
                context.attributes?.forEach { attribute ->
                    attributeOperationsContainer.getRowAttributeOperation(attribute.javaClass)?.let { operation ->
                        if (operation.priority() >= 0 && !operationRendered) {
                            exposedExportOperations.beginRow(this)
                            operationRendered = true
                        }
                        operation.renderAttribute(this, attribute)
                    }
                }
            }
            if (!operationRendered) {
                exposedExportOperations.beginRow(this)
            }
        }
    }

    override fun renderColumn(context: AttributedColumn) {
        with(context.crop()) {
            context.attributes?.let { attributes ->
                attributes.forEach { attribute ->
                    attributeOperationsContainer.getColumnAttributeOperation(attribute.javaClass)
                        ?.renderAttribute(this, attribute)
                }
            }
        }
    }

    override fun renderRowCell(context: AttributedCell) {
        if (enableAttributeSetCaching) context.ensureAttributesCacheEntry()
        with(context.crop()) {
            var operationRendered = false
            if (!context.attributes.isNullOrEmpty()) {
                context.attributes.forEach { attribute ->
                    attributeOperationsContainer.getCellAttributeOperation(attribute.javaClass)?.let { operation ->
                        if (operation.priority() >= 0 && !operationRendered) {
                            exposedExportOperations.renderRowCell(this)
                            operationRendered = true
                        }
                        operation.renderAttribute(this, attribute)
                    }
                }
            }
            if (!operationRendered){
                exposedExportOperations.renderRowCell(this)
            }
        }
    }

    override fun endRow(context: AttributedRowWithCells<T>) {
        exposedExportOperations.endRow(context.crop())
    }

}

