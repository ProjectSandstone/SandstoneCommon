/**
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
package com.github.projectsandstone.common.scheduler

import com.github.projectsandstone.api.scheduler.SandstoneExecutorService
import com.github.projectsandstone.api.scheduler.Scheduler
import com.github.projectsandstone.api.scheduler.Task
import java.time.Duration

abstract class SandstoneScheduler : Scheduler {
    override fun createAsyncExecutor(plugin: Any): SandstoneExecutorService {
        return com.github.projectsandstone.common.scheduler.SandstoneExecutorService(this, plugin, isAsync = true)
    }

    override fun createSyncExecutor(plugin: Any): SandstoneExecutorService {
        return com.github.projectsandstone.common.scheduler.SandstoneExecutorService(this, plugin, isAsync = true)
    }

    override fun createTask(plugin: Any, name: String?, delay: Duration, interval: Duration, isAsync: Boolean, runnable: Runnable): Task {
        return SandstoneTask(plugin, name, delay, interval, isAsync, runnable)
    }
}