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
import com.github.projectsandstone.api.plugin.DependencyResolver
import com.github.projectsandstone.api.plugin.DependencyState
import com.github.projectsandstone.api.plugin.PluginContainer
import com.github.projectsandstone.api.plugin.PluginManager
import com.github.projectsandstone.api.util.exception.CircularDependencyException
import com.github.projectsandstone.api.util.exception.DependencyException
import com.github.projectsandstone.api.util.exception.IncompatibleDependencyException
import com.github.projectsandstone.api.util.exception.MissingDependencyException
import com.github.projectsandstone.common.util.idVersion

class SandstoneDependencyResolver(override val pluginManager: PluginManager) : DependencyResolver {



    override fun getDependenciesState(pluginContainer: PluginContainer): List<DependencyState> {
        val dependencies = pluginContainer.dependencies

        return List(dependencies.size, init = { index ->

            val it = dependencies[index]

            val id = it.id
            val incompatible = it.incompatibleVersions
            val version = it.version
            val required = it.isRequired

            val plugin = this.pluginManager.getPlugin(id)
            val failed = this.pluginManager.getFailedPlugin(id)

            if (failed == null && plugin == null && required) {
                return@List DependencyState(it, DependencyState.State.MISSING)
            }

            if (plugin != null) {

                val pluginVersion = plugin.version

                if (incompatible.isNotEmpty()) {
                    if (pluginVersion.versionString.matches(Regex.fromLiteral(incompatible))) {
                        return@List DependencyState(it, DependencyState.State.INCOMPATIBLE)
                    }
                }

                if (version.isNotEmpty()) {
                    if (!pluginVersion.versionString.matches(Regex.fromLiteral(version))) {
                        return@List DependencyState(it, DependencyState.State.INCOMPATIBLE)
                    }
                }

            } else if (failed != null) {
                return@List DependencyState(it, DependencyState.State.FAILED)
            }

            return@List DependencyState(it, DependencyState.State.PRESENT)

        })
    }

    override fun checkDependencies(pluginContainer: PluginContainer) {

        pluginContainer.dependenciesState.forEach { (it, state) ->

            val incompatible = it.incompatibleVersions
            val version = it.version
            val id = it.id
            val required = it.isRequired

            val plugin = this.pluginManager.getPlugin(id)

            if (state == DependencyState.State.INCOMPATIBLE) {
                if (plugin != null) {
                    val pluginVersion = plugin.version

                    if (incompatible.isNotEmpty() && required) {
                        if (pluginVersion.versionString.matches(incompatible.toRegex())) {
                            handleIncompatibleVersion(pluginContainer, incompatible, plugin)
                        }
                    }

                    if (version.isNotEmpty()) {
                        if (!pluginVersion.versionString.matches(version.toRegex())) {
                            handleVersionDoesNotMatch(pluginContainer, version, plugin)
                        }
                    }
                } else if(required) {
                    handleIncompatible(pluginContainer, id)
                }
            } else if (required) {
                if (state == DependencyState.State.MISSING) {
                    handleMissing(pluginContainer, id)
                } else if (state == DependencyState.State.FAILED) {
                    handleFailed(pluginContainer, id)
                }
            }

        }

    }

    private fun handleIncompatibleVersion(pluginContainer: PluginContainer, pattern: String, dependency: PluginContainer) {
        if (pluginContainer.optional)
            Sandstone.logger.info("Plugin '${pluginContainer.idVersion}' was not enabled because of " +
                    "incompatible dependency plugin version '${dependency.idVersion}' defined by pattern '$pattern'")
        else
            throw IncompatibleDependencyException("Incompatible plugin version detected. Plugin: " +
                    "${pluginContainer.idVersion}. Incompatible dependency dependency: ${dependency.idVersion}.")
    }

    private fun handleVersionDoesNotMatch(pluginContainer: PluginContainer, versionPattern: String, dependency: PluginContainer) {
        if (pluginContainer.optional)
            Sandstone.logger.info("Plugin '${pluginContainer.idVersion}' was not enabled because the version of " +
                    "dependency plugin '${dependency.idVersion}' does not match version pattern: $versionPattern")
        else
            Sandstone.logger.error("Incompatible dependency",
                    IncompatibleDependencyException("Incompatible plugin version detected. Plugin " +
                            "'${pluginContainer.idVersion}' only supports versions that matches pattern: " +
                            "'$versionPattern'. Found plugin: ${dependency.idVersion}")
            )
    }

    private fun handleIncompatible(pluginContainer: PluginContainer, dependencyId: String) {
        if (pluginContainer.optional)
            Sandstone.logger.info("Plugin ${pluginContainer.idVersion} was not enabled " +
                    "because of incompatible dependency plugin id $dependencyId")
        else
            throw IncompatibleDependencyException("Incompatible plugin detected. Plugin: " +
                    "${pluginContainer.idVersion}. Incompatible dependency plugin id: $dependencyId.")
    }

    private fun handleMissing(pluginContainer: PluginContainer, dependencyId: String) {
        if (pluginContainer.optional)
            Sandstone.logger.info("Plugin ${pluginContainer.idVersion} was not enabled " +
                    "because of missing dependency plugin id $dependencyId")
        else
            throw MissingDependencyException("Dependency '$dependencyId' of plugin '${pluginContainer.idVersion}' is missing.")
    }

    private fun handleFailed(pluginContainer: PluginContainer, dependencyId: String) {
        if (pluginContainer.optional)
            Sandstone.logger.info("Plugin ${pluginContainer.idVersion} was not enabled " +
                    "because dependency plugin id $dependencyId failed to load")
        else
            throw MissingDependencyException("Dependency '$dependencyId' of plugin '$pluginContainer' failed to load.")
    }

    /**
     * Check if [plugin] has dependency [dependencyToFind].
     *
     * @param plugin Plugin to check dependencies
     * @param dependencyToFind Dependency to find
     * @param queue Plugins in the loading queue.
     */
    fun hasDirectOrIndirectDependency(plugin: PluginContainer,
                                      dependencyToFind: PluginContainer,
                                      queue: List<PluginContainer>): Boolean =
            hasDirectOrIndirectDependency(plugin, dependencyToFind, queue, true)

    private fun hasDirectOrIndirectDependency(plugin: PluginContainer,
                                              dependencyToFind: PluginContainer,
                                              queue: List<PluginContainer>,
                                              check: Boolean): Boolean {
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