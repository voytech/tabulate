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

interface ExportOperationsFactory<CTX, T, O> {
    fun createLifecycleOperations(creationContext: CTX): LifecycleOperations<T, O>
    fun createTableOperation(creationContext: CTX): TableOperation<T>
    fun createTableRenderOperations(creationContext: CTX): TableRenderOperations<T>
}

abstract class AdaptingLifecycleOperations<T, O, A>(val adaptee: A) : LifecycleOperations<T, O>

abstract class AdaptingTableRenderOperations<T, A>(val adaptee: A) : TableRenderOperations<T>

abstract class ExportOperationsConfiguringFactory<CTX, T, O> : ExportOperationsProvider<CTX, T, O> {

    private val attributeOperations: AttributesOperations<T> = AttributesOperations()

    final override fun test(ident: Identifiable): Boolean = getFormat() == ident.getFormat()

    final override fun createLifecycleOperations(creationContext: CTX): LifecycleOperations<T, O> {
        return getExportOperationsFactory().createLifecycleOperations(creationContext)
    }

    final override fun createTableOperation(creationContext: CTX): TableOperation<T> {
        val tableOp = getExportOperationsFactory().createTableOperation(creationContext)
        return registerAttributesOperations(creationContext).let {
            if (!attributeOperations.isEmpty()) {
                AttributeAwareTableOperations(attributeOperations, tableOp)
            } else {
                tableOp
            }
        }
    }

    final override fun createTableRenderOperations(creationContext: CTX): TableRenderOperations<T> {
        val tableOps = getExportOperationsFactory().createTableRenderOperations(creationContext)
        return registerAttributesOperations(creationContext).let {
            if (!attributeOperations.isEmpty()) {
                AttributeAwareTableRenderOperations(attributeOperations, tableOps)
            } else {
                tableOps
            }
        }
    }

    override fun createOperations(): ExportOperations<T, O> {
      return getFactoryContext().let {
          ExportOperations(
              lifecycleOperations = createLifecycleOperations(it),
              tableOperation = createTableOperation(it),
              tableRenderOperations = createTableRenderOperations(it)
          )
      }
    }

    abstract fun getExportOperationsFactory(): ExportOperationsFactory<CTX, T, O>

    open fun getAttributeOperationsFactory(): AttributeRenderOperationsFactory<CTX, T>? = null

    private fun registerAttributesOperations(
        creationContext: CTX,
        attributeOperations: AttributesOperations<T>,
        factory: AttributeRenderOperationsFactory<CTX,T>?,
    ): AttributesOperations<T> {
        return attributeOperations.apply {
            factory?.let {
                it.createCellAttributeRenderOperations(creationContext)?.forEach { op -> this.register(op) }
                it.createTableAttributeRenderOperations(creationContext)?.forEach { op -> this.register(op) }
                it.createRowAttributeRenderOperations(creationContext)?.forEach { op -> this.register(op) }
                it.createColumnAttributeRenderOperations(creationContext)?.forEach { op -> this.register(op) }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun registerClientDefinedAttributesOperations(creationContext: CTX, attributeOperations: AttributesOperations<T>): AttributesOperations<T> {
        val loader: ServiceLoader<AttributeRenderOperationsProvider<*,*>> =
            ServiceLoader.load(AttributeRenderOperationsProvider::class.java)
        loader.filter { it.test(this) }
            .map { it as AttributeRenderOperationsProvider<CTX,T>? }
            .forEach {
                registerAttributesOperations(creationContext, attributeOperations, it)
            }
        return attributeOperations
    }

    private fun registerAttributesOperations(creationContext: CTX): AttributesOperations<T> {
        return if (attributeOperations.isEmpty()) {
            registerAttributesOperations(creationContext, attributeOperations, getAttributeOperationsFactory())
            registerClientDefinedAttributesOperations(creationContext, attributeOperations)
        } else {
            attributeOperations
        }
    }

}
