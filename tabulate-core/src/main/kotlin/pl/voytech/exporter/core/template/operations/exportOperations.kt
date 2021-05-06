package pl.voytech.exporter.core.template.operations

import org.reactivestreams.Publisher
import pl.voytech.exporter.core.api.builder.TableBuilder
import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.template.ResultHandler
import pl.voytech.exporter.core.template.context.AttributedCell
import pl.voytech.exporter.core.template.context.AttributedColumn
import pl.voytech.exporter.core.template.context.AttributedRow
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
    fun renderRow(context: AttributedRow<T>) {}
    fun renderRowCell(context: AttributedCell)
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

abstract class AdaptingLifecycleOperations<T, O, A>(val adaptee: A) : LifecycleOperations<T, O>

abstract class AdaptingTableRenderOperations<T, A>(val adaptee: A) : TableRenderOperations<T>

abstract class ExportOperationsConfiguringFactory<T, O> : ExportOperationsProvider<T, O> {

    private val attributeOperations: AttributesOperations<T> = AttributesOperations()

    final override fun test(ident: Identifiable): Boolean = getFormat() == ident.getFormat()

    final override fun createLifecycleOperations(): LifecycleOperations<T, O> {
        return getExportOperationsFactory().createLifecycleOperations()
    }

    final override fun createTableOperation(): TableOperation<T> {
        val tableOp = getExportOperationsFactory().createTableOperation()
        return registerAttributesOperations().let {
            if (!attributeOperations.isEmpty()) {
                AttributeAwareTableOperations(attributeOperations, tableOp)
            } else {
                tableOp
            }
        }
    }

    final override fun createTableRenderOperations(): TableRenderOperations<T> {
        val tableOps = getExportOperationsFactory().createTableRenderOperations()
        return registerAttributesOperations().let {
            if (!attributeOperations.isEmpty()) {
                AttributeAwareTableRenderOperations(attributeOperations, tableOps)
            } else {
                tableOps
            }
        }
    }

    override fun createOperations(): ExportOperations<T, O> = ExportOperations(
        lifecycleOperations = createLifecycleOperations(),
        tableOperation = createTableOperation(),
        tableRenderOperations = createTableRenderOperations()
    )

    abstract fun getExportOperationsFactory(): ExportOperationsFactory<T, O>

    open fun getAttributeOperationsFactory(): AttributeRenderOperationsFactory<T>? = null

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
    private fun registerClientDefinedAttributesOperations(attributeOperations: AttributesOperations<T>): AttributesOperations<T> {
        val loader: ServiceLoader<AttributeRenderOperationsProvider<*>> =
            ServiceLoader.load(AttributeRenderOperationsProvider::class.java)
        (loader.filter { it.test(this) }
            .map { it as AttributeRenderOperationsProvider<T>? }).forEach {
                registerAttributesOperations(attributeOperations, it)
            }
        return attributeOperations
    }

    private fun registerAttributesOperations(): AttributesOperations<T> {
        return if(attributeOperations.isEmpty()) {
            registerAttributesOperations(attributeOperations, getAttributeOperationsFactory())
            registerClientDefinedAttributesOperations(attributeOperations)
        } else {
            attributeOperations
        }
    }

}
