package io.github.voytech.tabulate.api.builder.dsl

import io.github.voytech.tabulate.model.CellType
import io.github.voytech.tabulate.model.RowCellExpression
import io.github.voytech.tabulate.template.context.DefaultSteps
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.reflect.KProperty1

@TabulateMarker
class HeaderBuilderApi<T>(val builder: RowsBuilderApi<T>) {

    @JvmSynthetic
    fun columnTitle(id: String, block: CellBuilderApi<T>.() -> Unit) {
        builder.row(HEADER_ROW_INDEX) {
            cells {
                cell(id, block)
            }
        }
    }

    @JvmSynthetic
    fun columnTitle(ref: KProperty1<T, Any?>, block: CellBuilderApi<T>.() -> Unit) {
        builder.row(HEADER_ROW_INDEX) {
            cells {
                cell(ref, block)
            }
        }
    }

    @JvmSynthetic
    fun columnTitles(vararg names: String) =
        builder.row(HEADER_ROW_INDEX) {
            cells {
                names.forEach {
                    cell { value = it }
                }
            }
        }

    @JvmSynthetic
    fun attributes(block: RowLevelAttributesBuilderApi<T>.() -> Unit) {
        builder.row(HEADER_ROW_INDEX) {
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
    row(0) {
        cells {
            names.forEach {
                cell { value = it }
            }
        }
    }

fun <T> RowsBuilderApi<T>.rowNumberingOn(id: String) {
    row {
        matching { source -> source.rowIndexValue() > 0 && source.rowIndex.steps.isEmpty() }
        cells {
            cell(id) {
                expression = RowCellExpression { source -> source.rowIndexValue() }
            }
        }
    }
}

fun <T> RowsBuilderApi<T>.footer(block: RowBuilderApi<T>.() -> Unit) {
    row(0, DefaultSteps.TRAILING_ROWS, block)
}

fun <T> RowsBuilderApi<T>.trailingRow(block: RowBuilderApi<T>.() -> Unit) {
    row(DefaultSteps.TRAILING_ROWS, block)
}

fun <T> RowsBuilderApi<T>.trailingRow(index: Int, block: RowBuilderApi<T>.() -> Unit) {
    row(index, DefaultSteps.TRAILING_ROWS, block)
}

private fun <T, R> cellBuilderBlock(
    cSpan: Int = 1,
    rSpan: Int = 1,
    cType: CellType,
    valueSupplier: () -> R,
): CellBuilderApi<T>.() -> Unit = {
    colSpan = cSpan
    rowSpan = rSpan
    value = valueSupplier()
    type = cType
}

fun <T> RowBuilderApi<T>.textCell(
    index: Int? = null,
    colSpan: Int = 1,
    rowSpan: Int = 1,
    valueSupplier: () -> String
) {
    val block = cellBuilderBlock<T, String>(colSpan, rowSpan, CellType.STRING, valueSupplier)
    index?.let { cell(it, block) } ?: cell(block)
}

fun <T> RowBuilderApi<T>.decimalCell(
    index: Int? = null,
    colSpan: Int = 1,
    rowSpan: Int = 1,
    valueSupplier: () -> BigDecimal
) {
    val block = cellBuilderBlock<T, BigDecimal>(colSpan, rowSpan, CellType.NUMERIC, valueSupplier)
    index?.let { cell(it, block) } ?: cell(block)
}

fun <T> RowBuilderApi<T>.dataCell(
    index: Int? = null,
    colSpan: Int = 1,
    rowSpan: Int = 1,
    valueSupplier: () -> LocalDate) {
    val block = cellBuilderBlock<T, LocalDate>(colSpan, rowSpan, CellType.DATE, valueSupplier)
    index?.let { cell(it, block) } ?: cell(block)
}

fun <T> RowBuilderApi<T>.boolCell(
    index: Int? = null,
    colSpan: Int = 1,
    rowSpan: Int = 1,
    valueSupplier: () -> Boolean) {
    val block = cellBuilderBlock<T, Boolean>(colSpan, rowSpan, CellType.BOOLEAN, valueSupplier)
    index?.let { cell(it, block) } ?: cell(block)
}

fun <T> RowBuilderApi<T>.imageUrlCell(
    index: Int? = null,
    colSpan: Int = 1,
    rowSpan: Int = 1,
    valueSupplier: () -> String) {
    val block = cellBuilderBlock<T, String>(colSpan, rowSpan, CellType.IMAGE_URL, valueSupplier)
    index?.let { cell(it, block) } ?: cell(block)
}

fun <T> RowBuilderApi<T>.imageDataCell(
    index: Int? = null,
    colSpan: Int = 1,
    rowSpan: Int = 1,
    valueSupplier: () -> ByteArray) {
    val block = cellBuilderBlock<T, ByteArray>(colSpan, rowSpan, CellType.IMAGE_DATA, valueSupplier)
    index?.let { cell(it, block) } ?: cell(block)
}