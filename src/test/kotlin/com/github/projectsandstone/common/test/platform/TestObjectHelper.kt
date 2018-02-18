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
package com.github.projectsandstone.common.test.platform

import com.github.jonathanxd.iutils.collection.view.ViewCollections
import com.github.jonathanxd.iutils.collection.view.ViewUtils
import com.github.jonathanxd.iutils.text.Text
import com.github.jonathanxd.kwcommands.printer.CommonPrinter
import com.github.jonathanxd.kwcommands.printer.Printer
import com.github.jonathanxd.kwcommands.util.KLocale
import com.github.projectsandstone.api.SandstoneObjectHelper
import com.github.projectsandstone.api.text.channel.MessageReceiver
import org.slf4j.Logger
import java.util.*
import java.util.function.Function

object TestObjectHelper : SandstoneObjectHelper {
    private val loggerPrinter: MutableMap<Logger, Printer> = WeakHashMap()
    private val messageReceiverPrinter: MutableMap<MessageReceiver, Printer> = WeakHashMap()

    override fun <U, T> createLiveCollection(from: Collection<U>, mapper: (U) -> T): Collection<T> =
        object : Collection<T> {
            override val size: Int
                get() = from.size

            override fun contains(element: T): Boolean =
                from.any { mapper(it) == element }

            override fun containsAll(elements: Collection<T>): Boolean =
                elements.all { this.contains(it) }

            override fun isEmpty(): Boolean = from.isEmpty()

            override fun iterator(): Iterator<T> = object : Iterator<T> {

                val iter = from.iterator()

                override fun next(): T = mapper(iter.next())
                override fun hasNext(): Boolean = iter.hasNext()
            }
        }

    override fun <U, T> createLiveList(from: List<U>, mapper: (U) -> T): List<T> =
        ViewCollections.listMapped(
            from,
            Function { mapper(it) },
            Function { throw UnsupportedOperationException("Unmodifiable") },
            ViewUtils.unmodifiable(),
            ViewUtils.unmodifiable()
        )

    override fun <U, T> createLiveSet(from: Set<U>, mapper: (U) -> T): Set<T> =
        ViewCollections.setMapped(
            from,
            Function { mapper(it) },
            ViewUtils.unmodifiable(),
            ViewUtils.unmodifiable()
        )


    override fun createPrinter(logger: Logger): Printer =
        this.loggerPrinter.computeIfAbsent(logger) {
            CommonPrinter(KLocale.localizer, { logger.info(it) }, false)
        }

    override fun createPrinter(receiver: MessageReceiver): Printer =
        this.messageReceiverPrinter.computeIfAbsent(receiver) {
            CommonPrinter(KLocale.localizer, { receiver.sendMessage(Text.of(it)) }, false)
        }
}