package io.github.voytech.tabulate.template

import io.github.voytech.tabulate.template.context.WritableRenderingContext

class BaseTabulationHandler<T, O>(private val output: O) : TabulationHandler<T, O, WritableRenderingContext<O>> {
    override fun orchestrate(
        source: Iterable<T>,
        templateApi: TableExportTemplateApi<T, O>,
        renderingContext: WritableRenderingContext<O>,
    ): O {
        templateApi.begin()
        source.forEach{ templateApi.renderNextRow(it) }
        templateApi.end(output)
        return output
    }
}