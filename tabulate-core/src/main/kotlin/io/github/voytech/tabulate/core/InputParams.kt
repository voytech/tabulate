package io.github.voytech.tabulate.core

import io.github.voytech.tabulate.core.model.StateAttributes

class InputParams {
    companion object {

        /**
         * This parameter is used mostly in following circumstances:
         *  - when exporting collection of objects directly (call on collection extension function)
         *  - when layouts are not used, so we export only single component like Table.
         *  - layout size is not defined by DSL.
         */

        private const val ALLOW_MEASURE_BEFORE_RENDER = "allow-measure-before-render"

        fun params() = mapOf<String, Any>()

        inline fun <reified R: Any> param(key: String, value: R) = mapOf<String, Any>(key to value)

        inline fun <reified R: Any> Map<String,Any>.param(key: String, value: R): Map<String,Any> = this + (key to value)

        fun Map<String,Any>.allowMeasureBeforeRender(flag: Boolean): Map<String,Any> = this + (ALLOW_MEASURE_BEFORE_RENDER to flag)

        fun StateAttributes.allowMeasureBeforeRender(default: Boolean = true): Boolean = get<Boolean>(ALLOW_MEASURE_BEFORE_RENDER) ?: default

    }

}