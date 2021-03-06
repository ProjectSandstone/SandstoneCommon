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

import com.github.jonathanxd.iutils.kt.Try
import com.github.jonathanxd.iutils.kt.classOf
import com.github.jonathanxd.iutils.kt.leftOrRight
import com.github.jonathanxd.iutils.kt.typeInfo
import com.github.jonathanxd.kwcommands.argument.ArgumentType
import com.github.jonathanxd.kwcommands.argument.ArgumentTypeHelper
import com.github.jonathanxd.kwcommands.command.Command
import com.github.jonathanxd.kwcommands.parser.Input
import com.github.jonathanxd.kwcommands.parser.SingleInput
import com.github.jonathanxd.kwcommands.parser.ValueOrValidation
import com.github.jonathanxd.kwcommands.parser.ValueOrValidationFactory
import com.github.jonathanxd.kwcommands.util.simpleArgumentType
import com.github.projectsandstone.api.Game
import com.github.projectsandstone.api.block.BlockType
import com.github.projectsandstone.api.command.ArgumentTypes
import com.github.projectsandstone.api.entity.EntityType
import com.github.projectsandstone.api.entity.living.player.Player
import com.github.projectsandstone.api.item.ItemType
import com.github.projectsandstone.api.plugin.PluginContainer
import com.github.projectsandstone.api.util.extension.registry.getRegistryEntry
import java.util.*

class ArgumentTypesImpl(val game: Game) : ArgumentTypes {
    private val player = simpleArgumentType<Player>(PlayerArgumentType(this.game), null, typeInfo())
    private val plugin =
        simpleArgumentType<PluginContainer>(PluginArgumentType(this.game), null, typeInfo())
    private val command =
        simpleArgumentType<Command>(CommandArgumentType(this.game), null, typeInfo())
    private val entityType =
        simpleArgumentType<EntityType>(EntityTypeArgumentType(this.game), null, typeInfo())
    private val itemType =
        simpleArgumentType<ItemType>(ItemTypeArgumentType(this.game), null, typeInfo())
    private val blockType =
        simpleArgumentType<BlockType>(BlockTypeArgumentType(this.game), null, typeInfo())

    override fun player(): ArgumentType<SingleInput, Player> = this.player
    override fun plugin(): ArgumentType<SingleInput, PluginContainer> = this.plugin
    override fun command(): ArgumentType<SingleInput, Command> = this.command
    override fun entityType(): ArgumentType<SingleInput, EntityType> = this.entityType
    override fun itemType(): ArgumentType<SingleInput, ItemType> = this.itemType
    override fun blockType(): ArgumentType<SingleInput, BlockType> = this.blockType
}

internal class PlayerArgumentType(val game: Game) : ArgumentTypeHelper<SingleInput, Player>() {
    override fun parse(
        input: SingleInput,
        valueOrValidationFactory: ValueOrValidationFactory
    ): ValueOrValidation<Player> =
        valueOrValidationFactory.valueOrValidation(input.input.let {
            if (it.length == 36) // UUID
                Try<UUID, Exception> { UUID.fromString(it) }
                    .map { game.server.getOnlinePlayer(it) }
                    .mapError { null }
                    .getSuccessOrError({it}, {it})
            else
                game.server.getOnlinePlayer(it)
        })

    override fun possibilities(): List<Input> =
        this.game.server.players.map { it.name }.map(::SingleInput)
}

internal class PluginArgumentType(val game: Game) :
    ArgumentTypeHelper<SingleInput, PluginContainer>() {
    override fun parse(
        input: SingleInput,
        valueOrValidationFactory: ValueOrValidationFactory
    ): ValueOrValidation<PluginContainer> =
        valueOrValidationFactory.valueOrValidation(game.pluginManager.getPlugin(input.input))

    override fun possibilities(): List<Input> =
        this.game.pluginManager.plugins.map { it.name }.map(::SingleInput)
}

internal class CommandArgumentType(val game: Game) : ArgumentTypeHelper<SingleInput, Command>() {

    override fun parse(
        input: SingleInput,
        valueOrValidationFactory: ValueOrValidationFactory
    ): ValueOrValidation<Command> =
        valueOrValidationFactory.valueOrValidation(this.game.commandManager.getCommand(input.input))

    override fun possibilities(): List<Input> =
        this.game.commandManager.commands.groupBy { it.command.name }
            .flatMap { flat ->
                if (flat.value.size == 1) listOf(flat.value.single().command.name)
                else flat.value.map { "${pluginNamePrefix(it.ownerPlugin)}:${it.command.name}" }
            }
            .map(::SingleInput)

    private fun pluginNamePrefix(plugin: Any?): String =
        plugin?.let {
            this.game.pluginManager.getPlugin(it)?.name?.let { "$it:" }
        } ?: ""
}

internal class EntityTypeArgumentType(val game: Game) :
    ArgumentTypeHelper<SingleInput, EntityType>() {

    override fun parse(
        input: SingleInput,
        valueOrValidationFactory: ValueOrValidationFactory
    ): ValueOrValidation<EntityType> =
        valueOrValidationFactory.valueOrValidation(this.game.registry.getRegistryEntry(input.input))

    override fun possibilities(): List<Input> =
        this.game.registry.getAll(classOf<EntityType>()).map { it.id }.map(::SingleInput)
}

internal class ItemTypeArgumentType(val game: Game) :
    ArgumentTypeHelper<SingleInput, ItemType>() {

    override fun parse(
        input: SingleInput,
        valueOrValidationFactory: ValueOrValidationFactory
    ): ValueOrValidation<ItemType> =
        valueOrValidationFactory.valueOrValidation(this.game.registry.getRegistryEntry(input.input))

    override fun possibilities(): List<Input> =
        this.game.registry.getAll(classOf<ItemType>()).map { it.id }.map(::SingleInput)
}

internal class BlockTypeArgumentType(val game: Game) :
    ArgumentTypeHelper<SingleInput, BlockType>() {

    override fun parse(
        input: SingleInput,
        valueOrValidationFactory: ValueOrValidationFactory
    ): ValueOrValidation<BlockType> =
        valueOrValidationFactory.valueOrValidation(this.game.registry.getRegistryEntry(input.input))

    override fun possibilities(): List<Input> =
        this.game.registry.getAll(classOf<BlockType>()).map { it.id }.map(::SingleInput)
}
