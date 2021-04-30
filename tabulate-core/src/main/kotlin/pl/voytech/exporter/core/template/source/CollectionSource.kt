package pl.voytech.exporter.core.template.source

import pl.voytech.exporter.core.template.Sink
import pl.voytech.exporter.core.template.Source

class CollectionSource<T>(private val collection: Collection<T>) : Source<T> {
    override fun subscribe(sink: Sink<T>) {
        sink.onStart()
        collection.forEach {
            sink.onNext(it)
        }
        sink.onComplete()
    }
}