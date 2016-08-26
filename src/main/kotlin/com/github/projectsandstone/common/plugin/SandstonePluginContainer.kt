/**
 *      SandstoneCommon - Common implementation of SandstoneAPI
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) 2016 Sandstone <https://github.com/ProjectSandstone/>
 *      Copyright (c) contributors
 *
 *
 *      Permission is hereby granted, free of charge, to any person obtaining a copy
 *      of this software and associated documentation files (the "Software"), to deal
 *      in the Software without restriction, including without limitation the rights
 *      to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *      copies of the Software, and to permit persons to whom the Software is
 *      furnished to do so, subject to the following conditions:
 *
 *      The above copyright notice and this permission notice shall be included in
 *      all copies or substantial portions of the Software.
 *
 *      THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *      IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *      FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *      AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *      LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *      OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *      THE SOFTWARE.
 */
package com.github.projectsandstone.common.plugin

import com.github.projectsandstone.api.logging.Logger
import com.github.projectsandstone.api.plugin.*
import java.nio.file.Path

/**
 * Created by jonathan on 17/08/16.
 */
open class SandstonePluginContainer(override val file: Path?,
                               override val id: String,
                               override val name: String,
                               override val version: String,
                               override val description: String?,
                               override val usePlatformInternals: Boolean,
                               override val classLoader: PluginClassLoader,
                               override val dependencies: Array<DependencyContainer>?,
                               override val authors: Array<String>?) : PluginContainer {

    internal var instance_: Any? = null
    internal lateinit var logger_: Logger
    internal lateinit var state_: PluginState

    override val instance: Any?
        get() = this.instance_

    override val logger: Logger
        get() = this.logger_

    override val state: PluginState
        get() = this.state_


    override fun equals(other: Any?): Boolean {
        if (other is PluginContainer)
            return this.id == other.id

        return super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun toString(): String {
        return "SandstonePluginContainer[file=$file, id=$id, name=$name, version=$version, useInternals=$usePlatformInternals]"
    }

    companion object {
        fun fromAnnotation(pluginClassLoader: PluginClassLoader, file: Path, annotation: Plugin): SandstonePluginContainer {
            return SandstonePluginContainer(
                    id = annotation.id,
                    name = annotation.name.ifEmpty { annotation.id },
                    version = annotation.version,
                    description = annotation.description.nullIfEmpty(),
                    file = file,
                    authors = annotation.authors.ifEmpty { emptyArray() },
                    dependencies = annotation.dependencies.ifEmpty { emptyArray() }.map {
                        SandstoneDependencyContainer(it.id, it.incompatibleVersions, it.isRequired, it.version)
                    }.toTypedArray(),
                    classLoader = pluginClassLoader,
                    usePlatformInternals = annotation.usePlatformInternals

            )
        }

        /**
         * Return current String if not empty, or null if is empty
         */
        fun String.nullIfEmpty(): String? = if (this.isNotEmpty()) this else null

        inline fun String.ifEmpty(func: (String) -> String): String = if (this.isNotEmpty()) this else func(this)

        inline fun <T> Array<T>.ifEmpty(func: (Array<T>) -> Array<T>): Array<T> = if (this.isNotEmpty()) this else func(this)

    }

}