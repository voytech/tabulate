package io.github.voytech.tabulate.template

import io.github.voytech.tabulate.template.context.FlushingRenderingContext

/**
 * Imperative, [Iterable] implementations supporting version of exporter.
 *
 * @author Wojciech Mąka
 */
class IteratingTabulationHandler<T, O>(private val output: O) : TabulationHandler<Iterable<T>, T, O, FlushingRenderingContext<O>> {
    /**
     * Invoked by [TableExportTemplate] in order run table export.
     *
     * @param source - imperative [Iterable] source.
     * @param templateApi - an API provided by [TableExportTemplate] for limited control over exporting process.
     * @param renderingContext - a [FlushingRenderingContext] context performs output flushing at the end.
     */
    override fun orchestrate(
        source: Iterable<T>,
        templateApi: TableExportTemplateApi<T>,
        renderingContext: FlushingRenderingContext<O>,
    ): O {
        templateApi.begin()
        source.forEach{ templateApi.nextRow(it) }
        templateApi.end()
        renderingContext.write(output)
        return output
    }
}