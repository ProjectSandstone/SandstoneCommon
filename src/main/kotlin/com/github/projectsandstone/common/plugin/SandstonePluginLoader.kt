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
import com.github.projectsandstone.api.event.SandstoneEventFactory
import com.github.projectsandstone.api.event.plugin.PluginLoadFailedEvent
import com.github.projectsandstone.api.plugin.*
import com.github.projectsandstone.api.util.exception.DependencyException
import com.github.projectsandstone.common.asm.ASM
import com.github.projectsandstone.common.guice.SandstoneModule
import com.github.projectsandstone.common.guice.SandstonePluginModule
import com.github.projectsandstone.common.util.getInstance
import java.net.URL
import java.nio.file.Path
import java.util.jar.JarFile

class SandstonePluginLoader(override val pluginManager: PluginManager) : PluginLoader {

    val CLASS_LENGTH = ".class".length

    override fun load(plugin: PluginContainer) {
        if (plugin !is SandstonePluginContainer) {
            Sandstone.logger.error("Cannot load plugin container: $plugin. Only SandstonePluginContainers are supported!")
            return
        }

        // Load classes
        this.init(plugin)


        plugin.dependenciesState_ = this.pluginManager.dependencyResolver.getDependenciesState(plugin)

        if (plugin.state_ != PluginState.ABOUT_TO_LOAD) {
            Sandstone.logger.error("Cannot load plugin container: $plugin. Current state: ${plugin.state_}!")
            return
        }

        plugin.state_ != PluginState.LOADING

        Sandstone.eventManager.dispatch(SandstoneEventFactory.instance.createPluginLoadingEvent(this.pluginManager, plugin), Sandstone)

        try {
            pluginManager.dependencyResolver.checkDependencies(plugin)
        } catch (e: DependencyException) {
            Sandstone.eventManager.dispatch(SandstoneEventFactory.instance.createPluginLoadFailedEvent(this.pluginManager, plugin, PluginLoadFailedEvent.Reason.DependencyResolutionFailed), Sandstone)
            Sandstone.logger.exception(e)

            plugin.state_ != PluginState.FAILED
            return
        }

        val pluginClassLoader = plugin.classLoader

        if (pluginClassLoader !is ClassLoader) {
            Sandstone.logger.error("Cannot load plugin container: $plugin. Plugin class load is not a ClassLoader!")
            return
        }

        plugin.logger_ = Sandstone.loggerFactory.createLogger(plugin)

        val clName = plugin.mainClass

        if (plugin.state_ != PluginState.FAILED) {

            try {
                val klass = pluginClassLoader.loadClass(clName)

                val injector = SandstoneModule.injector.createChildInjector(SandstonePluginModule(pluginManager, plugin, klass))

                val instance = getInstance(klass)?.let { injector.injectMembers(it) } ?: injector.getInstance(klass)

                plugin.definition!!.invalidate()
                plugin.definition = null
                plugin.instance_ = instance

                // Register listeners etc.
                // Plugin Listeners WILL RUN BEFORE all listeners, in dependency order.
                Sandstone.game.eventManager.registerListeners(instance, instance)
            } catch (exception: Exception) {
                plugin.state_ = PluginState.FAILED

                plugin.definition?.invalidate()
                plugin.definition = null

                Sandstone.logger.exception(exception, "Failed to load plugin: '$plugin'!")
                Sandstone.eventManager.dispatch(SandstoneEventFactory.instance.createPluginLoadFailedEvent(this.pluginManager, plugin, PluginLoadFailedEvent.Reason.Exception(exception)), Sandstone)
            }
        }


        if (plugin.state_ != PluginState.FAILED) {
            plugin.state_ = PluginState.LOADED
        }
    }

    fun createFromClasses(classes: Array<String>): List<PluginContainer> {

        val classList = classes.toList()

        // TODO: Change this for Java 9 (when it get released).
        val urls = emptyArray<URL>()

        val classLoader = SandstoneClassLoader(
                urls = urls,
                file = null,
                parent = this.javaClass.classLoader,
                useInternal = false,
                classes = classList)

        val loadedClasses = loadClasses(classLoader, classList, urls, null)

        return loadedClasses.map {
            val annotation = it.getDeclaredAnnotation(Plugin::class.java)

            if(annotation != null) {
                return@map SandstonePluginContainer.fromAnnotation(classLoader, null, it.canonicalName, classes, annotation)
            }

            return@map null
        }.filterNotNull()

    }

    fun createFromFile(file: Path): List<PluginContainer> {
        val filePathAsFile = file.toFile()

        val containers = mutableListOf<SandstonePluginContainer>()

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

                containers += desc

                var name = next.name

                name = name.replace('/', '.').substring(0, name.length - CLASS_LENGTH)

                mutableClasses.add(name)
            }

        }

        containers.forEach {
            it.file_ = file
            it.classes = mutableClasses.toTypedArray()
        }

        return containers
    }

    private fun init(sandstonePluginContainer: SandstonePluginContainer) {
        val classes = sandstonePluginContainer.classes.toList()
        val file = sandstonePluginContainer.file_
        val filePathAsFile = sandstonePluginContainer.file_?.toFile()

        // TODO: Change this for Java 9 (when it get released).
        val urls = if(filePathAsFile != null) arrayOf(URL("jar:file:$filePathAsFile!/")) else emptyArray()

        if (classes.isEmpty()) {
            throw IllegalArgumentException("Empty class collection of plugin '$sandstonePluginContainer'!")
        }

        if(sandstonePluginContainer.classLoader_ == null) {

            val classLoader = SandstoneClassLoader(
                    urls = urls,
                    file = file,
                    parent = this.javaClass.classLoader,
                    useInternal = sandstonePluginContainer.usePlatformInternals,
                    classes = classes)

            this.loadClasses(classLoader, classes, urls, file)

            sandstonePluginContainer.classLoader_ = classLoader
        }

        sandstonePluginContainer.definition = SandstonePluginDefinition(sandstonePluginContainer)
        sandstonePluginContainer.state_ = PluginState.ABOUT_TO_LOAD

    }

    private fun loadClasses(classLoader: ClassLoader, classes: List<String>, urls: Array<URL>, file: Path?): List<Class<*>> {
        val mapped: MutableList<Class<*>> = mutableListOf()

        for (className in classes) {
            try {
                mapped += classLoader.loadClass(className)
            } catch (e: Exception) {
                Sandstone.logger.exception(e, "Failed to load class '$className'. Other classes will not be loaded to avoid future problems. Additional information: [urls: $urls, file: $file]. Some plugins may not be loaded!")
                mapped.clear()
                break
            }
        }

        return mapped
    }
}