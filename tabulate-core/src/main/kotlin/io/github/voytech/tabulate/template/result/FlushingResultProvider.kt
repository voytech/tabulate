package io.github.voytech.tabulate.template.result

import io.github.voytech.tabulate.template.context.RenderingContext

fun interface FlushingResultProvider<CTX: RenderingContext,O>: ResultProvider<CTX> {
    fun flush(context: CTX, output: O)
}