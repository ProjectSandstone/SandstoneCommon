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

import com.github.projectsandstone.api.Sandstone
import com.github.projectsandstone.api.plugin.DependencyResolver
import com.github.projectsandstone.api.plugin.DependencyState
import com.github.projectsandstone.api.plugin.PluginContainer
import com.github.projectsandstone.api.plugin.PluginManager
import com.github.projectsandstone.api.util.exception.CircularDependencyException
import com.github.projectsandstone.api.util.exception.DependencyException
import com.github.projectsandstone.api.util.exception.IncompatibleDependencyException
import com.github.projectsandstone.api.util.exception.MissingDependencyException

class SandstoneDependencyResolver(override val pluginManager: PluginManager) : DependencyResolver {

    override fun getDependenciesState(pluginContainer: PluginContainer): Array<DependencyState> {
        val dependencies = pluginContainer.dependencies

        return Array(dependencies.size, init = { index ->

            val it = dependencies[index]

            val id = it.id
            val incompatible = it.incompatibleVersions
            val version = it.version
            val required = it.isRequired

            val plugin = this.pluginManager.getPlugin(id)
            val failed = this.pluginManager.getFailedPlugin(id)

            if (failed == null && plugin == null && required) {
                return@Array DependencyState(it, DependencyState.State.MISSING)
            }

            if (plugin != null) {

                val pluginVersion = plugin.version

                if (incompatible.isNotEmpty()) {
                    if (pluginVersion.versionString.matches(Regex.fromLiteral(incompatible))) {
                        return@Array DependencyState(it, DependencyState.State.INCOMPATIBLE)
                    }
                }

                if (version.isNotEmpty()) {
                    if (!pluginVersion.versionString.matches(Regex.fromLiteral(version))) {
                        return@Array DependencyState(it, DependencyState.State.INCOMPATIBLE)
                    }
                }

            } else if (failed != null) {
                return@Array DependencyState(it, DependencyState.State.FAILED)
            }

            return@Array DependencyState(it, DependencyState.State.PRESENT)

        })
    }

    override fun checkDependencies(pluginContainer: PluginContainer) {

        pluginContainer.dependenciesState.forEach { dependency ->
            val it = dependency.dependencyContainer

            val incompatible = it.incompatibleVersions
            val version = it.version
            val id = it.id
            val required = it.isRequired

            val plugin = this.pluginManager.getPlugin(id)

            if (dependency.state == DependencyState.State.INCOMPATIBLE) {
                if (plugin != null) {
                    val pluginVersion = plugin.version

                    if (incompatible.isNotEmpty() && required) {
                        if (pluginVersion.versionString.matches(incompatible.toRegex())) {
                            throw IncompatibleDependencyException("Incompatible plugin version detected. Plugin: $pluginContainer. Incompatible dependency plugin: $plugin.")
                        }
                    }

                    if (version.isNotEmpty()) {
                        if (!pluginVersion.versionString.matches(version.toRegex())) {
                            Sandstone.logger.exception(
                                    IncompatibleDependencyException("Incompatible plugin version detected. Plugin '$pluginContainer' only supports versions that matches pattern: '$version'. Found plugin: $plugin"),
                                    "Incompatible version."
                            )
                        }
                    }
                } else if(required) {
                    throw IncompatibleDependencyException("Incompatible plugin detected. Plugin: $pluginContainer. Incompatible dependency plugin: $plugin.")
                }
            } else if (required) {
                if (dependency.state == DependencyState.State.MISSING) {
                    throw MissingDependencyException("Dependency '$it' of plugin '$pluginContainer' is missing.")
                } else if (dependency.state == DependencyState.State.FAILED) {
                    throw MissingDependencyException("Dependency '$it' of plugin '$pluginContainer' failed to load.")
                }
            }

        }

    }

    /**
     * Check if [plugin] has dependency [dependencyToFind].
     *
     * @param plugin Plugin to check dependencies
     * @param dependencyToFind Dependency to find
     * @param queue Plugins in the loading queue.
     */
    fun hasDirectOrIndirectDependency(plugin: PluginContainer, dependencyToFind: PluginContainer, queue: List<PluginContainer>): Boolean {
        return hasDirectOrIndirectDependency(plugin, dependencyToFind, queue, true)
    }

    private fun hasDirectOrIndirectDependency(plugin: PluginContainer, dependencyToFind: PluginContainer, queue: List<PluginContainer>, check: Boolean): Boolean {
        val dependencies = plugin.dependencies

        val dependenciesContainer = dependencies.map {
            this.pluginManager.getPlugin(it.id)?.let { queue.find { t -> t.id == it.id } }
                    ?: throw DependencyException("Unresolved dependency found: '$it'!!!")
        }

        if(check)
            this.checkCircularDependency(plugin, queue)

        for (container in dependenciesContainer) {
            if (container.id == dependencyToFind.id) {
                return true
            } else {
                if (this.hasDirectOrIndirectDependency(container, dependencyToFind, queue, false)) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * Check if [pluginContainer] has a circular dependency.
     */
    private fun checkCircularDependency(pluginContainer: PluginContainer, queue: List<PluginContainer>) {
        if (hasDirectOrIndirectDependency(pluginContainer, pluginContainer, queue, false))
            throw CircularDependencyException("Plugin: $pluginContainer.")
    }

}