package io.github.voytech.tabulate.components.table.operation

import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.components.table.model.attributes.CellAttribute
import io.github.voytech.tabulate.components.table.model.attributes.ColumnAttribute
import io.github.voytech.tabulate.components.table.model.attributes.RowAttribute
import io.github.voytech.tabulate.components.table.model.attributes.TableAttribute
import io.github.voytech.tabulate.components.table.model.attributes.cell.CellAlignmentAttribute
import io.github.voytech.tabulate.components.table.model.attributes.cell.CellBackgroundAttribute
import io.github.voytech.tabulate.components.table.model.attributes.cell.CellBordersAttribute
import io.github.voytech.tabulate.components.table.model.attributes.cell.CellTextStylesAttribute
import io.github.voytech.tabulate.components.table.model.attributes.column.ColumnWidthAttribute
import io.github.voytech.tabulate.components.table.model.attributes.row.RowHeightAttribute
import io.github.voytech.tabulate.components.table.model.attributes.table.TemplateFileAttribute
import io.github.voytech.tabulate.core.model.AttributeClassifier
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.operation.AbstractAttributeOperation
import io.github.voytech.tabulate.core.template.operation.AttributeOperation

/**
 * Table attribute operation associated with table rendering context
 * @author Wojciech Mąka
 * @since 0.1.0
 */
abstract class TableAttributeRenderOperation<CTX : RenderingContext, ATTR : TableAttribute<*>, AC : TableContext>(private val context: Class<AC>) :
    AbstractAttributeOperation<CTX, Table<*>, TableAttribute<*>, ATTR, AC>() {
    override fun operationContextClass(): Class<AC> = context
    override fun classifier(): AttributeClassifier<TableAttribute<*>, Table<*>> = AttributeClassifier.classify()
}

/**
 * Row attribute operation associated with row rendering context
 * @author Wojciech Mąka
 * @since 0.1.0
 */
abstract class RowAttributeRenderOperation<CTX : RenderingContext, ATTR : RowAttribute<*>, AC : RowContext>(private val context: Class<AC>) :
    AbstractAttributeOperation<CTX, Table<*>, RowAttribute<*>, ATTR, AC>() {
    override fun operationContextClass(): Class<AC> = context
    override fun classifier(): AttributeClassifier<RowAttribute<*>, Table<*>> = AttributeClassifier.classify()
}


/**
 * Cell attribute operation associated with cell rendering context
 * @author Wojciech Mąka
 * @since 0.1.0
 */
abstract class CellAttributeRenderOperation<CTX : RenderingContext, ATTR : CellAttribute<*>> :
    AbstractAttributeOperation<CTX, Table<*>, CellAttribute<*>, ATTR, CellContext>() {
    override fun operationContextClass(): Class<CellContext> = reify()
    override fun classifier(): AttributeClassifier<CellAttribute<*>, Table<*>> = AttributeClassifier.classify()
}

/**
 * Column attribute operation associated with column rendering context
 * @author Wojciech Mąka
 * @since 0.1.0
 */
abstract class ColumnAttributeRenderOperation<CTX : RenderingContext, ATTR : ColumnAttribute<*>, AC : ColumnContext>(private val context: Class<AC>) :
    AbstractAttributeOperation<CTX, Table<*>, ColumnAttribute<*>, ATTR, AC>() {
    override fun operationContextClass(): Class<AC> = context

    override fun classifier(): AttributeClassifier<ColumnAttribute<*>, Table<*>> = AttributeClassifier.classify()
}

/**
 * Factory for providing attribute operations for all supported context categories.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
interface TableAttributesOperationsFactory<CTX : RenderingContext> {
    fun createTableAttributeRenderOperations(): Set<AttributeOperation<CTX, Table<*>, TableAttribute<*>, *, *>>? = null
    fun createRowAttributeRenderOperations(): Set<AttributeOperation<CTX, Table<*>, RowAttribute<*>, *, *>>? = null
    fun createColumnAttributeRenderOperations(): Set<AttributeOperation<CTX, Table<*>, ColumnAttribute<*>, *, *>>? = null
    fun createCellAttributeRenderOperations(): Set<AttributeOperation<CTX, Table<*>, CellAttribute<*>, *, *>>? = null
}

/**
 * Factory for providing attribute operations for all built-in model attributes.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
interface StandardAttributeRenderOperationsProvider<CTX : RenderingContext> {
    fun createTemplateFileRenderer(): TableAttributeRenderOperation<CTX, TemplateFileAttribute, *>
    fun createColumnWidthRenderer(): ColumnAttributeRenderOperation<CTX, ColumnWidthAttribute, *>
    fun createRowHeightRenderer(): RowAttributeRenderOperation<CTX, RowHeightAttribute, *>
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
    private val additionalTableAttributeRenderers: Set<TableAttributeRenderOperation<CTX, *, *>> = setOf(),
    private val additionalColumnAttributeRenderers: Set<ColumnAttributeRenderOperation<CTX, *, *>> = setOf(),
    private val additionalRowAttributeRenderers: Set<RowAttributeRenderOperation<CTX, *, *>> = setOf(),
    private val additionalCellAttributeRenderers: Set<CellAttributeRenderOperation<CTX, *>> = setOf()
) : TableAttributesOperationsFactory<CTX> {

    override fun createTableAttributeRenderOperations(): Set<TableAttributeRenderOperation<CTX, *, *>> =
        setOf(
            standardAttributeRenderers.createTemplateFileRenderer()
        ) union additionalTableAttributeRenderers

    override fun createRowAttributeRenderOperations(): Set<RowAttributeRenderOperation<CTX, *, *>> = setOf(
        standardAttributeRenderers.createRowHeightRenderer()
    ) union additionalRowAttributeRenderers

    override fun createColumnAttributeRenderOperations(): Set<ColumnAttributeRenderOperation<CTX, *, *>> =
        setOf(
            standardAttributeRenderers.createColumnWidthRenderer()
        ) union additionalColumnAttributeRenderers

    override fun createCellAttributeRenderOperations(): Set<CellAttributeRenderOperation<CTX, *>> =
        setOf(
            standardAttributeRenderers.createCellTextStyleRenderer(),
            standardAttributeRenderers.createCellBordersRenderer(),
            standardAttributeRenderers.createCellAlignmentRenderer(),
            standardAttributeRenderers.createCellBackgroundRenderer()
        ) union additionalCellAttributeRenderers

}

