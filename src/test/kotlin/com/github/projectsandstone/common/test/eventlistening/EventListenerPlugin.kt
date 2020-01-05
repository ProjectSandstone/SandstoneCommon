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
package com.github.projectsandstone.common.test.eventlistening

import com.github.jonathanxd.iutils.text.Text
import com.github.projectsandstone.api.Sandstone
import com.github.projectsandstone.api.entity.EntityType
import com.github.projectsandstone.api.entity.EntityTypes
import com.github.projectsandstone.api.entity.living.player.Player
import com.github.projectsandstone.api.event.init.PostInitializationEvent
import com.github.projectsandstone.api.event.message.MessageEvent
import com.github.projectsandstone.api.inventory.CarriedInventory
import com.github.projectsandstone.api.inventory.Carrier
import com.github.projectsandstone.api.plugin.Plugin
import com.github.projectsandstone.api.util.SID
import com.github.projectsandstone.api.world.Location
import com.github.projectsandstone.api.world.World
import com.github.koresframework.eventsys.event.annotation.Listener
import com.github.koresframework.eventsys.event.annotation.Name
import org.slf4j.Logger
import java.util.*
import javax.inject.Inject

@Plugin(id = "com.github.projectsandstone.common.test.eventlistenerplugin", name = "Event Listening Test Plugin", version = "1.0.0")
class EventListenerPlugin @Inject constructor(val logger: Logger) {

    @Listener
    fun listen(postInit: PostInitializationEvent) {

        Sandstone.eventManager.dispatch(Sandstone.eventFactory.createMessageEvent(Text.of("Test")), this)
        Sandstone.eventManager.dispatch(Sandstone.eventFactory.createMessageEvent(Text.of("Test"), TestPlayer()), this)

    }

    @Listener
    fun message(message: MessageEvent, @Name("player") player: Player?) {
        logger.info("Message: ${message.message}. From: ${player?.name ?: "NULL"}")

        logger.info("Stack:")
        Thread.dumpStack()
    }

}

class TestPlayer : Player {
    override val type: EntityType
        get() = EntityTypes.PLAYER

    override val inventory: CarriedInventory<Carrier>
        get() = TODO("not implemented")

    override val sandstoneId: SID
        get() = SID.UuidSid(UUID.randomUUID())

    override val location: Location<World>
        get() = TODO("not implemented")

    override val name: String
        get() = "XZ"

    override fun sendMessage(text: Text) {

    }

    override fun teleport(location: Location<*>) {

    }

    override val isOnline: Boolean
        get() = true

    override fun kick() {
    }

    override val player: Player?
        get() = this

    override fun kick(reason: Text) {

    }

}