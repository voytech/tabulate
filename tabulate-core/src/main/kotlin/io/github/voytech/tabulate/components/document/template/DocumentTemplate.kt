package io.github.voytech.tabulate.components.document.template

import io.github.voytech.tabulate.components.document.api.builder.dsl.DocumentBuilderApi
import io.github.voytech.tabulate.components.document.model.Document
import io.github.voytech.tabulate.core.api.builder.dsl.buildModel
import io.github.voytech.tabulate.core.StandaloneExportTemplate
import io.github.voytech.tabulate.core.documentFormat
import java.io.File

fun (DocumentBuilderApi.() -> Unit).export(file: File, params: Map<String, Any> = emptyMap()) = file.documentFormat().let { format ->
    buildModel(DocumentBuilderApi().apply(this)).let { doc ->
        StandaloneExportTemplate(format).export(doc, file.outputStream(), params)
    }
}

fun (DocumentBuilderApi.() -> Unit).export(file: String, params: Map<String, Any> = emptyMap()) = export(File(file), params)