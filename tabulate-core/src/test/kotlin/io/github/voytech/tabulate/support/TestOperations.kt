package io.github.voytech.tabulate.support

import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.AttributeClassifier
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.template.operation.AbstractAttributeOperation
import io.github.voytech.tabulate.core.template.operation.AttributedModel
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
import io.github.voytech.tabulate.support.Spy.Companion.operationPriorities
import java.util.*

data class InterceptedContext(
    val operation: InterceptedOperation,
    val context: AttributedModel<out Attribute<*>>,
    val attribute: Attribute<*>? = null
)

class Spy private constructor() {
    private val visitedOperations: LinkedList<InterceptedContext> = LinkedList()

    internal fun track(
        interceptedOperation: InterceptedOperation,
        context: AttributedModel<out Attribute<*>>,
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

abstract class InterceptedRenderOperation<T : Attribute<*>, G : T, E : AttributedModel<T>>(
    protected var spy: Spy? = null, private val clazz: Class<G>, private val contextClass: Class<E>
) : AbstractAttributeOperation<TestRenderingContext, Table<*>, T, G, E>(), InterceptedOperation {

    override fun priority(): Int = operationPriorities[clazz] ?: 1
    override fun renderAttribute(renderingContext: TestRenderingContext, context: E, attribute: G) {
        spy?.track(this, context, attribute)
    }

    override fun attributeClass(): Class<G> = clazz
    override fun renderingContextClass(): Class<TestRenderingContext> = reify()

    override fun operationContextClass(): Class<E> = contextClass

}

class OpenTableTestOperation(private val spy: Spy? = null): OpenTableOperation<TestRenderingContext>, InterceptedOperation {
    override fun render(renderingContext: TestRenderingContext, context: TableOpeningContext) {
        spy?.track(this, context)
    }
}

class OpenColumnTestOperation(private val spy: Spy? = null): OpenColumnOperation<TestRenderingContext>, InterceptedOperation {
    override fun render(renderingContext: TestRenderingContext, context: ColumnOpeningContext) {
        spy?.track(this, context)
    }
}

class OpenRowTestOperation(private val spy: Spy? = null): OpenRowOperation<TestRenderingContext>, InterceptedOperation {
    override fun render(renderingContext: TestRenderingContext, context: RowOpeningContext) {
        spy?.track(this, context)
    }
}

class RenderRowCellTestOperation(private val spy: Spy? = null): RenderRowCellOperation<TestRenderingContext>, InterceptedOperation {
    override fun render(renderingContext: TestRenderingContext, context: CellContext) {
        spy?.track(this, context)
    }
}

class CloseRowTestOperation(private val spy: Spy? = null): CloseRowOperation<TestRenderingContext>, InterceptedOperation {
    override fun render(renderingContext: TestRenderingContext, context: RowClosingContext<*>) {
        spy?.track(this, context)
    }
}

class CloseColumnTestOperation(private val spy: Spy? = null): CloseColumnOperation<TestRenderingContext>, InterceptedOperation {
    override fun render(renderingContext: TestRenderingContext, context: ColumnClosingContext) {
        spy?.track(this, context)
    }
}

class CloseTableTestOperation(private val spy: Spy? = null): CloseTableOperation<TestRenderingContext>, InterceptedOperation {
    override fun render(renderingContext: TestRenderingContext, context: TableClosingContext) {
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
) : InterceptedRenderOperation<RowAttribute<*>, T, RowOpeningContext>(spy, clazz, RowOpeningContext::class.java)

abstract class InterceptedColumnAttributeRenderOperation<T : ColumnAttribute<*>>(
    spy: Spy? = null,
    clazz: Class<T>
) : InterceptedRenderOperation<ColumnAttribute<*>, T, ColumnOpeningContext>(spy, clazz, ColumnOpeningContext::class.java)

abstract class InterceptedTableAttributeRenderOperation<T : TableAttribute<*>>(
    spy: Spy? = null,
    clazz: Class<T>
) : InterceptedRenderOperation<TableAttribute<*>, T, TableOpeningContext>(spy, clazz, TableOpeningContext::class.java)

class CellTextStylesAttributeTestRenderOperation(spy: Spy? = null) :
    InterceptedCellAttributeRenderOperation<CellTextStylesAttribute>(spy, CellTextStylesAttribute::class.java) {
    override fun classifier(): AttributeClassifier<CellAttribute<*>, Table<*>> = CellAttribute::class.java.classify()
}

class CellBordersAttributeTestRenderOperation(spy: Spy? = null) :
    InterceptedCellAttributeRenderOperation<CellBordersAttribute>(spy, CellBordersAttribute::class.java) {
    override fun classifier(): AttributeClassifier<CellAttribute<*>, Table<*>> = CellAttribute::class.java.classify()

}

class CellBackgroundAttributeTestRenderOperation(spy: Spy? = null) :
    InterceptedCellAttributeRenderOperation<CellBackgroundAttribute>(spy, CellBackgroundAttribute::class.java) {
    override fun classifier(): AttributeClassifier<CellAttribute<*>, Table<*>> = CellAttribute::class.java.classify()

}

class CellAlignmentAttributeTestRenderOperation(spy: Spy? = null) :
    InterceptedCellAttributeRenderOperation<CellAlignmentAttribute>(spy, CellAlignmentAttribute::class.java) {
    override fun classifier(): AttributeClassifier<CellAttribute<*>, Table<*>> = CellAttribute::class.java.classify()

}

class ColumnWidthAttributeTestRenderOperation(spy: Spy? = null) :
    InterceptedColumnAttributeRenderOperation<ColumnWidthAttribute>(spy, ColumnWidthAttribute::class.java) {
    override fun classifier(): AttributeClassifier<ColumnAttribute<*>, Table<*>> = ColumnAttribute::class.java.classify()

}

class RowHeightAttributeTestRenderOperation(spy: Spy? = null) :
    InterceptedRowAttributeRenderOperation<RowHeightAttribute>(spy, RowHeightAttribute::class.java) {
    override fun classifier(): AttributeClassifier<RowAttribute<*>, Table<*>> = RowAttribute::class.java.classify()
}

class TemplateFileAttributeTestRenderOperation(spy: Spy? = null) :
    InterceptedTableAttributeRenderOperation<TemplateFileAttribute>(spy, TemplateFileAttribute::class.java) {
    override fun classifier(): AttributeClassifier<TableAttribute<*>, Table<*>> = TableAttribute::class.java.classify()
}

class ShadowingCellTextStylesAttributeTestRenderOperation :
    InterceptedCellAttributeRenderOperation<CellTextStylesAttribute>(null, CellTextStylesAttribute::class.java) {
        init { this.spy = Spy.spy } // It is loaded by service loader. Cannot have constructor context.

    override fun classifier(): AttributeClassifier<CellAttribute<*>, Table<*>> = CellAttribute::class.java.classify()
}

