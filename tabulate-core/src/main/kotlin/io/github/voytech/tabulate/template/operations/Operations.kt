package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.model.attributes.*
import io.github.voytech.tabulate.template.context.RenderingContext
import java.util.logging.Logger

class OperationTypeInfo<CTX : RenderingContext, ATTR_CAT : Attribute<*>, E : AttributedModel<ATTR_CAT>>(
    val renderingContextType: Class<CTX>,
    val operationContextType: Class<E>,
    val attributeLevelType: Class<ATTR_CAT>
)

interface TypeInfoProvider<CTX : RenderingContext, ATTR_CAT : Attribute<*>, E : AttributedModel<ATTR_CAT>> {
    fun typeInfo(): OperationTypeInfo<CTX, ATTR_CAT, E>
}

fun interface Operation<CTX : RenderingContext, ATTR_CAT : Attribute<*>, E : AttributedModel<ATTR_CAT>> {
    fun render(renderingContext: CTX, context: E)
}

class RuntimeReifiedOperation<CTX : RenderingContext, ATTR_CAT : Attribute<*>, E : AttributedModel<ATTR_CAT>>(
    private val operation: Operation<CTX, ATTR_CAT, E>,
    private val typeInfo: OperationTypeInfo<CTX, ATTR_CAT, E>
) : Operation<CTX, ATTR_CAT, E>, TypeInfoProvider<CTX, ATTR_CAT, E> {
    override fun typeInfo(): OperationTypeInfo<CTX, ATTR_CAT, E> = typeInfo
    override fun render(renderingContext: CTX, context: E) {
        operation.render(renderingContext, context)
    }
}

@Suppress("UNCHECKED_CAST")
internal inline fun <CTX : RenderingContext, reified ATTR_CAT : Attribute<*>, reified E : AttributedModel<ATTR_CAT>>
        Operation<CTX, ATTR_CAT, E>.runtimeReify(clazz: Class<CTX>): RuntimeReifiedOperation<CTX, ATTR_CAT, E> =
    RuntimeReifiedOperation(this, OperationTypeInfo(clazz, E::class.java, ATTR_CAT::class.java))

data class Operations<CTX : RenderingContext>(
    internal val operationsByContextClass: Map<Class<out AttributedModel<*>>, Operation<CTX, *, *>>
) {
    @Suppress("UNCHECKED_CAST")
    private fun <E : AttributedModel<*>> getByContextOrNull(clazz: Class<E>): Operation<CTX, *, E>? {
        return operationsByContextClass[clazz] as? Operation<CTX, *, E>?
    }

    internal fun <E : AttributedModel<*>> render(renderingContext: CTX, context: E) {
        getByContextOrNull(context.javaClass)?.render(renderingContext, context) ?: run {
            logger.warning("No render operation for context class: ${context.javaClass.name} !")
        }
    }

    companion object {
        val logger: Logger = Logger.getLogger(Operations::class.java.name)
    }
}

class OperationsBuilder<CTX : RenderingContext> {
    var createTable: CreateTableOperation<CTX>? = CreateTableOperation { _, _ -> }
    var closeTable: CloseTableOperation<CTX>? = CloseTableOperation { _, _ -> }
    var openColumn: OpenColumnOperation<CTX>? = OpenColumnOperation { _, _ -> }
    var closeColumn: CloseColumnOperation<CTX>? = CloseColumnOperation { _, _ -> }
    var openRow: OpenRowOperation<CTX>? = OpenRowOperation { _, _ -> }
    var closeRow: CloseRowOperation<CTX>? = CloseRowOperation { _, _ -> }
    var renderRowCell: RenderRowCellOperation<CTX>? = RenderRowCellOperation { _, _ -> }

    internal fun build(
        clazz: Class<CTX>,
        attributesOperations: AttributesOperationsContainer<CTX>? = null
    ): Operations<CTX> = Operations(
        listOfNotNull(
            createTable?.runtimeReify(clazz),
            closeTable?.runtimeReify(clazz),
            openColumn?.runtimeReify(clazz),
            closeColumn?.runtimeReify(clazz),
            openRow?.runtimeReify(clazz),
            closeRow?.runtimeReify(clazz),
            renderRowCell?.runtimeReify(clazz)
        ).map { reifiedOperation ->
            attributesOperations?.let { AttributesHandlingOperation(reifiedOperation, it) } ?: reifiedOperation
        }.associateBy { it.typeInfo().operationContextType }
    )
}

fun interface CreateTableOperation<CTX : RenderingContext> : Operation<CTX, TableAttribute<*>, TableCreationContext>

fun interface OpenColumnOperation<CTX : RenderingContext> : Operation<CTX, ColumnAttribute<*>, ColumnOpeningContext>

fun interface OpenRowOperation<CTX : RenderingContext> : Operation<CTX, RowAttribute<*>, RowOpeningContext>

fun interface RenderRowCellOperation<CTX : RenderingContext> : Operation<CTX, CellAttribute<*>, CellContext>

fun interface CloseRowOperation<CTX : RenderingContext> : Operation<CTX, RowAttribute<*>, RowClosingContext<*>>

fun interface CloseColumnOperation<CTX : RenderingContext> : Operation<CTX, ColumnAttribute<*>, ColumnClosingContext>

fun interface CloseTableOperation<CTX : RenderingContext> : Operation<CTX, TableAttribute<*>, TableClosingContext>

internal class AttributesHandlingOperation<
        CTX : RenderingContext,
        ATTR_CAT : Attribute<*>,
        E : AttributedModel<ATTR_CAT>>(
    private val operation: RuntimeReifiedOperation<CTX, ATTR_CAT, E>,
    attributeOperationsContainer: AttributesOperationsContainer<CTX>,
    private val enableAttributeSetBasedCaching: Boolean = true
) : Operation<CTX, ATTR_CAT, E>, TypeInfoProvider<CTX, ATTR_CAT, E> {

    private val attributeOperations = attributeOperationsContainer.getOperationsBy(operation.typeInfo())

    private val filteredOperationCache: AttributeClassBasedCache<ATTR_CAT, List<AttributeOperation<CTX, ATTR_CAT, *, AttributedModel<ATTR_CAT>>>> =
        AttributeClassBasedCache()

    @Suppress("UNCHECKED_CAST")
    private fun <OP : AttributeOperation<CTX, ATTR_CAT, *, AttributedModel<ATTR_CAT>>> AttributedModel<ATTR_CAT>.forEachOperation(
        unfiltered: List<OP>, consumer: (operation: OP) -> Boolean
    ) {
        attributes?.let { _attributes ->
            if (_attributes.isNotEmpty()) {
                if (filteredOperationCache[_attributes].isNullOrEmpty()) {
                    filteredOperationCache[_attributes] = unfiltered.filterTo(mutableListOf()) { consumer(it) }
                } else {
                    filteredOperationCache[_attributes]!!.forEach { consumer(it as OP) }
                }
            }
        }
    }

    private fun E.withAttributeSetCacheIfEnabled(block: () -> Unit) {
        if (enableAttributeSetBasedCaching) withAttributeSetBasedCache { block() } else block()
    }

    @Suppress("UNCHECKED_CAST")
    private fun <ATTR : ATTR_CAT> AttributeOperation<CTX, ATTR_CAT, ATTR, AttributedModel<ATTR_CAT>>.renderAttribute(
        renderingContext: CTX,
        context: AttributedModel<ATTR_CAT>,
        clazz: Class<out Attribute<*>>,
    ): Boolean {
        return context.getModelAttribute(clazz as Class<ATTR>)?.let {
            renderAttribute(renderingContext, context, it).let { true }
        } ?: false
    }

    override fun render(renderingContext: CTX, context: E) {
        context.withAttributeSetCacheIfEnabled {
            var operationRendered = false
            if (!context.attributes.isNullOrEmpty()) {
                context.forEachOperation(attributeOperations) { attributeOperation ->
                    if (attributeOperation.priority() >= 0 && !operationRendered) {
                        operation.render(renderingContext, context)
                        operationRendered = true
                    }
                    attributeOperation.renderAttribute(
                        renderingContext,
                        context,
                        attributeOperation.attributeClass()
                    )
                }
            }
            if (!operationRendered) {
                operation.render(renderingContext, context)
            }
        }
    }

    override fun typeInfo(): OperationTypeInfo<CTX, ATTR_CAT, E> = operation.typeInfo()

}