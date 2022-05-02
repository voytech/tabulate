package io.github.voytech.tabulate.test

import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.components.table.operation.*
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.operation.OperationsBuilder
import io.github.voytech.tabulate.core.template.operation.factories.ExportOperationsFactory
import io.github.voytech.tabulate.core.template.result.OutputBinding
import io.github.voytech.tabulate.core.template.spi.DocumentFormat
import java.io.OutputStream
import java.util.logging.Logger

class TestRenderingContext: RenderingContext

fun interface RowCellTest {
    fun test(context: CellContext)
}

interface CloseRowTest {
    fun <T> test(context: RowEnd<T>) { }
}

fun interface OpenRowTest {
    fun test(context: ColumnStart)
}

class TestOutputBinding: OutputBinding<TestRenderingContext, Unit> {

    override fun outputClass() = Unit.javaClass

    override fun flush() {
        logger.info("This is fake implementation of ResultProvider flushing results into ether")
    }

    override fun setOutput(renderingContext: TestRenderingContext, output: Unit) {
        logger.info("This is fake implementation of ResultProvider flushing results into ether")
    }

    companion object {
        val logger: Logger = Logger.getLogger(TestOutputBinding::class.java.name)
    }
}

class OutputStreamTestOutputBinding: OutputBinding<TestRenderingContext, OutputStream> {
    override fun outputClass() = OutputStream::class.java
    override fun setOutput(renderingContext: TestRenderingContext, output: OutputStream) {
        logger.info("This is fake implementation of ResultProvider flushing results into OutputStream")
    }

    override fun flush() {
        logger.info("This is fake implementation of ResultProvider flushing results into OutputStream")
    }

    companion object {
        val logger: Logger = Logger.getLogger(OutputStreamTestOutputBinding::class.java.name)
    }
}

class TestExportOperationsFactory: ExportOperationsFactory<TestRenderingContext, Table<Any>>() {

    override fun getDocumentFormat(): DocumentFormat<TestRenderingContext>  = DocumentFormat.format("test")

    override fun provideExportOperations(): OperationsBuilder<TestRenderingContext,*>.() -> Unit = {
        operation(OpenColumnOperation { _, context ->  columnTest?.test(context) })
        operation(CloseRowOperation { _, context ->  rowTest?.test(context) })
        operation(RenderRowCellOperation { _, context ->  cellTest?.test(context) })
    }

    override fun getModelClass(): Class<Table<Any>> = reify()

    companion object {
        @JvmStatic
        var cellTest: RowCellTest? = null
        @JvmStatic
        var rowTest: CloseRowTest? = null
        @JvmStatic
        var columnTest: OpenRowTest? = null

        fun clear() {
            cellTest = null
            rowTest = null
            columnTest = null
        }
    }

}