package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.model.attributes.Attribute
import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.model.attributes.alias.ColumnAttribute
import io.github.voytech.tabulate.model.attributes.alias.RowAttribute
import io.github.voytech.tabulate.model.attributes.alias.TableAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellAlignmentAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellBackgroundAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellBordersAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellTextStylesAttribute
import io.github.voytech.tabulate.model.attributes.column.ColumnWidthAttribute
import io.github.voytech.tabulate.model.attributes.row.RowHeightAttribute
import io.github.voytech.tabulate.model.attributes.table.TemplateFileAttribute
import io.github.voytech.tabulate.template.context.RenderingContext

private typealias AttributeCategory = Attribute<*>

interface AttributeOperation<CTX: RenderingContext, ATTR_CAT : AttributeCategory, ATTR: ATTR_CAT, E: ModelAttributeAccessor<ATTR_CAT>> {
    fun attributeType(): Class<ATTR>
    fun priority(): Int = DEFAULT
    fun renderAttribute(renderingContext: CTX, context: E, attribute: ATTR)

    companion object {
        const val LOWEST = Int.MIN_VALUE
        const val LOWER = -1
        const val DEFAULT = 1
    }
}

interface TableAttributeRenderOperation<CTX: RenderingContext, ATTR : TableAttribute>
    : AttributeOperation<CTX, TableAttribute, ATTR, TableContext>

interface RowAttributeRenderOperation<CTX: RenderingContext, ATTR : RowAttribute>
    : AttributeOperation<CTX, RowAttribute, ATTR, RowContext>

interface CellAttributeRenderOperation<CTX: RenderingContext, ATTR : CellAttribute>
    : AttributeOperation<CTX, CellAttribute, ATTR, RowCellContext>

interface ColumnAttributeRenderOperation<CTX: RenderingContext, ATTR : ColumnAttribute>
    : AttributeOperation<CTX, ColumnAttribute, ATTR, ColumnContext>


interface AttributeRenderOperationsFactory<CTX: RenderingContext> {
    fun createTableAttributeRenderOperations(): Set<TableAttributeRenderOperation<CTX, out TableAttribute>>? = null
    fun createRowAttributeRenderOperations(): Set<RowAttributeRenderOperation<CTX, out RowAttribute>>? = null
    fun createColumnAttributeRenderOperations(): Set<ColumnAttributeRenderOperation<CTX, out ColumnAttribute>>? = null
    fun createCellAttributeRenderOperations(): Set<CellAttributeRenderOperation<CTX, out CellAttribute>>? = null
}

interface StandardAttributeRenderOperationsProvider<CTX: RenderingContext> {
    fun createTemplateFileRenderer(): TableAttributeRenderOperation<CTX, TemplateFileAttribute>
    fun createColumnWidthRenderer(): ColumnAttributeRenderOperation<CTX, ColumnWidthAttribute>
    fun createRowHeightRenderer(): RowAttributeRenderOperation<CTX, RowHeightAttribute>
    fun createCellTextStyleRenderer(): CellAttributeRenderOperation<CTX, CellTextStylesAttribute>
    fun createCellBordersRenderer(): CellAttributeRenderOperation<CTX, CellBordersAttribute>
    fun createCellAlignmentRenderer(): CellAttributeRenderOperation<CTX, CellAlignmentAttribute>
    fun createCellBackgroundRenderer(): CellAttributeRenderOperation<CTX, CellBackgroundAttribute>
}

class StandardAttributeRenderOperationsFactory<CTX: RenderingContext>(
    private val standardAttributeRenderers: StandardAttributeRenderOperationsProvider<CTX>,
    private val additionalTableAttributeRenderers:  Set<TableAttributeRenderOperation<CTX, out TableAttribute>> = setOf(),
    private val additionalColumnAttributeRenderers:  Set<ColumnAttributeRenderOperation<CTX, out ColumnAttribute>> = setOf(),
    private val additionalRowAttributeRenderers:  Set<RowAttributeRenderOperation<CTX, out RowAttribute>> = setOf(),
    private val additionalCellAttributeRenderers:  Set<CellAttributeRenderOperation<CTX, out CellAttribute>> = setOf()) : AttributeRenderOperationsFactory<CTX> {

    override fun createTableAttributeRenderOperations(): Set<TableAttributeRenderOperation<CTX, out TableAttribute>> = setOf(
        standardAttributeRenderers.createTemplateFileRenderer()
    ) union additionalTableAttributeRenderers

    override fun createRowAttributeRenderOperations(): Set<RowAttributeRenderOperation<CTX, out RowAttribute>> = setOf(
        standardAttributeRenderers.createRowHeightRenderer()
    ) union additionalRowAttributeRenderers

    override fun createColumnAttributeRenderOperations(): Set<ColumnAttributeRenderOperation<CTX, out ColumnAttribute>> = setOf(
        standardAttributeRenderers.createColumnWidthRenderer()
    ) union additionalColumnAttributeRenderers

    override fun createCellAttributeRenderOperations(): Set<CellAttributeRenderOperation<CTX, out CellAttribute>> = setOf(
        standardAttributeRenderers.createCellTextStyleRenderer(),
        standardAttributeRenderers.createCellBordersRenderer(),
        standardAttributeRenderers.createCellAlignmentRenderer(),
        standardAttributeRenderers.createCellBackgroundRenderer()
    ) union additionalCellAttributeRenderers

}

