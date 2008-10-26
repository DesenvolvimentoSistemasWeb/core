package org.jboss.webbeans;

import java.lang.annotation.Annotation;
import java.util.HashMap;

import javax.webbeans.ContextNotActiveException;
import javax.webbeans.manager.Bean;
import javax.webbeans.manager.Context;
import javax.webbeans.manager.Manager;

import org.jboss.webbeans.util.MapWrapper;

/**
 * Basic implementation of javax.webbeans.Context, backed by a HashMap
 * 
 * @author Shane Bryzak
 * @author Nicklas Karlsson (nickarls@gmail.com)
 * @author Pete Muir
 * 
 */
public class BasicContext implements Context
{
   
   private class BeanMap extends MapWrapper<Bean<? extends Object>, Object>
   {

      public BeanMap()
      {
         super(new HashMap<Bean<? extends Object>, Object>());
      }
      
      @SuppressWarnings("unchecked")
      public <T extends Object> T get(Bean<? extends T> key)
      {
         return (T) super.get(key);
      }

   }
   
   private BeanMap beans;
   private Class<? extends Annotation> scopeType;
   private boolean active;

   public BasicContext(Class<? extends Annotation> scopeType)
   {
      this.scopeType = scopeType;
      beans = new BeanMap();
      active = true;
   }

   public <T> T get(Bean<T> bean, boolean create)
   {
      if (!active)
      {
         throw new ContextNotActiveException();
      }
      
      if (beans == null)
      {
         // Context has been destroyed
         return null;
      }
      
      T instance = beans.get(bean);
      
      if (instance != null)
      {
         return instance;
      }

      if (!create)
      {
         return null;
      }

      // TODO should bean creation be synchronized?

      instance = bean.create();

      beans.put(bean, instance);
      return instance;
   }

   public Class<? extends Annotation> getScopeType()
   {
      return scopeType;
   }

   private <T> void destroy(Manager manager, Bean<T> bean)
   {
      bean.destroy(beans.get(bean));
   }

   public void destroy(Manager manager)
   {
      for (Bean<? extends Object> bean : beans.keySet())
      {
         destroy(manager, bean);
      }
      beans = null;
   }

   public boolean isActive()
   {
      return active;
   }

   public void setActive(boolean active)
   {
      this.active = active;
   }

}
