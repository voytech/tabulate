package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.template.context.*
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

abstract class ExportOperationsConfiguringFactory<T, CTX : RenderingContext> : ExportOperationsProvider<T> {

    private val context: CTX by lazy {
        createRenderingContext()
    }

    private val attributeOperations: AttributesOperations<T> by lazy {
        registerAttributesOperations(context)
    }

    protected abstract fun createRenderingContext(): CTX

    protected abstract fun createExportOperations(renderingContext: CTX): BasicContextExportOperations<T>

    protected abstract fun createResultProviders(renderingContext: CTX): List<ResultProvider<*>>

    protected open fun getAttributeOperationsFactory(renderingContext: CTX): AttributeRenderOperationsFactory<T>? = null

    final override fun createExportOperations(): TableExportOperations<T> =
        AttributeAwareTableExportOperations(attributeOperations, createExportOperations(context))

    final override fun createResultProviders(): List<ResultProvider<*>> {
        return createResultProviders(context)
    }

    private fun registerAttributesOperations(
        attributeOperations: AttributesOperations<T>,
        factory: AttributeRenderOperationsFactory<T>?,
    ): AttributesOperations<T> {
        return attributeOperations.apply {
            factory?.let {
                it.createCellAttributeRenderOperations()?.forEach { op -> this.register(op) }
                it.createTableAttributeRenderOperations()?.forEach { op -> this.register(op) }
                it.createRowAttributeRenderOperations()?.forEach { op -> this.register(op) }
                it.createColumnAttributeRenderOperations()?.forEach { op -> this.register(op) }
            }
        }
    }


    @Suppress("UNCHECKED_CAST")
    private fun registerClientDefinedAttributesOperations(
        renderingContext: CTX,
        attributeOperations: AttributesOperations<T>,
    ): AttributesOperations<T> {
        ServiceLoader.load(AttributeRenderOperationsProvider::class.java)
            .filter { it.getContextClass() == renderingContext.javaClass }
            .map { it as AttributeRenderOperationsProvider<T, CTX> }
            .forEach {
                registerAttributesOperations(attributeOperations, it.getAttributeOperationsFactory(renderingContext))
            }
        return attributeOperations
    }

    private fun registerAttributesOperations(renderingContext: CTX): AttributesOperations<T> {
        return AttributesOperations<T>().let {
            registerAttributesOperations(it, getAttributeOperationsFactory(renderingContext))
            registerClientDefinedAttributesOperations(renderingContext, it)
        }
    }

}
