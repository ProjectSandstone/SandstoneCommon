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
package com.github.projectsandstone.common.event

import com.github.jonathanxd.iutils.kt.typedKeyOf
import com.github.koresframework.eventsys.context.EnvironmentContext
import com.github.koresframework.eventsys.logging.Level
import com.github.koresframework.eventsys.logging.LoggerInterface
import com.github.koresframework.eventsys.logging.MessageType
import org.slf4j.Logger

val loggerKey = typedKeyOf<Logger>("logger")

class WrapperLoggerInterface(val logger: Logger) : LoggerInterface {

    override fun log(message: String,
                     messageType: MessageType,
                     ctx: EnvironmentContext) {
        log(messageType.level, message, ctx)

        if (messageType.level == Level.FATAL)
            throw FatalException()
    }

    override fun log(message: String,
                     messageType: MessageType,
                     throwable: Throwable,
                     ctx: EnvironmentContext) {
        log(messageType.level, message, throwable, ctx)

        if (messageType.level == Level.FATAL)
            throw FatalException()
    }

    override fun log(messages: List<String>,
                     messageType: MessageType,
                     ctx: EnvironmentContext) {
        messages.forEach { message ->
            log(messageType.level, message, ctx)
        }

        if (messageType.level == Level.FATAL)
            throw FatalException()
    }

    override fun log(messages: List<String>,
                     messageType: MessageType,
                     throwable: Throwable,
                     ctx: EnvironmentContext) {
        messages.forEach { message ->
            log(messageType.level, message, throwable, ctx)
        }

        if (messageType.level == Level.FATAL)
            throw FatalException()
    }

    private fun log(level: Level,
                    message: String,
                    throwable: Throwable,
                    ctx: EnvironmentContext) = when (level) {
        Level.WARN -> this.logger(ctx).warn(message, throwable)
        Level.ERROR,
        Level.FATAL -> this.logger(ctx).error(message, throwable)
        Level.DEBUG -> this.logger(ctx).debug(message, throwable)
        Level.TRACE -> this.logger(ctx).trace(message, throwable)
        Level.INFO -> this.logger(ctx).info(message, throwable)
    }

    private fun log(level: Level,
                    message: String,
                    ctx: EnvironmentContext) = when (level) {
        Level.WARN -> this.logger(ctx).warn(message)
        Level.ERROR,
        Level.FATAL -> this.logger(ctx).error(message)
        Level.DEBUG -> this.logger(ctx).debug(message)
        Level.TRACE -> this.logger(ctx).trace(message)
        Level.INFO -> this.logger(ctx).info(message)
    }

    private fun logger(ctx: EnvironmentContext) =
            loggerKey.getOrElse(ctx, this.logger)

    class FatalException : Exception()
}

