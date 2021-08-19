package io.github.voytech.tabulate.template.result

import io.github.voytech.tabulate.template.context.RenderingContext

/**
 * Having rendering context, get rendered data and pass to the output.
 * this method can be implemented for various scenarios.
 * It can call context 3rd party method to flush entire content to the output, ot it
 * can take recently rendered row content and append to the output.
 */
fun interface FlushingResultProvider<CTX: RenderingContext,O>: ResultProvider<CTX> {
    fun flush(context: CTX, output: O)
}