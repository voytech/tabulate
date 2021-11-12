package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.template.context.RenderingContext
import io.github.voytech.tabulate.template.result.ResultProvider
import io.github.voytech.tabulate.template.spi.AttributeRenderOperationsProvider
import io.github.voytech.tabulate.template.spi.ExportOperationsProvider
import java.util.*

interface TableExportOperations<T> {
    fun createTable(context: AttributedTable)
    fun renderColumn(context: AttributedColumn) {}
    fun beginRow(context: AttributedRow<T>) {}
    fun renderRowCell(context: AttributedCell)
    fun endRow(context: AttributedRowWithCells<T>) {}
}

interface ExposedContextExportOperations<T> {
    fun createTable(context: TableContext) {}
    fun renderColumn(context: ColumnContext) {}
    fun beginRow(context: RowContext<T>) {}
    fun renderRowCell(context: RowCellContext)
    fun endRow(context: RowContextWithCells<T>) {}
}

abstract class ExportOperationsConfiguringFactory<T, CTX : RenderingContext> : ExportOperationsProvider<T> {

    private val context: CTX by lazy {
        createRenderingContext()
    }

    private val attributeOperationsContainer: AttributesOperationsContainer<T> by lazy {
        registerAttributesOperations(context)
    }

    protected abstract fun createRenderingContext(): CTX

    protected abstract fun createExportOperations(renderingContext: CTX): ExposedContextExportOperations<T>

    protected abstract fun createResultProviders(renderingContext: CTX): List<ResultProvider<*>>

    protected open fun getAttributeOperationsFactory(renderingContext: CTX): AttributeRenderOperationsFactory<T>? = null

    final override fun createExportOperations(): TableExportOperations<T> =
        AttributeDelegatingExportOperations(attributeOperationsContainer, createExportOperations(context))

    final override fun createResultProviders(): List<ResultProvider<*>> {
        return createResultProviders(context)
    }

    private fun registerAttributesOperations(
        attributeOperationsContainer: AttributesOperationsContainer<T>,
        factory: AttributeRenderOperationsFactory<T>?,
    ): AttributesOperationsContainer<T> {
        return attributeOperationsContainer.apply {
            factory?.let { this.registerAttributesOperations(it) }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun registerClientDefinedAttributesOperations(
        renderingContext: CTX,
        attributeOperationsContainer: AttributesOperationsContainer<T>,
    ): AttributesOperationsContainer<T> {
        ServiceLoader.load(AttributeRenderOperationsProvider::class.java)
            .filter { it.getContextClass() == renderingContext.javaClass }
            .map { it as AttributeRenderOperationsProvider<T, CTX> }
            .forEach {
                registerAttributesOperations(attributeOperationsContainer, it.getAttributeOperationsFactory(renderingContext))
            }
        return attributeOperationsContainer
    }

    private fun registerAttributesOperations(renderingContext: CTX): AttributesOperationsContainer<T> {
        return AttributesOperationsContainer<T>().let {
            registerAttributesOperations(it, getAttributeOperationsFactory(renderingContext))
            registerClientDefinedAttributesOperations(renderingContext, it)
        }
    }

}
