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

import com.github.jonathanxd.kores.type.bindedDefaultResolver
import com.github.jonathanxd.kores.type.canonicalName
import com.github.jonathanxd.kores.type.javaSpecName
import com.github.jonathanxd.redin.Injector
import com.github.jonathanxd.redin.SINGLETON
import com.github.projectsandstone.api.Sandstone
import com.github.projectsandstone.api.event.SandstoneEventFactoryCache
import com.github.projectsandstone.api.event.plugin.PluginLoadFailedEvent
import com.github.projectsandstone.api.plugin.*
import com.github.projectsandstone.api.util.exception.DependencyException
import com.github.projectsandstone.common.asm.ASM
import com.github.projectsandstone.common.di.SandstonePluginDependencyInjection
import com.github.projectsandstone.common.util.getInstance
import java.lang.reflect.Type
import java.net.URL
import java.nio.file.Path
import java.util.jar.JarFile

class SandstonePluginLoader(
        override val pluginManager: PluginManager,
        private val di: SandstonePluginDependencyInjection
) : PluginLoader {

    private val classLoaders = mutableListOf<SandstoneClassLoader>()
    val CLASS_LENGTH = ".class".length

    override fun load(plugin: PluginContainer) {
        if (plugin !is SandstonePluginContainer) {
            Sandstone.logger.error("Cannot load plugin container: $plugin. Only SandstonePluginContainers are supported!")
            return
        }

        // Load classes
        this.init(plugin)


        plugin.dependenciesState_ =
                this.pluginManager.dependencyResolver.getDependenciesState(plugin)

        if (plugin.state_ != PluginState.ABOUT_TO_LOAD) {
            Sandstone.logger.error("Cannot load plugin container: $plugin. Current state: ${plugin.state_}!")
            return
        }

        plugin.state_ = PluginState.LOADING

        Sandstone.eventManager.dispatch(
                SandstoneEventFactoryCache.getInstance()
                        .createPluginLoadingEvent(this.pluginManager, plugin), Sandstone
        )

        try {
            pluginManager.dependencyResolver.checkDependencies(plugin)
        } catch (e: DependencyException) {
            Sandstone.eventManager.dispatch(
                    SandstoneEventFactoryCache.getInstance()
                            .createPluginLoadFailedEvent(
                                    PluginLoadFailedEvent.Reason.DependencyResolutionFailed,
                                    this.pluginManager,
                                    plugin
                            ), Sandstone
            )

            Sandstone.logger.error("Dependency missing for plugin: '$plugin'", e)

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

                val injector = this.di.createPluginInjector(this.pluginManager, plugin, klass)

                val instance =
                        getInstance(klass)?.let { injector.injectMembers(it) }
                                ?: injector[klass, SINGLETON]

                plugin.definition!!.invalidate()
                plugin.definition = null
                plugin.instance_ = instance

                // Register listeners etc.
                // Plugin Listeners WILL RUN BEFORE all listeners, in dependency order.
                Sandstone.game.eventListenerRegistry.registerListeners(instance, instance)
            } catch (exception: Exception) {
                plugin.state_ = PluginState.FAILED

                plugin.definition?.invalidate()
                plugin.definition = null

                Sandstone.logger.error("Failed to load plugin: '$plugin'!", exception)
                Sandstone.eventManager.dispatch(
                        SandstoneEventFactoryCache.getInstance()
                                .createPluginLoadFailedEvent(
                                        PluginLoadFailedEvent.Reason.Exception(exception),
                                        this.pluginManager,
                                        plugin
                                ), Sandstone
                )
            }
        }


        if (plugin.state_ != PluginState.FAILED) {
            plugin.state_ = PluginState.LOADED
        }
    }

    /**
     * Finds [PluginContainers][PluginContainer] which may own the provided [instance]. This is done by
     * retrieving the class loader of [instance] class and checking if it is a [PluginClassLoader],
     * then retrieving [plugin containers][PluginClassLoader.pluginContainers] related to it.
     *
     * Multiple [PluginContainers][PluginContainer] are returned because one loaded resource may have
     * more than one plugin declared in it.
     *
     * Also if [instance] is the main instance of a plugin, only [PluginContainers][PluginContainer]
     * matching this instance class will be returned, if possible.
     */
    fun findPlugins(instance: Any): List<PluginContainer> =
            this.findPlugins(instance::class.java)

    /**
     * Finds [PluginContainers][PluginContainer] which may own the provided [loadedClass]. This is done by
     * retrieving the class loader of [loadedClass] and checking if it is a [PluginClassLoader],
     * then retrieving [plugin containers][PluginClassLoader.pluginContainers] related to it.
     *
     * Multiple [PluginContainers][PluginContainer] are returned because one loaded resource may have
     * more than one plugin declared in it.
     *
     * Also if [loadedClass] is the main class of a plugin, only [PluginContainers][PluginContainer]
     * matching this class will be returned, if possible.
     */
    fun findPlugins(loadedClass: Class<*>): List<PluginContainer> {
        val classLoader = loadedClass.classLoader as? PluginClassLoader
        val plugins = classLoader?.pluginContainers.orEmpty()
        return plugins.filterOrAll {
            it.mainClass == loadedClass.canonicalName
                    || it.instance?.javaClass == loadedClass
        }
    }

    /**
     * Finds [PluginContainers][PluginContainer] which may own the provided instance. This is done by
     * retrieving the class of [type] and then the class loader and checking if it is a [PluginClassLoader],
     * then retrieving [plugin containers][PluginClassLoader.pluginContainers] related to it.
     *
     * Multiple [PluginContainers][PluginContainer] are returned because one loaded resource may have
     * more than one plugin declared in it.
     *
     * Also if [type] is the main class of a plugin, only [PluginContainers][PluginContainer]
     * matching this class will be returned, if possible.
     *
     * Note: If [Class] of [type] cannot be retrieved or resolved to a runtime type, a scanning
     * will run in all known [PluginClassLoaders][PluginClassLoader] trying to find a class that
     * the [java spec name][Type.javaSpecName] matches the [java spec name][Type.javaSpecName] of type.
     */
    fun findPlugins(type: Type): List<PluginContainer> {
        val resolve = type.bindedDefaultResolver.resolve()
        return if (resolve.isRight && resolve.right is Class<*>) {
            this.findPlugins(resolve.right as Class<*>)
        } else {
            this.classLoaders.find { it.loadedClassesName.contains(type.javaSpecName) }
                    ?.let {
                        it.pluginContainers.filterOrAll { pluginContainer ->
                            pluginContainer.mainClass == type.canonicalName
                        }
                    }.orEmpty()
        }
    }

    private fun List<PluginContainer>.filterOrAll(
            filter: (PluginContainer) -> Boolean): List<PluginContainer> {
        val matchingPlugins = this.filter(filter)
        return if (matchingPlugins.isNotEmpty()) matchingPlugins
        else this
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
                classes = classList
        ).also {
            this.classLoaders.add(it)
        }

        val loadedClasses = loadClasses(classLoader, classList, urls, null)

        return loadedClasses.map {
            val annotation = it.getDeclaredAnnotation(Plugin::class.java)

            if (annotation != null) {
                return@map SandstonePluginContainer.fromAnnotation(
                        classLoader,
                        null,
                        it.canonicalName,
                        classes,
                        annotation
                )
            }

            return@map null
        }.filterNotNull().onEach {
            classLoader.addPluginContainer(it)
        }

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
        val urls =
                if (filePathAsFile != null) arrayOf(URL("jar:file:$filePathAsFile!/")) else emptyArray()

        if (classes.isEmpty()) {
            throw IllegalArgumentException("Empty class collection of plugin '$sandstonePluginContainer'!")
        }

        if (sandstonePluginContainer.classLoader_ == null) {

            val classLoader = SandstoneClassLoader(
                    urls = urls,
                    file = file,
                    parent = this.javaClass.classLoader,
                    useInternal = sandstonePluginContainer.usePlatformInternals,
                    classes = classes
            ).also {
                this.classLoaders.add(it)
            }

            this.loadClasses(classLoader, classes, urls, file)

            sandstonePluginContainer.classLoader_ = classLoader
        }

        sandstonePluginContainer.definition = SandstonePluginDefinition(sandstonePluginContainer)
        sandstonePluginContainer.state_ = PluginState.ABOUT_TO_LOAD

    }

    private fun loadClasses(
            classLoader: ClassLoader,
            classes: List<String>,
            urls: Array<URL>,
            file: Path?
    ): List<Class<*>> {
        val mapped: MutableList<Class<*>> = mutableListOf()

        for (className in classes) {
            try {
                mapped += classLoader.loadClass(className)
            } catch (e: Exception) {
                Sandstone.logger.error(
                        "Failed to load class '$className'. Other classes will not be loaded to avoid future problems. Additional information: [urls: $urls, file: $file]. Some plugins may not be loaded!",
                        e
                )
                mapped.clear()
                break
            }
        }

        return mapped
    }
}