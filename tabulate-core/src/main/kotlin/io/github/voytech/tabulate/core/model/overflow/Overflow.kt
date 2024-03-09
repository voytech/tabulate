package io.github.voytech.tabulate.core.model.overflow

enum class Overflow {

    /**
     * Skip this component current rendering iteration and abandon all subsequent iterations.
     * Should be accomplished by issuing iterations.skip() which internally sets attribute skip=true for current iteration.
     * This method: iterations.skip() should be called at the end of measuring phase, just after detecting overflow state
     * after execution of render operation.
     * Using skip=true attribute for iteration is crucial
     */
    SKIP,

    /**
     * Clip this component current rendering iteration and abandon subsequent iterations.
     * This overflow handling strategy does not require any built-in iteration's API methods.
     * It is only crucial not to push new iterations, because model export should end just right after clipping its content.
     */
    CLIP,

    /**
     * Clip this component current rendering iteration and continues subsequent iterations.
     * This overflow handling strategy does not require any built-in iteration's API methods.
     * It is only crucial to remember to push new iterations (if only model is not fully rendered yet).
     */
    CLIP_AND_CONTINUE,

    /**
     * Skip this component current rendering iteration, and move to next iterations for finishing component rendering.
     */
    SKIP_AND_CONTINUE,

    /**
     * Skip this component's current rendering iteration, then re-attempt rendering in the next iteration.
     * After skipping, space allocation should take place as if the component would be rendered to prevent
     * retry in the same place.
     *
     * Steps:
     * 1. Retry should be detected after overflow detection by rendering operation results.
     * 2. Regardless of the rendering operation result kind, if there is a component attribute denoting that the retry strategy should be used, it should take precedence.
     * 3. After retry is detected in component measure logic, the iteration API needs to be used, and its built-in method `iterations.retry()` should be called.
     * 4. The `iterations.retry()` method should manage underlying iteration state by appending the attribute on the current iteration to be skipped on rendering phase (`skip=true`),
     *    and additionally, it should call `newIteration(retry=true)`.
     * 5. Model ExportApi logic should encapsulate details of correctly interpreting the current iteration `skip=true` attribute to perform full measured space allocation for the rendering phase.
     * 6. Model ExportApi logic should encapsulate details of correctly interpreting the current iteration `retry=true`. If this iteration attribute is set to true, `export(force=true)` should be called on the model within the current model export context.
     */
    RETRY,
}