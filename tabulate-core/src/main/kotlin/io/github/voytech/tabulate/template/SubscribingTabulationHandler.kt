package io.github.voytech.tabulate.template

import io.github.voytech.tabulate.template.context.FlushingRenderingContext
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

class SubscribingTabulationHandler<T, O>(private val output: O) : TabulationHandler<Publisher<T>,T, O, FlushingRenderingContext<O>> {

    inner class UnboundSubscriber(private val templateApi: TableExportTemplateApi<T, O>): Subscriber<T> {

        override fun onSubscribe(subscription: Subscription) {
            templateApi.begin()
            subscription.request(Long.MAX_VALUE)
        }

        override fun onNext(record: T) {
            templateApi.renderNextRow(record)
        }

        override fun onError(t: Throwable?) {
            TODO("Not yet implemented - renderOnError ?")
        }

        override fun onComplete() {
            templateApi.end(output)
        }
    }

    override fun orchestrate(
        source: Publisher<T>,
        templateApi: TableExportTemplateApi<T, O>,
        renderingContext: FlushingRenderingContext<O>,
    ): O {
        source.subscribe(UnboundSubscriber(templateApi))
        return output
    }

}