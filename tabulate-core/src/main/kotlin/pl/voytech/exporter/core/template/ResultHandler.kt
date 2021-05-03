package pl.voytech.exporter.core.template

import org.reactivestreams.Publisher

fun interface ResultHandler<T, O> {

    fun createResult(source: Publisher<T>): O

}