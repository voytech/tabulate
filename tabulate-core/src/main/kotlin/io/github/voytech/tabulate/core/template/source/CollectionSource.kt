package io.github.voytech.tabulate.core.template.source

import org.reactivestreams.Subscriber

@Suppress("ReactiveStreamsPublisherImplementation")
class CollectionSource<T>(private val collection: Collection<T>) : Source<T>() {

    override fun handleData(subscriber: Subscriber<in T>) {
        collection.forEach { element -> subscriber.onNext(element) }
    }
}