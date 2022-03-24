package io.github.voytech.tabulate.benchmarks

import io.github.voytech.tabulate.model.attributes.CellAttribute
import io.github.voytech.tabulate.model.attributes.ColumnAttribute
import io.github.voytech.tabulate.model.attributes.RowAttribute
import io.github.voytech.tabulate.model.attributes.TableAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellAlignmentAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellBackgroundAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellBordersAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellTextStylesAttribute
import io.github.voytech.tabulate.model.attributes.column.ColumnWidthAttribute
import io.github.voytech.tabulate.model.attributes.row.RowHeightAttribute
import io.github.voytech.tabulate.model.attributes.table.TemplateFileAttribute
import io.github.voytech.tabulate.template.context.RenderingContext
import io.github.voytech.tabulate.template.operations.*
import io.github.voytech.tabulate.template.result.OutputBinding
import io.github.voytech.tabulate.template.spi.TabulationFormat

class BenchmarkRenderingContext : RenderingContext

class BenchmarkOutputBinding: OutputBinding<BenchmarkRenderingContext,Unit> {
    override fun outputClass(): Class<Unit> = Unit::class.java

    override fun setOutput(renderingContext: BenchmarkRenderingContext, output: Unit) { }

    override fun flush() { }
}

class BenchmarkExportOperationsFactory: ExportOperationsConfiguringFactory<BenchmarkRenderingContext>() {
    override fun provideExportOperations(): TableExportOperations<BenchmarkRenderingContext> = object: TableExportOperations<BenchmarkRenderingContext> {
        override fun renderRowCell(renderingContext: BenchmarkRenderingContext, context: RowCellContext) { /* NOOP */}
    }

    override fun createOutputBindings(): List<OutputBinding<BenchmarkRenderingContext, *>>  = listOf(
        BenchmarkOutputBinding()
    )

    override fun getAttributeOperationsFactory(): AttributeRenderOperationsFactory<BenchmarkRenderingContext> =
        TestAttributeOperations()

    override fun getTabulationFormat(): TabulationFormat<BenchmarkRenderingContext> =
        TabulationFormat.format("benchmark", BenchmarkRenderingContext::class.java)

}

class TestAttributeOperations : AttributeRenderOperationsFactory<BenchmarkRenderingContext> {
    override fun createCellAttributeRenderOperations(): Set<CellAttributeRenderOperation<BenchmarkRenderingContext, out CellAttribute<*>>> =
        setOf(
            CellTextStylesAttributeTestRenderOperation(),
            CellBordersAttributeTestRenderOperation(),
            CellBackgroundAttributeTestRenderOperation(),
            CellAlignmentAttributeTestRenderOperation()
        )

    override fun createColumnAttributeRenderOperations(): Set<ColumnAttributeRenderOperation<BenchmarkRenderingContext, out ColumnAttribute<*>>> =
        setOf(ColumnWidthAttributeTestRenderOperation())

    override fun createRowAttributeRenderOperations(): Set<RowAttributeRenderOperation<BenchmarkRenderingContext, out RowAttribute<*>>> =
        setOf(RowHeightAttributeTestRenderOperation())

    override fun createTableAttributeRenderOperations(): Set<TableAttributeRenderOperation<BenchmarkRenderingContext, out TableAttribute<*>>> =
        setOf(TemplateFileAttributeTestRenderOperation())

}

class CellTextStylesAttributeTestRenderOperation :
    CellAttributeRenderOperation<BenchmarkRenderingContext, CellTextStylesAttribute> {
    override fun attributeType(): Class<CellTextStylesAttribute> = CellTextStylesAttribute::class.java
    override fun renderAttribute(
        renderingContext: BenchmarkRenderingContext,
        context: RowCellContext,
        attribute: CellTextStylesAttribute
    ) {}
}

class CellBordersAttributeTestRenderOperation : CellAttributeRenderOperation<BenchmarkRenderingContext, CellBordersAttribute>{
    override fun attributeType(): Class<CellBordersAttribute> = CellBordersAttribute::class.java
    override fun renderAttribute(
        renderingContext: BenchmarkRenderingContext,
        context: RowCellContext,
        attribute: CellBordersAttribute
    ) {}
}

class CellBackgroundAttributeTestRenderOperation :
    CellAttributeRenderOperation<BenchmarkRenderingContext, CellBackgroundAttribute> {
    override fun attributeType(): Class<CellBackgroundAttribute> = CellBackgroundAttribute::class.java
    override fun renderAttribute(
        renderingContext: BenchmarkRenderingContext,
        context: RowCellContext,
        attribute: CellBackgroundAttribute
    ) {}
}

class CellAlignmentAttributeTestRenderOperation :
    CellAttributeRenderOperation<BenchmarkRenderingContext, CellAlignmentAttribute> {
    override fun attributeType(): Class<CellAlignmentAttribute> = CellAlignmentAttribute::class.java
    override fun renderAttribute(
        renderingContext: BenchmarkRenderingContext,
        context: RowCellContext,
        attribute: CellAlignmentAttribute
    ) {}
}

class ColumnWidthAttributeTestRenderOperation:
    ColumnAttributeRenderOperation<BenchmarkRenderingContext, ColumnWidthAttribute> {
    override fun attributeType(): Class<ColumnWidthAttribute> = ColumnWidthAttribute::class.java
    override fun renderAttribute(
        renderingContext: BenchmarkRenderingContext,
        context: ColumnContext,
        attribute: ColumnWidthAttribute
    ) {}
}

class RowHeightAttributeTestRenderOperation :
    RowAttributeRenderOperation<BenchmarkRenderingContext, RowHeightAttribute> {
    override fun attributeType(): Class<RowHeightAttribute> = RowHeightAttribute::class.java
    override fun renderAttribute(
        renderingContext: BenchmarkRenderingContext,
        context: RowContext,
        attribute: RowHeightAttribute
    ) {}
}

class TemplateFileAttributeTestRenderOperation :
    TableAttributeRenderOperation<BenchmarkRenderingContext, TemplateFileAttribute> {
    override fun attributeType(): Class<TemplateFileAttribute> = TemplateFileAttribute::class.java
    override fun renderAttribute(
        renderingContext: BenchmarkRenderingContext,
        context: TableContext,
        attribute: TemplateFileAttribute
    ) {}
}