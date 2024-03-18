package io.github.voytech.tabulate.components.image.api.builder.dsl

import io.github.voytech.tabulate.components.image.model.Image
import io.github.voytech.tabulate.components.image.operation.ImageRenderable
import io.github.voytech.tabulate.components.table.api.builder.dsl.TableLevelAttributesBuilderApi
import io.github.voytech.tabulate.core.model.attributes.*

// Attributes available on image level

fun ImageAttributesBuilderApi.margins(block: MarginsAttribute.Builder.() -> Unit) =
    attribute(MarginsAttribute.builder<ImageRenderable>().apply(block))

fun ImageAttributesBuilderApi.alignment(block: AlignmentAttribute.Builder.() -> Unit) =
    attribute(AlignmentAttribute.builder<ImageRenderable>().apply(block))

fun ImageAttributesBuilderApi.borders(block: BordersAttribute.Builder.() -> Unit) =
    attribute(BordersAttribute.builder<ImageRenderable>().apply(block))

fun ImageAttributesBuilderApi.background(block: BackgroundAttribute.Builder.() -> Unit) =
    attribute(BackgroundAttribute.builder<ImageRenderable>().apply(block))

fun ImageAttributesBuilderApi.height(block: HeightAttribute.Builder.() -> Unit) {
    attribute(HeightAttribute.builder<ImageRenderable>().apply(block))
}

fun ImageAttributesBuilderApi.width(block: WidthAttribute.Builder.() -> Unit) {
    attribute(WidthAttribute.builder<ImageRenderable>().apply(block))
}

fun ImageAttributesBuilderApi.verticalOverflow(block: VerticalOverflowAttribute.Builder.() -> Unit) =
    attribute(VerticalOverflowAttribute.builder(Image::class.java).apply(block))

fun ImageAttributesBuilderApi.horizontalOverflow(block: HorizontalOverflowAttribute.Builder.() -> Unit) =
    attribute(HorizontalOverflowAttribute.builder(Image::class.java).apply(block))