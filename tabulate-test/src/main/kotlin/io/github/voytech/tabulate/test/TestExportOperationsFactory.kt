package io.github.voytech.tabulate.test

import io.github.voytech.tabulate.template.TabulationFormat
import io.github.voytech.tabulate.template.context.RenderingContext
import io.github.voytech.tabulate.template.operations.*
import io.github.voytech.tabulate.template.result.ResultProvider
import io.github.voytech.tabulate.template.spi.ExportOperationsProvider
import java.io.OutputStream
import java.util.logging.Logger

class TestRenderingContext: RenderingContext

fun interface AttributedCellTest {
    fun test(context: AttributedCell)
}

interface AttributedRowTest {
    fun <T> test(context: AttributedRowWithCells<T>) { }
}

fun interface AttributedColumnTest {
    fun test(context: AttributedColumn)
}

class TestResultProvider: ResultProvider<TestRenderingContext, Unit> {

    override fun outputClass() = Unit.javaClass

    override fun flush() {
        logger.info("This is fake implementation of ResultProvider flushing results into ether")
    }

    override fun setOutput(renderingContext: TestRenderingContext, output: Unit) {
        logger.info("This is fake implementation of ResultProvider flushing results into ether")
    }

    companion object {
        val logger: Logger = Logger.getLogger(TestResultProvider::class.java.name)
    }
}

class OutputStreamTestResultProvider: ResultProvider<TestRenderingContext, OutputStream> {
    override fun outputClass() = OutputStream::class.java
    override fun setOutput(renderingContext: TestRenderingContext, output: OutputStream) {
        logger.info("This is fake implementation of ResultProvider flushing results into OutputStream")
    }

    override fun flush() {
        logger.info("This is fake implementation of ResultProvider flushing results into OutputStream")
    }

    companion object {
        val logger: Logger = Logger.getLogger(OutputStreamTestResultProvider::class.java.name)
    }
}

class TestExportOperationsFactory:
    ExportOperationsProvider<TestRenderingContext> {

    override fun supportsFormat() = TabulationFormat.format("test")

    override fun createExportOperations(): AttributedContextExportOperations<TestRenderingContext> = object:
        AttributedContextExportOperations<TestRenderingContext> {

        override fun renderColumn(renderingContext: TestRenderingContext, context: AttributedColumn) {
            columnTest?.test(context)
        }

        override fun renderRowCell(renderingContext: TestRenderingContext, context: AttributedCell) {
            cellTest?.test(context)
        }

        override fun <T> beginRow(renderingContext: TestRenderingContext, context: AttributedRow<T>) {
            println("begin row: $context")
        }

        override fun <T> endRow(renderingContext: TestRenderingContext, context: AttributedRowWithCells<T>) {
            rowTest?.test(context)
        }

        override fun createTable(renderingContext: TestRenderingContext, context: AttributedTable) {
            println("table context: $context")
        }

    }

    override fun createResultProviders(): List<ResultProvider<TestRenderingContext, *>> = listOf(
        TestResultProvider(), OutputStreamTestResultProvider()
    )

    companion object {
        @JvmStatic
        var cellTest: AttributedCellTest? = null
        @JvmStatic
        var rowTest: AttributedRowTest? = null
        @JvmStatic
        var columnTest: AttributedColumnTest? = null

        fun clear() {
            cellTest = null
            rowTest = null
            columnTest = null
        }
    }

    override fun getContextClass(): Class<TestRenderingContext> = TestRenderingContext::class.java

    override fun createRenderingContext(): TestRenderingContext =
        TestRenderingContext()

}