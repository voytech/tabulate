package io.github.voytech.tabulate.core.api.builder.dsl

import io.github.voytech.tabulate.core.api.builder.CompositeModelBuilderState
import io.github.voytech.tabulate.core.api.builder.ModelBuilderState
import io.github.voytech.tabulate.core.model.Model

@DslMarker
annotation class TabulateMarker

@TabulateMarker
abstract class ModelBuilderApi<T : Model<T>, B : ModelBuilderState<T>> internal constructor(
    internal val builder: B,
) {
    protected fun getBuilder() = builder
}

@TabulateMarker
abstract class CompositeModelBuilderApi<T : Model<T>, B : CompositeModelBuilderState<T>> internal constructor(
    builder: B,
) : ModelBuilderApi<T, B>(builder) {

    fun <E : Model<E>, R : ModelBuilderApi<E, *>> bind(other: R) = builder.bind(other.builder)

}

fun <T : Model<T>, B : ModelBuilderState<T>> buildModel(api: ModelBuilderApi<T, B>): T =
    api.builder.build()