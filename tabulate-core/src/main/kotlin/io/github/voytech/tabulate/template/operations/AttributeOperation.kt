package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.model.attributes.*
import io.github.voytech.tabulate.model.attributes.cell.CellAlignmentAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellBackgroundAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellBordersAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellTextStylesAttribute
import io.github.voytech.tabulate.model.attributes.column.ColumnWidthAttribute
import io.github.voytech.tabulate.model.attributes.row.RowHeightAttribute
import io.github.voytech.tabulate.model.attributes.table.TemplateFileAttribute
import io.github.voytech.tabulate.template.context.RenderingContext

data class AttributeOperationTypeInfo<CTX : RenderingContext, ATTR_CAT : Attribute<*>, ATTR : ATTR_CAT, E : AttributedModel<ATTR_CAT>>(
    internal val renderingContextClass: Class<CTX>,
    internal val operationContextClass: Class<E>,
    internal val attributeClass: Class<ATTR>
)

/**
 * A base class for all exporting (rendering) attribute operations associated for specific rendering contexts.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
interface AttributeOperation<CTX : RenderingContext, ATTR_CAT : Attribute<*>, ATTR : ATTR_CAT, E : AttributedModel<ATTR_CAT>> {
    fun typeInfo(): AttributeOperationTypeInfo<CTX, ATTR_CAT, ATTR, E>
    fun priority(): Int = DEFAULT
    fun renderAttribute(renderingContext: CTX, context: E, attribute: ATTR)

    companion object {
        const val LOWEST = Int.MIN_VALUE
        const val LOWER = -1
        const val DEFAULT = 1
    }
}

fun <CTX : RenderingContext, ATTR_CAT : Attribute<*>, ATTR : ATTR_CAT, E : AttributedModel<ATTR_CAT>> AttributeOperation<CTX, ATTR_CAT, ATTR, E>.attributeClass() =
    typeInfo().attributeClass

/**
 * Table attribute operation associated with table rendering context
 * @author Wojciech Mąka
 * @since 0.1.0
 */
interface TableAttributeRenderOperation<CTX : RenderingContext, ATTR : TableAttribute<*>>
    : AttributeOperation<CTX, TableAttribute<*>, ATTR, TableOpeningContext> {
    override fun typeInfo(): AttributeOperationTypeInfo<CTX, TableAttribute<*>, ATTR, TableOpeningContext> =
        AttributeOperationTypeInfo(renderingContextClass(), TableOpeningContext::class.java, attributeType())
    fun attributeType(): Class<ATTR>
    fun renderingContextClass(): Class<CTX>
}
    /**
 * Row attribute operation associated with row rendering context
 * @author Wojciech Mąka
 * @since 0.1.0
 */
interface RowAttributeRenderOperation<CTX : RenderingContext, ATTR : RowAttribute<*>>
    : AttributeOperation<CTX, RowAttribute<*>, ATTR, RowOpeningContext> {
        override fun typeInfo(): AttributeOperationTypeInfo<CTX, RowAttribute<*>, ATTR, RowOpeningContext> =
            AttributeOperationTypeInfo(renderingContextClass(), RowOpeningContext::class.java, attributeType())
        fun attributeType(): Class<ATTR>
        fun renderingContextClass(): Class<CTX>
}

/**
 * Cell attribute operation associated with cell rendering context
 * @author Wojciech Mąka
 * @since 0.1.0
 */
interface CellAttributeRenderOperation<CTX : RenderingContext, ATTR : CellAttribute<*>>
    : AttributeOperation<CTX, CellAttribute<*>, ATTR, CellContext> {
    override fun typeInfo(): AttributeOperationTypeInfo<CTX, CellAttribute<*>, ATTR, CellContext> =
        AttributeOperationTypeInfo(renderingContextClass(), CellContext::class.java, attributeType())
    fun attributeType(): Class<ATTR>
    fun renderingContextClass(): Class<CTX>
}

/**
 * Column attribute operation associated with column rendering context
 * @author Wojciech Mąka
 * @since 0.1.0
 */
interface ColumnAttributeRenderOperation<CTX : RenderingContext, ATTR : ColumnAttribute<*>>
    : AttributeOperation<CTX, ColumnAttribute<*>, ATTR, ColumnOpeningContext> {
    override fun typeInfo(): AttributeOperationTypeInfo<CTX, ColumnAttribute<*>, ATTR, ColumnOpeningContext> =
        AttributeOperationTypeInfo(renderingContextClass(), ColumnOpeningContext::class.java, attributeType())
    fun attributeType(): Class<ATTR>
    fun renderingContextClass(): Class<CTX>
}

/**
 * Factory for providing attribute operations for all supported context categories.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
interface AttributeOperationsFactory<CTX : RenderingContext> {
    fun createTableAttributeRenderOperations(): Set<TableAttributeRenderOperation<CTX, out TableAttribute<*>>>? = null
    fun createRowAttributeRenderOperations(): Set<RowAttributeRenderOperation<CTX, out RowAttribute<*>>>? = null
    fun createColumnAttributeRenderOperations(): Set<ColumnAttributeRenderOperation<CTX, out ColumnAttribute<*>>>? = null
    fun createCellAttributeRenderOperations(): Set<CellAttributeRenderOperation<CTX, out CellAttribute<*>>>? = null
}

/**
 * Factory for providing attribute operations for all built-in model attributes.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
interface StandardAttributeRenderOperationsProvider<CTX : RenderingContext> {
    fun createTemplateFileRenderer(): TableAttributeRenderOperation<CTX, TemplateFileAttribute>
    fun createColumnWidthRenderer(): ColumnAttributeRenderOperation<CTX, ColumnWidthAttribute>
    fun createRowHeightRenderer(): RowAttributeRenderOperation<CTX, RowHeightAttribute>
    fun createCellTextStyleRenderer(): CellAttributeRenderOperation<CTX, CellTextStylesAttribute>
    fun createCellBordersRenderer(): CellAttributeRenderOperation<CTX, CellBordersAttribute>
    fun createCellAlignmentRenderer(): CellAttributeRenderOperation<CTX, CellAlignmentAttribute>
    fun createCellBackgroundRenderer(): CellAttributeRenderOperation<CTX, CellBackgroundAttribute>
}

/**
 * Factory for providing attribute operations for all built-in model attributes as well as attribute operations for
 * user defined attributes.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class StandardAttributeOperationsFactory<CTX : RenderingContext>(
    private val standardAttributeRenderers: StandardAttributeRenderOperationsProvider<CTX>,
    private val additionalTableAttributeRenderers: Set<TableAttributeRenderOperation<CTX, out TableAttribute<*>>> = setOf(),
    private val additionalColumnAttributeRenderers: Set<ColumnAttributeRenderOperation<CTX, out ColumnAttribute<*>>> = setOf(),
    private val additionalRowAttributeRenderers: Set<RowAttributeRenderOperation<CTX, out RowAttribute<*>>> = setOf(),
    private val additionalCellAttributeRenderers: Set<CellAttributeRenderOperation<CTX, out CellAttribute<*>>> = setOf()
) : AttributeOperationsFactory<CTX> {

    override fun createTableAttributeRenderOperations(): Set<TableAttributeRenderOperation<CTX, out TableAttribute<*>>> =
        setOf(
            standardAttributeRenderers.createTemplateFileRenderer()
        ) union additionalTableAttributeRenderers

    override fun createRowAttributeRenderOperations(): Set<RowAttributeRenderOperation<CTX, out RowAttribute<*>>> = setOf(
        standardAttributeRenderers.createRowHeightRenderer()
    ) union additionalRowAttributeRenderers

    override fun createColumnAttributeRenderOperations(): Set<ColumnAttributeRenderOperation<CTX, out ColumnAttribute<*>>> =
        setOf(
            standardAttributeRenderers.createColumnWidthRenderer()
        ) union additionalColumnAttributeRenderers

    override fun createCellAttributeRenderOperations(): Set<CellAttributeRenderOperation<CTX, out CellAttribute<*>>> =
        setOf(
            standardAttributeRenderers.createCellTextStyleRenderer(),
            standardAttributeRenderers.createCellBordersRenderer(),
            standardAttributeRenderers.createCellAlignmentRenderer(),
            standardAttributeRenderers.createCellBackgroundRenderer()
        ) union additionalCellAttributeRenderers

}

