package pl.voytech.exporter.core.template

interface Sink<T> {
    fun onStart()
    fun onNext(record: T)
    fun onComplete()
    fun onError(throwable: Throwable) {
        error(throwable)
    }
}