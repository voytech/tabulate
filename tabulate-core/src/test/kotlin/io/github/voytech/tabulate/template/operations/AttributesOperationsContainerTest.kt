package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.model.attributes.CellAttribute
import io.github.voytech.tabulate.model.attributes.ColumnAttribute
import io.github.voytech.tabulate.model.attributes.RowAttribute
import io.github.voytech.tabulate.model.attributes.TableAttribute
import io.github.voytech.tabulate.support.*
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AttributesOperationsContainerTest {

    @Test
    fun `should register attributes operations`() {
        val container = AttributesOperationsContainer<TestRenderingContext>()
        container.registerAttributesOperations(object: AttributeOperationsFactory<TestRenderingContext> {
            override fun createCellAttributeRenderOperations(): Set<CellAttributeRenderOperation<TestRenderingContext, out CellAttribute<*>>>
                = setOf(CellTextStylesAttributeTestRenderOperation(), CellBordersAttributeTestRenderOperation())

            override fun createColumnAttributeRenderOperations(): Set<ColumnAttributeRenderOperation<TestRenderingContext, out ColumnAttribute<*>>>
                = setOf(ColumnWidthAttributeTestRenderOperation())

            override fun createRowAttributeRenderOperations(): Set<RowAttributeRenderOperation<TestRenderingContext, out RowAttribute<*>>>
               = setOf(RowHeightAttributeTestRenderOperation())

            override fun createTableAttributeRenderOperations(): Set<TableAttributeRenderOperation<TestRenderingContext, out TableAttribute<*>>>
               = setOf(TemplateFileAttributeTestRenderOperation())

        })
        assertFalse(container.isEmpty())
        assertTrue(container.getOperationsBy(OperationTypeInfo(TestRenderingContext::class.java,CellContext::class.java,CellAttribute::class.java)).isNotEmpty())
        assertTrue(container.getOperationsBy(OperationTypeInfo(TestRenderingContext::class.java,TableOpeningContext::class.java,TableAttribute::class.java)).isNotEmpty())
        assertTrue(container.getOperationsBy(OperationTypeInfo(TestRenderingContext::class.java,RowOpeningContext::class.java,RowAttribute::class.java)).isNotEmpty())
        assertTrue(container.getOperationsBy(OperationTypeInfo(TestRenderingContext::class.java,ColumnOpeningContext::class.java,ColumnAttribute::class.java)).isNotEmpty())

    }

}