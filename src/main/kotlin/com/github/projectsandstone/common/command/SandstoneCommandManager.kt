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

import com.github.jonathanxd.iutils.collection.view.ViewCollections
import com.github.jonathanxd.iutils.collection.view.ViewUtils
import com.github.jonathanxd.kwcommands.command.Command
import com.github.jonathanxd.kwcommands.completion.CompletionImpl
import com.github.jonathanxd.kwcommands.dispatch.CommandDispatcherImpl
import com.github.jonathanxd.kwcommands.help.CommonHelpInfoHandler
import com.github.jonathanxd.kwcommands.information.InformationProviders
import com.github.jonathanxd.kwcommands.information.InformationProvidersImpl
import com.github.jonathanxd.kwcommands.manager.CommandManagerImpl
import com.github.jonathanxd.kwcommands.manager.InstanceProvider
import com.github.jonathanxd.kwcommands.manager.instanceProvider
import com.github.jonathanxd.kwcommands.parser.CommandParserImpl
import com.github.jonathanxd.kwcommands.processor.CommandResult
import com.github.jonathanxd.kwcommands.reflect.env.ReflectionEnvironment
import com.github.projectsandstone.api.Game
import com.github.projectsandstone.api.Sandstone
import com.github.projectsandstone.api.annotation.PluginInstance
import com.github.projectsandstone.api.command.*
import com.github.projectsandstone.api.entity.living.player.Player
import com.github.projectsandstone.common.util.isOwnerEq
import java.util.function.Function
import javax.inject.Inject

class SandstoneCommandManager @Inject constructor(val game: Game) : CommandManager {
    private val manager = CommandManagerImpl()
    private val parser = CommandParserImpl(this.manager)
    private val dispatcher = CommandDispatcherImpl(this.manager)
    private val reflect = ReflectionEnvironment(this.manager)
    private val commandComplete = CompletionImpl(this.parser)
    private val klassCmds = mapOf<Pair<Class<*>, Any>, List<Command>>()
    private val helpInfoHandler = CommonHelpInfoHandler()

    override val argumentTypes: ArgumentTypes = ArgumentTypesImpl(this.game)

    override val commands: Set<RegisteredCommand> = ViewCollections.setMapped(manager.commandsWithOwner,
        Function { RegisteredCommand(it.first, it.second) },
        ViewUtils.unmodifiable(),
        ViewUtils.unmodifiable()
    )

    override fun registerCommand(command: Command, @PluginInstance ownerPlugin: Any): Boolean =
        this.manager.registerCommand(command, ownerPlugin)

    override fun unregisterCommand(command: Command, @PluginInstance ownerPlugin: Any): Boolean =
        this.manager.unregisterCommand(command, ownerPlugin)

    override fun registerInstance(any: Any, @PluginInstance ownerPlugin: Any): Boolean =
        this.reflect.registerCommands(fromInstance(any, ownerPlugin), ownerPlugin)

    override fun <T: Any> registerInstance(klass: Class<T>, instance: T, @PluginInstance ownerPlugin: Any): Boolean =
        this.reflect.registerCommands(fromClass(klass, instance, ownerPlugin), ownerPlugin)

    override fun <T: Any> registerInstance(
        klass: Class<T>,
        instanceProvider: InstanceProvider,
        ownerPlugin: Any
    ): Boolean =
        this.reflect.registerCommands(fromClass(klass, instanceProvider, ownerPlugin), ownerPlugin)

    override fun unregisterInstance(any: Any, @PluginInstance ownerPlugin: Any): Boolean =
        fromInstance(any, ownerPlugin).all {
            manager.unregisterCommand(it, ownerPlugin)
        }

    override fun unregisterCommands(klass: Class<*>, @PluginInstance ownerPlugin: Any): Boolean =
        klassCmds.filter { (k, _) -> k.first == klass }
            .all { (_, v) ->
                v.all { manager.unregisterCommand(it, ownerPlugin) }
            }

    override fun getCommandsRegisteredBy(@PluginInstance ownerPlugin: Any): Set<Command> =
        this.manager.createCommandsPair()
            .filter { (_, owner) -> this.game.pluginManager.isOwnerEq(ownerPlugin, owner) }
            .map { (c, _) -> c }.toSet()

    override fun getCommand(name: String, owner: Any?): Command? =
        this.manager.commandsWithOwner.firstOrNull { (cmd, cmdOwner) ->
            cmd.name == name && (owner == null || owner == cmdOwner)
        }?.first

    override fun getSubCommand(parent: Command, name: String): Command? =
        this.manager.getSubCommand(parent,  name)

    override fun createInformationProviders(): InformationProviders =
        InformationProvidersImpl().also {
            it.registerBaseProviders()
        }

    override fun createInformationProviders(@PluginInstance pluginInstance: Any): InformationProviders =
        createInformationProviders().also {
            it.registerPluginInstance(pluginInstance)
        }

    override fun createInformationProviders(player: Player): InformationProviders =
        createInformationProviders().also {
            it.registerPlayerInstance(player)
        }

    override fun createInformationProviders(
        pluginInstance: Any,
        player: Player
    ): InformationProviders =
        createInformationProviders().also {
            it.registerPluginInstance(pluginInstance)
            it.registerPlayerInstance(player)
        }

    private fun InformationProviders.registerBaseProviders() {
        this.registerInformationProvider(SubjectProvider(this))
    }

    private fun InformationProviders.registerPluginInstance(@PluginInstance pluginInstance: Any) {
        val plugin = game.pluginManager.getRequiredPlugin(pluginInstance)
        this.registerInformation(PLUGIN_INFO, plugin)
        this.registerInformation(LOGGER_INFO, plugin.logger)
    }

    private fun InformationProviders.registerPlayerInstance(player: Player) {
        this.registerInformation(COMMAND_SOURCE_INFO, player)
        this.registerInformation(PLAYER_INFO, player)
    }

    override fun dispatch(
        commandStr: String,
        informationProviders: InformationProviders
    ): List<CommandResult> {
        val parse = this.parser.parse(commandStr, null)

        return when {
            parse.isRight -> this.dispatcher.dispatch(parse.right, informationProviders)
            parse.isLeft -> {
                val source = informationProviders.find(COMMAND_SOURCE_INFO)
                if (source != null) {
                    helpInfoHandler.handleFail(
                        parse.left,
                        this.game.objectHelper.createPrinter(source.value)
                    )
                } else {
                    val logger = informationProviders.find(PLUGIN_INFO)?.value
                        ?.let { game.pluginManager.getPlugin(it) }
                        ?.logger
                            ?: informationProviders.find(LOGGER_INFO)?.value
                            ?: Sandstone.logger

                    helpInfoHandler.handleFail(
                        parse.left,
                        this.game.objectHelper.createPrinter(logger)
                    )
                }
                emptyList()
            }
            else -> emptyList()
        }
    }

    override fun getSuggestions(
        command: String,
        informationProviders: InformationProviders
    ): List<String> {
        return this.commandComplete.complete(command, null, informationProviders)
    }

    override fun printCommandHelp(source: CommandSource, command: String) {
        val printer = this.game.objectHelper.createPrinter(source)
        val parse = this.parser.parse(command, null)
        if (parse.isLeft) {
            this.helpInfoHandler.handleFail(parse.left, printer)
        } else {
            for (commandContainer in parse.right) {
                printer.printCommand(commandContainer.command, 0)
            }

            printer.flush()
        }
    }

    private fun fromInstance(any: Any, @PluginInstance ownerPlugin: Any) =
        ownerPlugin.let {
            game.pluginManager.getRequiredPlugin(it)
            klassCmds[any::class.java to any]
                    ?: this.reflect.fromClass(any::class.java, instanceProvider { any }, it)
        }


    private fun fromClass(klass: Class<*>, any: Any?, @PluginInstance ownerPlugin: Any) =
        ownerPlugin.let {
            game.pluginManager.getRequiredPlugin(it)
            klassCmds[klass to any]
                    ?: this.reflect.fromClass(klass, instanceProvider { any }, it)
        }

    private fun fromClass(klass: Class<*>, provider: InstanceProvider, @PluginInstance ownerPlugin: Any) =
        ownerPlugin.let {
            game.pluginManager.getRequiredPlugin(ownerPlugin)
            klassCmds[klass to provider]
                    ?: this.reflect.fromClass(klass, provider, it)
        }

}