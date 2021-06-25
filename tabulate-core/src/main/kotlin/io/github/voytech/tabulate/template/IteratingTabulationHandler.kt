package io.github.voytech.tabulate.template

import io.github.voytech.tabulate.template.context.RenderingContext
import io.github.voytech.tabulate.template.result.FlushingResultProvider

/**
 * Imperative, [Iterable] implementations supporting version of exporter.
 *
 * @author Wojciech Mąka
 */
class IteratingTabulationHandler<T,CTX: RenderingContext, O>(private val output: O) : TabulationHandler<Iterable<T>, T, O, CTX, FlushingResultProvider<CTX, O>> {
    /**
     * Invoked by [TableExportTemplate] in order run table export.
     *
     * @param source - imperative [Iterable] source.
     * @param templateApi - an API provided by [TableExportTemplate] for limited control over exporting process.
     * @param renderingContext - a [FlushingResultProvider] context performs output flushing at the end.
     */
    override fun orchestrate(
        source: Iterable<T>,
        templateApi: TableExportTemplateApi<T>,
        renderingContext: CTX,
        resultProvider: FlushingResultProvider<CTX,O>,
    ): O {
        templateApi.begin()
        source.forEach{ templateApi.nextRow(it) }
        templateApi.end()
        resultProvider.flush(renderingContext, output)
        return output
    }
}