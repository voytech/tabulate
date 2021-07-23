package io.github.voytech.tabulate.template

import io.github.voytech.tabulate.template.context.RenderingContext
import io.github.voytech.tabulate.template.result.FlushingResultProvider
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

/**
 * Reactive, subscribing (chain terminating) version of exporter.
 *
 * @author Wojciech Mąka
 */
class SubscribingTabulationHandler<T, CTX: RenderingContext, O>(private val output: O) : TabulationHandler<Publisher<T>,T, O, CTX, FlushingResultProvider<CTX,O>> {

    @Suppress("ReactiveStreamsSubscriberImplementation")
    inner class UnboundSubscriber(
        private val templateApi: TabulationTemplateApi<T>,
        private val renderingContext: CTX,
        private val resultProvider: FlushingResultProvider<CTX, O>
    ): Subscriber<T> {

        override fun onSubscribe(subscription: Subscription) {
            templateApi.begin()
            subscription.request(Long.MAX_VALUE)
        }

        override fun onNext(record: T) {
            templateApi.nextRow(record)
        }

        override fun onError(t: Throwable?) { }

        override fun onComplete() {
            templateApi.end()
            resultProvider.flush(renderingContext, output)
        }
    }

    /**
     * Invoked by [TabulationTemplate] in order run table export.
     *
     * @param source - reactive [Publisher] source.
     * @param templateApi - an API provided by [TabulationTemplate] for limited control over exporting process.
     * @param renderingContext - a [FlushingResultProvider] context performs output flushing at the end.
     */
    override fun orchestrate(
        source: Publisher<T>,
        templateApi: TabulationTemplateApi<T>,
        renderingContext: CTX,
        resultProvider: FlushingResultProvider<CTX, O>,
    ): O {
        source.subscribe(UnboundSubscriber(templateApi, renderingContext, resultProvider))
        return output
    }

}