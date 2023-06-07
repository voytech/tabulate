package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.model.attributes.alias.ColumnAttribute
import io.github.voytech.tabulate.model.attributes.alias.RowAttribute
import io.github.voytech.tabulate.model.attributes.alias.TableAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellBordersAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellTextStylesAttribute
import io.github.voytech.tabulate.model.attributes.column.ColumnWidthAttribute
import io.github.voytech.tabulate.model.attributes.row.RowHeightAttribute
import io.github.voytech.tabulate.model.attributes.table.TemplateFileAttribute
import io.github.voytech.tabulate.support.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class AttributesOperationsContainerTest {

    @BeforeEach
    fun setup() {
        Spy.spy.reset()
    }

    @Test
    fun `should register attributes operations`() {
        val container = AttributesOperationsContainer<TestRenderingContext>()
        container.registerAttributesOperations(object: AttributeRenderOperationsFactory<TestRenderingContext> {
            override fun createCellAttributeRenderOperations(): Set<CellAttributeRenderOperation<TestRenderingContext, out CellAttribute>>
                = setOf(CellTextStylesAttributeTestRenderOperation(), CellBordersAttributeTestRenderOperation())

            override fun createColumnAttributeRenderOperations(): Set<ColumnAttributeRenderOperation<TestRenderingContext, out ColumnAttribute>>
                = setOf(ColumnWidthAttributeTestRenderOperation())

            override fun createRowAttributeRenderOperations(): Set<RowAttributeRenderOperation<TestRenderingContext, out RowAttribute>>
               = setOf(RowHeightAttributeTestRenderOperation())

            override fun createTableAttributeRenderOperations(): Set<TableAttributeRenderOperation<TestRenderingContext, out TableAttribute>>
               = setOf(TemplateFileAttributeTestRenderOperation())

        })
        assertFalse(container.isEmpty())
        assertNotNull(container.getCellAttributeOperation(CellTextStylesAttribute::class.java))
        assertNotNull(container.getCellAttributeOperation(CellBordersAttribute::class.java))
        assertNotNull(container.getTableAttributeOperation(TemplateFileAttribute::class.java))
        assertNotNull(container.getRowAttributeOperation(RowHeightAttribute::class.java))
        assertNotNull(container.getColumnAttributeOperation(ColumnWidthAttribute::class.java))

    }

}