package io.github.voytech.tabulate.api.builder.dsl

import io.github.voytech.tabulate.model.RowCellExpression
import io.github.voytech.tabulate.template.context.IndexLabel
import kotlin.reflect.KProperty1

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

fun <T> RowBuilderApi<T>.cell(id: String, block: CellBuilderApi<T>.() -> Unit) {
    cells {
        cell(id, block)
    }
}

fun <T> RowBuilderApi<T>.cell(index: Int, block: CellBuilderApi<T>.() -> Unit) {
    cells {
        cell(index, block)
    }
}

fun <T> RowBuilderApi<T>.cell(ref: KProperty1<T, Any?>, block: CellBuilderApi<T>.() -> Unit) {
    cells {
        cell(ref, block)
    }
}

fun <T> RowBuilderApi<T>.cell(block: CellBuilderApi<T>.() -> Unit) {
    cells {
        cell(block)
    }
}

fun <T> RowsBuilderApi<T>.header(block: HeaderBuilderApi<T>.() -> Unit) =
    HeaderBuilderApi(this).apply(block)


fun <T> RowsBuilderApi<T>.header(vararg names: String) =
    row {
        insertWhen { it.rowIndexValue() == 0 && !it.hasRecord() }
        cells {
            names.forEach {
                cell { value = it }
            }
        }
    }

fun <T> RowsBuilderApi<T>.rowNumberingOn(id: String) {
    row {
        matching { source -> source.rowIndexValue() > 0 && source.rowIndex.labels.isEmpty() }
        cells {
            cell(id) {
                expression = RowCellExpression { source -> source.rowIndexValue() }
            }
        }
    }
}

fun <T> RowsBuilderApi<T>.footer(block: RowBuilderApi<T>.() -> Unit) {
    row(0, IndexLabel.TRAILING_ROWS, block)
}

fun <T> RowsBuilderApi<T>.trailingRow(block: RowBuilderApi<T>.() -> Unit) {
    row(IndexLabel.TRAILING_ROWS, block)
}

fun <T> RowsBuilderApi<T>.trailingRow(index: Int, block: RowBuilderApi<T>.() -> Unit) {
    row(index, IndexLabel.TRAILING_ROWS, block)
}