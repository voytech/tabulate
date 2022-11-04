package io.github.voytech.tabulate.components.document.template

import io.github.voytech.tabulate.components.document.api.builder.dsl.DocumentBuilderApi
import io.github.voytech.tabulate.components.document.model.Document
import io.github.voytech.tabulate.components.document.operation.DocumentEnd
import io.github.voytech.tabulate.components.document.operation.DocumentStart
import io.github.voytech.tabulate.core.api.builder.dsl.buildModel
import io.github.voytech.tabulate.core.template.*
import java.io.File

typealias StandaloneDocumentTemplate = StandaloneExportTemplate<DocumentTemplate, Document, DocumentTemplateContext>

/**
 * Entry point for document exporting.
 * @author Wojciech Mąka
 * @since 0.*.*
 */
class DocumentTemplate : ExportTemplate<DocumentTemplate, Document, DocumentTemplateContext>() {

    override fun createTemplateContext(parentContext: TemplateContext<*, *>, model: Document): DocumentTemplateContext =
        DocumentTemplateContext(model, parentContext.services)

    override fun doExport(templateContext: DocumentTemplateContext) = with(templateContext) {
        createLayoutScope {
            with(model) {
                render(DocumentStart(templateContext))
                nodes.forEach { it.export(templateContext) }
                render(DocumentEnd(templateContext))
            }
        }
    }

}

fun (DocumentBuilderApi.() -> Unit).export(file: File) = file.documentFormat().let { format ->
    buildModel(DocumentBuilderApi().apply(this)).let { doc ->
        StandaloneDocumentTemplate(format).export(doc, file.outputStream())
    }
}

fun (DocumentBuilderApi.() -> Unit).export(file: String) = export(File(file))