package io.github.voytech.tabulate.core.builder.fluent.support;

import io.github.voytech.tabulate.template.result.ResultProvider;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;

public class OutputStreamTestResultProvider implements ResultProvider<TestRenderingContext, OutputStream> {

    @NotNull
    @Override
    public Class<OutputStream> outputClass() {
        return OutputStream.class;
    }

    @Override
    public void setOutput(@NotNull TestRenderingContext renderingContext, OutputStream output) { }

    @Override
    public void flush() { }
}
