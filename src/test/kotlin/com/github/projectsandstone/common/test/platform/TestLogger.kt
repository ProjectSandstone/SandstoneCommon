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

import com.github.projectsandstone.eventsys.logging.Level
import org.slf4j.Logger
import org.slf4j.Marker
import org.slf4j.helpers.MessageFormatter

class TestLogger(val name_: String?) : Logger {
    override fun getName(): String = this.name_ ?: ""

    override fun warn(msg: String?) {
        msg?.let { this.logInternal(Level.WARN, it) }
    }

    override fun warn(format: String?, arg: Any?) {
        format?.let { this.logInternal(Level.WARN, it, arg) }
    }

    override fun warn(format: String?, vararg arguments: Any?) {
        format?.let { this.logInternal(Level.WARN, it, *arguments) }
    }

    override fun warn(format: String?, arg1: Any?, arg2: Any?) {
        format?.let { this.logInternal(Level.WARN, it, arg1, arg2) }
    }

    override fun warn(msg: String?, t: Throwable) {
        msg?.let { this.logInternalException(Level.WARN, it, t) }
    }

    override fun warn(marker: Marker?, msg: String?) {
        msg?.let { this.logInternal(Level.WARN, it) }
    }

    override fun warn(marker: Marker?, format: String?, arg: Any?) {
        format?.let { this.logInternal(Level.WARN, it, arg) }
    }

    override fun warn(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        format?.let { this.logInternal(Level.WARN, it, arg1, arg2) }
    }

    override fun warn(marker: Marker?, format: String?, vararg arguments: Any?) {
        format?.let { this.logInternal(Level.WARN, it, *arguments) }
    }

    override fun warn(marker: Marker?, msg: String?, t: Throwable) {
        msg?.let { this.logInternalException(Level.WARN, it, t) }
    }

    // Info

    override fun info(msg: String?) {
        msg?.let { this.logInternal(Level.INFO, it) }
    }

    override fun info(format: String?, arg: Any?) {
        format?.let { this.logInternal(Level.INFO, it, arg) }
    }

    override fun info(format: String?, vararg arguments: Any?) {
        format?.let { this.logInternal(Level.INFO, it, *arguments) }
    }

    override fun info(format: String?, arg1: Any?, arg2: Any?) {
        format?.let { this.logInternal(Level.INFO, it, arg1, arg2) }
    }

    override fun info(msg: String?, t: Throwable) {
        msg?.let { this.logInternalException(Level.INFO, it, t) }
    }

    override fun info(marker: Marker?, msg: String?) {
        msg?.let { this.logInternal(Level.INFO, it) }
    }

    override fun info(marker: Marker?, format: String?, arg: Any?) {
        format?.let { this.logInternal(Level.INFO, it, arg) }
    }

    override fun info(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        format?.let { this.logInternal(Level.INFO, it, arg1, arg2) }
    }

    override fun info(marker: Marker?, format: String?, vararg arguments: Any?) {
        format?.let { this.logInternal(Level.INFO, it, *arguments) }
    }

    override fun info(marker: Marker?, msg: String?, t: Throwable) {
        msg?.let { this.logInternalException(Level.INFO, it, t) }
    }

    override fun isErrorEnabled(): Boolean = true

    override fun isErrorEnabled(marker: Marker?): Boolean = true

    // Error

    override fun error(msg: String?) {
        msg?.let { this.logInternal(Level.ERROR, it) }
    }

    override fun error(format: String?, arg: Any?) {
        format?.let { this.logInternal(Level.ERROR, it, arg) }
    }

    override fun error(format: String?, vararg arguments: Any?) {
        format?.let { this.logInternal(Level.ERROR, it, *arguments) }
    }

    override fun error(format: String?, arg1: Any?, arg2: Any?) {
        format?.let { this.logInternal(Level.ERROR, it, arg1, arg2) }
    }

    override fun error(msg: String?, t: Throwable) {
        msg?.let { this.logInternalException(Level.ERROR, it, t) }
    }

    override fun error(marker: Marker?, msg: String?) {
        msg?.let { this.logInternal(Level.ERROR, it) }
    }

    override fun error(marker: Marker?, format: String?, arg: Any?) {
        format?.let { this.logInternal(Level.ERROR, it, arg) }
    }

    override fun error(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        format?.let { this.logInternal(Level.ERROR, it, arg1, arg2) }
    }

    override fun error(marker: Marker?, format: String?, vararg arguments: Any?) {
        format?.let { this.logInternal(Level.ERROR, it, *arguments) }
    }

    override fun error(marker: Marker?, msg: String?, t: Throwable) {
        msg?.let { this.logInternalException(Level.ERROR, it, t) }
    }

    override fun isDebugEnabled(): Boolean = true

    override fun isDebugEnabled(marker: Marker?): Boolean = true

    // Debug

    override fun debug(msg: String?) {
        msg?.let { this.logInternal(Level.DEBUG, it) }
    }

    override fun debug(format: String?, arg: Any?) {
        format?.let { this.logInternal(Level.DEBUG, it, arg) }
    }

    override fun debug(format: String?, vararg arguments: Any?) {
        format?.let { this.logInternal(Level.DEBUG, it, *arguments) }
    }

    override fun debug(format: String?, arg1: Any?, arg2: Any?) {
        format?.let { this.logInternal(Level.DEBUG, it, arg1, arg2) }
    }

    override fun debug(msg: String?, t: Throwable) {
        msg?.let { this.logInternalException(Level.DEBUG, it, t) }
    }

    override fun debug(marker: Marker?, msg: String?) {
        msg?.let { this.logInternal(Level.DEBUG, it) }
    }

    override fun debug(marker: Marker?, format: String?, arg: Any?) {
        format?.let { this.logInternal(Level.DEBUG, it, arg) }
    }

    override fun debug(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        format?.let { this.logInternal(Level.DEBUG, it, arg1, arg2) }
    }

    override fun debug(marker: Marker?, format: String?, vararg arguments: Any?) {
        format?.let { this.logInternal(Level.DEBUG, it, *arguments) }
    }

    override fun debug(marker: Marker?, msg: String?, t: Throwable) {
        msg?.let { this.logInternalException(Level.DEBUG, it, t) }
    }

    override fun isInfoEnabled(): Boolean = true

    override fun isInfoEnabled(marker: Marker?): Boolean = true

    override fun trace(msg: String?) {
        msg?.let { this.logInternal(Level.TRACE, it) }
    }

    override fun trace(format: String?, arg: Any?) {
        format?.let { this.logInternal(Level.TRACE, it, arg) }
    }

    override fun trace(format: String?, vararg arguments: Any?) {
        format?.let { this.logInternal(Level.TRACE, it, *arguments) }
    }

    override fun trace(format: String?, arg1: Any?, arg2: Any?) {
        format?.let { this.logInternal(Level.TRACE, it, arg1, arg2) }
    }

    override fun trace(msg: String?, t: Throwable) {
        msg?.let { this.logInternalException(Level.TRACE, it, t) }
    }

    override fun trace(marker: Marker?, msg: String?) {
        msg?.let { this.logInternal(Level.TRACE, it) }
    }

    override fun trace(marker: Marker?, format: String?, arg: Any?) {
        format?.let { this.logInternal(Level.TRACE, it, arg) }
    }

    override fun trace(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        format?.let { this.logInternal(Level.TRACE, it, arg1, arg2) }
    }

    override fun trace(marker: Marker?, format: String?, vararg arguments: Any?) {
        format?.let { this.logInternal(Level.TRACE, it, *arguments) }
    }

    override fun trace(marker: Marker?, msg: String?, t: Throwable) {
        msg?.let { this.logInternalException(Level.TRACE, it, t) }
    }

    override fun isWarnEnabled(): Boolean = true

    override fun isWarnEnabled(marker: Marker?): Boolean = true

    override fun isTraceEnabled(): Boolean = true

    override fun isTraceEnabled(marker: Marker?): Boolean = true

    private fun logInternal(level: Level, message: String, vararg args: Any?) {
        val printer = getPrinter(level)

        val nMessage =
                if(args.isEmpty()) message
        else MessageFormatter.arrayFormat(message, args).message

        if (this.name_ != null) {
            printer.println("SandstoneLogger: [$name] $nMessage")
        } else {
            printer.println("SandstoneLogger: $nMessage")
        }
    }

    private fun logInternalException(level: Level, message: String, exception: Throwable, vararg args: Any?) {
        this.logInternal(level, message, *args)
        this.logInternal(level, "Exception:")
        exception.printStackTrace(getPrinter(level))
    }

    companion object {
        private fun getPrinter(level: Level) =
                if (level != Level.ERROR && level != Level.FATAL) System.out
                else System.err
    }

}