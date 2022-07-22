package io.github.voytech.tabulate.core.api.builder

import io.github.voytech.tabulate.api.builder.exception.BuilderException
import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.template.loadAttributeConstraints
import kotlin.properties.ObservableProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

typealias DslBlock<T> = (T) -> Unit

sealed interface BuilderInterface<T> {
    fun build(): T
}

/**
 * Basic builder contract.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
abstract class Builder<T> {
    @JvmSynthetic
    internal abstract fun build(): T
}

abstract class InternalBuilder<T> : BuilderInterface<T> {
    @JvmSynthetic
    abstract override fun build(): T
}

interface ModelBuilderState<T : Model<T>> : BuilderInterface<T>

interface CompositeModelBuilderState<T : Model<T>> : ModelBuilderState<T> {
    fun <E : Model<E>> bind(node: ModelBuilderState<E>)
}

abstract class ModelPartBuilderState<T : ModelPart> : BuilderInterface<T>

/**
 * Abstract helper class for nesting model part builders in a way that helps to achieve builder method invocation idempotence.
 * @author Wojciech Mąka
 * @since 0.*.0
 */
abstract class ModelPartBuilderCollection<K : Any, M : ModelPart, P : BuilderInterface<*>, B : BuilderInterface<M>>(
    protected val parentBuilder: P,
    private val transform: ((List<M>) -> List<M>)? = null,
) {
    private val builders: MutableMap<K, B> = LinkedHashMap()

    fun size(): Int = builders.size

    protected fun ensureBuilder(key: K): B = builders.computeIfAbsent(key, ::createBuilder)

    protected fun ensureBuilder(index: Int): B {
        val atIndex = builders.values.toList().getOrNull(index)
        return atIndex ?: createBuilder(index)!!.let {
            builders[it.first] = it.second
            it.second
        }
    }

    protected abstract fun createBuilder(key: K): B

    protected open fun createBuilder(index: Int): Pair<K, B>? = null

    fun entries() = builders.entries

    fun moveAt(key: K, index: Int): Boolean =
        get(key)?.takeIf { it.orCollides(index) }?.let { value ->
            builders.entries.asSequence()
                .mapIndexed { idx, entry -> IndexedValue(idx, entry.toPair()) }
                .filter { it.value.first != key }
                .toMutableList().apply { add(IndexedValue(index, key to value)) }
                .sortedBy { it.index }
        }?.let { copy ->
            builders.clear()
            copy.forEach {
                builders[it.value.first] = it.value.second
            }.let { true }
        } ?: false

    fun values(): List<B> = builders.values.toList()

    operator fun get(key: K): B? = builders[key]

    operator fun get(index: Int): B? = builders.values.toList().getOrNull(index)

    fun find(predicate: (B) -> Boolean): B? = builders.values.find(predicate)

    fun build(): List<M> = builders.values.map { it.build() }.let {
        transform?.invoke(it) ?: it
    }

    fun buildMap(): Map<K, M> = builders.mapValues { it.value.build() }

    private fun B.orCollides(index: Int): Boolean = get(index)?.let { existing ->
        (existing != this).also {
            if (it) {
                throw BuilderException("Could not move builder at index $index because index is in use by another builder.")
            }
        }
    } ?: true

}

/**
 * Base class for all table builders that allow creating attributes.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
abstract class AttributesAwareBuilder<T : AttributedModelOrPart<T>> : InternalBuilder<T>() {

    private var attributes: MutableSet<Attribute<*>> = mutableSetOf()

    private val constraints: AttributesConstraints by lazy {
        loadAttributeConstraints()
    }

    private val constraintsForModel: List<ModelAttributeConstraint<*, *>> by lazy {
        constraints.disabledOnModels[modelClass()] ?: emptyList()
    }

    abstract fun modelClass(): Class<T>

    @JvmSynthetic
    protected open fun <A : Attribute<A>, B : AttributeBuilder<A>> attribute(builder: B) {
        with(builder.build()) {
            if (!constraintsForModel.contains(ModelAttributeConstraint(modelClass(), javaClass))) {
                attributes.add(this)
            }
        }
    }

    /**
     * Collects all attributes from stateful builder managed set, and merges them all by class by wrapping
     * in [Attributes] class
     * @author Wojciech Mąka
     * @since 0.2.0
     */
    fun attributes(): Attributes<T> = Attributes(modelClass(), attributes)

}


/**
 * Base class for all attribute builders. Needs to be derived by all custom attribute builders in order to correctly
 * merge two attributes. Internally it tracks property changes that are copied onto attribute instance when
 * builder is materialized. This helps to choose only mutated property values as winners when attribute merging occurs.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
abstract class AttributeBuilder<T : Attribute<*>> : Builder<T>() {
    private val propertyChanges = mutableSetOf<KProperty<*>>()
    private val mappings = mutableMapOf<String, String>()
    fun <F> observable(initialValue: F, fieldMapping: Pair<String, String>? = null): ReadWriteProperty<Any?, F> {
        if (fieldMapping != null) {
            mappings[fieldMapping.first] = fieldMapping.second
        }
        return object : ObservableProperty<F>(initialValue) {
            override fun afterChange(property: KProperty<*>, oldValue: F, newValue: F) {
                propertyChanges.add(property)
            }
        }
    }

    protected abstract fun provide(): T

    @JvmSynthetic
    final override fun build(): T {
        return provide().apply {
            nonDefaultProps = propertyChanges.map {
                if (mappings.containsKey(it.name)) mappings[it.name]!! else it.name
            }.toSet()
        }
    }
}