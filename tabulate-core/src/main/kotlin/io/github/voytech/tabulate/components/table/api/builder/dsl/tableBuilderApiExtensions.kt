package io.github.voytech.tabulate.components.table.api.builder.dsl

import io.github.voytech.tabulate.components.table.model.RowCellExpression
import io.github.voytech.tabulate.components.table.template.AdditionalSteps
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.reflect.KProperty1

@TabulateMarker
class HeaderBuilderApi<T>(val builder: RowsBuilderApi<T>) {

    @JvmSynthetic
    fun columnTitle(id: String, block: CellBuilderApi<T>.() -> Unit) {
        builder.newRow(HEADER_ROW_INDEX) {
            cells {
                cell(id, block)
            }
        }
    }

    @JvmSynthetic
    fun columnTitle(ref: KProperty1<T, Any?>, block: CellBuilderApi<T>.() -> Unit) {
        builder.newRow(HEADER_ROW_INDEX) {
            cells {
                cell(ref, block)
            }
        }
    }

    @JvmSynthetic
    fun columnTitles(vararg names: String) =
        builder.newRow(HEADER_ROW_INDEX) {
            cells {
                names.forEach {
                    cell { value = it }
                }
            }
        }

    @JvmSynthetic
    fun attributes(block: RowLevelAttributesBuilderApi<T>.() -> Unit) {
        builder.newRow(HEADER_ROW_INDEX) {
            attributes(block)
        }
    }

    companion object {
        const val HEADER_ROW_INDEX = 0
    }

}

fun <T> RowsBuilderApi<T>.header(block: HeaderBuilderApi<T>.() -> Unit) =
    HeaderBuilderApi(this).apply(block)


fun <T> RowsBuilderApi<T>.header(vararg names: String) =
    newRow(0) {
        cells {
            names.forEach {
                cell { value = it }
            }
        }
    }

fun <T> RowsBuilderApi<T>.rowNumberingOn(id: String) {
    row({ source -> source.rowIndexValue() > 0 && source.rowIndex.step == null }) {
        cells {
            cell(id) {
                expression = RowCellExpression { source -> source.rowIndexValue() }
            }
        }
    }
}

fun <T> RowsBuilderApi<T>.footer(block: RowBuilderApi<T>.() -> Unit) {
    newRow(0, AdditionalSteps.TRAILING_ROWS, block)
}

fun <T> RowsBuilderApi<T>.newTrailingRow(block: RowBuilderApi<T>.() -> Unit) {
    newRow(AdditionalSteps.TRAILING_ROWS, block)
}

fun <T> RowsBuilderApi<T>.newTrailingRow(index: Int, block: RowBuilderApi<T>.() -> Unit) {
    newRow(index, AdditionalSteps.TRAILING_ROWS, block)
}

private fun <T, R> cellBuilderBlock(
    cSpan: Int = 1,
    rSpan: Int = 1,
    valueSupplier: () -> R,
): CellBuilderApi<T>.() -> Unit = {
    colSpan = cSpan
    rowSpan = rSpan
    value = valueSupplier()
}

fun <T> RowBuilderApi<T>.textCell(
    index: Int? = null,
    colSpan: Int = 1,
    rowSpan: Int = 1,
    valueSupplier: () -> String
) {
    val block = cellBuilderBlock<T, String>(colSpan, rowSpan, valueSupplier)
    index?.let { cell(it, block) } ?: cell(block)
}

fun <T> RowBuilderApi<T>.decimalCell(
    index: Int? = null,
    colSpan: Int = 1,
    rowSpan: Int = 1,
    valueSupplier: () -> BigDecimal
) {
    val block = cellBuilderBlock<T, BigDecimal>(colSpan, rowSpan, valueSupplier)
    index?.let { cell(it, block) } ?: cell(block)
}

fun <T> RowBuilderApi<T>.dataCell(
    index: Int? = null,
    colSpan: Int = 1,
    rowSpan: Int = 1,
    valueSupplier: () -> LocalDate) {
    val block = cellBuilderBlock<T, LocalDate>(colSpan, rowSpan, valueSupplier)
    index?.let { cell(it, block) } ?: cell(block)
}

fun <T> RowBuilderApi<T>.boolCell(
    index: Int? = null,
    colSpan: Int = 1,
    rowSpan: Int = 1,
    valueSupplier: () -> Boolean) {
    val block = cellBuilderBlock<T, Boolean>(colSpan, rowSpan, valueSupplier)
    index?.let { cell(it, block) } ?: cell(block)
}

fun <T> RowBuilderApi<T>.imageUrlCell(
    index: Int? = null,
    colSpan: Int = 1,
    rowSpan: Int = 1,
    valueSupplier: () -> String) {
    val block = cellBuilderBlock<T, String>(colSpan, rowSpan, valueSupplier)
    index?.let { cell(it, block) } ?: cell(block)
}

fun <T> RowBuilderApi<T>.imageDataCell(
    index: Int? = null,
    colSpan: Int = 1,
    rowSpan: Int = 1,
    valueSupplier: () -> ByteArray) {
    val block = cellBuilderBlock<T, ByteArray>(colSpan, rowSpan, valueSupplier)
    index?.let { cell(it, block) } ?: cell(block)
}