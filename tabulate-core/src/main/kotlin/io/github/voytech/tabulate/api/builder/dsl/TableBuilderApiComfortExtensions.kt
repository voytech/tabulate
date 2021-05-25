package io.github.voytech.tabulate.api.builder.dsl

import io.github.voytech.tabulate.model.RowCellExpression
import kotlin.reflect.KProperty1

class HeaderBuilderApi<T>(val builder: RowsBuilderApi<T>) {

    @JvmSynthetic
    fun cell(id: String, block: CellBuilderApi<T>.() -> Unit) {
        builder.row(HEADER_ROW_INDEX) {
            cells {
                cell(id, block)
            }
        }
    }

    @JvmSynthetic
    fun cell(ref: KProperty1<T, Any?>, block: CellBuilderApi<T>.() -> Unit) {
        builder.row(HEADER_ROW_INDEX) {
            cells {
                cell(ref, block)
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
        insertWhen { it.rowIndex == 0 && !it.hasRecord()}
        cells {
            names.forEach {
                cell { value = it }
            }
        }
    }

fun <T> RowsBuilderApi<T>.rowNumberingOn(id: String) {
    row {
        matching { source -> source.rowIndex > 0 }
        cells {
            cell(id) {
                expression = RowCellExpression { source -> source.rowIndex + 1 }
            }
        }
    }
}