package io.github.voytech.tabulate.benchmarks

import io.github.voytech.tabulate.template.TabulationFormat
import io.github.voytech.tabulate.template.context.RenderingContext
import io.github.voytech.tabulate.template.operations.ExportOperationsConfiguringFactory
import io.github.voytech.tabulate.template.operations.RowCellContext
import io.github.voytech.tabulate.template.operations.TableExportOperations
import io.github.voytech.tabulate.template.result.OutputBinding

class BenchmarkRenderingContext : RenderingContext

class BenchmarkOutputBinding: OutputBinding<BenchmarkRenderingContext,Unit> {
    override fun outputClass(): Class<Unit> = Unit::class.java

    override fun setOutput(renderingContext: BenchmarkRenderingContext, output: Unit) { }

    override fun flush() { }
}

class BenchmarkExportOperationsFactory: ExportOperationsConfiguringFactory<BenchmarkRenderingContext>() {
    override fun provideExportOperations(): TableExportOperations<BenchmarkRenderingContext> = object: TableExportOperations<BenchmarkRenderingContext> {
        override fun renderRowCell(renderingContext: BenchmarkRenderingContext, context: RowCellContext) { }
    }

    override fun createOutputBindings(): List<OutputBinding<BenchmarkRenderingContext, *>>  = listOf(
        BenchmarkOutputBinding()
    )

    override fun getContextClass(): Class<BenchmarkRenderingContext> = BenchmarkRenderingContext::class.java

    override fun createRenderingContext(): BenchmarkRenderingContext = BenchmarkRenderingContext()

    override fun supportsFormat(): TabulationFormat = TabulationFormat.format("benchmark")

}

