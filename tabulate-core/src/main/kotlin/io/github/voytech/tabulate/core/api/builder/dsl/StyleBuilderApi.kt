package io.github.voytech.tabulate.core.api.builder.dsl

import io.github.voytech.tabulate.core.api.builder.TableBuilder
import io.github.voytech.tabulate.core.model.attributes.cell.CellTextStylesAttribute


@TabulateMarker
class CellStyleBuilderApi<T> private constructor(private val builder: ColumnBuilderApi<T>) {

    @JvmSynthetic
    fun text(block: CellTextStylesAttribute.Builder.() -> Unit): CellTextStylesAttribute =
        CellTextStylesAttribute.Builder()
            .apply(block).build()


    companion object {
        @JvmSynthetic
        internal fun <T> new(builder: ColumnBuilderApi<T>): CellStyleBuilderApi<T> = CellStyleBuilderApi(builder)
    }

}

fun <T> ColumnBuilderApi<T>.style(block: CellStyleBuilderApi<T>.() -> Unit) =
    CellStyleBuilderApi.new(this).apply(block)