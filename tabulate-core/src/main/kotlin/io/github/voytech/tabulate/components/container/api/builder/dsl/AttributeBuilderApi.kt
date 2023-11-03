package io.github.voytech.tabulate.components.container.api.builder.dsl

import io.github.voytech.tabulate.components.container.model.Container
import io.github.voytech.tabulate.components.container.opration.ContainerRenderable
import io.github.voytech.tabulate.core.model.attributes.*

// Attributes available on text level

fun ContainerAttributesBuilderApi.margins(block: MarginsAttribute.Builder.() -> Unit) =
    attribute(MarginsAttribute.builder<Container>().apply(block))

fun ContainerAttributesBuilderApi.alignment(block: AlignmentAttribute.Builder.() -> Unit) =
    attribute(AlignmentAttribute.builder<Container>().apply(block))

fun ContainerAttributesBuilderApi.width(block: WidthAttribute.Builder.() -> Unit) =
    attribute(WidthAttribute.builder<Container>().apply(block))

fun ContainerAttributesBuilderApi.height(block: HeightAttribute.Builder.() -> Unit) =
    attribute(HeightAttribute.builder<Container>().apply(block))

fun ContainerAttributesBuilderApi.borders(block: BordersAttribute.Builder.() -> Unit) =
    attribute(BordersAttribute.builder(ContainerRenderable::class.java, Container::class.java).apply(block))

fun ContainerAttributesBuilderApi.background(block: BackgroundAttribute.Builder.() -> Unit) =
    attribute(BackgroundAttribute.builder<ContainerRenderable>().apply(block))
