/*
 * Copyright (C) 2015 The Minium Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package minium.internal;

import java.lang.reflect.Method;

import minium.Elements;
import minium.FindElements;
import platypus.MixinClass;
import platypus.MixinClasses;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

public class Locator<T extends Elements> {

    private MixinClass<T> mixinClass;
    private transient Supplier<? extends T> rootSupplier;

    public Locator(T root, Class<T> intf, Class<?> ... others) {
        this(intf, others);
        setRoot(root);
    }

    public Locator(Class<T> intf, Class<?> ... others) {
        Preconditions.checkArgument(intf.isInterface(), "class %s is not an interface", intf);
        mixinClass = MixinClasses.builder(intf).addInterfaces(others).addInterfaces(InternalLocator.class).build();
    }

    @SuppressWarnings("unchecked")
    public Locator(T root) {
        this((Class<T>) root.getClass());
        setRoot(root);
    }

    public void setRoot(Supplier<? extends T> supplier) {
        this.rootSupplier = supplier;
    }

    public void setRoot(T root) {
        setRoot(Suppliers.ofInstance(root));
    }

    public T getRoot() {
        return rootSupplier == null ? null : rootSupplier.get();
    }

    public MixinClass<T> getMixinClass() {
        return mixinClass;
    }

    public void release() {
        rootSupplier = null;
    }

    public T root() {
        return InternalLocator.MethodInvocationImpl.createInternalLocator(this, null);
    }

    @SuppressWarnings("unchecked")
    public T selector(String selector) {
        return (T) root().as(FindElements.class).find(selector);
    }

    protected T createFinder(Method method, Object ... args) {
        return InternalLocator.MethodInvocationImpl.createInternalLocator(this, null, method, args);
    }

    @Override
    public String toString() {
        return "by";
    }
}
