package io.github.voytech.tabulate.api.builder.dsl

import io.github.voytech.tabulate.model.attributes.cell.CellAlignmentAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellBackgroundAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellBordersAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellTextStylesAttribute


@TabulateMarker
class CellStyleBuilderApi<T> private constructor(private val builder: ColumnBuilderApi<T>) {

    @JvmSynthetic
    fun text(block: CellTextStylesAttribute.Builder.() -> Unit) =
        CellTextStylesAttribute.Builder()
            .apply(block).build()
            .let { builder.attributes { attribute(it) } }

    @JvmSynthetic
    fun borders(block: CellBordersAttribute.Builder.() -> Unit) =
        CellBordersAttribute.Builder()
            .apply(block).build()
            .let { builder.attributes { attribute(it) } }

    @JvmSynthetic
    fun background(block: CellBackgroundAttribute.Builder.() -> Unit) =
        CellBackgroundAttribute.Builder()
            .apply(block).build()
            .let { builder.attributes { attribute(it) } }

    @JvmSynthetic
    fun alignment(block: CellAlignmentAttribute.Builder.() -> Unit) =
        CellAlignmentAttribute.Builder()
            .apply(block).build()
            .let { builder.attributes { attribute(it) } }


    companion object {
        @JvmSynthetic
        internal fun <T> new(builder: ColumnBuilderApi<T>): CellStyleBuilderApi<T> = CellStyleBuilderApi(builder)
    }

}

fun <T> ColumnBuilderApi<T>.style(block: CellStyleBuilderApi<T>.() -> Unit) =
    CellStyleBuilderApi.new(this).apply(block)