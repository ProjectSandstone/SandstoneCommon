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
package com.github.projectsandstone.common;

import com.github.jonathanxd.iutils.object.TypeInfo;
import com.github.projectsandstone.api.Game;
import com.github.projectsandstone.api.event.Event;
import com.github.projectsandstone.api.event.EventListener;
import com.github.projectsandstone.api.event.EventManager;
import com.github.projectsandstone.api.plugin.DependencyResolver;
import com.github.projectsandstone.api.plugin.PluginContainer;
import com.github.projectsandstone.api.plugin.PluginLoader;
import com.github.projectsandstone.api.plugin.PluginManager;
import com.github.projectsandstone.api.service.RegisteredProvider;
import com.github.projectsandstone.api.service.ServiceManager;
import com.github.projectsandstone.api.util.exception.CircularDependencyException;
import com.github.projectsandstone.api.util.exception.DependencyException;
import com.github.projectsandstone.api.util.exception.MissingDependencyException;
import com.github.projectsandstone.api.util.exception.PluginLoadException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function3;

/**
 * Created by jonathan on 15/08/16.
 */
public class GameImpl {/*implements Game {

    private PluginManager pluginManager;

    @NotNull
    @Override
    public PluginManager getPluginManager() {
        return pluginManager = new PluginManager() {
            @NotNull
            @Override
            public DependencyResolver getDependencyResolver() {
                return new DependencyResolver() {
                    @NotNull
                    @Override
                    public PluginManager getPluginManager() {
                        return pluginManager;
                    }

                    @NotNull
                    @Override
                    public Set<PluginContainer> createDependencySet() {
                        return Collections.emptySet();
                    }

                    @Override
                    public void checkDependencies(PluginContainer pluginContainer) throws DependencyException {

                    }
                };
            }

            @NotNull
            @Override
            public PluginLoader getPluginLoader() {
                return new PluginLoader() {
                    @NotNull
                    @Override
                    public PluginManager getPluginManager() {
                        return pluginManager;
                    }

                    @NotNull
                    @Override
                    public List<PluginContainer> loadFile(Path path) throws PluginLoadException {
                        return Collections.emptyList();
                    }

                    @Override
                    public void load(PluginContainer pluginContainer) {

                    }
                };
            }

            @Override
            public boolean loadPlugin(PluginContainer pluginContainer) throws MissingDependencyException, CircularDependencyException {
                return false;
            }

            @Nullable
            @Override
            public PluginContainer loadPlugin(Path path) throws SecurityException, IOException, OutOfMemoryError, MissingDependencyException, CircularDependencyException {
                return null;
            }

            @NotNull
            @Override
            public List<PluginContainer> loadPlugins(Path path) throws SecurityException, IOException, NotDirectoryException {
                return Collections.emptyList();
            }

            @Nullable
            @Override
            public PluginContainer getPlugin(String s) {
                return null;
            }

            @Nullable
            @Override
            public PluginContainer getPlugin(Object o) {
                return null;
            }

            @NotNull
            @Override
            public PluginContainer getRequirePlugin(Object o) {
                throw new UnsupportedOperationException();
            }

            @Nullable
            @Override
            public PluginContainer getPlugin(Function1<? super PluginContainer, Boolean> function1) {
                return null;
            }

            @NotNull
            @Override
            public List<PluginContainer> getPlugins(Function1<? super PluginContainer, Boolean> function1) {
                return Collections.emptyList();
            }

            @NotNull
            @Override
            public List<PluginContainer> getPlugins() {
                return Collections.emptyList();
            }
        };
    }

    @NotNull
    @Override
    public ServiceManager getServiceManager() {
        return new ServiceManager() {
            @Override
            public <T> void setProvider(Object o, Class<T> aClass, T t) {

            }

            @Nullable
            @Override
            public <T> T provide(Class<T> aClass) {
                return null;
            }

            @Override
            public <T> void watch(Function1<? super RegisteredProvider<T>, Boolean> function1) {

            }

            @Override
            public <T> void watch(Function3<Object, ? super Class<T>, ? super T, Boolean> function3, Function1<? super RegisteredProvider<T>, Boolean> function1) {

            }

            @Nullable
            @Override
            public <T> RegisteredProvider<T> getRegisteredProvider(Class<T> aClass) {
                return null;
            }
        };
    }

    @NotNull
    @Override
    public EventManager getEventManager() {
        return new EventManager() {
            @Override
            public <T extends Event> void registerListener(Object o, Class<T> aClass, EventListener<? super T> eventListener) {

            }

            @Override
            public <T extends Event> void registerListener(Object o, TypeInfo<T> typeInfo, EventListener<? super T> eventListener) {

            }

            @Override
            public void registerListeners(Object o, Object o1) {

            }

            @Override
            public <T extends Event> void dispatch(T t, PluginContainer pluginContainer) throws Throwable {

            }

            @NotNull
            @Override
            public <T extends Event> Set<EventListener<T>> getListeners(TypeInfo<T> typeInfo) {
                return Collections.emptySet();
            }

            @NotNull
            @Override
            public Set<EventListener<?>> getListeners() {
                return Collections.emptySet();
            }
        };
    }

    @NotNull
    @Override
    public Path getGamePath() {
        return Paths.get("A");
    }

    @NotNull
    @Override
    public Path getSavePath() {
        return Paths.get("A");
    }*/
}
