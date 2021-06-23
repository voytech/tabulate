package io.github.voytech.tabulate.template

import io.github.voytech.tabulate.template.context.FlushingRenderingContext
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

/**
 * Reactive, subscribing (chain terminating) version of exporter.
 *
 * @author Wojciech Mąka
 */
class SubscribingTabulationHandler<T, O>(private val output: O) : TabulationHandler<Publisher<T>,T, O, FlushingRenderingContext<O>> {

    @Suppress("ReactiveStreamsSubscriberImplementation")
    inner class UnboundSubscriber(
        private val templateApi: TableExportTemplateApi<T>,
        private val renderingContext: FlushingRenderingContext<O>): Subscriber<T> {

        override fun onSubscribe(subscription: Subscription) {
            templateApi.begin()
            subscription.request(Long.MAX_VALUE)
        }

        override fun onNext(record: T) {
            templateApi.nextRow(record)
        }

        override fun onError(t: Throwable?) {
            TODO("can continue, can fail entire export, can retry entire export as separate job")
        }

        override fun onComplete() {
            templateApi.end()
            renderingContext.write(output)
        }
    }

    /**
     * Invoked by [TableExportTemplate] in order run table export.
     *
     * @param source - reactive [Publisher] source.
     * @param templateApi - an API provided by [TableExportTemplate] for limited control over exporting process.
     * @param renderingContext - a [FlushingRenderingContext] context performs output flushing at the end.
     */
    override fun orchestrate(
        source: Publisher<T>,
        templateApi: TableExportTemplateApi<T>,
        renderingContext: FlushingRenderingContext<O>,
    ): O {
        source.subscribe(UnboundSubscriber(templateApi, renderingContext))
        return output
    }

}