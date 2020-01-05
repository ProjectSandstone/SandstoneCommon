/*
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

import com.github.jonathanxd.kores.type.javaSpecName
import com.github.projectsandstone.api.Sandstone
import com.github.projectsandstone.api.plugin.PluginClassLoader
import com.github.projectsandstone.api.plugin.PluginContainer
import com.github.projectsandstone.common.Constants
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Path
import java.util.*

/**
 * @property file Plugin file path
 * @property useInternal Whether the plugin explicitly specifies that uses platform internals.
 * @property classes Plugin classes
 */
class SandstoneClassLoader(
        urls: Array<URL>,
        parent: ClassLoader,
        override val file: Path?,
        override val useInternal: Boolean,
        override val classes: List<String>) : URLClassLoader(urls, parent), PluginClassLoader {

    internal val loadedClassesName = mutableSetOf<String>() // String could be compressed
    private val pluginContainers_: MutableList<PluginContainer> = mutableListOf()

    override val pluginContainers: List<PluginContainer> =
            Collections.unmodifiableList(this.pluginContainers_)

    override var isInitialized: Boolean = false

    fun defineClass(name: String, data: ByteArray): Class<*> =
            defineClass(name, data, 0, data.size).also {
                this.loadedClassesName.add(it.javaSpecName)
            }

    override fun loadClass(name: String?): Class<*> {
        this.checkInternalAPI(name)

        return super.loadClass(name)
    }

    override fun loadClass(name: String?, resolve: Boolean): Class<*> {

        this.checkInternalAPI(name)

        return super.loadClass(name, resolve)
    }

    override fun findClass(name: String?): Class<*> {
        this.checkInternalAPI(name) // TODO: Remove? findClass does load classes from resources

        return super.findClass(name).also {
            this.loadedClassesName.add(it.javaSpecName)
        }
    }

    private fun checkInternalAPI(name: String?) {
        Constants.cachedThreadPool.execute {
            if (!useInternal
                    && (Sandstone.game.platform.isInternalClass(name) || name == Sandstone::class.java.canonicalName)) {
                Sandstone.logger.warn("Plugin ${this.getPluginName()} used an internal/platform dependent API without specifying that. ")
            }
        }
    }

    internal fun addPluginContainer(pluginContainer: PluginContainer) {
        if(!this.isInitialized)
            this.isInitialized = true

        this.pluginContainers_ += pluginContainer
    }

    private fun getPluginName(): String = if(!this.isInitialized) file?.fileName.toString() else pluginContainers_[0].name
}