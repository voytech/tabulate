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

interface AttributeOperation<T : Attribute<*>> {
    fun attributeType(): Class<out T>
    fun priority(): Int = HIGHER

    companion object {
        const val LOWER = -1
        const val HIGHER = 1
    }
}

interface TableAttributeRenderOperation<T : TableAttribute> : AttributeOperation<T> {
    fun renderAttribute(table: TableContext, attribute: T)
}

interface RowAttributeRenderOperation<E,T : RowAttribute> : AttributeOperation<T> {
    fun renderAttribute(context: RowContext<E>, attribute: T)
}

interface CellAttributeRenderOperation<T : CellAttribute> : AttributeOperation<T> {
    fun renderAttribute(context: RowCellContext, attribute: T)
}

interface ColumnAttributeRenderOperation<T : ColumnAttribute> : AttributeOperation<T> {
    fun renderAttribute(context: ColumnContext, attribute: T)
}

interface AttributeRenderOperationsFactory<T> {
    fun createTableAttributeRenderOperations(): Set<TableAttributeRenderOperation<out TableAttribute>>? = null
    fun createRowAttributeRenderOperations(): Set<RowAttributeRenderOperation<T, out RowAttribute>>? = null
    fun createColumnAttributeRenderOperations(): Set<ColumnAttributeRenderOperation<out ColumnAttribute>>? = null
    fun createCellAttributeRenderOperations(): Set<CellAttributeRenderOperation<out CellAttribute>>? = null
}

abstract class BaseTableAttributeRenderOperation<A, T: TableAttribute>(open val renderingContext: A): TableAttributeRenderOperation<T>

abstract class BaseRowAttributeRenderOperation<A, E, T: RowAttribute>(open val renderingContext: A): RowAttributeRenderOperation<E, T>

abstract class BaseCellAttributeRenderOperation<A, T: CellAttribute>(open val renderingContext: A): CellAttributeRenderOperation<T>

abstract class BaseColumnAttributeRenderOperation<A, T: ColumnAttribute>(open val renderingContext: A): ColumnAttributeRenderOperation<T>

interface StandardAttributeRenderOperationsProvider<A, T> {
    fun createTemplateFileRenderer(renderingContext: A): TableAttributeRenderOperation<TemplateFileAttribute>
    fun createColumnWidthRenderer(renderingContext: A): ColumnAttributeRenderOperation<ColumnWidthAttribute>
    fun createRowHeightRenderer(renderingContext: A): RowAttributeRenderOperation<T, RowHeightAttribute>
    fun createCellTextStyleRenderer(renderingContext: A): CellAttributeRenderOperation<CellTextStylesAttribute>
    fun createCellBordersRenderer(renderingContext: A): CellAttributeRenderOperation<CellBordersAttribute>
    fun createCellAlignmentRenderer(renderingContext: A): CellAttributeRenderOperation<CellAlignmentAttribute>
    fun createCellBackgroundRenderer(renderingContext: A): CellAttributeRenderOperation<CellBackgroundAttribute>
}

class StandardAttributeRenderOperationsFactory<A,T>(
    private val renderingContext: A,
    private val standardAttributeRenderers: StandardAttributeRenderOperationsProvider<A,T>,
    private val additionalTableAttributeRenderers:  Set<TableAttributeRenderOperation<out TableAttribute>> = setOf(),
    private val additionalColumnAttributeRenderers:  Set<ColumnAttributeRenderOperation<out ColumnAttribute>> = setOf(),
    private val additionalRowAttributeRenderers:  Set<RowAttributeRenderOperation<T, out RowAttribute>> = setOf(),
    private val additionalCellAttributeRenderers:  Set<CellAttributeRenderOperation<out CellAttribute>> = setOf()) : AttributeRenderOperationsFactory<T> {

    override fun createTableAttributeRenderOperations(): Set<TableAttributeRenderOperation<out TableAttribute>> = setOf(
        standardAttributeRenderers.createTemplateFileRenderer(renderingContext)
    ) union additionalTableAttributeRenderers

    override fun createRowAttributeRenderOperations(): Set<RowAttributeRenderOperation<T, out RowAttribute>> = setOf(
        standardAttributeRenderers.createRowHeightRenderer(renderingContext)
    ) union additionalRowAttributeRenderers

    override fun createColumnAttributeRenderOperations(): Set<ColumnAttributeRenderOperation<out ColumnAttribute>> = setOf(
        standardAttributeRenderers.createColumnWidthRenderer(renderingContext)
    ) union additionalColumnAttributeRenderers

    override fun createCellAttributeRenderOperations(): Set<CellAttributeRenderOperation<out CellAttribute>> = setOf(
        standardAttributeRenderers.createCellTextStyleRenderer(renderingContext),
        standardAttributeRenderers.createCellBordersRenderer(renderingContext),
        standardAttributeRenderers.createCellAlignmentRenderer(renderingContext),
        standardAttributeRenderers.createCellBackgroundRenderer(renderingContext)
    ) union additionalCellAttributeRenderers

}

