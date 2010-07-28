/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.beanDeployment.session.multiple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.spi.Bean;
import javax.inject.Inject;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.manager.BeanManagerImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class BootstrapTest
{
   @Deployment
   public static Archive<?> deploy() 
   {
      return ShrinkWrap.create(EnterpriseArchive.class, "test.ear")
         .addModule(
               ShrinkWrap.create(JavaArchive.class)
                  .addPackage(BootstrapTest.class.getPackage())
                  .addManifestResource(EmptyAsset.INSTANCE, "beans.xml")
         );
   }
   
   @Inject
   private BeanManagerImpl beanManager;
   
   @Test
   public void testMultipleEnterpriseBean()
   {
      List<Bean<?>> beans = beanManager.getBeans();
      Map<Class<?>, Bean<?>> classes = new HashMap<Class<?>, Bean<?>>();
      for (Bean<?> bean : beans)
      {
         if (bean instanceof RIBean)
         {
            classes.put(((RIBean<?>) bean).getType(), bean);
         }
      }
      Assert.assertTrue(classes.containsKey(Hound.class));
      Assert.assertTrue(classes.containsKey(Elephant.class));
      Assert.assertTrue(classes.containsKey(Panther.class));
      Assert.assertTrue(classes.containsKey(Tiger.class));
      
      Assert.assertTrue(classes.get(Hound.class) instanceof SessionBean);
      Assert.assertTrue(classes.get(Elephant.class) instanceof SessionBean);
      Assert.assertTrue(classes.get(Panther.class) instanceof SessionBean);
      Assert.assertTrue(classes.get(Tiger.class) instanceof SessionBean);
   }
   
}
