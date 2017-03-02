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
package com.github.projectsandstone.common.guice

import com.github.projectsandstone.api.logging.Logger
import com.github.projectsandstone.api.plugin.PluginContainer
import com.github.projectsandstone.api.plugin.PluginDefinition
import com.github.projectsandstone.api.plugin.PluginManager
import com.github.projectsandstone.common.plugin.SandstonePluginContainer
import com.google.inject.AbstractModule
import com.google.inject.Scopes
import com.google.inject.name.Names

class SandstonePluginModule(val pluginManager: PluginManager, val pluginContainer: SandstonePluginContainer, val pluginClass: Class<*>) : AbstractModule() {

    override fun configure() {

        bind(PluginContainer::class.java).toInstance(this.pluginContainer)
        bind(PluginDefinition::class.java).toInstance(this.pluginContainer.definition!!)
        bind(Logger::class.java).toInstance(pluginContainer.logger)
        bind(this.pluginClass).`in`(Scopes.SINGLETON)

        // Bindings to @Named

        pluginManager.getPlugins().forEach {
            bind(PluginContainer::class.java).annotatedWith(Names.named(it.id)).toInstance(it)
        }

    }

}