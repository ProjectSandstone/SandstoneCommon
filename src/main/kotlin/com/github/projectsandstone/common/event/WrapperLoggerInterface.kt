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

import com.github.projectsandstone.eventsys.logging.Level
import com.github.projectsandstone.eventsys.logging.LoggerInterface
import com.github.projectsandstone.eventsys.logging.MessageType
import org.slf4j.Logger


class WrapperLoggerInterface(val logger: Logger) : LoggerInterface {

    override fun log(message: String, messageType: MessageType) {
        log(messageType.level, message)

        if (messageType.level == Level.FATAL)
            throw FatalException()
    }

    override fun log(message: String, messageType: MessageType, throwable: Throwable) {
        log(messageType.level, message, throwable)

        if (messageType.level == Level.FATAL)
            throw FatalException()
    }

    override fun log(messages: List<String>, messageType: MessageType) {
        messages.forEach { message ->
            log(messageType.level, message)
        }

        if (messageType.level == Level.FATAL)
            throw FatalException()
    }

    override fun log(messages: List<String>, messageType: MessageType, throwable: Throwable) {
        messages.forEach { message ->
            log(messageType.level, message, throwable)
        }

        if (messageType.level == Level.FATAL)
            throw FatalException()
    }

    private fun log(level: Level, message: String, throwable: Throwable) = when(level) {
        Level.WARN -> this.logger.warn(message, throwable)
        Level.ERROR,
        Level.FATAL -> this.logger.error(message, throwable)
        Level.DEBUG -> this.logger.debug(message, throwable)
        Level.TRACE -> this.logger.trace(message, throwable)
        Level.INFO -> this.logger.info(message, throwable)
    }

    private fun log(level: Level, message: String) = when(level) {
        Level.WARN -> this.logger.warn(message)
        Level.ERROR,
        Level.FATAL -> this.logger.error(message)
        Level.DEBUG -> this.logger.debug(message)
        Level.TRACE -> this.logger.trace(message)
        Level.INFO -> this.logger.info(message)
    }

    class FatalException : Exception()
}

