package io.github.voytech.tabulate.components.text.api.builder.dsl

import io.github.voytech.tabulate.components.text.operation.TextRenderable
import io.github.voytech.tabulate.core.model.attributes.*

// Attributes available on text level

fun TextAttributesBuilderApi.margins(block: MarginsAttribute.Builder.() -> Unit) =
    attribute(MarginsAttribute.builder<TextRenderable>().apply(block))

fun TextAttributesBuilderApi.text(block: TextStylesAttribute.Builder.() -> Unit) =
    attribute(TextStylesAttribute.builder<TextRenderable>().apply(block))

fun TextAttributesBuilderApi.alignment(block: AlignmentAttribute.Builder.() -> Unit) =
    attribute(AlignmentAttribute.builder<TextRenderable>().apply(block))

fun TextAttributesBuilderApi.borders(block: BordersAttribute.Builder.() -> Unit) =
    attribute(BordersAttribute.builder<TextRenderable>().apply(block))

fun TextAttributesBuilderApi.background(block: BackgroundAttribute.Builder.() -> Unit) =
    attribute(BackgroundAttribute.builder<TextRenderable>().apply(block))

fun TextAttributesBuilderApi.columnWidth(block: WidthAttribute.Builder.() -> Unit) =
    attribute(WidthAttribute.builder(TextRenderable::class.java).apply(block))

fun TextAttributesBuilderApi.rowHeight(block: HeightAttribute.Builder.() -> Unit) =
    attribute(HeightAttribute.builder(TextRenderable::class.java).apply(block))

fun TextAttributesBuilderApi.height(block: HeightAttribute.Builder.() -> Unit) {
    attribute(HeightAttribute.Builder(TextRenderable::class.java).apply(block))
}