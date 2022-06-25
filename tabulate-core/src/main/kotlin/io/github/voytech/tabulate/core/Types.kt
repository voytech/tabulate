package io.github.voytech.tabulate.core

import java.util.function.Predicate

inline fun <reified C : Any> reify(): Class<C> = C::class.java

interface Invoke
fun interface InvokeWithOneParam<T1> : Invoke {
    operator fun invoke(t1: T1)
}

fun interface InvokeWithTwoParams<T1, T2> : Invoke {
    operator fun invoke(t1: T1, t2: T2)
}

fun interface InvokeWithThreeParams<T1, T2, T3> : Invoke {
    operator fun invoke(t1: T1, t2: T2, t3: T3)
}


class TypesInfo(val types: List<Class<*>>) {

    private val id: String = types.joinToString(",")

    fun dropLast(number: Int): TypesInfo = TypesInfo(types.dropLast(number))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as TypesInfo
        if (id != other.id) return false
        return true
    }

    override fun hashCode(): Int = id.hashCode()

    companion object {
        operator fun invoke(vararg type: Class<*>) = TypesInfo(type.toList())

    }

}

class ReifiedInvocation<C : Invoke>(val meta: TypesInfo, val delegate: C)

operator fun <T1, OP : InvokeWithOneParam<T1>> ReifiedInvocation<OP>.invoke(t1: T1) = delegate.invoke(t1)

operator fun <T1, T2, OP : InvokeWithTwoParams<T1, T2>> ReifiedInvocation<OP>.invoke(t1: T1, t2: T2) =
    delegate.invoke(t1, t2)

operator fun <T1, T2, T3, OP : InvokeWithThreeParams<T1, T2, T3>> ReifiedInvocation<OP>.invoke(t1: T1, t2: T2, t3: T3) =
    delegate.invoke(t1, t2, t3)


inline fun <reified T1, C1 : InvokeWithOneParam<T1>> C1.reifyParameters(): ReifiedInvocation<C1> =
    ReifiedInvocation(TypesInfo(T1::class.java), this)

inline fun <reified T1, reified T2, C2 : InvokeWithTwoParams<T1, T2>> C2.reifyParameters(): ReifiedInvocation<C2> =
    ReifiedInvocation(TypesInfo(T1::class.java, T2::class.java), this)

inline fun <reified T1, reified T2, reified T3, C3 : InvokeWithThreeParams<T1, T2, T3>> C3.reifyParameters(): ReifiedInvocation<C3> =
    ReifiedInvocation(TypesInfo(T1::class.java, T2::class.java, T3::class.java), this)

typealias Transformer<C> = (ReifiedInvocation<C>) -> C


sealed class GenericParamsBasedDispatch<C : Invoke>(val container: MutableMap<TypesInfo, ReifiedInvocation<C>> = mutableMapOf()) {
    fun transform(mapping: Transformer<C>) {
        container.replaceAll { typeInfo, consumer -> ReifiedInvocation(typeInfo, mapping(consumer)) }
    }

    fun isEmpty(): Boolean = container.isEmpty()

    fun find(predicate: Predicate<TypesInfo>): List<ReifiedInvocation<C>> =
        container.values.filter { predicate.test(it.meta) }
}

@Suppress("UNCHECKED_CAST")
class SingleParamBasedDispatch : GenericParamsBasedDispatch<InvokeWithOneParam<*>>() {
    inline fun <reified T1 : Any> InvokeWithOneParam<T1>.bind(): Unit =
        reifyParameters().let { container[it.meta] = it as ReifiedInvocation<InvokeWithOneParam<*>> }

    fun <T1 : Any> InvokeWithOneParam<T1>.bind(clazz: Class<T1>): Unit =
        ReifiedInvocation(TypesInfo(clazz), this).let {
            container[it.meta] = it as ReifiedInvocation<InvokeWithOneParam<*>>
        }

    operator fun <T1 : Any> invoke(t1: T1) {
        TypesInfo(t1::class.java).let {
            (container[it]?.delegate as? InvokeWithOneParam<T1>)?.invoke(t1)
        }
    }
}

@Suppress("UNCHECKED_CAST")
class TwoParamsBasedDispatch : GenericParamsBasedDispatch<InvokeWithTwoParams<*, *>>() {
    inline fun <reified T1, reified T2> InvokeWithTwoParams<T1, T2>.bind(): Unit =
        reifyParameters().let { container[it.meta] = it as ReifiedInvocation<InvokeWithTwoParams<*, *>> }

    fun <T1, T2> InvokeWithTwoParams<T1, T2>.bind(clazz1: Class<T1>, clazz2: Class<T2>): Unit =
        ReifiedInvocation(TypesInfo(clazz1, clazz2), this).let {
            container[it.meta] = it as ReifiedInvocation<InvokeWithTwoParams<*, *>>
        }

    operator fun <T1 : Any, T2 : Any> invoke(t1: T1, t2: T2) {
        TypesInfo(t1::class.java, t2::class.java).let {
            (container[it]?.delegate as? InvokeWithTwoParams<T1, T2>)?.invoke(t1, t2)
        }
    }
}

@Suppress("UNCHECKED_CAST")
class ThreeParamsBasedDispatch : GenericParamsBasedDispatch<InvokeWithThreeParams<*, *, *>>() {
    inline fun <reified T1, reified T2, reified T3> InvokeWithThreeParams<T1, T2, T3>.bind(): Unit =
        reifyParameters().let { container[it.meta] = it as ReifiedInvocation<InvokeWithThreeParams<*, *, *>> }

    fun <T1, T2, T3> InvokeWithThreeParams<T1, T2, T3>.bind(
        clazz1: Class<T1>,
        clazz2: Class<T2>,
        clazz3: Class<T3>,
    ): Unit =
        ReifiedInvocation(TypesInfo(clazz1, clazz2, clazz3), this).let {
            container[it.meta] = it as ReifiedInvocation<InvokeWithThreeParams<*, *, *>>
        }

    operator fun <T1 : Any, T2 : Any, T3 : Any> invoke(t1: T1, t2: T2, t3: T3) {
        TypesInfo(t1::class.java, t2::class.java, t3::class.java).let {
            (container[it]?.delegate as? InvokeWithThreeParams<T1, T2, T3>)?.invoke(t1, t2, t3)
        }
    }
}
