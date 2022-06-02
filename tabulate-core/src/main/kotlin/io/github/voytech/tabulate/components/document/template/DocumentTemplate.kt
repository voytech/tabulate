package io.github.voytech.tabulate.components.document.template

import io.github.voytech.tabulate.components.document.api.builder.dsl.DocumentBuilderApi
import io.github.voytech.tabulate.components.document.model.Document
import io.github.voytech.tabulate.components.document.operation.DocumentEnd
import io.github.voytech.tabulate.components.document.operation.DocumentStart
import io.github.voytech.tabulate.core.api.builder.dsl.buildModel
import io.github.voytech.tabulate.core.template.*
import io.github.voytech.tabulate.core.template.exception.OutputBindingResolvingException
import io.github.voytech.tabulate.core.template.result.OutputBinding
import io.github.voytech.tabulate.core.template.spi.OutputBindingsProvider
import java.io.File

/**
 * Entry point for document exporting.
 * @author Wojciech Mąka
 * @since 0.*.*
 */
class DocumentTemplate(
    private val format: DocumentFormat,
) {

    private val outputBindingsProvider: OutputBindingsProvider<RenderingContext> by lazy {
        loadFirstByDocumentFormat<OutputBindingsProvider<RenderingContext>, RenderingContext>(format)!!
    }

    fun <O : Any> export(model: Document, output: O) {
        val registry: ExportTemplateApis<RenderingContext> = loadRegistry(format)
        val renderingContext = loadRenderingContext(format)
        val templateContext = DocumentTemplateContext(model)
        val operations = registry.getOperations(model)
        resolveOutputBinding(output).run {
            setOutput(renderingContext, output)
            operations?.render(renderingContext, DocumentStart(templateContext))
            model.nodes.forEach { it.export(renderingContext, templateContext, registry) }
            operations?.render(renderingContext, DocumentEnd(templateContext))
            flush()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <O : Any> resolveOutputBinding(output: O): OutputBinding<RenderingContext, O> {
        return outputBindingsProvider.createOutputBindings()
            .filter {
                it.outputClass().isAssignableFrom(output::class.java)
            }.map { it as OutputBinding<RenderingContext, O> }
            .firstOrNull() ?: throw OutputBindingResolvingException()
    }

}

fun (DocumentBuilderApi.() -> Unit).export(file: File) = file.documentFormat().let {
    DocumentTemplate(it).export(buildModel(DocumentBuilderApi().apply(this)), file.outputStream())
}

fun (DocumentBuilderApi.() -> Unit).export(file: String) = export(File(file))