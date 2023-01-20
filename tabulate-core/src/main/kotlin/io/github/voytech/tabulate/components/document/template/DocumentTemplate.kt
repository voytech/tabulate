package io.github.voytech.tabulate.components.document.template

import io.github.voytech.tabulate.components.document.api.builder.dsl.DocumentBuilderApi
import io.github.voytech.tabulate.components.document.model.Document
import io.github.voytech.tabulate.core.api.builder.dsl.buildModel
import io.github.voytech.tabulate.core.template.StandaloneExportTemplate
import io.github.voytech.tabulate.core.template.documentFormat
import java.io.File

typealias StandaloneDocumentTemplate = StandaloneExportTemplate<Document>

fun (DocumentBuilderApi.() -> Unit).export(file: File) = file.documentFormat().let { format ->
    buildModel(DocumentBuilderApi().apply(this)).let { doc ->
        StandaloneDocumentTemplate(format).export(doc, file.outputStream())
    }
}

fun (DocumentBuilderApi.() -> Unit).export(file: String) = export(File(file))