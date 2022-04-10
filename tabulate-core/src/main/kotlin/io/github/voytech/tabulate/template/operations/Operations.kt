package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.model.attributes.*
import io.github.voytech.tabulate.template.context.RenderingContext
import java.util.logging.Logger

class OperationTypeInfo<CTX : RenderingContext, ATTR_CAT : Attribute<*>, E : AttributedModel<ATTR_CAT>>(
    val renderingContextType: Class<CTX>,
    val operationContextType: Class<E>,
    val attributeLevelType: Class<ATTR_CAT>
)

fun interface Operation<CTX : RenderingContext, ATTR_CAT : Attribute<*>, E : AttributedModel<ATTR_CAT>> {
    fun render(renderingContext: CTX, context: E)
}

fun interface OpenTableOperation<CTX : RenderingContext> : Operation<CTX, TableAttribute<*>, TableOpeningContext>

fun interface OpenColumnOperation<CTX : RenderingContext> : Operation<CTX, ColumnAttribute<*>, ColumnOpeningContext>

fun interface OpenRowOperation<CTX : RenderingContext> : Operation<CTX, RowAttribute<*>, RowOpeningContext>

fun interface RenderRowCellOperation<CTX : RenderingContext> : Operation<CTX, CellAttribute<*>, CellContext>

fun interface CloseRowOperation<CTX : RenderingContext> : Operation<CTX, RowAttribute<*>, RowClosingContext<*>>

fun interface CloseColumnOperation<CTX : RenderingContext> : Operation<CTX, ColumnAttribute<*>, ColumnClosingContext>

fun interface CloseTableOperation<CTX : RenderingContext> : Operation<CTX, TableAttribute<*>, TableClosingContext>

private typealias ReifiedOperation<CTX, ATTR_CAT, E> = Pair<OperationTypeInfo<CTX, ATTR_CAT, E>, Operation<CTX, ATTR_CAT, E>>

@Suppress("UNCHECKED_CAST")
internal inline fun <CTX : RenderingContext, reified ATTR_CAT : Attribute<*>, reified E : AttributedModel<ATTR_CAT>>
        Operation<CTX, ATTR_CAT, E>.enrich(ctx: Class<CTX>, attributesOps: AttributesOperationsContainer<CTX>? = null):
        ReifiedOperation<CTX, ATTR_CAT, E> =
    OperationTypeInfo(ctx, E::class.java, ATTR_CAT::class.java).let { info ->
        info to (attributesOps?.let { AttributesHandlingOperation(info, this, attributesOps) } ?: this)
    }

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
    var openTable: OpenTableOperation<CTX>? = OpenTableOperation { _, _ -> }
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
            openTable?.enrich(clazz, attributesOperations),
            closeTable?.enrich(clazz, attributesOperations),
            openColumn?.enrich(clazz, attributesOperations),
            closeColumn?.enrich(clazz, attributesOperations),
            openRow?.enrich(clazz, attributesOperations),
            closeRow?.enrich(clazz, attributesOperations),
            renderRowCell?.enrich(clazz, attributesOperations)
        ).associate { it.first.operationContextType to it.second }
    )
}

internal class AttributesHandlingOperation<
        CTX : RenderingContext,
        ATTR_CAT : Attribute<*>,
        E : AttributedModel<ATTR_CAT>>(
    typeInfo: OperationTypeInfo<CTX, ATTR_CAT, E>,
    private val operation: Operation<CTX, ATTR_CAT, E>,
    attributeOperationsContainer: AttributesOperationsContainer<CTX>,
    private val enableAttributeSetBasedCaching: Boolean = true
) : Operation<CTX, ATTR_CAT, E> {

    private val attributeOperations: List<AttributeOperation<CTX, ATTR_CAT, *, E>> =
        attributeOperationsContainer.getOperationsBy(typeInfo)

    private val filteredOperationCache: AttributeClassBasedCache<ATTR_CAT, List<AttributeOperation<CTX, ATTR_CAT, *, E>>> =
        AttributeClassBasedCache()

    @Suppress("UNCHECKED_CAST")
    private fun <OP : AttributeOperation<CTX, ATTR_CAT, *, E>> E.forEachOperation(
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
    private fun <ATTR : ATTR_CAT> AttributeOperation<CTX, ATTR_CAT, ATTR, E>.renderAttribute(
        renderingContext: CTX,
        context: E,
        clazz: Class<out ATTR_CAT>,
    ): Boolean {
        return context.getModelAttribute(clazz)?.let {
            renderAttribute(renderingContext, context, it as ATTR).let { true }
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
                    with(attributeOperation) {
                        renderAttribute(renderingContext, context, attributeClass())
                    }
                }
            }
            if (!operationRendered) {
                operation.render(renderingContext, context)
            }
        }
    }

}