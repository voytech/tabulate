package pl.voytech.exporter.core.template.operations

import org.reactivestreams.Publisher
import pl.voytech.exporter.core.api.builder.TableBuilder
import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.template.ResultHandler
import pl.voytech.exporter.core.template.context.*
import pl.voytech.exporter.core.template.operations.impl.AttributeAwareTableOperations
import pl.voytech.exporter.core.template.operations.impl.AttributeAwareTableRenderOperations
import pl.voytech.exporter.core.template.operations.impl.AttributesOperations
import pl.voytech.exporter.core.template.spi.AttributeRenderOperationsProvider
import pl.voytech.exporter.core.template.spi.ExportOperationsProvider
import pl.voytech.exporter.core.template.spi.Identifiable
import java.util.*


interface LifecycleOperations<T, O> {
    fun initialize(source: Publisher<T>, resultHandler: ResultHandler<T, O>)
    fun finish()
}

interface TableOperation<T> {
    fun createTable(builder: TableBuilder<T>): Table<T> = builder.build()
}

interface TableRenderOperations<T> {
    fun renderColumn(context: AttributedColumn) {}
    fun beginRow(context: AttributedRow<T>) {}
    fun renderRowCell(context: AttributedCell)
    fun endRow(context: AttributedRow<T>) {}
}

interface TableRenderOperationsOverlay<T> {
    fun renderColumn(context: ColumnContext) {}
    fun beginRow(context: RowContext<T>) {}
    fun renderRowCell(context: RowCellContext)
    fun endRow(context: RowContext<T>) {}
}

class TableRenderOperationsAdapter<T>(private val overlay: TableRenderOperationsOverlay<T>): TableRenderOperations<T> {
    override fun renderColumn(context: AttributedColumn) = overlay.renderColumn(context.narrow())
    override fun beginRow(context: AttributedRow<T>) = overlay.beginRow(context.narrow())
    override fun renderRowCell(context: AttributedCell) = overlay.renderRowCell(context.narrow())
    override fun endRow(context: AttributedRow<T>) = overlay.endRow(context.narrow())
}

class ExportOperations<T, O>(
    val lifecycleOperations: LifecycleOperations<T, O>,
    val tableOperation: TableOperation<T>,
    val tableRenderOperations: TableRenderOperations<T>,
)

interface ExportOperationsFactory<T, O> {
    fun createLifecycleOperations(): LifecycleOperations<T, O>
    fun createTableOperation(): TableOperation<T>
    fun createTableRenderOperations(): TableRenderOperations<T>
}

abstract class ExportOperationsConfiguringFactory<CTX, T, O> : ExportOperationsProvider<T, O> {

    private val creationContext: CTX by lazy {
        provideFactoryContext()
    }

    private val attributeOperations: AttributesOperations<T> by lazy {
        registerAttributesOperations(creationContext)
    }

    final override fun test(ident: Identifiable): Boolean = getFormat() == ident.getFormat()

    override fun createOperations(): ExportOperations<T, O> {
        return ExportOperations(
            lifecycleOperations = createLifecycleOperations(),
            tableOperation = createTableOperation(),
            tableRenderOperations = createTableRenderOperations()
        )
    }

    open fun getAttributeOperationsFactory(creationContext: CTX): AttributeRenderOperationsFactory<T>? = null

    abstract fun getExportOperationsFactory(creationContext: CTX): ExportOperationsFactory<T, O>

    abstract fun provideFactoryContext(): CTX

    private fun createLifecycleOperations(): LifecycleOperations<T, O> {
        return getExportOperationsFactory(creationContext).createLifecycleOperations()
    }

    private fun createTableOperation(): TableOperation<T> {
        val tableOp = getExportOperationsFactory(creationContext).createTableOperation()
        return if (!attributeOperations.isEmpty()) {
            AttributeAwareTableOperations(attributeOperations, tableOp)
        } else {
            tableOp
        }
    }

    private fun createTableRenderOperations(): TableRenderOperations<T> {
        val tableOps = getExportOperationsFactory(creationContext).createTableRenderOperations()
        return if (!attributeOperations.isEmpty()) {
            AttributeAwareTableRenderOperations(attributeOperations, tableOps)
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
        loader.filter { it.test(this) }
            .map { it as AttributeRenderOperationsProvider<CTX, T>? }
            .forEach {
                registerAttributesOperations(attributeOperations, it!!.getAttributeOperationsFactory(creationContext))
            }
        return attributeOperations
    }

    private fun registerAttributesOperations(creationContext: CTX): AttributesOperations<T> {
        return AttributesOperations<T>().let {
            registerAttributesOperations(it, getAttributeOperationsFactory(creationContext))
            registerClientDefinedAttributesOperations(creationContext, it)
        }
    }

}
