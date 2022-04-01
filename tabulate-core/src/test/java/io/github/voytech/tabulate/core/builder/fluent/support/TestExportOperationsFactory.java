package io.github.voytech.tabulate.core.builder.fluent.support;

import io.github.voytech.tabulate.template.operations.*;
import io.github.voytech.tabulate.template.result.OutputBinding;
import io.github.voytech.tabulate.template.spi.ExportOperationsProvider;
import io.github.voytech.tabulate.template.spi.TabulationFormat;
import kotlin.collections.MapsKt;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static io.github.voytech.tabulate.template.spi.TabulationFormat.format;

public class TestExportOperationsFactory<T> implements ExportOperationsProvider<TestRenderingContext> {

    @NotNull
    @Override
    public TestRenderingContext createRenderingContext() {
        return new TestRenderingContext();
    }

    @NotNull
    @Override
    public List<OutputBinding<TestRenderingContext, ?>> createOutputBindings() {
        return Collections.singletonList(new TestOutputBinding());
    }

    @NotNull
    @Override
    public TabulationFormat<TestRenderingContext> getTabulationFormat() {
        return format("test", TestRenderingContext.class,"default");
    }

    @NotNull
    @Override
    public Operations<TestRenderingContext> createExportOperations() {
        return new Operations<TestRenderingContext>(MapsKt.emptyMap());
    }
}
