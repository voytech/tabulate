package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.model.attributes.Attribute
import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.model.attributes.alias.ColumnAttribute
import io.github.voytech.tabulate.model.attributes.alias.RowAttribute
import io.github.voytech.tabulate.model.attributes.alias.TableAttribute
import io.github.voytech.tabulate.template.context.RenderingContext
import java.util.logging.Logger

@Suppress("UNCHECKED_CAST")
internal class AttributesOperationsContainer<CTX : RenderingContext> {

    private val attributeRenderOperationsByClass: MutableMap<Class<out Attribute<*>>, AttributeOperation<CTX, *, *, *>> =
        mutableMapOf()

    fun getCellAttributeOperation(clazz: Class<out CellAttribute>): CellAttributeRenderOperation<CTX, CellAttribute>? =
        (attributeRenderOperationsByClass[clazz] as CellAttributeRenderOperation<CTX, CellAttribute>?).also {
            warnNoOperation(it, clazz)
        }

    fun getRowAttributeOperation(clazz: Class<out RowAttribute>): RowAttributeRenderOperation<CTX, RowAttribute>? =
        (attributeRenderOperationsByClass[clazz] as RowAttributeRenderOperation<CTX, RowAttribute>?).also {
            warnNoOperation(it, clazz)
        }

    fun getColumnAttributeOperation(clazz: Class<out ColumnAttribute>): ColumnAttributeRenderOperation<CTX, ColumnAttribute>? =
        (attributeRenderOperationsByClass[clazz] as ColumnAttributeRenderOperation<CTX, ColumnAttribute>?).also {
            warnNoOperation(it, clazz)
        }

    fun getTableAttributeOperation(clazz: Class<out  TableAttribute>): TableAttributeRenderOperation<CTX, TableAttribute>? =
        (attributeRenderOperationsByClass[clazz] as TableAttributeRenderOperation<CTX, TableAttribute>?).also {
            warnNoOperation(it, clazz)
        }

    private fun warnNoOperation(operation: AttributeOperation<*, *, *, *>?, clazz: Class<out Attribute<*>>) {
        if (operation == null) logger.warning("No attribute render operation for class: ${clazz.name} !")
    }

    private fun register(operation: AttributeOperation<CTX, out Attribute<*>, out Attribute<*>, out ModelAttributeAccessor<out Attribute<*>>>) {
        attributeRenderOperationsByClass[operation.attributeType()] = operation
    }

    internal fun registerAttributesOperations(factory: AttributeRenderOperationsFactory<CTX>): AttributesOperationsContainer<CTX> {
        factory.createCellAttributeRenderOperations()?.forEach { register(it) }
        factory.createTableAttributeRenderOperations()?.forEach { register(it) }
        factory.createRowAttributeRenderOperations()?.forEach { register(it) }
        factory.createColumnAttributeRenderOperations()?.forEach { register(it) }
        return this
    }

    fun isEmpty(): Boolean = attributeRenderOperationsByClass.isEmpty()

    companion object {
        val logger: Logger = Logger.getLogger(AttributesOperationsContainer::class.java.name)
    }
}