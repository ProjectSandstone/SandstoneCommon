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
package com.github.projectsandstone.common

import com.github.jonathanxd.iutils.condition.Conditions
import com.github.projectsandstone.api.Game
import com.github.projectsandstone.api.Implementation
import com.github.projectsandstone.api.Sandstone
import com.github.projectsandstone.api.logging.LoggerFactory
import com.github.projectsandstone.api.plugin.PluginState
import com.github.projectsandstone.api.registry.RegistryEntry
import com.github.projectsandstone.api.util.extension.registry.getEntryGeneric
import com.github.projectsandstone.api.util.version.Schemes
import com.github.projectsandstone.common.util.idVersion
import com.github.projectsandstone.common.util.version.SemVerSchemeImpl
import org.slf4j.Logger
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.nio.file.Path

object SandstoneInit {

    @JvmStatic
    fun initConsts() {
        this.init(Schemes::class.java, "semVerScheme_", SemVerSchemeImpl)
    }

    @JvmStatic
    fun initPath(path: Path) {
        this.init("sandstonePath_", path)
    }

    @JvmStatic
    fun initGame(game: Game) {
        this.init("game_", game)
    }

    @JvmStatic
    fun initLogger(logger: Logger) {
        this.init("logger_", logger)
    }

    @JvmStatic
    fun initLoggerFactory(loggerFactory: LoggerFactory) {
        this.init("loggerFactory_", loggerFactory)
    }

    @JvmStatic
    fun initImplementation(implementation: Implementation) {
        this.init("implementation_", implementation)
    }

    @JvmStatic
    fun initRegistryConstants(game: Game, constantsClass: Class<*>, instance: Any?) {
        val missingEntries = mutableListOf<String>()

        constantsClass.fields.forEach {
            it.isAccessible = true

            if (Modifier.isFinal(it.modifiers)) {
                val modifiersField = Field::class.java.getDeclaredField("modifiers")
                modifiersField.isAccessible = true
                modifiersField.setInt(it, it.modifiers and Modifier.FINAL.inv())
            }

            val type = it.type
            val name = it.name.toLowerCase()

            val entry = game.registry.getEntryGeneric<RegistryEntry>(name, type)

            if (entry == null)
                missingEntries += name
            else
                it[instance] = game.registry.getEntryGeneric(name, type)
        }

        if (missingEntries.isNotEmpty())
            Sandstone.logger
                    .error("Sandstone could not initialize all constants of class '$constantsClass'. Some entries were not registered: $missingEntries.")
    }

    internal fun init(clazz: Class<*>, fieldName: String, instance: Any) {
        val field = clazz.getDeclaredField(fieldName)

        field.isAccessible = true

        Conditions.checkNull(field[null], "Already initialized!")

        field[null] = instance
    }

    internal fun init(fieldName: String, instance: Any) {
        this.init(Sandstone::class.java, fieldName, instance)
    }

    /**
     * Load plugins from directory
     *
     * @param pluginsDir Directory with plugins.
     */
    @JvmStatic
    fun loadPlugins(pluginsDir: Path) {
        Sandstone.logger.info("Discovering plugins...")
        val plugins = Sandstone.game.pluginManager.createContainersFromPath(pluginsDir)

        Sandstone.logger.info("${plugins.size} plugins discovered, loading plugins: " +
                plugins.joinToString { "${it.name} ${it.version.versionString}" })

        Sandstone.game.pluginManager.loadAll(plugins)

        val currentPlugins = Sandstone.game.pluginManager.getPlugins()

        Sandstone.logger.info("${currentPlugins.count { it.state == PluginState.LOADED }} plugins loaded with success.")

        val failed = currentPlugins.count { it.state == PluginState.FAILED }

        if (failed > 0)
            Sandstone.logger.info("$failed plugins failed to load: " +
                currentPlugins.filter { it.state == PluginState.FAILED }.joinToString { "${it.name} ${it.version.versionString}" })
    }

}