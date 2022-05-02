package io.github.voytech.tabulate.support

import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.components.table.model.attributes.*
import io.github.voytech.tabulate.components.table.model.attributes.cell.CellAlignmentAttribute
import io.github.voytech.tabulate.components.table.model.attributes.cell.CellBackgroundAttribute
import io.github.voytech.tabulate.components.table.model.attributes.cell.CellBordersAttribute
import io.github.voytech.tabulate.components.table.model.attributes.cell.CellTextStylesAttribute
import io.github.voytech.tabulate.components.table.model.attributes.column.ColumnWidthAttribute
import io.github.voytech.tabulate.components.table.model.attributes.row.RowHeightAttribute
import io.github.voytech.tabulate.components.table.model.attributes.table.TemplateFileAttribute
import io.github.voytech.tabulate.components.table.operation.*
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.AttributeClassifier
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.template.operation.AbstractAttributeOperation
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
) : AbstractAttributeOperation<TestRenderingContext, Table<Any>, T, G, E>(), InterceptedOperation {

    override fun priority(): Int = operationPriorities[clazz] ?: 1
    override fun renderAttribute(renderingContext: TestRenderingContext, context: E, attribute: G) {
        spy?.track(this, context, attribute)
    }

    override fun attributeClass(): Class<G> = clazz
    override fun renderingContextClass(): Class<TestRenderingContext> = reify()

    override fun operationContextClass(): Class<E> = contextClass

}

class OpenTableTestOperation(private val spy: Spy? = null): OpenTableOperation<TestRenderingContext>, InterceptedOperation {
    override fun render(renderingContext: TestRenderingContext, context: TableStart) {
        spy?.track(this, context)
    }
}

class OpenColumnTestOperation(private val spy: Spy? = null): OpenColumnOperation<TestRenderingContext>, InterceptedOperation {
    override fun render(renderingContext: TestRenderingContext, context: ColumnStart) {
        spy?.track(this, context)
    }
}

class OpenRowTestOperation(private val spy: Spy? = null): OpenRowOperation<TestRenderingContext>, InterceptedOperation {
    override fun render(renderingContext: TestRenderingContext, context: RowStart) {
        spy?.track(this, context)
    }
}

class RenderRowCellTestOperation(private val spy: Spy? = null): RenderRowCellOperation<TestRenderingContext>, InterceptedOperation {
    override fun render(renderingContext: TestRenderingContext, context: CellContext) {
        spy?.track(this, context)
    }
}

class CloseRowTestOperation(private val spy: Spy? = null): CloseRowOperation<TestRenderingContext>, InterceptedOperation {
    override fun render(renderingContext: TestRenderingContext, context: RowEnd<*>) {
        spy?.track(this, context)
    }
}

class CloseColumnTestOperation(private val spy: Spy? = null): CloseColumnOperation<TestRenderingContext>, InterceptedOperation {
    override fun render(renderingContext: TestRenderingContext, context: ColumnEnd) {
        spy?.track(this, context)
    }
}

class CloseTableTestOperation(private val spy: Spy? = null): CloseTableOperation<TestRenderingContext>, InterceptedOperation {
    override fun render(renderingContext: TestRenderingContext, context: TableEnd) {
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
    override fun classifier(): AttributeClassifier<CellAttribute<*>, Table<Any>> = CellAttribute::class.java.classify()
}

class CellBordersAttributeTestRenderOperation(spy: Spy? = null) :
    InterceptedCellAttributeRenderOperation<CellBordersAttribute>(spy, CellBordersAttribute::class.java) {
    override fun classifier(): AttributeClassifier<CellAttribute<*>, Table<Any>> = CellAttribute::class.java.classify()

}

class CellBackgroundAttributeTestRenderOperation(spy: Spy? = null) :
    InterceptedCellAttributeRenderOperation<CellBackgroundAttribute>(spy, CellBackgroundAttribute::class.java) {
    override fun classifier(): AttributeClassifier<CellAttribute<*>, Table<Any>> = CellAttribute::class.java.classify()

}

class CellAlignmentAttributeTestRenderOperation(spy: Spy? = null) :
    InterceptedCellAttributeRenderOperation<CellAlignmentAttribute>(spy, CellAlignmentAttribute::class.java) {
    override fun classifier(): AttributeClassifier<CellAttribute<*>, Table<Any>> = CellAttribute::class.java.classify()

}

class ColumnWidthAttributeTestRenderOperation(spy: Spy? = null) :
    InterceptedColumnAttributeRenderOperation<ColumnWidthAttribute>(spy, ColumnWidthAttribute::class.java) {
    override fun classifier(): AttributeClassifier<ColumnAttribute<*>, Table<Any>> = ColumnAttribute::class.java.classify()

}

class RowHeightAttributeTestRenderOperation(spy: Spy? = null) :
    InterceptedRowAttributeRenderOperation<RowHeightAttribute>(spy, RowHeightAttribute::class.java) {
    override fun classifier(): AttributeClassifier<RowAttribute<*>, Table<Any>> = RowAttribute::class.java.classify()
}

class TemplateFileAttributeTestRenderOperation(spy: Spy? = null) :
    InterceptedTableAttributeRenderOperation<TemplateFileAttribute>(spy, TemplateFileAttribute::class.java) {
    override fun classifier(): AttributeClassifier<TableAttribute<*>, Table<Any>> = TableAttribute::class.java.classify()
}

class ShadowingCellTextStylesAttributeTestRenderOperation :
    InterceptedCellAttributeRenderOperation<CellTextStylesAttribute>(null, CellTextStylesAttribute::class.java) {
        init { this.spy = Spy.spy } // It is loaded by service loader. Cannot have constructor context.

    override fun classifier(): AttributeClassifier<CellAttribute<*>, Table<Any>> = CellAttribute::class.java.classify()
}

