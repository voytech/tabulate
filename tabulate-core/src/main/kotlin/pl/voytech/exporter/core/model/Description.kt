package pl.voytech.exporter.core.model

import pl.voytech.exporter.core.model.attributes.Attribute

data class Description internal constructor(
    val title: String,
    val attributes: Set<Attribute>?
)
