/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.workspace.type.config;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeUtility;

/**
 *
 */
public class ConfigPropertyType {
  private final static Pattern CONFIG_PROPERTY_REGEX = Pattern.compile("^(" + RuntimeClasses.ConfigProperty.replaceAll("\\.", "\\.") + "|" + Signature.getSignatureSimpleName(SignatureCache.createTypeSignature(RuntimeClasses.ConfigProperty)) + ")$");
  private final static Pattern CONFIG_OPERATION_REGEX = Pattern.compile("^(" + RuntimeClasses.ConfigOperation.replaceAll("\\.", "\\.") + "|" + Signature.getSignatureSimpleName(SignatureCache.createTypeSignature(RuntimeClasses.ConfigOperation)) + ")$");

  private final IType m_type;
  private TreeMap<String, ConfigurationMethod> m_configurationMethods;
  private List<IType> m_affectedTypes;
  private ITypeHierarchy m_superTypeHierarchy;

  public ConfigPropertyType(IType type) {
    m_type = type;
    // visit methods
    try {
      TreeMap<String, ConfigurationMethod> configurationMethods = new TreeMap<String, ConfigurationMethod>(new P_MethodNameComparator());
      ArrayList<IType> typesToVisit = new ArrayList<IType>();
      m_superTypeHierarchy = getType().newSupertypeHierarchy(null);
      IType[] superClasses = m_superTypeHierarchy.getAllSuperclasses(getType());
      for (IType t : superClasses) {
        if (TypeUtility.exists(t) && !t.getFullyQualifiedName().equals(Object.class.getName())) {
          typesToVisit.add(0, t);
        }
      }
      typesToVisit.add(getType());
      m_affectedTypes = typesToVisit;
      for (IType t : typesToVisit) {
        visitMethods(t, m_superTypeHierarchy, configurationMethods);
      }
      m_configurationMethods = configurationMethods;
    }
    catch (JavaModelException e) {
      ScoutSdk.logError("could not build ConfigPropertyType for '" + getType().getFullyQualifiedName() + "'.", e);
    }
  }

  public IType getType() {
    return m_type;
  }

  private void visitMethods(IType type, ITypeHierarchy superTypeHierarchy, Map<String, ConfigurationMethod> collector) throws JavaModelException {
    for (IMethod m : type.getMethods()) {
      if (TypeUtility.exists(m) && !m.isConstructor()) {
        String methodName = m.getElementName();
        ConfigurationMethod configMethod = collector.get(methodName);
        if (configMethod != null) {
          configMethod.pushMethod(m);
          continue;
        }
        for (IAnnotation a : m.getAnnotations()) {
          if (TypeUtility.exists(a)) {
            String annotationName = a.getElementName();
            if (CONFIG_PROPERTY_REGEX.matcher(annotationName).matches()) {
              String configPropertyType = null;
              for (IMemberValuePair p : a.getMemberValuePairs()) {
                if ("value".equals(p.getMemberName())) {
                  configPropertyType = getConfigPropertyType(p.getValue());
                  break;
                }
              }
              if (!StringUtility.isNullOrEmpty(configPropertyType)) {
                configMethod = new ConfigurationMethod(getType(), superTypeHierarchy, methodName, ConfigurationMethod.PROPERTY_METHOD);
                configMethod.setConfigAnnotationType(configPropertyType);
                configMethod.pushMethod(m);
                collector.put(methodName, configMethod);
                break;
              }
            }
            else if (CONFIG_OPERATION_REGEX.matcher(annotationName).matches()) {
              configMethod = new ConfigurationMethod(getType(), superTypeHierarchy, methodName, ConfigurationMethod.OPERATION_METHOD);
              configMethod.pushMethod(m);
              collector.put(methodName, configMethod);
              break;
            }
          }
        }
      }
    }
  }

  private static String getConfigPropertyType(Object val) throws JavaModelException {
    if (val == null) return null;
    String ret = val.toString();
    String configPropClassName = ConfigProperty.class.getSimpleName();
    if (ret.contains(configPropClassName)) {
      // if the configuration method is a source method and no binary method the final static String constants are not replaced by the compiler yet.
      // in this case not the value of the constant (e.g. "TEXT") is in the annotation value, but the reference to the constant (e.g. ConfigProperty.TEXT).
      // then parse the value of the constant within the ConfigProperty class.
      String constantName = ret.substring(configPropClassName.length() + 1);
      String configPropClassSource = TypeUtility.getType(ConfigProperty.class.getName()).getSource();
      Matcher m = Pattern.compile("String\\s*" + constantName + "\\s*=\\s*\"(.*)\"\\s*\\;").matcher(configPropClassSource);
      if (m.find()) {
        ret = m.group(1);
      }
      else {
        ret = constantName;
      }
    }
    return ret;
  }

  public ConfigurationMethod getConfigurationMethod(String name) {
    return m_configurationMethods.get(name);
  }

  public ConfigurationMethod getConfigurationMethod(IMethod method) {
    return m_configurationMethods.get(method.getElementName());
  }

  public ConfigurationMethod[] getConfigurationMethods(int methodType) {
    Collection<ConfigurationMethod> values = m_configurationMethods.values();
    ArrayList<ConfigurationMethod> result = new ArrayList<ConfigurationMethod>(values.size());
    for (ConfigurationMethod m : values) {
      if (m.getMethodType() == methodType) {
        result.add(m);
      }
    }
    return result.toArray(new ConfigurationMethod[result.size()]);
  }

  public ConfigurationMethod[] getConfigurationMethods() {
    return m_configurationMethods.values().toArray(new ConfigurationMethod[m_configurationMethods.size()]);
  }

  /**
   * @param declaringType
   * @return
   */
  public boolean isRelevantType(IType type) {
    return m_affectedTypes.contains(type);
  }

  public ConfigurationMethod updateIfChanged(IMethod method) {
    String methodName = method.getElementName();
    ConfigurationMethod newMethod = null;
    try {
      for (IType t : m_affectedTypes) {
        IMethod m = TypeUtility.getMethod(t, methodName);
        if (TypeUtility.exists(m)) {
          if (newMethod != null) {
            newMethod.pushMethod(m);
          }
          else {
            for (IAnnotation a : m.getAnnotations()) {
              if (TypeUtility.exists(a)) {
                String annotationName = a.getElementName();
                if (CONFIG_PROPERTY_REGEX.matcher(annotationName).matches()) {
                  String configPropertyType = null;
                  for (IMemberValuePair p : a.getMemberValuePairs()) {
                    if ("value".equals(p.getMemberName())) {
                      configPropertyType = (String) p.getValue();
                      break;
                    }
                  }
                  if (!StringUtility.isNullOrEmpty(configPropertyType)) {
                    newMethod = new ConfigurationMethod(getType(), m_superTypeHierarchy, methodName, ConfigurationMethod.PROPERTY_METHOD);
                    newMethod.setConfigAnnotationType(configPropertyType);
                    newMethod.pushMethod(m);
                    break;
                  }
                }
                else if (CONFIG_OPERATION_REGEX.matcher(annotationName).matches()) {
                  newMethod = new ConfigurationMethod(getType(), m_superTypeHierarchy, methodName, ConfigurationMethod.OPERATION_METHOD);
                  newMethod.pushMethod(m);
                  break;
                }
              }
            }
          }
        }
      }
    }
    catch (JavaModelException e) {
      ScoutSdk.logError("could not build ConfigPropertyType for '" + methodName + "' in type '" + getType().getFullyQualifiedName() + "'.", e);
    }
    if (newMethod != null) {
      ConfigurationMethod oldMethod = m_configurationMethods.get(methodName);
      if (!newMethod.equals(oldMethod)) {
        m_configurationMethods.put(methodName, newMethod);
        return newMethod;
      }
    }
    return null;
  }

  public void print(PrintStream printer) {
    printer.println("Configuration of '" + getType().getFullyQualifiedName() + "' ----------");
    printer.println("--- config PROPERTY methods:");
    for (ConfigurationMethod m : getConfigurationMethods(ConfigurationMethod.PROPERTY_METHOD)) {
      printer.println("  " + m.getMethodName() + " [" + m.getMethodStackSize() + "]");
    }
    printer.println("--- config OPERATION methods:");
    for (ConfigurationMethod m : getConfigurationMethods(ConfigurationMethod.OPERATION_METHOD)) {
      printer.println("  " + m.getMethodName() + " [" + m.getMethodStackSize() + "]");
    }
    printer.println("--------------------------------------------------------------------------");
  }

  private class P_MethodNameComparator implements Comparator<String> {
    @Override
    public int compare(String o1, String o2) {
      if (o1 == null) {
        if (o2 == null) {
          return 0;
        }
        else {
          return -1;
        }
      }
      else {
        if (o2 == null) {
          return 1;
        }
        else {
          return o1.toLowerCase().compareTo(o2.toLowerCase());
        }
      }
    }
  }

}
