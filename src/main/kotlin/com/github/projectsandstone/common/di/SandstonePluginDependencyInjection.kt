package com.github.projectsandstone.common.di

import com.github.projectsandstone.api.plugin.PluginManager
import com.github.projectsandstone.common.plugin.SandstonePluginContainer
import com.google.inject.Injector

interface SandstonePluginDependencyInjection {
    fun createPluginInjector(
        pluginManager: PluginManager,
        pluginContainer: SandstonePluginContainer, pluginClass: Class<*>
    ): Injector
}