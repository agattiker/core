/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.util.Set;

public class WrappedWBMethod<T> extends ForwardingWBMethod<T>
{
   
   private final WBMethod<T> delegate;
   private AnnotationStore annotationStore;
   
   public WrappedWBMethod(WBMethod<T> method, Set<Annotation> extraAnnotations)
   {
      this.delegate = method;
      this.annotationStore = AnnotationStore.wrap(method.getAnnotationStore(), extraAnnotations, extraAnnotations);
   }
   
   @Override
   protected WBMethod<T> delegate()
   {
      return delegate;
   }
   
   @Override
   public AnnotationStore getAnnotationStore()
   {
      return annotationStore;
   }
   
   /**
    * @see org.jboss.webbeans.introspector.WBAnnotated
    */
   public <A extends Annotation> A getAnnotation(Class<A> annotationType)
   {
      return getAnnotationStore().getAnnotation(annotationType);
   }

   /**
    * @see org.jboss.webbeans.introspector.WBAnnotated
    */
   public Set<Annotation> getAnnotations()
   {
      return getAnnotationStore().getAnnotations();
   }

   /**
    * @see org.jboss.webbeans.introspector.WBAnnotated
    */
   public Set<Annotation> getMetaAnnotations(Class<? extends Annotation> metaAnnotationType)
   {
      return getAnnotationStore().getMetaAnnotations(metaAnnotationType);
   }

   /**
    * @see org.jboss.webbeans.introspector.WBAnnotated
    */
   public Annotation[] getMetaAnnotationsAsArray(Class<? extends Annotation> metaAnnotationType)
   {
      return getAnnotationStore().getMetaAnnotationsAsArray(metaAnnotationType);
   }

   /**
    * @see org.jboss.webbeans.introspector.WBAnnotated
    */
   @Deprecated
   public Set<Annotation> getBindings()
   {
      return getAnnotationStore().getBindings();
   }

   /**
    * @see org.jboss.webbeans.introspector.WBAnnotated
    */
   @Deprecated
   public Annotation[] getBindingsAsArray()
   {
      return getAnnotationStore().getBindingsAsArray();
   }
   
   /**
    * @see org.jboss.webbeans.introspector.WBAnnotated
    */
   public boolean isAnnotationPresent(Class<? extends Annotation> annotationType)
   {
      return getAnnotationStore().isAnnotationPresent(annotationType);
   }
   
   public Set<Annotation> getDeclaredMetaAnnotations(Class<? extends Annotation> metaAnnotationType)
   {
      return getAnnotationStore().getDeclaredMetaAnnotations(metaAnnotationType);
   }
   
}