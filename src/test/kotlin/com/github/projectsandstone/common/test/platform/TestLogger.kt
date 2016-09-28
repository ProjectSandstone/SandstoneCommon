/**
 *      SandstoneCommon - Common implementation of SandstoneAPI
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) 2016 Sandstone <https://github.com/ProjectSandstone/>
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

import com.github.projectsandstone.api.logging.LogLevel
import com.github.projectsandstone.api.logging.Logger

class TestLogger(val name: String?) : Logger {

    override fun log(level: LogLevel, message: String) {
        val printer = getPrinter(level)

        if (name != null) {
            printer.println("[$name] $message")
        } else {
            printer.println(message)
        }
    }

    override fun log(level: LogLevel, exception: Exception) {
        this.log(level, "Exception:")
        exception.printStackTrace(getPrinter(level))
    }

    override fun log(level: LogLevel, exception: Exception, message: String) {
        this.log(level, "Exception:")

        this.log(level, message)
        exception.printStackTrace(getPrinter(level))
    }

    override fun log(level: LogLevel, exception: Exception, format: String, vararg objects: Any) {
        this.log(level, "Exception:")

        this.log(level, String.format(format, *objects))
        exception.printStackTrace(getPrinter(level))
    }

    override fun log(level: LogLevel, format: String, vararg objects: Any) {
        this.log(level, String.format(format, *objects))
    }

    companion object {
        private fun getPrinter(level: LogLevel) = if (level != LogLevel.ERROR && level != LogLevel.EXCEPTION) System.out else System.err
    }

}