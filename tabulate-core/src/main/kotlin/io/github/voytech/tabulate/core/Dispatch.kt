package io.github.voytech.tabulate.core

import java.util.function.Predicate

inline fun <reified C : Any> reify(): Class<C> = C::class.java

interface Invoke

fun interface InvokeWithTwoParams<T1, T2, R> : Invoke {
    operator fun invoke(t1: T1, t2: T2): R
}

fun interface InvokeWithThreeParams<T1, T2, T3> : Invoke {
    operator fun invoke(t1: T1, t2: T2, t3: T3)
}

sealed class ParamTypeInfo

data class TwoParamsTypeInfo<T1, T2>(
    internal val t1: Class<T1>, internal val t2: Class<T2>
) : ParamTypeInfo()

data class ThreeParamsTypeInfo<T1, T2, T3>(
    internal val t1: Class<T1>, internal val t2: Class<T2>, internal val t3: Class<T3>
) : ParamTypeInfo() {
    fun firstTwoParamTypes(): TwoParamsTypeInfo<T1, T2> = TwoParamsTypeInfo(t1, t2)
}

class ReifiedInvocation<C : Invoke, P : ParamTypeInfo>(val meta: P, val delegate: C)

operator fun <T1, T2, OP : InvokeWithTwoParams<T1, T2,*>> ReifiedInvocation<OP, TwoParamsTypeInfo<T1, T2>>.invoke(
    t1: T1, t2: T2
) = delegate.invoke(t1, t2)

operator fun <T1, T2, T3, OP : InvokeWithThreeParams<T1, T2, T3>> ReifiedInvocation<OP, ThreeParamsTypeInfo<T1, T2, T3>>.invoke(
    t1: T1, t2: T2, t3: T3
) = delegate.invoke(t1, t2, t3)

inline fun <reified T1, reified T2, C2 : InvokeWithTwoParams<T1, T2,*>> C2.reifyParameters(): ReifiedInvocation<C2, TwoParamsTypeInfo<T1, T2>> =
    ReifiedInvocation(TwoParamsTypeInfo(T1::class.java, T2::class.java), this)

inline fun <reified T1, reified T2, reified T3, C3 : InvokeWithThreeParams<T1, T2, T3>> C3.reifyParameters(): ReifiedInvocation<C3, ThreeParamsTypeInfo<T1, T2, T3>> =
    ReifiedInvocation(ThreeParamsTypeInfo(T1::class.java, T2::class.java, T3::class.java), this)

fun interface Transformer<C : Invoke, P : ParamTypeInfo> : (ReifiedInvocation<C, P>) -> C

sealed class GenericParamsBasedDispatch<C : Invoke, P : ParamTypeInfo>(val container: MutableMap<P, ReifiedInvocation<C, P>> = mutableMapOf()) {
    fun transform(mapping: Transformer<C, P>) {
        container.replaceAll { typeInfo, consumer -> ReifiedInvocation(typeInfo, mapping(consumer)) }
    }

    fun isEmpty(): Boolean = container.isEmpty()

    fun find(predicate: Predicate<P>): List<ReifiedInvocation<C, P>> =
        container.values.filter { predicate.test(it.meta) }
}

@Suppress("UNCHECKED_CAST")
class TwoParamsBasedDispatch : GenericParamsBasedDispatch<InvokeWithTwoParams<*, *, *>, TwoParamsTypeInfo<*, *>>() {
    inline fun <reified T1, reified T2> InvokeWithTwoParams<T1, T2, *>.bind(): Unit =
        reifyParameters().let {
            container[it.meta] = it as ReifiedInvocation<InvokeWithTwoParams<*, *, *>, TwoParamsTypeInfo<*, *>>
        }

    fun <T1, T2> InvokeWithTwoParams<T1, T2, *>.bind(clazz1: Class<T1>, clazz2: Class<T2>): Unit =
        ReifiedInvocation(TwoParamsTypeInfo(clazz1, clazz2), this).let {
            container[it.meta] = it as ReifiedInvocation<InvokeWithTwoParams<*, *, *>, TwoParamsTypeInfo<*, *>>
        }

    operator fun <T1 : Any, T2 : Any> invoke(t1: T1, t2: T2) {
        TwoParamsTypeInfo(t1::class.java, t2::class.java).let {
            (container[it]?.delegate as? InvokeWithTwoParams<T1, T2, *>)?.invoke(t1, t2)
        }
    }

    operator fun <T1 : Any, T2 : Any> get(t1: T1, t2: T2): InvokeWithTwoParams<T1, T2, *>? =
        container[TwoParamsTypeInfo(t1::class.java, t2::class.java)]?.delegate as? InvokeWithTwoParams<T1, T2, *>

}

@Suppress("UNCHECKED_CAST")
class ThreeParamsBasedDispatch : GenericParamsBasedDispatch<InvokeWithThreeParams<*, *, *>, ThreeParamsTypeInfo<*, *, *>>() {

    inline fun <reified T1, reified T2, reified T3> InvokeWithThreeParams<T1, T2, T3>.bind(): Unit =
        reifyParameters().let {
            container[it.meta] = it as ReifiedInvocation<InvokeWithThreeParams<*, *, *>, ThreeParamsTypeInfo<*, *, *>>
        }

    fun <T1, T2, T3> InvokeWithThreeParams<T1, T2, T3>.bind(
        clazz1: Class<T1>, clazz2: Class<T2>, clazz3: Class<T3>,
    ): Unit = ReifiedInvocation(ThreeParamsTypeInfo(clazz1, clazz2, clazz3), this).let {
        container[it.meta] = it as ReifiedInvocation<InvokeWithThreeParams<*, *, *>, ThreeParamsTypeInfo<*, *, *>>
    }

    operator fun <T1 : Any, T2 : Any, T3 : Any> invoke(t1: T1, t2: T2, t3: T3) {
        ThreeParamsTypeInfo(t1::class.java, t2::class.java, t3::class.java).let {
            (container[it]?.delegate as? InvokeWithThreeParams<T1, T2, T3>)?.invoke(t1, t2, t3)
        }
    }
}
