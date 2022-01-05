package io.github.voytech.tabulate.core.builder.fluent.support;

import io.github.voytech.tabulate.template.result.OutputBinding;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;

public class OutputStreamTestOutputBinding implements OutputBinding<TestRenderingContext, OutputStream> {

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
