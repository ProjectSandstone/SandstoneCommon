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
import com.github.projectsandstone.api.plugin.DependencyResolver
import com.github.projectsandstone.api.plugin.PluginContainer
import com.github.projectsandstone.api.plugin.PluginManager
import com.github.projectsandstone.api.util.exception.CircularDependencyException
import com.github.projectsandstone.api.util.exception.DependencyException
import com.github.projectsandstone.api.util.exception.IncompatibleDependencyException
import com.github.projectsandstone.api.util.exception.MissingDependencyException
import com.github.projectsandstone.common.util.DependencyComparator
import java.util.*

/**
 * Created by jonathan on 15/08/16.
 */
class SandstoneDependencyResolver(override val pluginManager: PluginManager) : DependencyResolver {
    override fun createDependencySet(): Set<PluginContainer> {
        return TreeSet(DependencyComparator(this))
    }

    override fun checkDependencies(pluginContainer: PluginContainer) {
        pluginContainer.dependencies?.let {
            it.forEach {
                val id = it.id
                val incompatible = it.incompatibleVersions
                val version = it.version
                val required = it.isRequired

                val plugin = this.pluginManager.getPlugin(id)

                if (plugin == null && required)
                    throw MissingDependencyException("Cannot find dependency ${it.toString()}.")

                if (plugin != null) {

                    val pluginVersion = plugin.version

                    if (incompatible.isNotEmpty()) {
                        if (pluginVersion.matches(Regex.fromLiteral(incompatible))) {
                            Sandstone.logger.exception(
                                    IncompatibleDependencyException("Incompatible plugin version detected. Plugin: $pluginContainer. Incompatible dependency plugin: $plugin."),
                                    "Incompatible plugin."
                            )
                        }
                    }

                    if (version.isNotEmpty()) {
                        if (!pluginVersion.matches(Regex.fromLiteral(version))) {
                            throw IncompatibleDependencyException("Incompatible plugin version detected. Plugin $pluginContainer only supports versions that matches pattern: $version. Found plugin: $plugin")
                        }
                    }

                }
            }
        }
    }

    /**
     * Check if [plugin] has dependency [dependencyToFind].
     * @param plugin Plugin to check dependencies
     * @param dependencyToFind Dependency to find
     */
    fun hasDirectOrIndirectDependency(plugin: PluginContainer, dependencyToFind: PluginContainer): Boolean {
        val dependencies = plugin.dependencies ?: return false

        val dependenciesContainer = dependencies.map {
            this.pluginManager.getPlugin(it.id)
                    ?: throw DependencyException("Unresolved dependency found: $it!!!")
        }

        for (container in dependenciesContainer) {
            if (container.id == dependencyToFind.id) {
                return true
            } else {
                if (this.hasDirectOrIndirectDependency(container, dependencyToFind)) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * Check if [pluginContainer] has a circular dependency.
     */
    private fun checkCircularDependency(pluginContainer: PluginContainer) {
        if(hasDirectOrIndirectDependency(pluginContainer, pluginContainer))
            throw CircularDependencyException("Plugin: $pluginContainer.")
    }

}