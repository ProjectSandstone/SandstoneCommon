/**
 *      SandstoneCommon - Common implementation of SandstoneAPI
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) Sandstone <https://github.com/ProjectSandstone/>
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
import com.github.projectsandstone.api.util.version.Version
import com.github.projectsandstone.common.util.CommonVersionScheme
import java.nio.file.Path

open class SandstonePluginContainer(var id_: String,
                                    var name_: String,
                                    var version_: Version,
                                    var description_: String?,
                                    var usePlatformInternals_: Boolean,
                                    override val dependencies: Array<DependencyContainer>,
                                    var authors_: Array<String>,
                                    override val mainClass: String) : PluginContainer {

    internal var definition: SandstonePluginDefinition? = null
    internal var instance_: Any? = null

    internal var file_: Path? = null
    internal var classLoader_: PluginClassLoader? = null
    internal lateinit var logger_: Logger
    internal lateinit var state_: PluginState
    internal lateinit var dependenciesState_: Array<DependencyState>
    internal lateinit var classes: Array<String>

    override val id: String
        get() = this.id_

    override val name: String
        get() = this.name_

    override val version: Version
        get() = this.version_

    override val description: String?
        get() = this.description_

    override val usePlatformInternals: Boolean
        get() = this.usePlatformInternals_

    override val authors: Array<String>
        get() = this.authors_

    override val dependenciesState: Array<DependencyState>
        get() = this.dependenciesState_

    override val instance: Any?
        get() = this.instance_

    override val logger: Logger
        get() = this.logger_

    override val state: PluginState
        get() = this.state_

    override val file: Path?
        get() = this.file_

    override val classLoader: PluginClassLoader
        get() = this.classLoader_!!

    override fun equals(other: Any?): Boolean {
        if (other is PluginContainer)
            return this.id == other.id

        return super.equals(other)
    }

    override fun hashCode(): Int {
        return this.id.hashCode()
    }

    override fun toString(): String {
        return "SandstonePluginContainer[file=$file, id=$id, name=$name, version=$version, useInternals=$usePlatformInternals]"
    }

    companion object {
        fun fromAnnotation(pluginClassLoader: PluginClassLoader, file: Path?, mainClass: String, classes: Array<String>, annotation: Plugin): SandstonePluginContainer {
            return SandstonePluginContainer(
                    id_ = annotation.id,
                    name_ = annotation.name.ifEmpty { annotation.id },
                    version_ = Version(annotation.version, CommonVersionScheme),
                    description_ = annotation.description.nullIfEmpty(),
                    //file = file,
                    authors_ = annotation.authors.ifEmpty { emptyArray() },
                    dependencies = annotation.dependencies.ifEmpty { emptyArray() }.map {
                        SandstoneDependencyContainer(it.id, it.incompatibleVersions, it.isRequired, it.version)
                    }.toTypedArray(),
                    //classLoader = pluginClassLoader,
                    usePlatformInternals_ = annotation.usePlatformInternals,
                    mainClass = mainClass
            ).run {
                this.classLoader_ = pluginClassLoader
                this.file_ = file
                this.classes = classes

                return@run this
            }
        }

        /**
         * Return current String if not empty, or null if is empty
         */
        fun String.nullIfEmpty(): String? = if (this.isNotEmpty()) this else null

        inline fun String.ifEmpty(func: (String) -> String): String = if (this.isNotEmpty()) this else func(this)

        inline fun <T> Array<T>.ifEmpty(func: (Array<T>) -> Array<T>): Array<T> = if (this.isNotEmpty()) this else func(this)

    }

}