package pl.voytech.exporter.core.api.builder

import pl.voytech.exporter.core.api.builder.dsl.TableMarker
import pl.voytech.exporter.core.model.attributes.*

@TableMarker
interface Builder<T> {
    fun build(): T
}

abstract class AttributesAware {
    private var attributes: Map<Class<out Attribute>, Set<Attribute>> = emptyMap()

    @JvmSynthetic
    open fun attributes(vararg attribute: Attribute) {
        attribute.forEach {
            supportedAttributeClasses().find { clazz -> clazz.isAssignableFrom(it.javaClass) }
                ?.let { baseClass ->
                    attributes = attributes + Pair(
                        baseClass,
                        attributes[baseClass]?.let { extensionSet -> extensionSet + it } ?: setOf(it)
                    )
                }
        }
    }

    @JvmSynthetic
    open fun attributes(vararg builders: AttributeBuilder<out Attribute>) {
        attributes(*(builders.map { it.build() }).toTypedArray())
    }

    @Suppress("UNCHECKED_CAST")
    @JvmSynthetic
    internal fun <C : Attribute> getAttributesByClass(clazz: Class<C>): Set<C>? = attributes[clazz] as Set<C>?

    @JvmSynthetic
    internal abstract fun supportedAttributeClasses(): Set<Class<out Attribute>>
}

abstract class AttributesAwareBuilder<T> : AttributesAware(), Builder<T>

interface AttributeBuilder<T : Attribute> : Builder<T>

interface CellAttributeBuilder : AttributeBuilder<CellAttribute>

interface RowAttributeBuilder : AttributeBuilder<RowAttribute>

interface ColumnAttributeBuilder : AttributeBuilder<ColumnAttribute>

interface TableAttributeBuilder : AttributeBuilder<TableAttribute>