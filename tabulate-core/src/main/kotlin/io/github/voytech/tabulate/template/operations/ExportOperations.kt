package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.template.context.RenderingContext
import io.github.voytech.tabulate.template.result.OutputBinding
import io.github.voytech.tabulate.template.spi.AttributeRenderOperationsProvider
import io.github.voytech.tabulate.template.spi.ExportOperationsProvider
import java.util.*

/**
 * Export operations operating on attribute-set enabled versions of contexts.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
interface AttributedContextExportOperations<CTX: RenderingContext> {
    fun createTable(renderingContext: CTX, context: AttributedTable)
    fun renderColumn(renderingContext: CTX, context: AttributedColumn) {}
    fun beginRow(renderingContext: CTX, context: AttributedRow) {}
    fun renderRowCell(renderingContext: CTX, context: AttributedCell)
    fun <T> endRow(renderingContext: CTX, context: AttributedRowWithCells<T>) {}
}

/**
 * Export operations compatible with context classes without attribute-set property included.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
interface TableExportOperations<CTX: RenderingContext> {
    fun createTable(renderingContext: CTX, context: TableContext) {}
    fun beginRow(renderingContext: CTX, context: RowContext) {}
    fun renderRowCell(renderingContext: CTX, context: RowCellContext)
    fun <T> endRow(renderingContext: CTX, context: RowContextWithCells<T>) {}
}

/**
 * Export operations factory that can discover attribute operations.
 * It exposes [TableExportOperations] as public API (an interface without attribute-set in operation context).
 * @author Wojciech Mąka
 * @since 0.1.0
 */
abstract class ExportOperationsConfiguringFactory<CTX : RenderingContext> : ExportOperationsProvider<CTX> {

    private val attributeOperationsContainer: AttributesOperationsContainer<CTX> by lazy  {
        registerAttributesOperations()
    }

    protected abstract fun provideExportOperations(): TableExportOperations<CTX>

    abstract override fun createOutputBindings(): List<OutputBinding<CTX,*>>

    protected open fun getAttributeOperationsFactory(): AttributeRenderOperationsFactory<CTX>? = null

    final override fun createExportOperations(): AttributedContextExportOperations<CTX> =
        TableOperationsWithAttributeSupport(attributeOperationsContainer, provideExportOperations())

    private fun registerAttributesOperations(
        attributeOperationsContainer: AttributesOperationsContainer<CTX>,
        factory: AttributeRenderOperationsFactory<CTX>?,
    ): AttributesOperationsContainer<CTX> {
        return attributeOperationsContainer.apply {
            factory?.let { this.registerAttributesOperations(it) }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun registerClientDefinedAttributesOperations(
        attributeOperationsContainer: AttributesOperationsContainer<CTX>,
    ): AttributesOperationsContainer<CTX> {
        ServiceLoader.load(AttributeRenderOperationsProvider::class.java)
            .filter { getContextClass().isAssignableFrom(it.getContextClass()) }
            .map { it as AttributeRenderOperationsProvider<CTX> }
            .forEach {
                registerAttributesOperations(attributeOperationsContainer, it.getAttributeOperationsFactory())
            }
        return attributeOperationsContainer
    }

    private fun registerAttributesOperations(): AttributesOperationsContainer<CTX> {
        return AttributesOperationsContainer<CTX>().let {
            registerAttributesOperations(it, getAttributeOperationsFactory())
            registerClientDefinedAttributesOperations(it)
        }
    }

}
