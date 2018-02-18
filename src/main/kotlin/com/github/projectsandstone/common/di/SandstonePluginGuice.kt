package com.github.projectsandstone.common.di

import com.github.projectsandstone.api.Sandstone
import com.github.projectsandstone.api.plugin.PluginManager
import com.github.projectsandstone.common.guice.SandstonePluginModule
import com.github.projectsandstone.common.plugin.SandstonePluginContainer
import com.google.inject.Injector

class SandstonePluginGuice(val injector: Injector) : SandstonePluginDependencyInjection {

    override fun createPluginInjector(
        pluginManager: PluginManager,
        pluginContainer: SandstonePluginContainer,
        pluginClass: Class<*>
    ): Injector = this.injector.createChildInjector(
        SandstonePluginModule(
            Sandstone.pluginManager,
            pluginContainer,
            pluginClass
        )
    )

}