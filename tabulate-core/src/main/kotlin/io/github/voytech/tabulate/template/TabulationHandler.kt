package io.github.voytech.tabulate.template

import io.github.voytech.tabulate.template.context.RenderingContext

fun interface TabulationHandler<T, O, CTX: RenderingContext> {

    fun orchestrate(
        source: Iterable<T>, templateApi: TableExportTemplateApi<T, O>, renderingContext: CTX,
    ): O

}