package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.model.attributes.*
import io.github.voytech.tabulate.template.context.RenderingContext

/**
 * [AttributedContextExportOperations] implementation dispatching attributes into corresponding attribute operations.
 * Such separation of concerns allows to register attribute renderers working for the same [RenderingContext] in
 * different compilation unit.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@Suppress("UNCHECKED_CAST")
internal class TableOperationsWithAttributeSupport<CTX : RenderingContext>(
    private val attributeOperationsContainer: AttributesOperationsContainer<CTX>,
    private val exposedExportOperations: TableExportOperations<CTX>,
    private val enableAttributeSetBasedCaching: Boolean = true
) : AttributedContextExportOperations<CTX> {

    private val sortedTableAttributeOperations: List<TableAttributeRenderOperation<CTX, TableAttribute<*>>> by lazy {
        attributeOperationsContainer.getOperationsBy<TableAttribute<*>>() as List<TableAttributeRenderOperation<CTX, TableAttribute<*>>>
    }

    private val sortedColumnAttributeOperations: List<ColumnAttributeRenderOperation<CTX, ColumnAttribute<*>>> by lazy {
        attributeOperationsContainer.getOperationsBy<ColumnAttribute<*>>() as List<ColumnAttributeRenderOperation<CTX, ColumnAttribute<*>>>
    }

    private val sortedRowAttributeOperations: List<RowAttributeRenderOperation<CTX, RowAttribute<*>>> by lazy {
        attributeOperationsContainer.getOperationsBy<RowAttribute<*>>() as List<RowAttributeRenderOperation<CTX, RowAttribute<*>>>
    }

    private val sortedCelAttributeOperations: List<CellAttributeRenderOperation<CTX, CellAttribute<*>>> by lazy {
        attributeOperationsContainer.getOperationsBy<CellAttribute<*>>() as List<CellAttributeRenderOperation<CTX, CellAttribute<*>>>
    }

    private val filteredOperationCache: AttributeClassBasedCache<Attribute<*>, List<AttributeOperation<CTX, *, *, *>>> =
        AttributeClassBasedCache()

    private inline fun <reified OP : AttributeOperation<CTX, CAT, *, *>,
            reified CAT : Attribute<*>> AttributedModel<CAT>.forEachOperation(
        unfiltered: List<OP>, consumer: (operation: OP) -> Boolean
    ) {
        val attributes = attributes!! as Attributes<Attribute<*>> // TODO play with variance on Attributes - try with covariance at declaration site.
        if (!attributes.isNullOrEmpty()) {
            if (filteredOperationCache[attributes].isNullOrEmpty()) {
                filteredOperationCache[attributes] = unfiltered.filterTo(mutableListOf()) { consumer(it) }
            } else {
                filteredOperationCache[attributes]!!.forEach { consumer(it as OP) }
            }
        }
    }

    private inline fun <reified T : Attribute<*>> AttributedModel<T>.withAttributeSetCacheIfEnabled(block: () -> Unit) {
        if (enableAttributeSetBasedCaching) withAttributeSetBasedCache { block() } else block()
    }

    override fun createTable(renderingContext: CTX, context: AttributedTable) {
        context.withAttributeSetCacheIfEnabled {
            context.skipAttributes().let { tableContext ->
                exposedExportOperations.createTable(renderingContext, tableContext).also {
                    sortedTableAttributeOperations.forEach { operation ->
                        context.attributes?.get(operation.attributeType())?.let { attribute ->
                            operation.renderAttribute(renderingContext, tableContext, attribute)
                        }
                    }
                }
            }
        }
    }

    override fun beginRow(renderingContext: CTX, context: AttributedRow) {
        context.withAttributeSetCacheIfEnabled {
            context.skipAttributes().let { rowContext ->
                var operationRendered = false
                if (!context.attributes.isNullOrEmpty()) {
                    context.forEachOperation(sortedRowAttributeOperations) { operation ->
                        if (operation.priority() >= 0 && !operationRendered) {
                            exposedExportOperations.beginRow(renderingContext, rowContext)
                            operationRendered = true
                        }
                        context.attributes?.get(operation.attributeType())?.let { attribute ->
                            operation.renderAttribute(renderingContext, rowContext, attribute).let { true }
                        } ?: false
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
                    sortedColumnAttributeOperations.forEach { operation ->
                        if (context.currentPhase in operation.applicableRenderingPhases()) {
                            attributes[operation.attributeType()]?.let { attribute ->
                                operation.renderAttribute(renderingContext, columnContext, attribute)
                            }
                        }
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
                    context.forEachOperation(sortedCelAttributeOperations) { operation ->
                        if (operation.priority() >= 0 && !operationRendered) {
                            exposedExportOperations.renderRowCell(renderingContext, rowCellContext)
                            operationRendered = true
                        }
                        context.attributes?.get(operation.attributeType())?.let { attribute ->
                            operation.renderAttribute(renderingContext, rowCellContext, attribute).let { true }
                        } ?: false
                    }
                }
                if (!operationRendered) {
                    exposedExportOperations.renderRowCell(renderingContext, rowCellContext)
                }
            }
        }
    }

    override fun <T> endRow(renderingContext: CTX, context: AttributedRowWithCells<T>) {
        exposedExportOperations.endRow(renderingContext, context.skipAttributes())
    }

}

