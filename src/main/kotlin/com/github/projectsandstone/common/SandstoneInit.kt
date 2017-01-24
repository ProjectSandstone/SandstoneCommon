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
package com.github.projectsandstone.common

import com.github.jonathanxd.iutils.condition.Conditions
import com.github.projectsandstone.api.Game
import com.github.projectsandstone.api.Sandstone
import com.github.projectsandstone.api.logging.Logger
import com.github.projectsandstone.api.logging.LoggerFactory
import com.github.projectsandstone.api.registry.RegistryEntry
import com.github.projectsandstone.api.util.exception.EntryNotFoundException
import com.github.projectsandstone.api.util.extension.registry.getEntryGeneric
import com.github.projectsandstone.api.util.version.Schemes
import com.github.projectsandstone.common.util.version.SemVerSchemeImpl
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.nio.file.Path

/**
 * Created by jonathan on 15/08/16.
 */
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
    fun initRegistryConstants(game: Game, constantsClass: Class<*>, instance: Any?) {
        val missingEntries = mutableListOf<String>()

        constantsClass.fields.forEach {
            it.isAccessible = true

            if(Modifier.isFinal(it.modifiers)) {
                val modifiersField = Field::class.java.getDeclaredField("modifiers")
                modifiersField.isAccessible = true
                modifiersField.setInt(it, it.modifiers and Modifier.FINAL.inv())
            }

            val type = it.type
            val name = it.name.toLowerCase()

            val entry = game.registry.getEntryGeneric<RegistryEntry>(name, type)

            if(entry == null)
                missingEntries += name
            else
                it[instance] = game.registry.getEntryGeneric(name, type)
        }

        if(missingEntries.isNotEmpty())
            Sandstone.logger.error("Sandstone could not initialize all constants of class '$constantsClass'. Some entries were not registered: $missingEntries.")
    }

    internal fun init(clazz: Class<*>, fieldName: String, instance: Any) {
        val field = clazz.getDeclaredField(fieldName)

        field.isAccessible = true

        Conditions.checkNull(field[Sandstone], "Already initialized!")

        field[Sandstone] = instance
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
        Sandstone.game.pluginManager.loadPlugins(pluginsDir)
    }

    /**
     * Start loaded plugins.
     */
    @JvmStatic
    fun startPlugins() {
        Sandstone.game.pluginManager.loadAllPlugins()
    }
}