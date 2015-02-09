/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.tck;

import static org.jboss.weld.config.ConfigurationKey.PROXY_UNSAFE;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.cdi.tck.impl.testng.SingleTestClassMethodInterceptor;
import org.jboss.cdi.tck.tests.implementation.simple.lifecycle.unproxyable.UnproxyableManagedBeanTest;
import org.jboss.cdi.tck.tests.lookup.clientProxy.unproxyable.beanConstructor.BeanConstructorWithParametersTest;
import org.jboss.cdi.tck.tests.lookup.clientProxy.unproxyable.privateConstructor.PrivateConstructorTest;
import org.jboss.weld.config.ConfigurationKey;
import org.testng.IMethodInstance;
import org.testng.ITestContext;

import com.google.common.collect.ImmutableSet;

/**
 * If unsafe proxies are enabled, this interceptor disables a set of TCK tests that are known to fail with unsafe proxies (because with unsafe proxies Weld is
 * less strict than required).
 *
 * @author Jozef Hartinger
 *
 */
public class WeldMethodInterceptor extends SingleTestClassMethodInterceptor {

    private static final String ADDITIONAL_VM_ARGS_PROPERTY = "additional.vm.args";
    private static final Set<String> UNSAFE_PROXY_EXCLUDED_CLASSES;
    private static final Logger LOG = Logger.getLogger(WeldMethodInterceptor.class.getName());

    static {
        ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        builder.add(UnproxyableManagedBeanTest.class.getName());
        builder.add(BeanConstructorWithParametersTest.class.getName());
        builder.add(PrivateConstructorTest.class.getName());
        UNSAFE_PROXY_EXCLUDED_CLASSES = builder.build();
    }

    private boolean isUnsafeProxyModeEnabled() {
        String additional = System.getProperty(ADDITIONAL_VM_ARGS_PROPERTY, "");
        if (additional.contains(PROXY_UNSAFE.get()) && !additional.contains(PROXY_UNSAFE.get() + '=' + false)) {
            return true;
        }
        return Boolean.valueOf(System.getProperty(ConfigurationKey.PROXY_UNSAFE.get(), "false"));
    }

    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        if (isUnsafeProxyModeEnabled()) {
            // exclude certain tests
            LOG.log(Level.INFO, "Unsafe proxy mode enabled");
            methods = new ArrayList<IMethodInstance>(methods);
            for (Iterator<IMethodInstance> iterator = methods.iterator(); iterator.hasNext();) {
                if (UNSAFE_PROXY_EXCLUDED_CLASSES.contains(iterator.next().getMethod().getRealClass().getName())) {
                    iterator.remove();
                }
            }
        }
        return super.intercept(methods, context);
    }
}