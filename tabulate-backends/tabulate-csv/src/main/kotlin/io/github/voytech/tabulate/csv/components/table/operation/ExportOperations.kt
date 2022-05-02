package io.github.voytech.tabulate.csv.components.table.operation


import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.components.table.operation.CellContext
import io.github.voytech.tabulate.components.table.operation.CloseRowOperation
import io.github.voytech.tabulate.components.table.operation.OpenRowOperation
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.template.operation.OperationsBuilder
import io.github.voytech.tabulate.core.template.operation.factories.ExportOperationsFactory
import io.github.voytech.tabulate.core.template.spi.DocumentFormat
import io.github.voytech.tabulate.core.template.spi.DocumentFormat.Companion.format
import io.github.voytech.tabulate.csv.CsvRenderingContext
import io.github.voytech.tabulate.csv.components.table.model.attributes.CellSeparatorCharacterAttribute


/**
 * Simple .csv export operations provider implementation.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class CsvExportOperationsFactory : ExportOperationsFactory<CsvRenderingContext, Table<Any>>() {

    override fun getDocumentFormat(): DocumentFormat<CsvRenderingContext> = format("csv")


    override fun provideExportOperations(): OperationsBuilder<CsvRenderingContext, Table<Any>>.() -> Unit = {
        operation(OpenRowOperation { renderingContext, _ ->
            renderingContext.line.clear()
        })
        operation(CloseRowOperation { renderingContext, context ->
            val lastIndex = context.rowCellValues.size - 1
            with(renderingContext) {
                context.rowCellValues.values.forEachIndexed { index, cell ->
                    line.append(cell.rawValue.toString())
                    if (index < lastIndex) line.append(cell.getSeparatorCharacter())
                }
                bufferedWriter.write(line.toString())
                bufferedWriter.newLine()
            }
        })
    }

    override fun getModelClass(): Class<Table<Any>> = reify()

    private fun CellContext.getSeparatorCharacter(): String =
        getModelAttribute(CellSeparatorCharacterAttribute::class.java)?.separator ?: ","

}