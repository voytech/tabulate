package io.github.voytech.tabulate.core.model.alignment

enum class DefaultHorizontalAlignment: HorizontalAlignment {
    LEFT,
    RIGHT,
    CENTER,
    JUSTIFY,
    FILL;

    override fun getHorizontalAlignmentId(): String = name

}

fun HorizontalAlignment?.orDefault() = this ?: DefaultHorizontalAlignment.LEFT