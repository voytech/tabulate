package io.github.voytech.tabulate.components.document.api.builder.dsl

import io.github.voytech.tabulate.components.document.api.builder.DocumentBuilderState
import io.github.voytech.tabulate.components.document.model.Document
import io.github.voytech.tabulate.core.api.builder.AttributeBuilder
import io.github.voytech.tabulate.core.api.builder.dsl.CompositeModelBuilderApi
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker
import io.github.voytech.tabulate.core.api.builder.dsl.buildModel


/**
 * Kotlin type-safe DSL table builder API for defining entire table.
 * Internally operates on corresponding builder state that is eventually materialized to table model.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
@TabulateMarker
class DocumentBuilderApi internal constructor() : CompositeModelBuilderApi<Document, DocumentBuilderState>(
    DocumentBuilderState()) {

    @set:JvmSynthetic
    @get:JvmSynthetic
    var name: String by this.builder::name

    @JvmSynthetic
    fun attributes(block: DocumentLevelAttributesBuilderApi.() -> Unit) {
        DocumentLevelAttributesBuilderApi(builder).apply(block)
    }
}

fun document(block: DocumentBuilderApi.() -> Unit): DocumentBuilderApi.() -> Unit = block

/**
 * Entry point function taking type-safe DSL table builder API as a parameter.
 * Materializes internal builder state and returns read-only [Document] model.
 * @return [Document]
 * @author Wojciech Mąka
 * @since 0.*.*
 */
fun createDocument(block: DocumentBuilderApi.() -> Unit): Document = buildModel(DocumentBuilderApi().apply(block))

/**
 * Kotlin type-safe DSL table attribute builder API for defining document level attributes.
 * Internally operates on corresponding builder state that is eventually materialized to table model.
 * @author Wojciech Mąka
 * @since 0.3.0
 */
@TabulateMarker
class DocumentLevelAttributesBuilderApi internal constructor(private val builderState: DocumentBuilderState) {
    @JvmSynthetic
    fun attribute(attribute: AttributeBuilder<*>) {
        builderState.attribute(attribute)
    }
}