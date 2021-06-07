package io.github.voytech.tabulate.template

import io.github.voytech.tabulate.template.context.FlushingRenderingContext

class IteratingTabulationHandler<T, O>(private val output: O) : TabulationHandler<Iterable<T>, T, O, FlushingRenderingContext<O>> {
    override fun orchestrate(
        source: Iterable<T>,
        templateApi: TableExportTemplateApi<T, O>,
        renderingContext: FlushingRenderingContext<O>,
    ): O {
        templateApi.begin()
        source.forEach{ templateApi.renderNextRow(it) }
        templateApi.end(output)
        return output
    }
}