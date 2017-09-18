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

import com.github.projectsandstone.api.Platform
import com.github.projectsandstone.api.platform.PlatformObjectConverter
import com.github.projectsandstone.api.util.version.Version
import com.github.projectsandstone.common.util.CommonVersionScheme

class TestPlatform : Platform {
    override val minecraftVersion: Version = Version("1.11.2", CommonVersionScheme)

    override val platformFullName: String = "Test Environment"

    override val platformBaseName: String = "testenv"

    override val platformName: String = "testenv"

    override val platformVersion: Version = Version("BETA", CommonVersionScheme)

    override fun isInternalClass(name: String?): Boolean {
        return false
    }

    override val platformObjectConverter: PlatformObjectConverter
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
}