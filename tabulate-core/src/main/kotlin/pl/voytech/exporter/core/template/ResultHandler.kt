package pl.voytech.exporter.core.template

fun interface ResultHandler<T, O> {

    fun createResult(source: Source<T>): O

}