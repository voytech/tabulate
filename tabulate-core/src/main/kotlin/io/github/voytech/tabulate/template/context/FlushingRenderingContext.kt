package io.github.voytech.tabulate.template.context

fun interface FlushingRenderingContext<O>: RenderingContext {
    fun write(output: O)
}