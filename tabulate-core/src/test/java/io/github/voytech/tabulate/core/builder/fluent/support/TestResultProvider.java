package io.github.voytech.tabulate.core.builder.fluent.support;

import io.github.voytech.tabulate.template.result.ResultProvider;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;

public class TestResultProvider implements ResultProvider<TestRenderingContext, Unit> {
    @NotNull
    @Override
    public Class<Unit> outputClass() {
        return Unit.class;
    }

    @Override
    public void setOutput(@NotNull TestRenderingContext renderingContext, Unit output) {

    }

    @Override
    public void flush() {

    }
}