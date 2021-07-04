package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.api.builder.TableBuilder
import io.github.voytech.tabulate.model.Table
import io.github.voytech.tabulate.template.context.AttributedCell
import io.github.voytech.tabulate.template.context.AttributedColumn
import io.github.voytech.tabulate.template.context.AttributedRow
import io.github.voytech.tabulate.template.context.RenderingContext
import io.github.voytech.tabulate.template.operations.impl.AttributeAwareTableExportOperations
import io.github.voytech.tabulate.template.operations.impl.AttributesOperations
import io.github.voytech.tabulate.template.spi.AttributeRenderOperationsProvider
import io.github.voytech.tabulate.template.spi.ExportOperationsProvider
import io.github.voytech.tabulate.template.spi.Identifiable
import java.util.*


interface TableExportOperations<T> {
    fun initialize() {}
    fun createTable(builder: TableBuilder<T>): Table<T> = builder.build()
    fun renderColumn(context: AttributedColumn) {}
    fun beginRow(context: AttributedRow<T>) {}
    fun renderRowCell(context: AttributedCell)
    fun endRow(context: AttributedRow<T>) {}
    fun finish() {}
}

interface ExportOperationsFactory<T> {
    fun createTableExportOperation(): TableExportOperations<T>
}

abstract class ExportOperationsConfiguringFactory<T,CTX: RenderingContext> : ExportOperationsProvider<T,CTX> {

    private val cachedRenderingContext: CTX by lazy {
        createRenderingContext()
    }

    private val attributeOperations: AttributesOperations<T> by lazy {
        registerAttributesOperations(cachedRenderingContext)
    }

    final override fun test(ident: Identifiable): Boolean = getFormat() == ident.getFormat()

    final override fun getRenderingContext(): CTX = cachedRenderingContext

    abstract fun createRenderingContext(): CTX

    open fun getAttributeOperationsFactory(renderingContext: CTX): AttributeRenderOperationsFactory<T>? = null

    override fun createExportOperations(): TableExportOperations<T> {
        val tableOps = createTableExportOperation()
        return if (!attributeOperations.isEmpty()) {
            AttributeAwareTableExportOperations(attributeOperations, tableOps)
        } else {
            tableOps
        }
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
        creationContext: CTX,
        attributeOperations: AttributesOperations<T>,
    ): AttributesOperations<T> {
        val loader: ServiceLoader<AttributeRenderOperationsProvider<*, *>> =
            ServiceLoader.load(AttributeRenderOperationsProvider::class.java)
        loader.filter { it.getContextClass() == creationContext.javaClass }
            .map { it as AttributeRenderOperationsProvider<T, CTX> }
            .forEach {
                registerAttributesOperations(attributeOperations, it.getAttributeOperationsFactory(creationContext))
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
