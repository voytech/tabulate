package io.github.voytech.tabulate.api.builder.dsl

import io.github.voytech.tabulate.model.RowSelectors

fun <T> RowsBuilderApi<T>.header(block: CellsBuilderApi<T>.() -> Unit) =
    row {
        insertWhen(RowSelectors.asTableHeader())
        cells(block)
    }


fun <T> RowsBuilderApi<T>.header(vararg names: String) =
    row {
        insertWhen(RowSelectors.asTableHeader())
        cells {
            names.forEach {
                cell { value = it }
            }
        }
    }
