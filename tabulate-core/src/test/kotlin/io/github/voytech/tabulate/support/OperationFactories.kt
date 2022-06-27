package io.github.voytech.tabulate.support

import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.components.table.model.attributes.cell.CellAlignmentAttribute
import io.github.voytech.tabulate.components.table.model.attributes.cell.CellBackgroundAttribute
import io.github.voytech.tabulate.components.table.model.attributes.cell.CellBordersAttribute
import io.github.voytech.tabulate.components.table.model.attributes.cell.CellTextStylesAttribute
import io.github.voytech.tabulate.components.table.model.attributes.column.ColumnWidthAttribute
import io.github.voytech.tabulate.components.table.model.attributes.row.RowHeightAttribute
import io.github.voytech.tabulate.components.table.model.attributes.table.TemplateFileAttribute
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.operation.AttributeOperationsBuilder
import io.github.voytech.tabulate.core.template.operation.OperationsBuilder
import io.github.voytech.tabulate.core.template.result.OutputBinding
import io.github.voytech.tabulate.core.template.spi.*
import io.github.voytech.tabulate.core.template.spi.DocumentFormat.Companion.format
import io.github.voytech.tabulate.support.Spy.Companion.operationPriorities


class TestOutputBindingProvider: OutputBindingsProvider<TestRenderingContext> {
    override fun createOutputBindings(): List<OutputBinding<TestRenderingContext, *>> = listOf(
        TestOutputBinding(), OutputStreamTestOutputBinding()
    )

    override fun getDocumentFormat(): DocumentFormat<TestRenderingContext> =
        format("spy")
}

class TestExportOperationsFactory : OperationsBundleProvider<TestRenderingContext, Table<Any>> {

    override fun provideExportOperations(): BuildOperations<TestRenderingContext> = {
        operation(StartTableTestOperation(Spy.spy))
        operation(StartColumnTestOperation(Spy.spy))
        operation(StartRowTestOperation(Spy.spy))
        operation(RenderRowCellTestOperation(Spy.spy))
        operation(EndRowTestOperation(Spy.spy))
        operation(EndColumnTestOperation(Spy.spy))
        operation(EndTableTestOperation(Spy.spy))
    }

    override fun provideAttributeOperations(): BuildAttributeOperations<TestRenderingContext> = {
        operation(CellTextStylesAttributeTestRenderOperation(Spy.spy),operationPriorities[CellTextStylesAttribute::class.java] ?: 1)
        operation(CellBordersAttributeTestRenderOperation(Spy.spy),operationPriorities[CellBordersAttribute::class.java] ?: 1)
        operation(CellBackgroundAttributeTestRenderOperation(Spy.spy),operationPriorities[CellBackgroundAttribute::class.java] ?: 1)
        operation(CellAlignmentAttributeTestRenderOperation(Spy.spy),operationPriorities[CellAlignmentAttribute::class.java] ?: 1)
        operation(ColumnWidthAttributeTestRenderOperation(Spy.spy),operationPriorities[ColumnWidthAttribute::class.java] ?: 1)
        operation(RowHeightAttributeTestRenderOperation(Spy.spy),operationPriorities[RowHeightAttribute::class.java] ?: 1)
        operation(TemplateFileAttributeTestRenderOperation(Spy.spy),operationPriorities[TemplateFileAttribute::class.java] ?: 1)
    }

    override fun getRenderingContextClass(): Class<TestRenderingContext> = reify()

    override fun getModelClass(): Class<Table<Any>> = reify()

    override fun getDocumentFormat(): DocumentFormat<TestRenderingContext> = format("spy")

}

class AlternativeTestRenderingContext : RenderingContext

class AnotherTestExportOperationsFactory : OperationsBundleProvider<AlternativeTestRenderingContext,Table<Any>>  {

    /**
     * atf - Alternative Test Format ;)
     */
    override fun getDocumentFormat():  DocumentFormat<AlternativeTestRenderingContext> =
        format("atf")

    override fun provideAttributeOperations(): AttributeOperationsBuilder<AlternativeTestRenderingContext>.() -> Unit = {
        // No attribute operations present at all.
    }

    override fun getRenderingContextClass(): Class<AlternativeTestRenderingContext> = reify()

    override fun provideExportOperations(): OperationsBuilder<AlternativeTestRenderingContext>.() -> Unit = {
        // No export operations present at all
    }

    override fun getModelClass(): Class<Table<Any>> = reify()

}