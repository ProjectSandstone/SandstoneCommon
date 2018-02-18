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

import com.github.projectsandstone.api.Game
import com.github.projectsandstone.api.Implementation
import com.github.projectsandstone.api.Platform
import com.github.projectsandstone.api.Sandstone
import com.github.projectsandstone.api.command.CommandManager
import com.github.projectsandstone.api.logging.LoggerFactory
import com.github.projectsandstone.api.plugin.PluginContainer
import com.github.projectsandstone.api.plugin.PluginDefinition
import com.github.projectsandstone.api.plugin.PluginManager
import com.github.projectsandstone.api.registry.Registry
import com.github.projectsandstone.api.service.ServiceManager
import com.github.projectsandstone.api.util.version.Schemes
import com.github.projectsandstone.api.util.version.VersionScheme
import com.github.projectsandstone.common.command.SandstoneCommandManager
import com.github.projectsandstone.common.event.SandstoneEventManager
import com.github.projectsandstone.common.plugin.SandstonePluginContainer
import com.github.projectsandstone.common.plugin.SandstonePluginManager
import com.github.projectsandstone.common.registry.SandstoneRegistry
import com.github.projectsandstone.common.util.version.AlphabeticVersionScheme
import com.github.projectsandstone.common.util.version.CommonVersionScheme
import com.github.projectsandstone.common.util.version.SemVerScheme
import com.github.projectsandstone.eventsys.event.EventManager
import com.google.inject.AbstractModule
import com.google.inject.Scopes
import com.google.inject.name.Names
import org.slf4j.Logger
import java.nio.file.Path

class SandstoneModule(
    val logger: Logger,
    val loggerFactory: LoggerFactory,
    val path: Path,
    val implementation: Implementation
) : AbstractModule() {

    override fun configure() {
        bind(Logger::class.java).toInstance(logger)
        bind(LoggerFactory::class.java).toInstance(loggerFactory)
        bind(Path::class.java).annotatedWith(Names.named("sandstonePath")).toInstance(path)
        bind(Implementation::class.java).toInstance(implementation)
        requestStaticInjection(Sandstone::class.java)
    }

}

class GameSandstoneModule(val game: Class<out Game>) : AbstractModule() {

    override fun configure() {
        bind(Game::class.java).to(game).`in`(Scopes.SINGLETON)
        bind(EventManager::class.java).to(SandstoneEventManager::class.java).`in`(Scopes.SINGLETON)
        bind(PluginManager::class.java).to(SandstonePluginManager::class.java)
            .`in`(Scopes.SINGLETON)
        bind(Registry::class.java).to(SandstoneRegistry::class.java).`in`(Scopes.SINGLETON)
        bind(CommandManager::class.java).to(SandstoneCommandManager::class.java)
            .`in`(Scopes.SINGLETON)
    }

}

class SandstonePluginModule(
    val pluginManager: PluginManager,
    val pluginContainer: SandstonePluginContainer,
    val pluginClass: Class<*>
) : AbstractModule() {

    override fun configure() {

        val provider = this.getProvider(Game::class.java)
        bind(Platform::class.java).toProvider(javax.inject.Provider { provider.get().platform })
        bind(ServiceManager::class.java).toProvider(javax.inject.Provider {
            provider.get().serviceManager
        })

        bind(PluginContainer::class.java).toInstance(this.pluginContainer)
        bind(PluginDefinition::class.java).toInstance(this.pluginContainer.definition!!)
        bind(Logger::class.java).toInstance(pluginContainer.logger)
        bind(this.pluginClass).`in`(Scopes.SINGLETON)

        // Bindings to @Named

        pluginManager.plugins.forEach {
            bind(PluginContainer::class.java).annotatedWith(Names.named(it.id)).toInstance(it)
        }

    }

}

class SandstoneSchemeModule : AbstractModule() {

    override fun configure() {
        bind(VersionScheme::class.java)
            .annotatedWith(Names.named("semVerScheme"))
            .to(SemVerScheme::class.java)

        bind(VersionScheme::class.java)
            .annotatedWith(Names.named("commonVersionScheme"))
            .to(CommonVersionScheme::class.java)

        bind(VersionScheme::class.java)
            .annotatedWith(Names.named("alphabeticScheme"))
            .to(AlphabeticVersionScheme::class.java)

        requestStaticInjection(Schemes::class.java)
    }

}