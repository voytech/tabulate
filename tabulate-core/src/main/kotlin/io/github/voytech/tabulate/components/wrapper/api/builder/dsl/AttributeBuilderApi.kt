package io.github.voytech.tabulate.components.wrapper.api.builder.dsl

import io.github.voytech.tabulate.components.wrapper.model.Wrapper
import io.github.voytech.tabulate.core.model.attributes.AlignmentAttribute
import io.github.voytech.tabulate.core.model.attributes.HeightAttribute
import io.github.voytech.tabulate.core.model.attributes.MarginsAttribute
import io.github.voytech.tabulate.core.model.attributes.WidthAttribute

// Attributes available on text level

fun WrapperAttributesBuilderApi.margins(block: MarginsAttribute.Builder.() -> Unit) =
    attribute(MarginsAttribute.builder<Wrapper>().apply(block))

fun WrapperAttributesBuilderApi.width(block: WidthAttribute.Builder.() -> Unit) =
    attribute(WidthAttribute.builder<Wrapper>().apply(block))

fun WrapperAttributesBuilderApi.height(block: HeightAttribute.Builder.() -> Unit) =
    attribute(HeightAttribute.builder<Wrapper>().apply(block))

fun WrapperAttributesBuilderApi.alignment(block: AlignmentAttribute.Builder.() -> Unit) =
    attribute(AlignmentAttribute.builder<Wrapper>().apply(block))