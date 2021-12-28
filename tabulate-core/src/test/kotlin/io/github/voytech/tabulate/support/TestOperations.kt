package io.github.voytech.tabulate.support

import io.github.voytech.tabulate.model.attributes.*
import io.github.voytech.tabulate.model.attributes.cell.CellAlignmentAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellBackgroundAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellBordersAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellTextStylesAttribute
import io.github.voytech.tabulate.model.attributes.column.ColumnWidthAttribute
import io.github.voytech.tabulate.model.attributes.row.RowHeightAttribute
import io.github.voytech.tabulate.model.attributes.table.TemplateFileAttribute
import io.github.voytech.tabulate.template.operations.*
import java.util.*

data class InterceptedContext(
    val operation: InterceptedOperation,
    val context: ModelAttributeAccessor<out Attribute<*>>,
    val attribute: Attribute<*>? = null
)

class Spy private constructor() {
    private val visitedOperations: LinkedList<InterceptedContext> = LinkedList()

    internal fun track(
        interceptedOperation: InterceptedOperation,
        context: ModelAttributeAccessor<out Attribute<*>>,
        attribute: Attribute<*>? = null
    ) = visitedOperations.add(InterceptedContext(interceptedOperation, context, attribute))

    fun readHistory(): Iterator<InterceptedContext> =
        LinkedList(visitedOperations).iterator().also { visitedOperations.clear() }

    companion object {
        val spy: Spy = Spy()
    }
}

interface InterceptedOperation

abstract class InterceptedRenderOperation<T : Attribute<*>, G : T, E : ModelAttributeAccessor<T>>(
    private val spy: Spy? = null, private val clazz: Class<G>, private val priority: Int = 1
) : AttributeOperation<TestRenderingContext, T, G, E>, InterceptedOperation {
    override fun priority(): Int = priority
    override fun attributeType(): Class<G> = clazz
    override fun renderAttribute(renderingContext: TestRenderingContext, context: E, attribute: G) {
        spy?.track(this, context, attribute)
    }
}

class TableExportTestOperations(private val spy: Spy? = null) : TableExportOperations<TestRenderingContext>, InterceptedOperation {

    override fun createTable(renderingContext: TestRenderingContext, context: TableContext) {
        spy?.track(this, context)
    }

    override fun beginRow(renderingContext: TestRenderingContext, context: RowContext) {
        spy?.track(this, context)
    }

    override fun renderRowCell(renderingContext: TestRenderingContext, context: RowCellContext) {
        spy?.track(this, context)
    }

    override fun <T> endRow(renderingContext: TestRenderingContext, context: RowContextWithCells<T>) {
        spy?.track(this, context)
    }

}

abstract class InterceptedCellAttributeRenderOperation<T : CellAttribute<*>>(
    spy: Spy? = null,
    clazz: Class<T>,
    priority: Int = 1
) : InterceptedRenderOperation<CellAttribute<*>, T, RowCellContext>(spy, clazz, priority),
    CellAttributeRenderOperation<TestRenderingContext,T>

abstract class InterceptedRowAttributeRenderOperation<T : RowAttribute<*>>(
    spy: Spy? = null,
    clazz: Class<T>,
    priority: Int = 1
) : InterceptedRenderOperation<RowAttribute<*>, T, RowContext>(spy, clazz, priority),
    RowAttributeRenderOperation<TestRenderingContext,T>

abstract class InterceptedColumnAttributeRenderOperation<T : ColumnAttribute<*>>(
    spy: Spy? = null,
    clazz: Class<T>,
    priority: Int = 1
) : InterceptedRenderOperation<ColumnAttribute<*>, T, ColumnContext>(spy, clazz, priority),
    ColumnAttributeRenderOperation<TestRenderingContext,T>

abstract class InterceptedTableAttributeRenderOperation<T : TableAttribute<*>>(
    spy: Spy? = null,
    clazz: Class<T>,
    priority: Int = 1
) : InterceptedRenderOperation<TableAttribute<*>, T, TableContext>(spy, clazz, priority),
    TableAttributeRenderOperation<TestRenderingContext,T>

class CellTextStylesAttributeTestRenderOperation(spy: Spy? = null, priority: Int = 1) :
    InterceptedCellAttributeRenderOperation<CellTextStylesAttribute>(spy, CellTextStylesAttribute::class.java, priority)

class CellBordersAttributeTestRenderOperation(spy: Spy? = null, priority: Int = 1) :
    InterceptedCellAttributeRenderOperation<CellBordersAttribute>(spy, CellBordersAttribute::class.java, priority)

class CellBackgroundAttributeTestRenderOperation(spy: Spy? = null, priority: Int = 1) :
    InterceptedCellAttributeRenderOperation<CellBackgroundAttribute>(spy, CellBackgroundAttribute::class.java, priority)

class CellAlignmentAttributeTestRenderOperation(spy: Spy? = null, priority: Int = 1) :
    InterceptedCellAttributeRenderOperation<CellAlignmentAttribute>(spy, CellAlignmentAttribute::class.java, priority)

class ColumnWidthAttributeTestRenderOperation(spy: Spy? = null, priority: Int = 1) :
    InterceptedColumnAttributeRenderOperation<ColumnWidthAttribute>(spy, ColumnWidthAttribute::class.java, priority)

class RowHeightAttributeTestRenderOperation(spy: Spy? = null, priority: Int = 1) :
    InterceptedRowAttributeRenderOperation<RowHeightAttribute>(spy, RowHeightAttribute::class.java, priority)

class TemplateFileAttributeTestRenderOperation(spy: Spy? = null, priority: Int = 1) :
    InterceptedTableAttributeRenderOperation<TemplateFileAttribute>(spy, TemplateFileAttribute::class.java, priority)
