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
package com.github.projectsandstone.common.util

import com.github.projectsandstone.api.plugin.PluginContainer
import com.github.projectsandstone.common.plugin.SandstoneDependencyResolver
import java.util.*

/**
 * Created by jonathan on 15/08/16.
 */
class DependencyComparator(val sandstoneDependencyResolver: SandstoneDependencyResolver) : Comparator<PluginContainer> {

    override fun compare(o1: PluginContainer?, o2: PluginContainer?): Int {
        if (o1 == null)
            return 1

        if (o2 == null)
            return -1

        // o1 hasDirectOrIndirectDependency of o2
        if (sandstoneDependencyResolver.hasDirectOrIndirectDependency(o1, o2)) {
            return 1
        }

        return -1
    }
}