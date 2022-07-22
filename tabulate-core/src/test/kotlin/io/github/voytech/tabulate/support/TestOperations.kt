package io.github.voytech.tabulate.support

import io.github.voytech.tabulate.components.table.model.attributes.table.TemplateFileAttribute
import io.github.voytech.tabulate.components.table.operation.*
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.attributes.*
import io.github.voytech.tabulate.core.template.operation.AttributeOperation
import io.github.voytech.tabulate.core.template.operation.AttributedContext
import java.util.*

data class InterceptedContext(
    val operation: InterceptedOperation,
    val context: AttributedContext,
    val attribute: Attribute<*>? = null
)

class Spy private constructor() {
    private val visitedOperations: LinkedList<InterceptedContext> = LinkedList()

    internal fun track(
        interceptedOperation: InterceptedOperation,
        context: AttributedContext,
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

abstract class InterceptedRenderOperation<T : Attribute<T>, E : AttributedContext>(
    private var spy: Spy? = null, private val clazz: Class<T>, private val contextClass: Class<E>
) : AttributeOperation<TestRenderingContext, T, E>, InterceptedOperation {

    override operator fun invoke(renderingContext: TestRenderingContext, context: E, attribute: T) {
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

class EndRowTestOperation<T: Any>(private val spy: Spy? = null): EndRowOperation<TestRenderingContext,T>, InterceptedOperation {
    override operator fun invoke(renderingContext: TestRenderingContext, context: RowEnd<T>) {
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

abstract class InterceptedCellAttributeRenderOperation<T : Attribute<T>>(
    spy: Spy? = null,
    clazz: Class<T>
) : InterceptedRenderOperation<T, CellContext>(spy, clazz, CellContext::class.java)


abstract class InterceptedRowAttributeRenderOperation<T : Attribute<T>>(
    spy: Spy? = null,
    clazz: Class<T>
) : InterceptedRenderOperation<T, RowStart>(spy, clazz, RowStart::class.java)

abstract class InterceptedColumnAttributeRenderOperation<T : Attribute<T>>(
    spy: Spy? = null,
    clazz: Class<T>
) : InterceptedRenderOperation<T, ColumnStart>(spy, clazz, ColumnStart::class.java)

abstract class InterceptedTableAttributeRenderOperation<T : Attribute<T>>(
    spy: Spy? = null,
    clazz: Class<T>
) : InterceptedRenderOperation<T, TableStart>(spy, clazz, TableStart::class.java)

class CellTextStylesAttributeTestRenderOperation(spy: Spy? = null) :
    InterceptedCellAttributeRenderOperation<TextStylesAttribute>(spy, TextStylesAttribute::class.java)

class CellBordersAttributeTestRenderOperation(spy: Spy? = null) :
    InterceptedCellAttributeRenderOperation<BordersAttribute>(spy, BordersAttribute::class.java)

class CellBackgroundAttributeTestRenderOperation(spy: Spy? = null) :
    InterceptedCellAttributeRenderOperation<BackgroundAttribute>(spy, BackgroundAttribute::class.java)

class CellAlignmentAttributeTestRenderOperation(spy: Spy? = null) :
    InterceptedCellAttributeRenderOperation<AlignmentAttribute>(spy, AlignmentAttribute::class.java)

class ColumnWidthAttributeTestRenderOperation(spy: Spy? = null) :
    InterceptedColumnAttributeRenderOperation<WidthAttribute>(spy, WidthAttribute::class.java)

class RowHeightAttributeTestRenderOperation(spy: Spy? = null) :
    InterceptedRowAttributeRenderOperation<HeightAttribute>(spy, HeightAttribute::class.java)

class TemplateFileAttributeTestRenderOperation(spy: Spy? = null) :
    InterceptedTableAttributeRenderOperation<TemplateFileAttribute>(spy, TemplateFileAttribute::class.java)
