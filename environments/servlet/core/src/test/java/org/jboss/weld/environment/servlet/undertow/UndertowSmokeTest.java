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
package org.jboss.weld.environment.servlet.undertow;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;

import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;

import junit.framework.Assert;

import org.jboss.weld.environment.servlet.Listener;
import org.junit.Test;

/**
 * Smoke test for Undertow integration. More sophisticated tests should be added to a separate undertow test project.
 *
 * @author Jozef Hartinger
 *
 */
public class UndertowSmokeTest {

    @Test
    public void testUndertow() throws ServletException, InterruptedException {
        DeploymentInfo servletBuilder = Servlets.deployment().setClassLoader(UndertowSmokeTest.class.getClassLoader())
                .setResourceManager(new ClassPathResourceManager(UndertowSmokeTest.class.getClassLoader())).setContextPath("/").setDeploymentName("test.war")
                .addServlet(Servlets.servlet("hello", InjectedServlet.class).addMapping("/*").setLoadOnStartup(1))

                .addListener(Servlets.listener(Listener.class));

        DeploymentManager manager = Servlets.defaultContainer().addDeployment(servletBuilder);
        manager.deploy();

        HttpHandler servletHandler = manager.start();
        PathHandler path = Handlers.path(Handlers.redirect("/")).addPrefixPath("/", servletHandler);
        Undertow server = Undertow.builder().addHttpListener(8080, "localhost").setHandler(path).build();
        server.start();

        try {
            Assert.assertTrue(InjectedServlet.SYNC.await(5, TimeUnit.SECONDS));
        } finally {
            server.stop();
        }
    }
}