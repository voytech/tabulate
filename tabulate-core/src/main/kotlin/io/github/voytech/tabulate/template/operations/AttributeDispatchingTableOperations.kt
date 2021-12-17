package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.api.builder.AttributeSetTransformer
import io.github.voytech.tabulate.api.builder.AttributeTransformerContainer
import io.github.voytech.tabulate.model.attributes.*
import io.github.voytech.tabulate.template.context.RenderingContext

@Suppress("UNCHECKED_CAST")
internal class AttributeDispatchingTableOperations<T, CTX : RenderingContext>(
    private val attributeOperationsContainer: AttributesOperationsContainer<CTX, T>,
    private val exposedExportOperations: TableExportOperations<T, CTX>,
    private val enableAttributeSetBasedCaching: Boolean = true
) : AttributedContextExportOperations<T, CTX> {

    inner class SortedTableAttributeSetTransformer : AttributeSetTransformer<TableAttribute<*>> {
        override fun transform(input: Set<TableAttribute<*>>): Set<TableAttribute<*>> =
            input.toSortedSet(compareBy {
                attributeOperationsContainer.getTableAttributeOperation(it.javaClass)?.priority() ?: 0
            })
    }

    inner class SortedColumnAttributeSetTransformer : AttributeSetTransformer<ColumnAttribute<*>> {
        override fun transform(input: Set<ColumnAttribute<*>>): Set<ColumnAttribute<*>> =
            input.toSortedSet(compareBy {
                attributeOperationsContainer.getColumnAttributeOperation(it.javaClass)?.priority() ?: 0
            })
    }

    inner class SortedRowAttributeSetTransformer : AttributeSetTransformer<RowAttribute<*>> {
        override fun transform(input: Set<RowAttribute<*>>): Set<RowAttribute<*>> =
            input.toSortedSet(compareBy {
                attributeOperationsContainer.getRowAttributeOperation(it.javaClass)?.priority() ?: 0
            })
    }

    inner class SortedCellAttributeSetTransformer : AttributeSetTransformer<CellAttribute<*>> {
        override fun transform(input: Set<CellAttribute<*>>): Set<CellAttribute<*>> =
            input.sortedBy {
                attributeOperationsContainer.getCellAttributeOperation(it.javaClass)?.priority() ?: 0
            }.toSet()
    }

    internal fun createAttributeTransformerContainer(): AttributeTransformerContainer {
        return AttributeTransformerContainer().also {
            it.set(TableAttribute::class.java, listOf(SortedTableAttributeSetTransformer()))
            it.set(ColumnAttribute::class.java, listOf(SortedColumnAttributeSetTransformer()))
            it.set(RowAttribute::class.java, listOf(SortedRowAttributeSetTransformer()))
            it.set(CellAttribute::class.java, listOf(SortedCellAttributeSetTransformer()))
        }
    }

    private inline fun <reified T : Attribute<*>> AttributedModel<T>.withAttributeSetCacheIfEnabled(block: () -> Unit) {
        if (enableAttributeSetBasedCaching) withAttributeSetBasedCache { block() } else block()
    }

    override fun createTable(renderingContext: CTX, context: AttributedTable) {
        context.withAttributeSetCacheIfEnabled {
            context.skipAttributes().let { tableContext ->
                return exposedExportOperations.createTable(renderingContext, tableContext).also {
                    context.attributes?.forEach { tableAttribute ->
                        attributeOperationsContainer.getTableAttributeOperation(tableAttribute.javaClass)
                            ?.renderAttribute(renderingContext, tableContext, tableAttribute)
                    }
                }
            }
        }
    }

    override fun beginRow(renderingContext: CTX, context: AttributedRow<T>) {
        context.withAttributeSetCacheIfEnabled {
            context.skipAttributes().let { rowContext ->
                var operationRendered = false
                if (!context.attributes.isNullOrEmpty()) {
                    context.attributes?.forEach { attribute ->
                        attributeOperationsContainer.getRowAttributeOperation(attribute.javaClass)?.let { operation ->
                            if (operation.priority() >= 0 && !operationRendered) {
                                exposedExportOperations.beginRow(renderingContext, rowContext)
                                operationRendered = true
                            }
                            operation.renderAttribute(renderingContext, rowContext, attribute)
                        }
                    }
                }
                if (!operationRendered) {
                    exposedExportOperations.beginRow(renderingContext, rowContext)
                }
            }
        }
    }

    override fun renderColumn(renderingContext: CTX, context: AttributedColumn) {
        context.withAttributeSetCacheIfEnabled {
            context.skipAttributes().let { columnContext ->
                context.attributes?.let { attributes ->
                    attributes.forEach { attribute ->
                        attributeOperationsContainer.getColumnAttributeOperation(attribute.javaClass)
                            ?.renderAttribute(renderingContext, columnContext, attribute)
                    }
                }
            }
        }
    }

    override fun renderRowCell(renderingContext: CTX, context: AttributedCell) {
        context.withAttributeSetCacheIfEnabled {
            context.skipAttributes().let { rowCellContext ->
                var operationRendered = false
                if (!context.attributes.isNullOrEmpty()) {
                    context.attributes.forEach { attribute ->
                        attributeOperationsContainer.getCellAttributeOperation(attribute.javaClass)?.let { operation ->
                            if (operation.priority() >= 0 && !operationRendered) {
                                exposedExportOperations.renderRowCell(renderingContext, rowCellContext)
                                operationRendered = true
                            }
                            operation.renderAttribute(renderingContext, rowCellContext, attribute)
                        }
                    }
                }
                if (!operationRendered) {
                    exposedExportOperations.renderRowCell(renderingContext, rowCellContext)
                }
            }
        }
    }

    override fun endRow(renderingContext: CTX, context: AttributedRowWithCells<T>) {
        exposedExportOperations.endRow(renderingContext, context.skipAttributes())
    }

}

