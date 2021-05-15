package io.github.voytech.tabulate.core.template

import org.reactivestreams.Publisher

fun interface ResultHandler<T, O> {

    fun createResult(source: Publisher<T>): O

}