package io.github.voytech.tabulate.core.api.builder

import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.AttributeClassifier
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.Model
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

interface ModelBuilderState<T : Model> : BuilderInterface<T>

interface CompositeModelBuilderState<T : Model> : ModelBuilderState<T> {
    fun <E: Model> bind(node: ModelBuilderState<E>)
}
/**
 * Base class for all table builders that allow creating attributes.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
abstract class AttributesAwareBuilder<T> : InternalBuilder<T>() {

    private var attributes: MutableMap<AttributeClassifier<*,*>, MutableSet<Attribute<*>>> = mutableMapOf()

    @JvmSynthetic
    protected open fun <A : Attribute<A>, B : AttributeBuilder<A>> attribute(builder: B) {
        applyAttribute(builder.build())
    }

    @Suppress("UNCHECKED_CAST")
    @JvmSynthetic
    protected fun <C : Attribute<*>> getAttributesByClassifier(classifier: AttributeClassifier<C,*>): Attributes<C> {
        return Attributes(
            if (attributes.containsKey(classifier)) attributes[classifier] as Set<C> else emptySet(),
            classifier.attributeCategory
        )
    }

    private fun applyAttribute(attribute: Attribute<*>) {
        if (!getUnsupportedClassifiers().contains(attribute.getClassifier())) {
            attributes.computeIfAbsent(attribute.getClassifier()) { mutableSetOf() }.run { add(attribute) }
        }
    }

    abstract fun getUnsupportedClassifiers(): Set<AttributeClassifier<*,*>>

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