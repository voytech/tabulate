package io.github.voytech.tabulate.support

import io.github.voytech.tabulate.model.attributes.*
import io.github.voytech.tabulate.model.attributes.cell.CellBordersAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellTextStylesAttribute
import io.github.voytech.tabulate.model.attributes.column.ColumnWidthAttribute
import io.github.voytech.tabulate.model.attributes.row.RowHeightAttribute
import io.github.voytech.tabulate.model.attributes.table.TemplateFileAttribute
import io.github.voytech.tabulate.template.operations.*
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.fail

class Spy {
    private val visitedOperations: LinkedList<AttributeOperation<TestRenderingContext, *, *, *>> = LinkedList()
    fun <T : Attribute<*>, G : T, E : ModelAttributeAccessor<T>> visit(operation: AttributeOperation<TestRenderingContext, T, G, E>) =
        visitedOperations.add(operation)

    fun assertOrder(vararg ops: AttributeOperation<TestRenderingContext, *, *, *>) {
        val iterator = visitedOperations.iterator()
        val refIterator = ops.iterator()
        while (iterator.hasNext()) {
            val original = iterator.next()
            if (!refIterator.hasNext()) fail("Requested operation was not visited.")
            val reference = refIterator.next()
            assertEquals(original.javaClass, reference.javaClass)
        }
    }
}

abstract class SpyRenderOperation<T : Attribute<*>, G : T, E : ModelAttributeAccessor<T>>(
    private val spy: Spy? = null, private val clazz: Class<G>
) : AttributeOperation<TestRenderingContext, T, G, E> {
    override fun attributeType(): Class<G> = clazz
    override fun renderAttribute(renderingContext: TestRenderingContext, context: E, attribute: G) {
        spy?.visit(this)
    }
}

class TestCellTextStyleRenderOperation(spy: Spy? = null) :
    CellAttributeRenderOperation<TestRenderingContext, CellTextStylesAttribute>,
    SpyRenderOperation<CellAttribute<*>, CellTextStylesAttribute, RowCellContext>(
        spy,
        CellTextStylesAttribute::class.java
    )

class TestBorderStyleRenderOperation(spy: Spy? = null, private val priority: Int = -1) :
    CellAttributeRenderOperation<TestRenderingContext, CellBordersAttribute>,
    SpyRenderOperation<CellAttribute<*>, CellBordersAttribute, RowCellContext>(spy, CellBordersAttribute::class.java) {
    override fun priority(): Int = priority
}

class TestColumnWidthRenderOperation(spy: Spy? = null) :
    ColumnAttributeRenderOperation<TestRenderingContext, ColumnWidthAttribute>,
    SpyRenderOperation<ColumnAttribute<*>, ColumnWidthAttribute, ColumnContext>(spy, ColumnWidthAttribute::class.java)

class TestRowHeightRenderOperation(spy: Spy? = null) :
    RowAttributeRenderOperation<TestRenderingContext, RowHeightAttribute>,
    SpyRenderOperation<RowAttribute<*>, RowHeightAttribute, RowContext>(spy, RowHeightAttribute::class.java)

class TestTableTemplateAttributeRenderOperation(spy: Spy? = null) :
    TableAttributeRenderOperation<TestRenderingContext, TemplateFileAttribute>,
    SpyRenderOperation<TableAttribute<*>, TemplateFileAttribute, TableContext>(spy, TemplateFileAttribute::class.java)