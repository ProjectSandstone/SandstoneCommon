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

import com.github.projectsandstone.api.Sandstone
import com.github.projectsandstone.api.plugin.*
import java.nio.file.Path
import java.util.*

/**
 * Created by jonathan on 15/08/16.
 */
class SandstonePluginManager : PluginManager {
    private val dependencyResolver_ = SandstoneDependencyResolver(this)
    private val pluginSet = dependencyResolver_.createDependencySet()
    private val unmodifiablePluginSet = Collections.unmodifiableSet(this.pluginSet)
    private val pluginLoader_ = SandstonePluginLoader(this)

    override val dependencyResolver: DependencyResolver
        get() = this.dependencyResolver_

    override val pluginLoader: PluginLoader
        get() = this.pluginLoader_

    override fun loadPlugin(file: Path): List<PluginContainer> {
        val containers = this.pluginLoader.loadFile(file)

        containers.forEach {
            this.dependencyResolver.checkDependencies(it)
        }

        pluginSet += containers

        return containers
    }

    override fun loadPlugin(pluginContainer: PluginContainer): Boolean {

        try {
            this.pluginLoader.load(pluginContainer)

            if(pluginContainer.state == PluginState.FAILED) {
                this.pluginSet -= pluginContainer
            } else if (!this.pluginSet.contains(pluginContainer)) {
                this.pluginSet.add(pluginContainer)
            }

            return true
        }catch (t: Exception) {
            Sandstone.logger.exception(t, "Failed to load PluginContainer: $pluginContainer!")
            return false
        }
    }

    override fun loadAllPlugins(): Boolean {
        val pluginList = pluginSet.toList().filter { it.state == PluginState.ABOUT_TO_LOAD }

        return pluginList.all { this.loadPlugin(it) }
    }

    override fun getPlugin(id: String): PluginContainer? {
        return this.pluginSet.find { it.id == id }
    }

    override fun getPlugins(): Set<PluginContainer> {
        return unmodifiablePluginSet
    }



}