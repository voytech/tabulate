package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.template.context.RenderingContext
import io.github.voytech.tabulate.template.result.ResultProvider
import io.github.voytech.tabulate.template.spi.AttributeRenderOperationsProvider
import io.github.voytech.tabulate.template.spi.ExportOperationsProvider
import java.util.*

interface AttributedContextExportOperations<T, CTX: RenderingContext> {
    fun createTable(renderingContext: CTX, context: AttributedTable)
    fun renderColumn(renderingContext: CTX, context: AttributedColumn) {}
    fun beginRow(renderingContext: CTX, context: AttributedRow<T>) {}
    fun renderRowCell(renderingContext: CTX, context: AttributedCell)
    fun endRow(renderingContext: CTX, context: AttributedRowWithCells<T>) {}
}

interface TableExportOperations<T, CTX: RenderingContext> {
    fun createTable(renderingContext: CTX, context: TableContext) {}
    fun renderColumn(renderingContext: CTX, context: ColumnContext) {}
    fun beginRow(renderingContext: CTX, context: RowContext<T>) {}
    fun renderRowCell(renderingContext: CTX, context: RowCellContext)
    fun endRow(renderingContext: CTX, context: RowContextWithCells<T>) {}
}

abstract class ExportOperationsConfiguringFactory<T, CTX : RenderingContext> : ExportOperationsProvider<CTX,T> {

    private val attributeOperationsContainer: AttributesOperationsContainer<CTX,T> by lazy  {
        registerAttributesOperations()
    }

    protected abstract fun provideExportOperations(): TableExportOperations<T, CTX>

    abstract override fun createResultProviders(): List<ResultProvider<CTX,*>>

    protected open fun getAttributeOperationsFactory(): AttributeRenderOperationsFactory<CTX, T>? = null

    final override fun createExportOperations(): AttributedContextExportOperations<T, CTX> =
        AttributeDispatchingTableOperations(attributeOperationsContainer, provideExportOperations())

    private fun registerAttributesOperations(
        attributeOperationsContainer: AttributesOperationsContainer<CTX, T>,
        factory: AttributeRenderOperationsFactory<CTX,T>?,
    ): AttributesOperationsContainer<CTX, T> {
        return attributeOperationsContainer.apply {
            factory?.let { this.registerAttributesOperations(it) }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun registerClientDefinedAttributesOperations(
        attributeOperationsContainer: AttributesOperationsContainer<CTX, T>,
    ): AttributesOperationsContainer<CTX, T> {
        ServiceLoader.load(AttributeRenderOperationsProvider::class.java)
            .filter { it.getContextClass() == getContextClass() }
            .map { it as AttributeRenderOperationsProvider<T, CTX> }
            .forEach {
                registerAttributesOperations(attributeOperationsContainer, it.getAttributeOperationsFactory())
            }
        return attributeOperationsContainer
    }

    private fun registerAttributesOperations(): AttributesOperationsContainer<CTX, T> {
        return AttributesOperationsContainer<CTX, T>().let {
            registerAttributesOperations(it, getAttributeOperationsFactory())
            registerClientDefinedAttributesOperations(it)
        }
    }

}
