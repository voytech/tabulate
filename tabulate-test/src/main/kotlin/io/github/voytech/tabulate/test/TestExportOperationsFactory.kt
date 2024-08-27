package io.github.voytech.tabulate.test

import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.components.table.rendering.*
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.operation.VoidOperation
import io.github.voytech.tabulate.core.result.OutputBinding
import io.github.voytech.tabulate.core.spi.BuildOperations
import io.github.voytech.tabulate.core.spi.DocumentFormat
import io.github.voytech.tabulate.core.spi.ExportOperationsProvider
import java.io.OutputStream
import java.util.logging.Logger

class TestRenderingContext: RenderingContext

fun interface RowCellTest {
    fun test(context: CellRenderableEntity)
}

interface CloseRowTest {
    fun <T> test(context: RowEndRenderableEntity<T>) { }
}

fun interface OpenRowTest {
    fun test(context: ColumnStartRenderableEntity)
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

class TestExportOperationsFactory: ExportOperationsProvider<TestRenderingContext, Table<Any>> {

    override fun getDocumentFormat(): DocumentFormat<TestRenderingContext> = DocumentFormat.format("test")

    override fun provideExportOperations(): BuildOperations<TestRenderingContext> = {
        operation(StartColumnOperation { _, context ->  columnTest?.test(context) })
        operation(EndRowOperation<TestRenderingContext,Table<Any>> { _, context ->  rowTest?.test(context) })
        operation(VoidOperation<TestRenderingContext,CellRenderableEntity> { _, context ->  cellTest?.test(context) })
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