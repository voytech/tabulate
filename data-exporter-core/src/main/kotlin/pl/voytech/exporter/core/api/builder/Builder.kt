package pl.voytech.exporter.core.api.builder

import pl.voytech.exporter.core.api.builder.dsl.TableMarker
import pl.voytech.exporter.core.model.extension.*

@TableMarker
interface Builder<T> {
    fun build(): T
}

abstract class ExtensionsAwareBuilder<T> : Builder<T> {

    private var extensions: Map<Class<out Extension>, Set<Extension>> = emptyMap()

    @JvmSynthetic
    fun extensions(vararg extension: Extension) {
        extension.forEach {
            supportedExtensionClasses().find { clazz -> clazz.isAssignableFrom(it.javaClass) }
                ?.let { baseClass ->
                    extensions = extensions + Pair(
                        baseClass,
                        extensions[baseClass]?.let { extensionSet -> extensionSet + it } ?: setOf(it)
                    )
                }
        }
    }

    @JvmSynthetic
    fun extensions(vararg builders: ExtensionBuilder<out Extension>) {
        extensions(*(builders.map { it.build() }).toTypedArray())
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <C: Extension> getExtensionsByClass(clazz: Class<C>): Set<C>? = extensions[clazz] as Set<C>?

    internal abstract fun supportedExtensionClasses(): Set<Class<out Extension>>
}

interface ExtensionBuilder<T : Extension> : Builder<T>

interface CellExtensionBuilder : ExtensionBuilder<CellExtension>

interface RowExtensionBuilder : ExtensionBuilder<RowExtension>

interface ColumnExtensionBuilder : ExtensionBuilder<ColumnExtension>

interface TableExtensionBuilder : ExtensionBuilder<TableExtension>