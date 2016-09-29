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
import com.github.projectsandstone.common.asm.ASM
import com.github.projectsandstone.common.asm.SimpleDesc
import com.github.projectsandstone.common.guice.SandstoneModule
import com.github.projectsandstone.common.guice.SandstonePluginModule
import java.net.URL
import java.nio.file.Path
import java.util.jar.JarFile

/**
 * Created by jonathan on 15/08/16.
 */
class SandstonePluginLoader(override val pluginManager: PluginManager) : PluginLoader {

    val CLASS_LENGTH = ".class".length

    override fun load(plugin: PluginContainer) {
        if (plugin !is SandstonePluginContainer) {
            Sandstone.logger.error("Cannot load plugin container: $plugin. Only SandstonePluginContainers are supported!")
            return
        }

        if(plugin.state_ != PluginState.ABOUT_TO_LOAD) {
            Sandstone.logger.error("Cannot load plugin container: $plugin. Already loaded!")
            return
        }

        val pluginClassLoader = plugin.classLoader

        if (pluginClassLoader !is ClassLoader) {
            Sandstone.logger.error("Cannot load plugin container: $plugin. Plugin class load is not a ClassLoader!")
            return
        }

        plugin.logger_ = Sandstone.loggerFactory.createLogger(plugin)
        plugin.state_ = PluginState.LOADING

        val clName = plugin.mainClass

        if (plugin.state_ != PluginState.FAILED) {

            try {
                val klass = pluginClassLoader.loadClass(clName)

                val injector = SandstoneModule.injector.createChildInjector(SandstonePluginModule(pluginManager, plugin, klass))

                val instance = injector.getInstance(klass)

                plugin.definition!!.invalidate()
                plugin.definition = null
                plugin.instance_ = instance

                // Register listeners (GameStartEvent) etc.
                // Plugin Listeners WILL RUN BEFORE all listeners, in dependency order.
                Sandstone.game.eventManager.registerListeners(instance, instance)
            } catch (exception: Exception) {
                plugin.state_ = PluginState.FAILED
                Sandstone.logger.exception(exception, "Failed to load plugin: $plugin!")
            }
        }


        if (plugin.state_ != PluginState.FAILED) {
            plugin.state_ = PluginState.LOADED
        }
    }

    override fun loadClasses(classes: Array<String>): List<PluginContainer> {
        return this.loadClasses(classes.toList(), null, emptyArray(), false)
    }

    override fun loadFile(file: Path): List<PluginContainer> {
        val filePathAsFile = file.toFile()

        var simpleDesc: SimpleDesc? = null

        val jarFile = JarFile(filePathAsFile)
        val mutableClasses = mutableListOf<String>()

        val enumeration = jarFile.entries()

        while (enumeration.hasMoreElements()) {
            val next = enumeration.nextElement()

            if (!next.name.endsWith(".class"))
                continue

            val stream = jarFile.getInputStream(next)

            val desc = ASM.findPluginAnnotation(stream)

            if (desc != null) {
                if (simpleDesc == null)
                    simpleDesc = desc

                var name = next.name

                name = name.replace('/', '.').substring(0, name.length - CLASS_LENGTH)

                mutableClasses.add(name)
            }

        }

        val classes = mutableClasses.toList()
        val urls = arrayOf(URL("jar:file:$filePathAsFile!/"))

        return this.loadClasses(classes, file, urls, simpleDesc?.usePlatformInternals ?: false)
    }


    private fun loadClasses(classes: List<String>, file: Path? = null, urls: Array<URL> = emptyArray(), useInternal: Boolean): List<PluginContainer> {

        if (classes.isEmpty()) {
            throw IllegalArgumentException("Empty class collection!")
        }

        val classLoader = SandstoneClassLoader(
                urls = urls,
                file = file,
                parent = this.javaClass.classLoader,
                useInternal = useInternal,
                classes = classes)

        val containers = mutableListOf<PluginContainer>()

        containers +=
                classes.map {
                    classLoader.loadClass(it)
                }.map {
                    val declaredAnnotation = it.getDeclaredAnnotation(Plugin::class.java)

                    if (declaredAnnotation != null) {
                        val container = SandstonePluginContainer.fromAnnotation(
                                classLoader,
                                file,
                                it.canonicalName,
                                declaredAnnotation)

                        val definition = SandstonePluginDefinition(container)

                        container.definition = definition

                        container.state_ = PluginState.ABOUT_TO_LOAD

                        /*return@map */container
                    } else {
                        null
                    }
                }.filterNotNull()

        return containers.toList()
    }

}