package io.github.voytech.tabulate.template.context

interface FlushingRenderingContext<O>: RenderingContext {
    fun write(output: O)
}