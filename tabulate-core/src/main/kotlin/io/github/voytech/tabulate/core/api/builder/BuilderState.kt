package io.github.voytech.tabulate.core.api.builder

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