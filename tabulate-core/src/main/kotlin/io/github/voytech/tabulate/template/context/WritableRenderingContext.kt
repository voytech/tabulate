package io.github.voytech.tabulate.template.context

interface WritableRenderingContext<O>: RenderingContext {
    fun write(output: O)
}