package pl.voytech.exporter.core.template.source

import pl.voytech.exporter.core.template.Sink
import pl.voytech.exporter.core.template.Source

class EmptySource<T>() : Source<T> {
    override fun subscribe(sink: Sink<T>) {
        sink.onStart()
        sink.onComplete()
    }
}