package io.github.voytech.tabulate.template

import io.github.voytech.tabulate.template.context.RenderingContext
import io.github.voytech.tabulate.template.result.ResultProvider

/**
 * A [TabulationHandler] implementations orchestrates exporting by calling [TabulationTemplateApi] template API methods.
 *
 * @author Wojciech Mąka
 */
fun interface TabulationHandler<I, T, O, CTX: RenderingContext, R: ResultProvider<CTX>> {

    /**
     * Invoked by [TabulationTemplate] in order run table export.
     *
     * @param source - generic parameter representing source of table data.
     * @param templateApi - an API provided by [TabulationTemplate] for limited control over exporting process.
     * @param renderingContext - generic parameter representing rendering context which holds third party API and state.
     */
    fun orchestrate(source: I, templateApi: TabulationTemplateApi<T>, renderingContext: CTX, resultProvider: R): O

}