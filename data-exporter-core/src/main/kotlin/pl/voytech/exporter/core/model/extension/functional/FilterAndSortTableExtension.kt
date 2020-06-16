package pl.voytech.exporter.core.model.extension.functional

import pl.voytech.exporter.core.model.extension.TableExtension

data class FilterAndSortTableExtension(
    val columnRange: IntRange,
    val rowRange: IntRange
) : TableExtension()