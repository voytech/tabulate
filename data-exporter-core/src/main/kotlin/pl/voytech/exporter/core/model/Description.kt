package pl.voytech.exporter.core.model

import pl.voytech.exporter.core.model.extension.Extension

data class Description internal constructor(
    val title: String,
    val extensions: Set<Extension>?
)
