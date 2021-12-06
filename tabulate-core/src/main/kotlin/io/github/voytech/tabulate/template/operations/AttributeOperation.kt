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

interface AttributeOperation<T : Attribute<*>> {
    fun attributeType(): Class<out T>
    fun priority(): Int = HIGHER

    companion object {
        const val LOWER = -1
        const val HIGHER = 1
    }
}

interface TableAttributeRenderOperation<CTX: RenderingContext, T : TableAttribute> : AttributeOperation<T> {
    fun renderAttribute(renderingContext: CTX, table: TableContext, attribute: T)
}

interface RowAttributeRenderOperation<CTX: RenderingContext, E, T : RowAttribute> : AttributeOperation<T> {
    fun renderAttribute(renderingContext: CTX, context: RowContext<E>, attribute: T)
}

interface CellAttributeRenderOperation<CTX: RenderingContext, T : CellAttribute> : AttributeOperation<T> {
    fun renderAttribute(renderingContext: CTX, context: RowCellContext, attribute: T)
}

interface ColumnAttributeRenderOperation<CTX: RenderingContext, T : ColumnAttribute> : AttributeOperation<T> {
    fun renderAttribute(renderingContext: CTX, context: ColumnContext, attribute: T)
}

interface AttributeRenderOperationsFactory<CTX: RenderingContext,T> {
    fun createTableAttributeRenderOperations(): Set<TableAttributeRenderOperation<CTX, out TableAttribute>>? = null
    fun createRowAttributeRenderOperations(): Set<RowAttributeRenderOperation<CTX, T, out RowAttribute>>? = null
    fun createColumnAttributeRenderOperations(): Set<ColumnAttributeRenderOperation<CTX, out ColumnAttribute>>? = null
    fun createCellAttributeRenderOperations(): Set<CellAttributeRenderOperation<CTX, out CellAttribute>>? = null
}

interface StandardAttributeRenderOperationsProvider<CTX: RenderingContext, T> {
    fun createTemplateFileRenderer(): TableAttributeRenderOperation<CTX, TemplateFileAttribute>
    fun createColumnWidthRenderer(): ColumnAttributeRenderOperation<CTX, ColumnWidthAttribute>
    fun createRowHeightRenderer(): RowAttributeRenderOperation<CTX, T, RowHeightAttribute>
    fun createCellTextStyleRenderer(): CellAttributeRenderOperation<CTX, CellTextStylesAttribute>
    fun createCellBordersRenderer(): CellAttributeRenderOperation<CTX, CellBordersAttribute>
    fun createCellAlignmentRenderer(): CellAttributeRenderOperation<CTX, CellAlignmentAttribute>
    fun createCellBackgroundRenderer(): CellAttributeRenderOperation<CTX, CellBackgroundAttribute>
}

class StandardAttributeRenderOperationsFactory<CTX: RenderingContext,T>(
    private val standardAttributeRenderers: StandardAttributeRenderOperationsProvider<CTX, T>,
    private val additionalTableAttributeRenderers:  Set<TableAttributeRenderOperation<CTX, out TableAttribute>> = setOf(),
    private val additionalColumnAttributeRenderers:  Set<ColumnAttributeRenderOperation<CTX, out ColumnAttribute>> = setOf(),
    private val additionalRowAttributeRenderers:  Set<RowAttributeRenderOperation<CTX, T, out RowAttribute>> = setOf(),
    private val additionalCellAttributeRenderers:  Set<CellAttributeRenderOperation<CTX, out CellAttribute>> = setOf()) : AttributeRenderOperationsFactory<CTX,T> {

    override fun createTableAttributeRenderOperations(): Set<TableAttributeRenderOperation<CTX, out TableAttribute>> = setOf(
        standardAttributeRenderers.createTemplateFileRenderer()
    ) union additionalTableAttributeRenderers

    override fun createRowAttributeRenderOperations(): Set<RowAttributeRenderOperation<CTX, T, out RowAttribute>> = setOf(
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

