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
package com.github.projectsandstone.common.guice

import com.github.jonathanxd.redin.BindContext
import com.github.jonathanxd.redin.Name
import com.github.jonathanxd.redin.SINGLETON
import com.github.jonathanxd.redin.provide
import com.github.koresframework.eventsys.channel.ChannelSet
import com.github.koresframework.eventsys.event.EventDispatcher
import com.github.koresframework.eventsys.event.EventListenerRegistry
import com.github.koresframework.eventsys.event.EventManager
import com.github.koresframework.eventsys.gen.event.CommonEventGenerator
import com.github.koresframework.eventsys.gen.event.EventGenerator
import com.github.koresframework.eventsys.impl.CommonChannelEventListenerRegistry
import com.github.koresframework.eventsys.impl.CommonEventDispatcher
import com.github.koresframework.eventsys.logging.LoggerInterface
import com.github.projectsandstone.api.Game
import com.github.projectsandstone.api.Implementation
import com.github.projectsandstone.api.Platform
import com.github.projectsandstone.api.command.CommandManager
import com.github.projectsandstone.api.logging.LoggerFactory
import com.github.projectsandstone.api.plugin.PluginContainer
import com.github.projectsandstone.api.plugin.PluginDefinition
import com.github.projectsandstone.api.plugin.PluginManager
import com.github.projectsandstone.api.registry.Registry
import com.github.projectsandstone.api.service.ServiceManager
import com.github.projectsandstone.api.util.version.VersionScheme
import com.github.projectsandstone.common.Constants
import com.github.projectsandstone.common.command.SandstoneCommandManager
import com.github.projectsandstone.common.event.*
import com.github.projectsandstone.common.plugin.SandstonePluginContainer
import com.github.projectsandstone.common.plugin.SandstonePluginManager
import com.github.projectsandstone.common.registry.SandstoneRegistry
import com.github.projectsandstone.common.util.version.AlphabeticVersionScheme
import com.github.projectsandstone.common.util.version.CommonVersionScheme
import com.github.projectsandstone.common.util.version.SemVerScheme
import org.slf4j.Logger
import java.nio.file.Path

fun BindContext.installSandstone(logger: Logger,
                                 loggerFactory: LoggerFactory,
                                 path: Path,
                                 implementation: Implementation) {
    bind<Logger>() toValue logger
    bind<LoggerFactory>() toValue loggerFactory
    bind<Path>() qualifiedWith Name("sandstonePath") toValue path
    bind<Implementation>() toValue implementation
}


fun BindContext.installSandstoneGame(game: Class<out Game>,
                                     logger: Logger) {
    bind<Game>() inScope SINGLETON toImplementation game
    bind<PluginManager>() inScope SINGLETON toImplementation SandstonePluginManager::class.java

    bind<LoggerInterface>() inScope SINGLETON toProvider { WrapperLoggerInterface(logger) }

    val loggerInterfaceRetrieve = injector.provide<LoggerInterface>(scope = SINGLETON)
    val eventGeneratorRetrieve = injector.provide<EventGenerator>(scope = SINGLETON)

    bind<EventGenerator>() inScope SINGLETON toProvider {
        CommonEventGenerator(loggerInterfaceRetrieve())
    }

    bind<EventListenerRegistry>() inScope SINGLETON toProvider {
        CommonChannelEventListenerRegistry(
                ChannelSet.ALL,
                Constants.listenerSorter,
                loggerInterfaceRetrieve(),
                eventGeneratorRetrieve()
        )
    }

    val eventListenerRegistryRetrieve = injector.provide<EventListenerRegistry>(scope = SINGLETON)

    bind<EventDispatcher>() inScope SINGLETON toProvider {
        CommonEventDispatcher(
                Constants.daemonThreadFactory,
                eventGeneratorRetrieve(),
                loggerInterfaceRetrieve(),
                eventListenerRegistryRetrieve()
        )

    }

    bind<EventManager>() inScope SINGLETON toImplementation SandstoneEventManager::class.java
    bind<Registry>() inScope SINGLETON toImplementation SandstoneRegistry::class.java
    bind<CommandManager>() inScope SINGLETON toImplementation SandstoneCommandManager::class.java
}

fun BindContext.installPlugin(pluginManager: PluginManager,
                              pluginContainer: SandstonePluginContainer,
                              pluginClass: Class<*>) {

    val provider = injector.provide<Game>(scope = SINGLETON)
    bind<Platform>() toProvider { provider().platform }
    bind<ServiceManager>() toProvider {
        provider().serviceManager
    }

    bind<PluginContainer>() toValue pluginContainer
    bind<PluginDefinition>() toValue pluginContainer.definition!!
    bind<Logger>() toValue pluginContainer.logger
    //bind(pluginClass) inScope Scopes.SINGLETON

    // Bindings to @Named

    pluginManager.plugins.forEach {
        bind<PluginContainer>() qualifiedWith Name(it.id) toValue it
    }

    // Binding overrides
    val eventGenerator = injector.provide<EventGenerator>(scope = SINGLETON)
    val eventListenerRegistry = injector.provide<EventListenerRegistry>(scope = SINGLETON)
    val eventDispatcher = injector.provide<EventDispatcher>(scope = SINGLETON)

    bind<EventGenerator>().toProvider {
        SandstoneEventGeneratorPerPlugin(eventGenerator(), pluginContainer)
    }

    bind<EventListenerRegistry>().toProvider {
        SandstoneListenerRegistryPerPlugin(eventListenerRegistry(), pluginContainer)
    }

    bind<EventDispatcher>().toProvider {
        SandstoneEventDispatcherPerPlugin(eventDispatcher(), pluginContainer)
    }

    val newDispatcher = injector.provide<EventDispatcher>(scope = SINGLETON)

    bind<EventManager>().toProvider {
        SandstoneEventManagerPerPlugin(newDispatcher())
    }

    val instance = injector[pluginClass, SINGLETON]

    @Suppress("UNCHECKED_CAST")
    bind(pluginClass as Class<Any>) inScope SINGLETON toValue instance
}

fun BindContext.installScheme() {

    bind<VersionScheme>() qualifiedWith Name("semVerScheme") toImplementation SemVerScheme::class.java

    bind<VersionScheme>() qualifiedWith Name("commonVersionScheme") toImplementation CommonVersionScheme::class.java

    bind<VersionScheme>() qualifiedWith Name("alphabeticScheme") toImplementation AlphabeticVersionScheme::class.java

}