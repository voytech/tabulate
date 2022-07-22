package io.github.voytech.tabulate.core.builder.fluent.support;

import io.github.voytech.tabulate.components.table.model.Table;
import io.github.voytech.tabulate.core.template.operation.OperationsBuilder;
import io.github.voytech.tabulate.core.template.spi.DocumentFormat;
import io.github.voytech.tabulate.core.template.spi.ExportOperationsProvider;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;

import static io.github.voytech.tabulate.core.template.spi.DocumentFormat.format;

public class TestExportOperationsFactory<T> implements ExportOperationsProvider<TestRenderingContext, Table<T>> {
    @NotNull
    @Override
    public Function1<OperationsBuilder<TestRenderingContext>, Unit> provideExportOperations() {
        return builder -> null;
    }

    @NotNull
    @Override
    public DocumentFormat<TestRenderingContext> getDocumentFormat() {
        return format("test", TestRenderingContext.class,"default");
    }

    @NotNull
    @Override
    public Class<Table<T>> getModelClass() {
        return Table.jclass();
    }


}
