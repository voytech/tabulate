package io.github.voytech.tabulate.support

import io.github.voytech.tabulate.model.attributes.Attribute
import io.github.voytech.tabulate.model.attributes.cell.CellBordersAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellTextStylesAttribute
import io.github.voytech.tabulate.model.attributes.column.ColumnWidthAttribute
import io.github.voytech.tabulate.model.attributes.row.RowHeightAttribute
import io.github.voytech.tabulate.model.attributes.table.TemplateFileAttribute
import io.github.voytech.tabulate.template.operations.*
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.fail

class Visitor {
    private val visitedOperations: LinkedList<AttributeOperation<*>> = LinkedList()
    fun <T : Attribute<*>> visit(operation: AttributeOperation<T>) = visitedOperations.add(operation)
    internal fun forEach(block: (op: AttributeOperation<*>) -> Unit) = visitedOperations.forEach(block)
    fun assertOrder(vararg ops: AttributeOperation<*>) {
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

class TestCellTextStyleRenderOperation(
    private val visitor: Visitor? = null
) : CellAttributeRenderOperation<TestRenderingContext, CellTextStylesAttribute> {
    override fun attributeType(): Class<out CellTextStylesAttribute> = CellTextStylesAttribute::class.java

    override fun renderAttribute(
        renderingContext: TestRenderingContext,
        context: RowCellContext,
        attribute: CellTextStylesAttribute
    ) {
        visitor?.visit(this)
    }
}

class TestBorderStyleRenderOperation(
    private val visitor: Visitor? = null
) : CellAttributeRenderOperation<TestRenderingContext, CellBordersAttribute> {
    override fun priority(): Int = -1
    override fun attributeType(): Class<out CellBordersAttribute> = CellBordersAttribute::class.java

    override fun renderAttribute(
        renderingContext: TestRenderingContext,
        context: RowCellContext,
        attribute: CellBordersAttribute
    ) {
        visitor?.visit(this)
    }
}

class TestColumnWidthRenderOperation(
    private val visitor: Visitor? = null
) : ColumnAttributeRenderOperation<TestRenderingContext, ColumnWidthAttribute> {
    override fun attributeType(): Class<out ColumnWidthAttribute> = ColumnWidthAttribute::class.java

    override fun renderAttribute(
        renderingContext: TestRenderingContext,
        context: ColumnContext,
        attribute: ColumnWidthAttribute
    ) {
        visitor?.visit(this)
    }
}

class TestRowHeightRenderOperation(
    private val visitor: Visitor? = null
) : RowAttributeRenderOperation<TestRenderingContext, RowHeightAttribute> {
    override fun attributeType(): Class<out RowHeightAttribute> = RowHeightAttribute::class.java

    override fun <T> renderAttribute(
        renderingContext: TestRenderingContext,
        context: RowContext<T>,
        attribute: RowHeightAttribute
    ) {
        visitor?.visit(this)
    }
}

class TestTableTemplateAttributeRenderOperation(
    private val visitor: Visitor? = null
) : TableAttributeRenderOperation<TestRenderingContext, TemplateFileAttribute> {
    override fun attributeType(): Class<out TemplateFileAttribute> = TemplateFileAttribute::class.java

    override fun renderAttribute(
        renderingContext: TestRenderingContext,
        table: TableContext,
        attribute: TemplateFileAttribute
    ) {
        visitor?.visit(this)
    }
}