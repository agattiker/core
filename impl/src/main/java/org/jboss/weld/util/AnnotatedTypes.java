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
package org.jboss.weld.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;

/**
 * Class that can take an AnnotatedType and return a unique string
 * representation of that type
 * 
 * @author Stuart Douglas <stuart@baileyroberts.com.au>
 */
public class AnnotatedTypes
{

   /**
    * Does the first stage of comparing AnnoatedCallables, however it cannot
    * compare the method parameters
    */
   private static class AnnotatedCallableComparator<T> implements Comparator<AnnotatedCallable<? super T>>
   {

      public int compare(AnnotatedCallable<? super T> arg0, AnnotatedCallable<? super T> arg1)
      {
         // compare the names first
         int result = (arg0.getJavaMember().getName().compareTo(arg1.getJavaMember().getName()));
         if (result != 0)
         {
            return result;
         }
         result = arg0.getJavaMember().getDeclaringClass().getName().compareTo(arg1.getJavaMember().getDeclaringClass().getName());
         if (result != 0)
         {
            return result;
         }
         result = arg0.getParameters().size() - arg1.getParameters().size();
         return result;
      }

   }

   private static class AnnotatedMethodComparator<T> implements Comparator<AnnotatedMethod<? super T>>
   {

      public static <T> Comparator<AnnotatedMethod<? super T>> instance()
      {
         return new AnnotatedMethodComparator<T>();
      }

      private AnnotatedCallableComparator<T> callableComparator = new AnnotatedCallableComparator<T>();

      public int compare(AnnotatedMethod<? super T> arg0, AnnotatedMethod<? super T> arg1)
      {
         int result = callableComparator.compare(arg0, arg1);
         if (result != 0)
         {
            return result;
         }
         for (int i = 0; i < arg0.getJavaMember().getParameterTypes().length; ++i)
         {
            Class<?> p0 = arg0.getJavaMember().getParameterTypes()[i];
            Class<?> p1 = arg1.getJavaMember().getParameterTypes()[i];
            result = p0.getName().compareTo(p1.getName());
            if (result != 0)
            {
               return result;
            }
         }
         return 0;
      }

   }

   private static class AnnotatedConstructorComparator<T> implements Comparator<AnnotatedConstructor<? super T>>
   {

      public static <T> Comparator<AnnotatedConstructor<? super T>> instance()
      {
         return new AnnotatedConstructorComparator<T>();
      }

      private AnnotatedCallableComparator<T> callableComparator = new AnnotatedCallableComparator<T>();

      public int compare(AnnotatedConstructor<? super T> arg0, AnnotatedConstructor<? super T> arg1)
      {
         int result = callableComparator.compare(arg0, arg1);
         if (result != 0)
         {
            return result;
         }
         for (int i = 0; i < arg0.getJavaMember().getParameterTypes().length; ++i)
         {
            Class<?> p0 = arg0.getJavaMember().getParameterTypes()[i];
            Class<?> p1 = arg1.getJavaMember().getParameterTypes()[i];
            result = p0.getName().compareTo(p1.getName());
            if (result != 0)
            {
               return result;
            }
         }
         return 0;
      }

   }

   private static class AnnotatedFieldComparator<T> implements Comparator<AnnotatedField<? super T>>
   {

      public static <T> Comparator<AnnotatedField<? super T>> instance()
      {
         return new AnnotatedFieldComparator<T>();
      }

      public int compare(AnnotatedField<? super T> arg0, AnnotatedField<? super T> arg1)
      {
         if (arg0.getJavaMember().getName().equals(arg1.getJavaMember().getName()))
         {
            return arg0.getJavaMember().getDeclaringClass().getName().compareTo(arg1.getJavaMember().getDeclaringClass().getName());
         }
         return arg0.getJavaMember().getName().compareTo(arg1.getJavaMember().getName());
      }

   }

   private static class AnnotationComparator implements Comparator<Annotation>
   {

      public static final Comparator<Annotation> INSTANCE = new AnnotationComparator();

      public int compare(Annotation arg0, Annotation arg1)
      {
         return arg0.annotationType().getName().compareTo(arg1.annotationType().getName());
      }
   }

   private static class MethodComparator implements Comparator<Method>
   {
      
      public static final Comparator<Method> INSTANCE = new MethodComparator();

      public int compare(Method arg0, Method arg1)
      {
         return arg0.getName().compareTo(arg1.getName());
      }
   }

   private static final char SEPERATOR = ';';

   /**
    * Generates a unique signature for an annotated type. Members without
    * annotations are omitted to reduce the length of the signature
    * 
    * @param <X>
    * @param annotatedType
    * @return
    */
   public static <X> String createTypeId(AnnotatedType<X> annotatedType)
   {
      return createTypeId(annotatedType.getJavaClass(), annotatedType.getAnnotations(), annotatedType.getMethods(), annotatedType.getFields(), annotatedType.getConstructors());
   }

   /**
    * Generates a unique signature for a concrete class
    * 
    * @param <X>
    * @param annotatedType
    * @return
    */
   public static <X> String createTypeId(Class<X> clazz, Collection<Annotation> annotations, Collection<AnnotatedMethod<? super X>> methods, Collection<AnnotatedField<? super X>> fields, Collection<AnnotatedConstructor<X>> constructors)
   {
      StringBuilder builder = new StringBuilder();

      builder.append(clazz.getName());
      builder.append(createAnnotationCollectionId(annotations));
      builder.append("{ ");

      // now deal with the fields
      List<AnnotatedField<? super X>> sortedFields = new ArrayList<AnnotatedField<? super X>>();
      sortedFields.addAll(fields);
      Collections.sort(sortedFields, AnnotatedFieldComparator.<X> instance());
      for (AnnotatedField<? super X> field : sortedFields)
      {
         if (!field.getAnnotations().isEmpty())
         {
            builder.append(createFieldId(field));
            builder.append(SEPERATOR);
         }
      }

      // methods
      List<AnnotatedMethod<? super X>> sortedMethods = new ArrayList<AnnotatedMethod<? super X>>();
      sortedMethods.addAll(methods);
      Collections.sort(sortedMethods, AnnotatedMethodComparator.<X> instance());
      for (AnnotatedMethod<? super X> method : sortedMethods)
      {
         if (!method.getAnnotations().isEmpty() || hasMethodParameters(method))
         {
            builder.append(createCallableId(method));
            builder.append(SEPERATOR);
         }
      }

      // constructors
      List<AnnotatedConstructor<? super X>> sortedConstructors = new ArrayList<AnnotatedConstructor<? super X>>();
      sortedConstructors.addAll(constructors);
      Collections.sort(sortedConstructors, AnnotatedConstructorComparator.<X> instance());
      for (AnnotatedConstructor<? super X> constructor : sortedConstructors)
      {
         if (!constructor.getAnnotations().isEmpty() || hasMethodParameters(constructor))
         {
            builder.append(createCallableId(constructor));
            builder.append(SEPERATOR);
         }
      }
      builder.append("}");

      return builder.toString();
   }

   private static <X> boolean hasMethodParameters(AnnotatedCallable<X> callable)
   {
      for (AnnotatedParameter<X> parameter : callable.getParameters())
      {
         if (parameter.getAnnotations().isEmpty())
         {
            return true;
         }
      }
      return false;
   }

   private static String createAnnotationCollectionId(Collection<Annotation> annotations)
   {
      if (annotations.isEmpty())
      {
         return "";
      }
      
      StringBuilder builder = new StringBuilder();
      builder.append('[');

      List<Annotation> annotationList = new ArrayList<Annotation>(annotations.size());
      annotationList.addAll(annotations);
      Collections.sort(annotationList, AnnotationComparator.INSTANCE);
      
      for (Annotation a : annotationList)
      {
         builder.append('@');
         builder.append(a.annotationType().getName());
         builder.append('(');
         Method[] declaredMethods = a.annotationType().getDeclaredMethods();
         List<Method> methods = new ArrayList<Method>(declaredMethods.length);
         // TODO Shouldn't the declared methods be added here?
         Collections.sort(methods, MethodComparator.INSTANCE);

         for (int i = 0; i < methods.size(); ++i)
         {
            Method method = methods.get(i);
            try
            {
               Object value = method.invoke(a);
               builder.append(method.getName());
               builder.append('=');
               builder.append(value.toString());
            }
            catch (NullPointerException e)
            {
               throw new RuntimeException("NullPointerException accessing annotation member, annotation:" + a.annotationType().getName() + " member: " + method.getName(), e);
            }
            catch (IllegalArgumentException e)
            {
               throw new RuntimeException("IllegalArgumentException accessing annotation member, annotation:" + a.annotationType().getName() + " member: " + method.getName(), e);
            }
            catch (IllegalAccessException e)
            {
               throw new RuntimeException("IllegalAccessException accessing annotation member, annotation:" + a.annotationType().getName() + " member: " + method.getName(), e);
            }
            catch (InvocationTargetException e)
            {
               throw new RuntimeException("InvocationTargetException accessing annotation member, annotation:" + a.annotationType().getName() + " member: " + method.getName(), e);
            }
            if (i + 1 != methods.size())
            {
               builder.append(',');
            }
         }
         builder.append(')');
      }
      builder.append(']');
      return builder.toString();
   }

   public static <X> String createFieldId(AnnotatedField<X> field)
   {
      return createFieldId(field.getJavaMember(), field.getAnnotations());
   }

   public static <X> String createFieldId(Field field, Collection<Annotation> annotations)
   {
      StringBuilder builder = new StringBuilder();
      builder.append(field.getDeclaringClass().getName());
      builder.append('.');
      builder.append(field.getName());
      builder.append(createAnnotationCollectionId(annotations));
      return builder.toString();
   }

   public static <X> String createCallableId(AnnotatedCallable<X> method)
   {
      StringBuilder builder = new StringBuilder();
      builder.append(method.getJavaMember().getDeclaringClass().getName());
      builder.append('.');
      builder.append(method.getJavaMember().getName());
      builder.append(createAnnotationCollectionId(method.getAnnotations()));
      builder.append(createParameterListId(method.getParameters()));
      return builder.toString();
   }

   public static <X> String createMethodId(Method method, Set<Annotation> annotations, List<AnnotatedParameter<X>> parameters)
   {
      StringBuilder builder = new StringBuilder();
      builder.append(method.getDeclaringClass().getName());
      builder.append('.');
      builder.append(method.getName());
      builder.append(createAnnotationCollectionId(annotations));
      builder.append(createParameterListId(parameters));
      return builder.toString();
   }

   public static <X> String createConstructorId(Constructor<X> constructor, Set<Annotation> annotations, List<AnnotatedParameter<X>> parameters)
   {
      StringBuilder builder = new StringBuilder();
      builder.append(constructor.getDeclaringClass().getName());
      builder.append('.');
      builder.append(constructor.getName());
      builder.append(createAnnotationCollectionId(annotations));
      builder.append(createParameterListId(parameters));
      return builder.toString();
   }

   public static <X> String createParameterListId(List<AnnotatedParameter<X>> parameters)
   {
      StringBuilder builder = new StringBuilder();
      builder.append("(");
      for (int i = 0; i < parameters.size(); ++i)
      {
         AnnotatedParameter<X> ap = parameters.get(i);
         builder.append(createParameterId(ap));
         if (i + 1 != parameters.size())
         {
            builder.append(',');
         }
      }
      builder.append(")");
      return builder.toString();
   }

   public static <X> String createParameterId(AnnotatedParameter<X> annotatedParameter)
   {
      return createParameterId(annotatedParameter.getBaseType(), annotatedParameter.getAnnotations());
   }

   public static <X> String createParameterId(Type type, Set<Annotation> annotations)
   {
      StringBuilder builder = new StringBuilder();
      if (type instanceof Class<?>)
      {
         Class<?> c = (Class<?>) type;
         builder.append(c.getName());
      }
      else
      {
         builder.append(type.toString());
      }
      builder.append(createAnnotationCollectionId(annotations));
      return builder.toString();
   }

   private AnnotatedTypes()
   {
   }

}