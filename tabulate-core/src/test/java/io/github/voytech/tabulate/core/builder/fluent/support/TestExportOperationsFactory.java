package io.github.voytech.tabulate.core.builder.fluent.support;

import io.github.voytech.tabulate.template.TabulationFormat;
import io.github.voytech.tabulate.template.operations.*;
import io.github.voytech.tabulate.template.result.OutputBinding;
import io.github.voytech.tabulate.template.spi.ExportOperationsProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class TestExportOperationsFactory<T> implements ExportOperationsProvider<TestRenderingContext> {
    @NotNull
    @Override
    public Class<TestRenderingContext> getContextClass() {
        return TestRenderingContext.class;
    }

    @NotNull
    @Override
    public TestRenderingContext createRenderingContext() {
        return new TestRenderingContext();
    }

    @NotNull
    @Override
    public AttributedContextExportOperations<TestRenderingContext> createExportOperations() {
        return new AttributedContextExportOperations<TestRenderingContext>() {
            @Override
            public <T> void endRow(@NotNull TestRenderingContext renderingContext, @NotNull AttributedRowWithCells<T> context) {
            }

            @Override
            public void beginRow(@NotNull TestRenderingContext renderingContext, @NotNull AttributedRow context) {
            }

            @Override
            public void renderColumn(@NotNull TestRenderingContext renderingContext, @NotNull AttributedColumn context) {
            }

            @Override
            public void createTable(@NotNull TestRenderingContext renderingContext, @NotNull AttributedTable context) {

            }

            @Override
            public void renderRowCell(@NotNull TestRenderingContext renderingContext, @NotNull AttributedCell context) {

            }
        };
    }

    @NotNull
    @Override
    public List<OutputBinding<TestRenderingContext, ?>> createOutputBindings() {
        return Collections.singletonList(new TestOutputBinding());
    }

    @NotNull
    @Override
    public TabulationFormat supportsFormat() {
        return TabulationFormat.format("test");
    }
}
