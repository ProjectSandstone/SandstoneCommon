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

import com.github.jonathanxd.iutils.type.TypeInfo
import com.github.jonathanxd.kwcommands.information.Information
import com.github.jonathanxd.kwcommands.information.InformationProvider
import com.github.jonathanxd.kwcommands.manager.InformationManager
import com.github.projectsandstone.api.service.permission.Subject

class SubjectProvider(val manager: InformationManager) : InformationProvider {
    @Suppress("UNCHECKED_CAST")
    override fun <T> provide(id: Information.Id, type: TypeInfo<T>): Information<T>? =
            if (id.id == Subject::class.java && "subject" in id.tags)
                manager.informationSet.firstOrNull {
                    it.value is Subject
                } as Information<T>?
            else null

}