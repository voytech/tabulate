package pl.voytech.exporter.core.model

import pl.voytech.exporter.core.model.hints.Hint

data class Description(
    val title: String,
    val hints: List<Hint>?
)
