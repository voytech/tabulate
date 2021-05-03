package pl.voytech.exporter.core.template.source

import org.reactivestreams.Subscriber

class EmptySource<T> : Source<T>() {

    override fun handleData(subscriber: Subscriber<in T>) { }

}