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
import com.github.projectsandstone.api.plugin.DependencyContainer
import com.github.projectsandstone.api.plugin.PluginClassLoader
import com.github.projectsandstone.api.plugin.PluginDefinition
import com.github.projectsandstone.api.plugin.PluginState
import com.github.projectsandstone.api.util.version.Version
import java.nio.file.Path

/**
 * Created by jonathan on 27/08/16.
 */
class SandstonePluginDefinition(var pluginContainer: SandstonePluginContainer?) : PluginDefinition {

    override var id: String
        get() = pluginContainer?.id ?: throw IllegalStateException("Post Definition Phase")
        set(id) {
            pluginContainer?.id_ = id
        }

    override var name: String
        get() = pluginContainer?.name ?: throw IllegalStateException("Post Definition Phase")
        set(name) {
            pluginContainer?.name_ = name
        }

    override var version: Version
        get() = pluginContainer?.version ?: throw IllegalStateException("Post Definition Phase")
        set(value) {
            pluginContainer?.version_ = value
        }

    override var description: String?
        get() = pluginContainer?.description ?: throw IllegalStateException("Post Definition Phase")
        set(value) {
            pluginContainer?.description_ = value ?: throw IllegalStateException("Post Definition Phase")
        }


    override var usePlatformInternals: Boolean
        get() = pluginContainer?.usePlatformInternals ?: throw IllegalStateException("Post Definition Phase")
        set(value) {
            pluginContainer?.usePlatformInternals_ = value
        }

    override val dependencies: Array<DependencyContainer>?
        get() = pluginContainer?.dependencies ?: throw IllegalStateException("Post Definition Phase")

    override var authors: Array<String>?
        get() = pluginContainer?.authors ?: throw IllegalStateException("Post Definition Phase")
        set(value) {
            pluginContainer?.authors_ = value
        }

    override val logger: Logger
        get() = pluginContainer?.logger ?: throw IllegalStateException("Post Definition Phase")

    override val file: Path?
        get() = pluginContainer?.file ?: throw IllegalStateException("Post Definition Phase")

    override val classLoader: PluginClassLoader
        get() = pluginContainer?.classLoader ?: throw IllegalStateException("Post Definition Phase")

    override val instance: Any?
        get() = pluginContainer?.instance ?: throw IllegalStateException("Post Definition Phase")

    override val state: PluginState
        get() = pluginContainer?.state ?: throw IllegalStateException("Post Definition Phase")

    fun invalidate() {
        this.pluginContainer = null
    }

}