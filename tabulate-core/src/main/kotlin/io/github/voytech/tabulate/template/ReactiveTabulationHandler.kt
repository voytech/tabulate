package io.github.voytech.tabulate.template

import io.github.voytech.tabulate.template.context.RenderingContext
import org.reactivestreams.Publisher

fun interface ReactiveTabulationHandler<T, O, CTX: RenderingContext> {

    fun orchestrate(source: Publisher<T>, templateApi: TableExportTemplateApi<T,O>, renderingContext: CTX): O

}