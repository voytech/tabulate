package io.github.voytech.tabulate.template

import io.github.voytech.tabulate.template.context.RenderingContext

fun interface TabulationHandler<I, T, O, CTX: RenderingContext> {

    fun orchestrate(source: I, templateApi: TableExportTemplateApi<T>, renderingContext: CTX): O

}