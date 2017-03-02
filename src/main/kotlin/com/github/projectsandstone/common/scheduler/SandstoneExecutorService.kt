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
import com.github.projectsandstone.api.scheduler.ScheduledFutureTask
import com.github.projectsandstone.api.scheduler.Scheduler
import java.time.Duration
import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class SandstoneExecutorService(val scheduler: Scheduler, val plugin: Any, val isAsync: Boolean) : AbstractExecutorService(), SandstoneExecutorService {

    override fun schedule(command: Runnable, delay: Long, unit: TimeUnit): ScheduledFutureTask<*> {
        return this.schedule(Executors.callable(command), delay, unit)
    }

    override fun <V> schedule(callable: Callable<V>, delay: Long, unit: TimeUnit): ScheduledFutureTask<V> {

        val delayMillis = unit.toMillis(delay)

        val callableTask: CallableTask<V> = CallableTask(callable)

        val task = scheduler.createTask(
                plugin = plugin,
                delay = Duration.ofMillis(delayMillis),
                isAsync = isAsync,
                runnable = callableTask)

        val submitted = scheduler.submit(task)

        return ScheduledFutureTaskImpl(task, submitted, callableTask)
    }

    override fun isTerminated(): Boolean = false

    override fun execute(command: Runnable) {
        scheduler.submit(scheduler.createTask(plugin = plugin, delay = Duration.ZERO, isAsync = false, runnable = command))
    }

    override fun shutdown() {

    }

    override fun shutdownNow(): MutableList<Runnable> = mutableListOf()

    override fun isShutdown(): Boolean = false

    override fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean = false

    override fun scheduleAtFixedRate(command: Runnable, initialDelay: Long, period: Long, unit: TimeUnit): ScheduledFutureTask<*> {
        val delay = unit.toMillis(initialDelay)
        val periodMillis = unit.toMillis(period)

        val callableTask: CallableTask<*> = CallableTask(Executors.callable(command))

        val task = scheduler.createTask(
                plugin = plugin,
                delay = Duration.ofMillis(delay),
                interval = Duration.ofMillis(periodMillis),
                isAsync = isAsync,
                runnable = callableTask)

        val submitted = scheduler.submit(task)

        return ScheduledFutureTaskImpl(task, submitted, callableTask)
    }

    override fun scheduleWithFixedDelay(command: Runnable, initialDelay: Long, delay: Long, unit: TimeUnit): ScheduledFutureTask<*> {
        return this.scheduleAtFixedRate(command, initialDelay, delay, unit)
    }

}