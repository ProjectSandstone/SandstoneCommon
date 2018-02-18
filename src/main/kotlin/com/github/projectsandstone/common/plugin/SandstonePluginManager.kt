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

import com.github.projectsandstone.api.Sandstone
import com.github.projectsandstone.api.constants.SandstonePlugin
import com.github.projectsandstone.api.plugin.*
import com.github.projectsandstone.common.di.SandstonePluginDependencyInjection
import com.github.projectsandstone.common.di.SandstonePluginGuice
import com.github.projectsandstone.common.util.DependencyComparator
import com.google.inject.Injector
import java.nio.file.Path
import java.util.*
import javax.inject.Inject

class SandstonePluginManager @Inject constructor(private val baseInjector: Injector) : PluginManager {

    private val pluginSet = mutableSetOf<PluginContainer>(SandstonePlugin)
    private val failedPluginSet = mutableSetOf<PluginContainer>()

    private val dependencyResolver_ = SandstoneDependencyResolver(this)
    private val unmodifiablePluginSet = Collections.unmodifiableSet(this.pluginSet)
    private val pluginLoader_ = SandstonePluginLoader(this, SandstonePluginGuice(this.baseInjector))

    override val dependencyResolver: DependencyResolver
        get() = this.dependencyResolver_

    override val pluginLoader: PluginLoader
        get() = this.pluginLoader_

    override val plugins: Set<PluginContainer> = this.unmodifiablePluginSet

    override fun loadPlugins(classes: Array<String>): List<PluginContainer> {
        val containers = this.createContainers(classes)

        containers.forEach {
            loadPlugin(it)
        }

        return containers
    }

    override fun loadFile(file: Path): List<PluginContainer> {
        val containers = this.createContainers(file)

        containers.forEach {
            loadPlugin(it)
        }

        return containers

    }

    override fun loadAll(pluginContainers: List<PluginContainer>) {
        val containers = pluginContainers.toMutableList().sortedWith(DependencyComparator(this.dependencyResolver_, pluginContainers))

        containers.forEach {
            loadPlugin(it)
        }
    }

    fun loadPlugin(pluginContainer: PluginContainer): Boolean {

        try {
            if(this.pluginSet.any { it.id == pluginContainer.id })
                throw IllegalArgumentException("Plugin with id ${pluginContainer.id} already loaded!")

            if (!this.pluginSet.any { pluginContainer == it }) {
                this.pluginSet.add(pluginContainer)
            }

            this.pluginLoader.load(pluginContainer)

            if (pluginContainer.state == PluginState.FAILED) {
                this.addToFailedSet(pluginContainer)
            } else if (!this.pluginSet.contains(pluginContainer)) {
                this.addToLoadedSet(pluginContainer)
            }

            return true
        } catch (t: Exception) {
            Sandstone.logger.error("Failed to load PluginContainer: $pluginContainer!", t)
            return false
        }
    }


    override fun getPlugin(id: String): PluginContainer? {
        return this.pluginSet.find { it.id == id }
    }

    override fun getFailedPlugin(id: String): PluginContainer? {
        return this.failedPluginSet.find { it.id == id }
    }

    private fun addToLoadedSet(pluginContainer: PluginContainer) {
        this.failedPluginSet.removeIf { it.id == pluginContainer.id }

        if(!this.pluginSet.any { it.id == pluginContainer.id })
            this.pluginSet += pluginContainer
    }

    private fun addToFailedSet(pluginContainer: PluginContainer) {
        this.pluginSet.removeIf { it.id == pluginContainer.id }

        if(!this.failedPluginSet.any { it.id == pluginContainer.id })
            this.failedPluginSet += pluginContainer
    }

    override fun createContainers(classes: Array<String>): List<PluginContainer> {
        return pluginLoader_.createFromClasses(classes)
    }

    override fun createContainers(file: Path): List<PluginContainer> {
        return pluginLoader_.createFromFile(file)
    }
}