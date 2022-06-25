package io.github.voytech.tabulate.support

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
import io.github.voytech.tabulate.components.table.operation.*
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.template.operation.AttributeOperation
import io.github.voytech.tabulate.core.template.operation.AttributedContext
import io.github.voytech.tabulate.support.Spy.Companion.operationPriorities
import java.util.*

data class InterceptedContext(
    val operation: InterceptedOperation,
    val context: AttributedContext<out Attribute<*>>,
    val attribute: Attribute<*>? = null
)

class Spy private constructor() {
    private val visitedOperations: LinkedList<InterceptedContext> = LinkedList()

    internal fun track(
        interceptedOperation: InterceptedOperation,
        context: AttributedContext<out Attribute<*>>,
        attribute: Attribute<*>? = null
    ) = visitedOperations.add(InterceptedContext(interceptedOperation, context, attribute))

    fun readHistory(): Iterator<InterceptedContext> =
        LinkedList(visitedOperations).iterator().also {
            visitedOperations.clear()
            operationPriorities.clear()
        }

    companion object {
        val spy: Spy = Spy()
        val operationPriorities: MutableMap<Class<out Attribute<*>>,Int> = mutableMapOf()
    }
}

interface InterceptedOperation

abstract class InterceptedRenderOperation<T : Attribute<*>, G : T, E : AttributedContext<T>>(
    protected var spy: Spy? = null, private val clazz: Class<G>, private val contextClass: Class<E>
) : AttributeOperation<TestRenderingContext, T, G, E>, InterceptedOperation {

    override fun priority(): Int = operationPriorities[clazz] ?: 1
    override operator fun invoke(renderingContext: TestRenderingContext, context: E, attribute: G) {
        spy?.track(this, context, attribute)
    }

}

class StartTableTestOperation(private val spy: Spy? = null): StartTableOperation<TestRenderingContext>, InterceptedOperation {
    override operator fun invoke(renderingContext: TestRenderingContext, context: TableStart) {
        spy?.track(this, context)
    }
}

class StartColumnTestOperation(private val spy: Spy? = null): StartColumnOperation<TestRenderingContext>, InterceptedOperation {
    override operator fun invoke(renderingContext: TestRenderingContext, context: ColumnStart) {
        spy?.track(this, context)
    }
}

class StartRowTestOperation(private val spy: Spy? = null): StartRowOperation<TestRenderingContext>, InterceptedOperation {
    override operator fun invoke(renderingContext: TestRenderingContext, context: RowStart) {
        spy?.track(this, context)
    }
}

class RenderRowCellTestOperation(private val spy: Spy? = null): RenderRowCellOperation<TestRenderingContext>, InterceptedOperation {
    override operator fun invoke(renderingContext: TestRenderingContext, context: CellContext) {
        spy?.track(this, context)
    }
}

class EndRowTestOperation(private val spy: Spy? = null): EndRowOperation<TestRenderingContext>, InterceptedOperation {
    override operator fun invoke(renderingContext: TestRenderingContext, context: RowEnd<*>) {
        spy?.track(this, context)
    }
}

class EndColumnTestOperation(private val spy: Spy? = null): EndColumnOperation<TestRenderingContext>, InterceptedOperation {
    override operator fun invoke(renderingContext: TestRenderingContext, context: ColumnEnd) {
        spy?.track(this, context)
    }
}

class EndTableTestOperation(private val spy: Spy? = null): EndTableOperation<TestRenderingContext>, InterceptedOperation {
    override operator fun invoke(renderingContext: TestRenderingContext, context: TableEnd) {
        spy?.track(this, context)
    }
}

abstract class InterceptedCellAttributeRenderOperation<T : CellAttribute<*>>(
    spy: Spy? = null,
    clazz: Class<T>
) : InterceptedRenderOperation<CellAttribute<*>, T, CellContext>(spy, clazz, CellContext::class.java)


abstract class InterceptedRowAttributeRenderOperation<T : RowAttribute<*>>(
    spy: Spy? = null,
    clazz: Class<T>
) : InterceptedRenderOperation<RowAttribute<*>, T, RowStart>(spy, clazz, RowStart::class.java)

abstract class InterceptedColumnAttributeRenderOperation<T : ColumnAttribute<*>>(
    spy: Spy? = null,
    clazz: Class<T>
) : InterceptedRenderOperation<ColumnAttribute<*>, T, ColumnStart>(spy, clazz, ColumnStart::class.java)

abstract class InterceptedTableAttributeRenderOperation<T : TableAttribute<*>>(
    spy: Spy? = null,
    clazz: Class<T>
) : InterceptedRenderOperation<TableAttribute<*>, T, TableStart>(spy, clazz, TableStart::class.java)

class CellTextStylesAttributeTestRenderOperation(spy: Spy? = null) :
    InterceptedCellAttributeRenderOperation<CellTextStylesAttribute>(spy, CellTextStylesAttribute::class.java) {
}

class CellBordersAttributeTestRenderOperation(spy: Spy? = null) :
    InterceptedCellAttributeRenderOperation<CellBordersAttribute>(spy, CellBordersAttribute::class.java) {
}

class CellBackgroundAttributeTestRenderOperation(spy: Spy? = null) :
    InterceptedCellAttributeRenderOperation<CellBackgroundAttribute>(spy, CellBackgroundAttribute::class.java) {
}

class CellAlignmentAttributeTestRenderOperation(spy: Spy? = null) :
    InterceptedCellAttributeRenderOperation<CellAlignmentAttribute>(spy, CellAlignmentAttribute::class.java) {
}

class ColumnWidthAttributeTestRenderOperation(spy: Spy? = null) :
    InterceptedColumnAttributeRenderOperation<ColumnWidthAttribute>(spy, ColumnWidthAttribute::class.java) {
}

class RowHeightAttributeTestRenderOperation(spy: Spy? = null) :
    InterceptedRowAttributeRenderOperation<RowHeightAttribute>(spy, RowHeightAttribute::class.java) {}

class TemplateFileAttributeTestRenderOperation(spy: Spy? = null) :
    InterceptedTableAttributeRenderOperation<TemplateFileAttribute>(spy, TemplateFileAttribute::class.java) {
}
