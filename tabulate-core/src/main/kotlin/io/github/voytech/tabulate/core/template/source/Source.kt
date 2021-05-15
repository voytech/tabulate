package io.github.voytech.tabulate.core.template.source

import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

@Suppress("ReactiveStreamsPublisherImplementation")
abstract class Source<T> : Publisher<T> {

    inner class UnboundNoopSubscription : Subscription {

        override fun request(n: Long) { }

        override fun cancel() { }
    }

    override fun subscribe(subscriber: Subscriber<in T>) {
        subscriber.onSubscribe(UnboundNoopSubscription())
        handleData(subscriber)
        subscriber.onComplete()
    }

    abstract fun handleData(subscriber: Subscriber<in T>)
}