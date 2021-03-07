package pl.voytech.exporter.core.model.attributes.functional

import pl.voytech.exporter.core.model.attributes.TableAttribute

data class FilterAndSortTableAttribute(
    val columnRange: IntRange,
    val rowRange: IntRange
) : TableAttribute()