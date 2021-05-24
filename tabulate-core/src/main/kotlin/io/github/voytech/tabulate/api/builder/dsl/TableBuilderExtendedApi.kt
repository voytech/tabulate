package io.github.voytech.tabulate.api.builder.dsl

import io.github.voytech.tabulate.api.builder.RowBuilder
import io.github.voytech.tabulate.model.RowCellExpression

class HeaderBuilderApi<T>(private val builder: RowsBuilderApi<T>) {

    private val rowBuilder: RowBuilder<T>  = builder.row {
        insertWhen {it.rowIndex == 0 && !it.hasRecord()}
    }

    private val cellsBuilder: CellsBuilderApi<T> = CellsBuilderApi.new(rowBuilder.cellsBuilder)

    @JvmSynthetic
    fun cell(id: String, block: CellBuilderApi<T>.() -> Unit) = cellsBuilder.cell(id, block)

    @JvmSynthetic
    fun cell(ref: ((record: T) -> Any?), block: CellBuilderApi<T>.() -> Unit) = cellsBuilder.cell(ref, block)

    @JvmSynthetic
    fun attributes(block: RowLevelAttributesBuilderApi<T>.() -> Unit) = RowLevelAttributesBuilderApi(rowBuilder).apply(block)

}

fun <T> RowsBuilderApi<T>.header(block: HeaderBuilderApi<T>.() -> Unit) =
    HeaderBuilderApi(this).apply(block)


fun <T> RowsBuilderApi<T>.header(vararg names: String) =
    row {
        insertWhen { it.rowIndex == 0 && !it.hasRecord()}
        cells {
            names.forEach {
                cell { value = it }
            }
        }
    }

fun <T> RowsBuilderApi<T>.rowNumberingOn(id: String) {
    row {
        allMatching { source -> source.rowIndex > 0 }
        cells {
            cell(id) {
                expression = RowCellExpression { source -> source.rowIndex + 1 }
            }
        }
    }
}