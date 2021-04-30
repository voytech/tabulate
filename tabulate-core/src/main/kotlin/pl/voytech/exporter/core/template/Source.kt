package pl.voytech.exporter.core.template

fun interface Source<T> {
    fun subscribe(sink: Sink<T>)
}