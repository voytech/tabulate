package io.github.voytech.tabulate.template

import io.github.voytech.tabulate.template.context.RenderingContext

/**
 * A [TabulationHandler] implementations orchestrates exporting by calling [TableExportTemplateApi] template API.
 *
 * @author Wojciech Mąka
 */
fun interface TabulationHandler<I, T, O, CTX: RenderingContext> {

    /**
     * Invoked by [TableExportTemplate] in order run table export.
     *
     * @param source - generic parameter representing source of table data.
     * @param templateApi - an API provided by [TableExportTemplate] for limited control over exporting process.
     * @param renderingContext - generic parameter representing rendering context which holds third party API and state.
     */
    fun orchestrate(source: I, templateApi: TableExportTemplateApi<T>, renderingContext: CTX): O

}