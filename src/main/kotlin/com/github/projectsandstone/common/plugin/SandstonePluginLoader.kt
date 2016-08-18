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
import com.github.projectsandstone.common.Constants
import com.github.projectsandstone.common.asm.ASM
import com.github.projectsandstone.common.asm.SimpleDesc
import com.github.projectsandstone.common.guice.SandstonePluginModule
import java.net.URL
import java.nio.file.Path
import java.util.jar.JarFile

/**
 * Created by jonathan on 15/08/16.
 */
class SandstonePluginLoader(override val pluginManager: PluginManager) : PluginLoader {
    override fun load(plugin: PluginContainer) {
        if (plugin !is SandstonePluginContainer) {
            Sandstone.logger.error("Cannot load plugin container: $plugin. Only SandstonePluginContainers are supported!")
            return
        }

        val pluginClassLoader = plugin.classLoader

        if (pluginClassLoader !is ClassLoader) {
            Sandstone.logger.error("Cannot load plugin container: $plugin. Plugin class load is not a ClassLoader!")
            return
        }

        plugin.logger_ = Sandstone.loggerFactory.createLogger(plugin)
        plugin.state_ = PluginState.LOADING

        pluginClassLoader.classes.forEach {
            val klass = pluginClassLoader.loadClass(it)

            val injector = Constants.injector.createChildInjector(SandstonePluginModule(plugin, klass))

            val instance = injector.getInstance(klass)

            plugin.instance_ = instance

            // Register listeners (GameStartEvent) etc.
            Sandstone.game.eventManager.registerListeners(instance, instance)
        }

    }

    override fun loadFile(file: Path): List<PluginContainer> {
        val filePathAsFile = file.toFile()

        var simpleDesc: SimpleDesc? = null

        val jarFile = JarFile(filePathAsFile)
        val mutableClasses = mutableListOf<String>()

        val enumeration = jarFile.entries()

        while (enumeration.hasMoreElements()) {
            val next = enumeration.nextElement()

            val stream = jarFile.getInputStream(next)

            val desc = ASM.findPluginAnnotation(stream)

            if (desc != null) {
                if (simpleDesc == null)
                    simpleDesc = desc

                mutableClasses.add(next.name)
            }

        }

        val classes = mutableClasses.toList()

        if (classes.size != 0) {
            val urls = arrayOf(URL("jar:file:$filePathAsFile!/"))

            val classLoader = SandstoneClassLoader(
                    urls = urls,
                    file = file,
                    parent = this.javaClass.classLoader,
                    useInternal = simpleDesc!!.usePlatformInternals,
                    classes = classes)

            val containers = mutableListOf<PluginContainer>()

            containers +=
                    classes.map { it.replace('/', '.') }.map {
                        classLoader.loadClass(it)
                    }.map {
                        val declaredAnnotation = it.getDeclaredAnnotation(Plugin::class.java)

                        if (declaredAnnotation != null) {
                            val container = SandstonePluginContainer.fromAnnotation(classLoader, file, declaredAnnotation)
                            container.state_ = PluginState.ABOUT_TO_LOAD

                            /*return */container
                        } else {
                            null
                        }
                    }.filterNotNull()

            return containers.toList()
        }

        return emptyList()
    }

}

