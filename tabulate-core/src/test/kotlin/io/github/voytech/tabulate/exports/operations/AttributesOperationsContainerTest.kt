package io.github.voytech.tabulate.exports.operations

class AttributesOperationsContainerTest {
/*
    @Test
    fun `should register attributes operations`() {
        val container = AttributesOperationsContainer<TestRenderingContext>()
        container.registerAttributesOperations(object: TableAttributesOperationsFactory<TestRenderingContext> {
            override fun createCellAttributeRenderOperations(): Set<AttributeOperation<TestRenderingContext,CellAttribute<*>,*,*>>
                = setOf(CellTextStylesAttributeTestRenderOperation(), CellBordersAttributeTestRenderOperation())

            override fun createColumnAttributeRenderOperations(): Set<AttributeOperation<TestRenderingContext,ColumnAttribute<*>,*,*>>
                = setOf(ColumnWidthAttributeTestRenderOperation())

            override fun createRowAttributeRenderOperations(): Set<AttributeOperation<TestRenderingContext,RowAttribute<*>,*,*>>
               = setOf(RowHeightAttributeTestRenderOperation())

            override fun createTableAttributeRenderOperations(): Set<AttributeOperation<TestRenderingContext,TableAttribute<*>,*,*>>
               = setOf(TemplateFileAttributeTestRenderOperation())

        })
        assertFalse(container.isEmpty())
        assertTrue(container.getOperationsBy(OperationTypeInfo(TestRenderingContext::class.java,CellContext::class.java,CellAttribute::class.java)).isNotEmpty())
        assertTrue(container.getOperationsBy(OperationTypeInfo(TestRenderingContext::class.java,TableOpeningContext::class.java,TableAttribute::class.java)).isNotEmpty())
        assertTrue(container.getOperationsBy(OperationTypeInfo(TestRenderingContext::class.java,RowOpeningContext::class.java,RowAttribute::class.java)).isNotEmpty())
        assertTrue(container.getOperationsBy(OperationTypeInfo(TestRenderingContext::class.java,ColumnOpeningContext::class.java,ColumnAttribute::class.java)).isNotEmpty())

    }
*/
}