package io.github.voytech.tabulate.core.model.alignment

enum class DefaultVerticalAlignment: VerticalAlignment {
    BOTTOM,
    TOP,
    MIDDLE;

    override fun getVerticalAlignmentId(): String = name
}