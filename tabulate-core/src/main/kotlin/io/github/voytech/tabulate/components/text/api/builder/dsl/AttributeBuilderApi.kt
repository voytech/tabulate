package io.github.voytech.tabulate.components.text.api.builder.dsl

import io.github.voytech.tabulate.components.text.model.Text
import io.github.voytech.tabulate.components.text.operation.TextRenderableEntity
import io.github.voytech.tabulate.core.model.attributes.*
import io.github.voytech.tabulate.core.model.overflow.OverflowWords

// Attributes available on text level

fun TextAttributesBuilderApi.margins(block: MarginsAttribute.Builder.() -> Unit) =
    attribute(MarginsAttribute.builder<Text>().apply(block))

fun TextAttributesBuilderApi.text(block: TextStylesAttribute.Builder.() -> Unit) =
    attribute(TextStylesAttribute.builder<TextRenderableEntity>().apply(block))

fun TextAttributesBuilderApi.alignment(block: AlignmentAttribute.Builder.() -> Unit) =
    attribute(AlignmentAttribute.builder<TextRenderableEntity>().apply(block))

fun TextAttributesBuilderApi.borders(block: BordersAttribute.Builder.() -> Unit) =
    attribute(BordersAttribute.builder<TextRenderableEntity>().apply(block))

fun TextAttributesBuilderApi.background(block: BackgroundAttribute.Builder.() -> Unit) =
    attribute(BackgroundAttribute.builder<TextRenderableEntity>().apply(block))

fun TextAttributesBuilderApi.clip(block: ClipAttribute.Builder.() -> Unit) =
    attribute(ClipAttribute.builder<TextRenderableEntity>().apply(block))

fun TextAttributesBuilderApi.height(block: HeightAttribute.Builder.() -> Unit) {
    attribute(HeightAttribute.Builder(TextRenderableEntity::class.java).apply(block))
}

fun TextAttributesBuilderApi.width(block: WidthAttribute.Builder.() -> Unit) {
    attribute(WidthAttribute.Builder(TextRenderableEntity::class.java).apply(block))
}

fun TextAttributesBuilderApi.verticalOverflow(block: VerticalOverflowAttribute.Builder.() -> Unit) =
    attribute(VerticalOverflowAttribute.builder(Text::class.java).apply(block))

fun TextAttributesBuilderApi.horizontalOverflow(block: HorizontalOverflowAttribute.Builder.() -> Unit) =
    attribute(HorizontalOverflowAttribute.builder(Text::class.java).apply(block))

fun TextAttributesBuilderApi.overflow(block: OverflowWords.() -> Unit) {
    verticalOverflow(block)
    horizontalOverflow(block)
}