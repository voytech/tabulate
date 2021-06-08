package io.github.voytech.tabulate.template

import io.github.voytech.tabulate.template.context.VoidRenderingContext

class NoContextBaseTabulationHandler<T, O>(private val output: O) : TabulationHandler<Iterable<T>, T, O, VoidRenderingContext> {
    override fun orchestrate(
        source: Iterable<T>,
        templateApi: TableExportTemplateApi<T, O>,
        renderingContext: VoidRenderingContext,
    ): O {
        templateApi.begin()
        source.forEach{ templateApi.nextRow(it) }
        templateApi.end(output)
        return output
    }
}