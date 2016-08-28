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
package com.github.projectsandstone.common

import com.github.jonathanxd.iutils.conditions.Conditions
import com.github.projectsandstone.api.Game
import com.github.projectsandstone.api.Sandstone
import com.github.projectsandstone.api.logging.Logger
import com.github.projectsandstone.api.logging.LoggerFactory
import com.github.projectsandstone.api.util.version.Schemes
import com.github.projectsandstone.common.util.version.SemVerSchemeImpl
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

    internal fun init(clazz: Class<*>, fieldName: String, instance: Any) {
        val field = clazz.getDeclaredField(fieldName)

        field.isAccessible = true

        Conditions.checkNull(field.get(Sandstone), "Already initialized!")

        field.set(Sandstone, instance)
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