package io.github.voytech.tabulate.support

import io.github.voytech.tabulate.model.attributes.*
import io.github.voytech.tabulate.model.attributes.cell.CellAlignmentAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellBackgroundAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellBordersAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellTextStylesAttribute
import io.github.voytech.tabulate.model.attributes.column.ColumnWidthAttribute
import io.github.voytech.tabulate.model.attributes.row.RowHeightAttribute
import io.github.voytech.tabulate.model.attributes.table.TemplateFileAttribute
import io.github.voytech.tabulate.support.Spy.Companion.operationPriorities
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

    fun reset() {
        visitedOperations.clear()
        operationPriorities.clear()
    }

    companion object {
        val spy: Spy = Spy()
        val operationPriorities: MutableMap<Class<out Attribute<*>>,Int> = mutableMapOf()
    }
}

interface InterceptedOperation

abstract class InterceptedRenderOperation<T : Attribute<*>, G : T, E : ModelAttributeAccessor<T>>(
    private val spy: Spy? = null, private val clazz: Class<G>
) : AttributeOperation<TestRenderingContext, T, G, E>, InterceptedOperation {
    override fun priority(): Int = operationPriorities[clazz] ?: 1
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
    clazz: Class<T>
) : InterceptedRenderOperation<CellAttribute<*>, T, RowCellContext>(spy, clazz),
    CellAttributeRenderOperation<TestRenderingContext,T>

abstract class InterceptedRowAttributeRenderOperation<T : RowAttribute<*>>(
    spy: Spy? = null,
    clazz: Class<T>
) : InterceptedRenderOperation<RowAttribute<*>, T, RowContext>(spy, clazz),
    RowAttributeRenderOperation<TestRenderingContext,T>

abstract class InterceptedColumnAttributeRenderOperation<T : ColumnAttribute<*>>(
    spy: Spy? = null,
    clazz: Class<T>
) : InterceptedRenderOperation<ColumnAttribute<*>, T, ColumnContext>(spy, clazz),
    ColumnAttributeRenderOperation<TestRenderingContext,T>

abstract class InterceptedTableAttributeRenderOperation<T : TableAttribute<*>>(
    spy: Spy? = null,
    clazz: Class<T>
) : InterceptedRenderOperation<TableAttribute<*>, T, TableContext>(spy, clazz),
    TableAttributeRenderOperation<TestRenderingContext,T>

class CellTextStylesAttributeTestRenderOperation(spy: Spy? = null) :
    InterceptedCellAttributeRenderOperation<CellTextStylesAttribute>(spy, CellTextStylesAttribute::class.java)

class CellBordersAttributeTestRenderOperation(spy: Spy? = null) :
    InterceptedCellAttributeRenderOperation<CellBordersAttribute>(spy, CellBordersAttribute::class.java)

class CellBackgroundAttributeTestRenderOperation(spy: Spy? = null) :
    InterceptedCellAttributeRenderOperation<CellBackgroundAttribute>(spy, CellBackgroundAttribute::class.java)

class CellAlignmentAttributeTestRenderOperation(spy: Spy? = null) :
    InterceptedCellAttributeRenderOperation<CellAlignmentAttribute>(spy, CellAlignmentAttribute::class.java)

class ColumnWidthAttributeTestRenderOperation(spy: Spy? = null) :
    InterceptedColumnAttributeRenderOperation<ColumnWidthAttribute>(spy, ColumnWidthAttribute::class.java)

class RowHeightAttributeTestRenderOperation(spy: Spy? = null) :
    InterceptedRowAttributeRenderOperation<RowHeightAttribute>(spy, RowHeightAttribute::class.java)

class TemplateFileAttributeTestRenderOperation(spy: Spy? = null) :
    InterceptedTableAttributeRenderOperation<TemplateFileAttribute>(spy, TemplateFileAttribute::class.java)
