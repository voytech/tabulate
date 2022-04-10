package io.github.voytech.tabulate.test

import io.github.voytech.tabulate.template.context.RenderingContext
import io.github.voytech.tabulate.template.operations.*
import io.github.voytech.tabulate.template.result.OutputBinding
import io.github.voytech.tabulate.template.spi.TabulationFormat
import java.io.OutputStream
import java.util.logging.Logger

class TestRenderingContext: RenderingContext

fun interface RowCellTest {
    fun test(context: CellContext)
}

interface CloseRowTest {
    fun <T> test(context: RowClosingContext<T>) { }
}

fun interface OpenRowTest {
    fun test(context: ColumnOpeningContext)
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

class TestExportOperationsFactory: ExportOperationsFactory<TestRenderingContext>() {

    override fun getTabulationFormat() = TabulationFormat.format("test", TestRenderingContext::class.java)
    
    override fun provideExportOperations(): OperationsBuilder<TestRenderingContext>.() -> Unit = {
        openColumn = OpenColumnOperation { _, context ->  columnTest?.test(context) }
        closeRow = CloseRowOperation { _, context -> rowTest?.test(context)  }
        renderRowCell = RenderRowCellOperation { _, context -> cellTest?.test(context)  }
    }

    override fun createOutputBindings(): List<OutputBinding<TestRenderingContext, *>> = listOf(
        TestOutputBinding(), OutputStreamTestOutputBinding()
    )

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