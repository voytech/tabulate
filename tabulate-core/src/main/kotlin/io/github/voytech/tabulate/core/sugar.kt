package io.github.voytech.tabulate.core

inline fun <reified C: Any> reify(): Class<C> = C::class.java