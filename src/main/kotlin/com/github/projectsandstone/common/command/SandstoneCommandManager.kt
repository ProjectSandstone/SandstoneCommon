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
package com.github.projectsandstone.common.command

import com.github.jonathanxd.kwcommands.command.Command
import com.github.jonathanxd.kwcommands.manager.CommandManagerImpl
import com.github.jonathanxd.kwcommands.manager.InformationManager
import com.github.jonathanxd.kwcommands.manager.InformationManagerImpl
import com.github.jonathanxd.kwcommands.manager.InstanceProvider
import com.github.jonathanxd.kwcommands.processor.CommandResult
import com.github.jonathanxd.kwcommands.processor.Processors
import com.github.jonathanxd.kwcommands.reflect.env.ReflectionEnvironment
import com.github.jonathanxd.kwcommands.util.registerInformation
import com.github.projectsandstone.api.Sandstone
import com.github.projectsandstone.api.command.CommandManager
import com.github.projectsandstone.api.command.LOGGER_SUBJECT
import com.github.projectsandstone.api.command.PLAYER_SUBJECT
import com.github.projectsandstone.api.command.PLUGIN_SUBJECT
import com.github.projectsandstone.api.entity.living.player.Player

class SandstoneCommandManager : CommandManager {
    private val manager = CommandManagerImpl()
    private val processor = Processors.createCommonProcessor(manager)
    private val reflect = ReflectionEnvironment(manager)
    private val klassCmds = mapOf<Pair<Class<*>, Any>, List<Command>>()

    override fun registerCommand(command: Command, ownerPlugin: Any): Boolean =
        this.manager.registerCommand(command, ownerPlugin)

    override fun unregisterCommand(command: Command, ownerPlugin: Any): Boolean =
            this.manager.unregisterCommand(command, ownerPlugin)

    override fun registerInstance(any: Any, ownerPlugin: Any): Boolean =
            this.reflect.registerCommands(fromInstance(any, ownerPlugin), ownerPlugin)

    override fun <T> registerInstance(klass: Class<T>, instance: T, ownerPlugin: Any): Boolean =
            this.reflect.registerCommands(fromClass(klass, instance, ownerPlugin), ownerPlugin)

    override fun <T> registerInstance(klass: Class<T>, instanceProvider: InstanceProvider, ownerPlugin: Any): Boolean =
            this.reflect.registerCommands(fromClass(klass, instanceProvider, ownerPlugin), ownerPlugin)

    override fun unregisterInstance(any: Any, ownerPlugin: Any): Boolean =
            fromInstance(any, ownerPlugin).all {
                manager.unregisterCommand(it, ownerPlugin)
            }

    override fun unregisterCommands(klass: Class<*>, ownerPlugin: Any): Boolean =
            klassCmds.filter { (k, _) -> k.first == klass }
                    .all { (_, v) -> v.all { manager.unregisterCommand(it, ownerPlugin) }
            }

    override fun getCommandsRegisteredBy(ownerPlugin: Any): Set<Command> =
            this.manager.createCommandsPair().filter { (_, owner) -> owner == ownerPlugin }
                    .map { (c, _) -> c }.toSet()

    override fun createInformationManager(): InformationManager =
        InformationManagerImpl().also {
            it.registerInformationProvider(SubjectProvider(it))
        }

    override fun createInformationManager(pluginInstance: Any): InformationManager =
        createInformationManager().also {
            val plugin = Sandstone.pluginManager.getRequiredPlugin(pluginInstance)
            it.registerInformation(PLUGIN_SUBJECT, plugin)
            it.registerInformation(LOGGER_SUBJECT, plugin.logger)
        }

    override fun createInformationManager(player: Player): InformationManager =
            createInformationManager().also {
                it.registerInformation(PLAYER_SUBJECT, player)
            }

    override fun createInformationManager(pluginInstance: Any, player: Player): InformationManager =
            createInformationManager(pluginInstance).also {
                it.registerInformation(PLAYER_SUBJECT, player)
            }

    override fun dispatch(commandList: List<String>, informationManager: InformationManager): List<CommandResult> =
        this.processor.handle(this.processor.process(commandList, null), informationManager)

    override fun getSuggestions(input: String, informationManager: InformationManager) {
        TODO("suggestions")
    }

    private fun fromInstance(any: Any, ownerPlugin: Any) =
            klassCmds[any::class.java to any]
                    ?: this.reflect.fromClass(any::class.java, {any}, ownerPlugin)

    private fun fromClass(klass: Class<*>, any: Any?, ownerPlugin: Any) =
            klassCmds[klass to any]
                    ?: this.reflect.fromClass(klass, {any}, ownerPlugin)

    private fun fromClass(klass: Class<*>, provider: InstanceProvider, ownerPlugin: Any) =
            klassCmds[klass to provider]
                    ?: this.reflect.fromClass(klass, {provider.get(it)}, ownerPlugin)
}