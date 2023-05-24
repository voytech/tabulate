package io.github.voytech.tabulate.support

import io.github.voytech.tabulate.components.table.model.attributes.table.TemplateFileAttribute
import io.github.voytech.tabulate.components.table.operation.*
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.attributes.*
import io.github.voytech.tabulate.core.operation.AttributeOperation
import io.github.voytech.tabulate.core.operation.AttributedContext
import io.github.voytech.tabulate.core.operation.VoidOperation
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
    override operator fun invoke(renderingContext: TestRenderingContext, context: TableStartRenderable) {
        spy?.track(this, context)
    }
}

class StartColumnTestOperation(private val spy: Spy? = null): StartColumnOperation<TestRenderingContext>, InterceptedOperation {
    override operator fun invoke(renderingContext: TestRenderingContext, context: ColumnStartRenderable) {
        spy?.track(this, context)
    }
}

class StartRowTestOperation(private val spy: Spy? = null): StartRowOperation<TestRenderingContext>, InterceptedOperation {
    override operator fun invoke(renderingContext: TestRenderingContext, context: RowStartRenderable) {
        spy?.track(this, context)
    }
}

class RenderRowCellTestOperation(private val spy: Spy? = null): VoidOperation<TestRenderingContext,CellRenderable>, InterceptedOperation {
    override operator fun invoke(renderingContext: TestRenderingContext, context: CellRenderable) {
        spy?.track(this, context)
    }
}

class EndRowTestOperation<T: Any>(private val spy: Spy? = null): EndRowOperation<TestRenderingContext,T>, InterceptedOperation {
    override operator fun invoke(renderingContext: TestRenderingContext, context: RowEndRenderable<T>) {
        spy?.track(this, context)
    }
}

class EndColumnTestOperation(private val spy: Spy? = null): EndColumnOperation<TestRenderingContext>, InterceptedOperation {
    override operator fun invoke(renderingContext: TestRenderingContext, context: ColumnEndRenderable) {
        spy?.track(this, context)
    }
}

class EndTableTestOperation(private val spy: Spy? = null): EndTableOperation<TestRenderingContext>, InterceptedOperation {
    override operator fun invoke(renderingContext: TestRenderingContext, context: TableEndRenderable) {
        spy?.track(this, context)
    }
}

abstract class InterceptedCellAttributeRenderOperation<T : Attribute<T>>(
    spy: Spy? = null,
    clazz: Class<T>
) : InterceptedRenderOperation<T, CellRenderable>(spy, clazz, CellRenderable::class.java)


abstract class InterceptedRowAttributeRenderOperation<T : Attribute<T>>(
    spy: Spy? = null,
    clazz: Class<T>
) : InterceptedRenderOperation<T, RowStartRenderable>(spy, clazz, RowStartRenderable::class.java)

abstract class InterceptedColumnAttributeRenderOperation<T : Attribute<T>>(
    spy: Spy? = null,
    clazz: Class<T>
) : InterceptedRenderOperation<T, ColumnStartRenderable>(spy, clazz, ColumnStartRenderable::class.java)

abstract class InterceptedTableAttributeRenderOperation<T : Attribute<T>>(
    spy: Spy? = null,
    clazz: Class<T>
) : InterceptedRenderOperation<T, TableStartRenderable>(spy, clazz, TableStartRenderable::class.java)

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
