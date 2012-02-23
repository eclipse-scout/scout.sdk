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
package org.eclipse.scout.sdk.workspace.type;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.IMethodFilter;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.MethodFilters;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;

public class ScoutStructuredType implements IStructuredType {

  private static final Pattern PROPERTY_BEAN_REGEX = Pattern.compile("^(get|set|is|add|remove|clear|delete)(.*)$");

  private final IType m_type;
  private final EnumSet<CATEGORIES> m_enabledCategories;
  private EnumSet<CATEGORIES> m_visitedCategories;
  private HashMap<CATEGORIES, IJavaElement[]> m_elements;
  private ITypeHierarchy m_typeHierarchy;

  public ScoutStructuredType(IType type, EnumSet<CATEGORIES> enabledCategories) {
    m_type = type;
    m_enabledCategories = enabledCategories;
    m_visitedCategories = EnumSet.noneOf(CATEGORIES.class);
    m_elements = new HashMap<CATEGORIES, IJavaElement[]>();
    // initialy put all into unknown categories
    m_typeHierarchy = TypeUtility.getLocalTypeHierarchy(type);
    List<IJavaElement> fields = new ArrayList<IJavaElement>();
    List<IJavaElement> enums = new ArrayList<IJavaElement>();
    List<IJavaElement> methods = new ArrayList<IJavaElement>();
    List<IJavaElement> types = new ArrayList<IJavaElement>();
    try {
      for (IJavaElement childElement : type.getChildren()) {
        switch (childElement.getElementType()) {
          case IJavaElement.FIELD:
            fields.add(childElement);
            break;
          case IJavaElement.METHOD:
            methods.add(childElement);
            break;
          case IJavaElement.TYPE:
            if (((IType) childElement).isEnum()) {
              enums.add(childElement);
            }
            else {
              types.add(childElement);
            }
            break;
          default:
            ScoutSdk.logError("Found not considered java element '" + childElement.getElementName() + "' of source builder for '" + getType().getFullyQualifiedName() + "'.");
            break;
        }
      }
      m_elements.put(CATEGORIES.FIELD_UNKNOWN, fields.toArray(new IJavaElement[fields.size()]));
      m_elements.put(CATEGORIES.ENUM, enums.toArray(new IJavaElement[enums.size()]));
      m_elements.put(CATEGORIES.METHOD_UNCATEGORIZED, methods.toArray(new IJavaElement[methods.size()]));
      m_elements.put(CATEGORIES.TYPE_UNCATEGORIZED, types.toArray(new IJavaElement[types.size()]));
    }
    catch (JavaModelException e) {
      ScoutSdk.logError("could not build structured type of '" + type.getFullyQualifiedName() + "'.", e);
    }
  }

  public IType getType() {
    return m_type;
  }

  @Override
  public IJavaElement[] getElements(CATEGORIES category) {
    cache(category);
    IJavaElement[] iJavaElements = m_elements.get(category);
    if (iJavaElements == null) {
      iJavaElements = new IJavaElement[0];
    }
    return iJavaElements;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends IJavaElement> T[] getElements(CATEGORIES category, Class<T> clazz) {
    IJavaElement[] elements = getElements(category);
    if (elements != null) {
      T[] result = (T[]) Array.newInstance(clazz, elements.length);
      for (int i = 0; i < elements.length; i++) {
        result[i] = (T) elements[i];
      }
      return result;
    }
    return null;
  }

  @Override
  public IJavaElement getSiblingMethodConfigGetConfigured(String methodName) {
    IJavaElement[] configProperties = getElements(CATEGORIES.METHOD_CONFIG_PROPERTY);
    if (configProperties != null) {
      for (IJavaElement reference : configProperties) {
        if (reference.getElementName().compareTo(methodName) > 0) {
          return reference;
        }
      }
    }
    return getSibling(CATEGORIES.METHOD_CONFIG_PROPERTY);
  }

  @Override
  public IJavaElement getSiblingMethodConfigExec(String methodName) {
    cache(CATEGORIES.METHOD_CONFIG_EXEC);
    IJavaElement[] references = getElements(CATEGORIES.METHOD_CONFIG_EXEC);
    if (references != null) {
      for (IJavaElement reference : references) {
        if (reference.getElementName().compareTo(methodName) > 0) {
          return reference;
        }
      }
    }
    return getSibling(CATEGORIES.METHOD_CONFIG_EXEC);
  }

  @Override
  public IJavaElement getSiblingMethodFieldGetter(String methodName) {
    cache(CATEGORIES.METHOD_INNER_TYPE_GETTER);
    IJavaElement[] references = getElements(CATEGORIES.METHOD_INNER_TYPE_GETTER);
    if (references != null) {
      for (IJavaElement reference : references) {
        if (reference.getElementName().compareTo(methodName) > 0) {
          return reference;
        }
      }
    }
    return getSibling(CATEGORIES.METHOD_INNER_TYPE_GETTER);
  }

  @Override
  public IJavaElement getSiblingMethodStartHandler(String methodName) {
    cache(CATEGORIES.METHOD_START_HANDLER);
    IJavaElement[] references = getElements(CATEGORIES.METHOD_START_HANDLER);
    if (references != null) {
      for (IJavaElement reference : references) {
        if (reference.getElementName().compareTo(methodName) > 0) {
          return reference;
        }
      }
    }
    return getSibling(CATEGORIES.METHOD_START_HANDLER);
  }

  @Override
  public IJavaElement getSiblingTypeKeyStroke(String keyStrokeName) {
    cache(CATEGORIES.TYPE_KEYSTROKE);
    IJavaElement[] types = getElements(CATEGORIES.TYPE_KEYSTROKE);
    if (types != null) {
      for (IJavaElement fh : types) {
        if (fh.getElementName().compareTo(keyStrokeName) > 0) {
          return fh;
        }
      }
    }
    return getSibling(CATEGORIES.TYPE_KEYSTROKE);
  }

  @Override
  public IJavaElement getSiblingComposerAttribute(String attributeName) {
    cache(CATEGORIES.TYPE_COMPOSER_ATTRIBUTE);
    IJavaElement[] attributes = getElements(CATEGORIES.TYPE_COMPOSER_ATTRIBUTE);
    if (attributes != null) {
      for (IJavaElement element : attributes) {
        if (element.getElementName().compareTo(attributeName) > 0) {
          return element;
        }
      }
    }
    return getSibling(CATEGORIES.TYPE_COMPOSER_ATTRIBUTE);
  }

  @Override
  public IJavaElement getSiblingComposerEntity(String entityName) {
    cache(CATEGORIES.TYPE_COMPOSER_ENTRY);
    IJavaElement[] entities = getElements(CATEGORIES.TYPE_COMPOSER_ENTRY);
    if (entities != null) {
      for (IJavaElement element : entities) {
        if (element.getElementName().compareTo(entityName) > 0) {
          return element;
        }
      }
    }
    return getSibling(CATEGORIES.TYPE_COMPOSER_ENTRY);
  }

  @Override
  public IJavaElement getSiblingTypeFormHandler(String formHandlerName) {
    cache(CATEGORIES.TYPE_FORM_HANDLER);
    IJavaElement[] formHandlers = getElements(CATEGORIES.TYPE_FORM_HANDLER);
    if (formHandlers != null) {
      for (IJavaElement fh : formHandlers) {
        if (fh.getElementName().compareTo(formHandlerName) > 0) {
          return fh;
        }
      }
    }
    return getSibling(CATEGORIES.TYPE_UNCATEGORIZED);
  }

  @Override
  public IJavaElement getSibling(CATEGORIES category) {
    boolean search = false;
    CATEGORIES[] methodCategories = CATEGORIES.values();
    for (int i = 0; i < methodCategories.length; i++) {
      cache(methodCategories[i]);
      if (search) {
        IJavaElement[] elements = m_elements.get(methodCategories[i]);
        if (elements != null && elements.length > 0) {
          return elements[0];
        }
      }
      if (methodCategories[i].equals(category)) {
        search = true;
      }
    }
    return null;
  }

  protected final void cache(CATEGORIES category) {
    if (m_enabledCategories.contains(category) && !m_visitedCategories.contains(category)) {
      try {
        ArrayList<IJavaElement> unknownMethods = new ArrayList<IJavaElement>(Arrays.asList(m_elements.get(CATEGORIES.METHOD_UNCATEGORIZED)));
        ArrayList<IJavaElement> unknownTypes = new ArrayList<IJavaElement>(Arrays.asList(m_elements.get(CATEGORIES.TYPE_UNCATEGORIZED)));
        switch (category) {
          case FIELD_LOGGER:
          case FIELD_STATIC:
          case FIELD_MEMBER:
            visitFields(new ArrayList<IJavaElement>(Arrays.asList(m_elements.get(CATEGORIES.FIELD_UNKNOWN))));
            m_visitedCategories.add(CATEGORIES.FIELD_LOGGER);
            m_visitedCategories.add(CATEGORIES.FIELD_STATIC);
            m_visitedCategories.add(CATEGORIES.FIELD_MEMBER);
            break;
          case METHOD_CONSTRUCTOR:
            visitMethodConstructors(unknownMethods);
            m_visitedCategories.add(CATEGORIES.METHOD_CONSTRUCTOR);
            m_elements.put(CATEGORIES.METHOD_UNCATEGORIZED, unknownMethods.toArray(new IJavaElement[unknownMethods.size()]));

            break;
          case METHOD_CONFIG_PROPERTY:
            visitMethodConfigProperty(unknownMethods, m_typeHierarchy);
            m_visitedCategories.add(CATEGORIES.METHOD_CONFIG_PROPERTY);
            m_elements.put(CATEGORIES.METHOD_UNCATEGORIZED, unknownMethods.toArray(new IJavaElement[unknownMethods.size()]));

            break;
          case METHOD_CONFIG_EXEC:
            visitMethodConfigExec(unknownMethods, m_typeHierarchy);
            m_visitedCategories.add(CATEGORIES.METHOD_CONFIG_EXEC);
            m_elements.put(CATEGORIES.METHOD_UNCATEGORIZED, unknownMethods.toArray(new IJavaElement[unknownMethods.size()]));
            break;
          case METHOD_FORM_DATA_BEAN:
            m_visitedCategories.add(CATEGORIES.METHOD_FORM_DATA_BEAN);
            visitMethodFormDataBean(unknownMethods);
            m_elements.put(CATEGORIES.METHOD_UNCATEGORIZED, unknownMethods.toArray(new IJavaElement[unknownMethods.size()]));
            break;
          case METHOD_OVERRIDDEN:
            visitMethodOverridden(unknownMethods, m_typeHierarchy);
            m_visitedCategories.add(CATEGORIES.METHOD_OVERRIDDEN);
            m_elements.put(CATEGORIES.METHOD_UNCATEGORIZED, unknownMethods.toArray(new IJavaElement[unknownMethods.size()]));

            break;
          case METHOD_START_HANDLER:
            visitMethodStartHandler(unknownMethods);
            m_visitedCategories.add(CATEGORIES.METHOD_START_HANDLER);
            m_elements.put(CATEGORIES.METHOD_UNCATEGORIZED, unknownMethods.toArray(new IJavaElement[unknownMethods.size()]));

            break;
          case METHOD_INNER_TYPE_GETTER:
            visitMethodInnerTypeGetter(unknownMethods);
            m_visitedCategories.add(CATEGORIES.METHOD_INNER_TYPE_GETTER);
            m_elements.put(CATEGORIES.METHOD_UNCATEGORIZED, unknownMethods.toArray(new IJavaElement[unknownMethods.size()]));

            break;
          case METHOD_LOCAL_BEAN:
            visitMethodLocalBean(unknownMethods);
            m_visitedCategories.add(CATEGORIES.METHOD_LOCAL_BEAN);
            m_elements.put(CATEGORIES.METHOD_UNCATEGORIZED, unknownMethods.toArray(new IJavaElement[unknownMethods.size()]));
            break;
          case TYPE_FORM_FIELD:
            visitTypeFormFields(unknownTypes);
            m_visitedCategories.add(CATEGORIES.TYPE_FORM_FIELD);
            m_elements.put(CATEGORIES.TYPE_UNCATEGORIZED, unknownTypes.toArray(new IJavaElement[unknownTypes.size()]));
            break;
          case TYPE_COLUMN:
            visitTypeColumns(unknownTypes);
            m_visitedCategories.add(CATEGORIES.TYPE_COLUMN);
            m_elements.put(CATEGORIES.TYPE_UNCATEGORIZED, unknownTypes.toArray(new IJavaElement[unknownTypes.size()]));
            break;
          case TYPE_CODE:
            visitTypeCodes(unknownTypes);
            m_visitedCategories.add(CATEGORIES.TYPE_CODE);
            m_elements.put(CATEGORIES.TYPE_UNCATEGORIZED, unknownTypes.toArray(new IJavaElement[unknownTypes.size()]));
            break;
          case TYPE_FORM:
            visitTypeForms(unknownTypes);
            m_visitedCategories.add(CATEGORIES.TYPE_FORM);
            m_elements.put(CATEGORIES.TYPE_UNCATEGORIZED, unknownTypes.toArray(new IJavaElement[unknownTypes.size()]));
            break;
          case TYPE_TABLE:
            visitTypeTables(unknownTypes);
            m_visitedCategories.add(CATEGORIES.TYPE_TABLE);
            m_elements.put(CATEGORIES.TYPE_UNCATEGORIZED, unknownTypes.toArray(new IJavaElement[unknownTypes.size()]));
            break;
          case TYPE_ACTIVITY_MAP:
            visitTypeActivityMaps(unknownTypes);
            m_visitedCategories.add(CATEGORIES.TYPE_ACTIVITY_MAP);
            m_elements.put(CATEGORIES.TYPE_UNCATEGORIZED, unknownTypes.toArray(new IJavaElement[unknownTypes.size()]));
            break;
          case TYPE_TREE:
            visitTypeTrees(unknownTypes);
            m_visitedCategories.add(CATEGORIES.TYPE_TREE);
            m_elements.put(CATEGORIES.TYPE_UNCATEGORIZED, unknownTypes.toArray(new IJavaElement[unknownTypes.size()]));
            break;
          case TYPE_CALENDAR:
            visitTypeCalendar(unknownTypes);
            m_visitedCategories.add(CATEGORIES.TYPE_CALENDAR);
            m_elements.put(CATEGORIES.TYPE_UNCATEGORIZED, unknownTypes.toArray(new IJavaElement[unknownTypes.size()]));
            break;
          case TYPE_CALENDAR_ITEM_PROVIDER:
            visitTypeCalendarItemProvider(unknownTypes);
            m_visitedCategories.add(CATEGORIES.TYPE_CALENDAR_ITEM_PROVIDER);
            m_elements.put(CATEGORIES.TYPE_UNCATEGORIZED, unknownTypes.toArray(new IJavaElement[unknownTypes.size()]));
            break;
          case TYPE_WIZARD:
            visitTypeWizards(unknownTypes);
            m_visitedCategories.add(CATEGORIES.TYPE_WIZARD);
            m_elements.put(CATEGORIES.TYPE_UNCATEGORIZED, unknownTypes.toArray(new IJavaElement[unknownTypes.size()]));
            break;
          case TYPE_WIZARD_STEP:
            visitTypeWizardSteps(unknownTypes);
            m_visitedCategories.add(CATEGORIES.TYPE_WIZARD_STEP);
            m_elements.put(CATEGORIES.TYPE_UNCATEGORIZED, unknownTypes.toArray(new IJavaElement[unknownTypes.size()]));
            break;
          case TYPE_MENU:
            visitTypeMenus(unknownTypes);
            m_visitedCategories.add(CATEGORIES.TYPE_MENU);
            m_elements.put(CATEGORIES.TYPE_UNCATEGORIZED, unknownTypes.toArray(new IJavaElement[unknownTypes.size()]));
            break;
          case TYPE_VIEW_BUTTON:
            visitTypeViewbuttons(unknownTypes);
            m_visitedCategories.add(CATEGORIES.TYPE_VIEW_BUTTON);
            m_elements.put(CATEGORIES.TYPE_UNCATEGORIZED, unknownTypes.toArray(new IJavaElement[unknownTypes.size()]));
            break;
          case TYPE_TOOL_BUTTON:
            visitTypeToolbuttons(unknownTypes);
            m_visitedCategories.add(CATEGORIES.TYPE_TOOL_BUTTON);
            m_elements.put(CATEGORIES.TYPE_UNCATEGORIZED, unknownTypes.toArray(new IJavaElement[unknownTypes.size()]));
            break;
          case TYPE_KEYSTROKE:
            visitTypeKeystrokes(unknownTypes);
            m_visitedCategories.add(CATEGORIES.TYPE_KEYSTROKE);
            m_elements.put(CATEGORIES.TYPE_UNCATEGORIZED, unknownTypes.toArray(new IJavaElement[unknownTypes.size()]));
            break;
          case TYPE_COMPOSER_ATTRIBUTE:
            visitTypeComposerAttribute(unknownTypes);
            m_visitedCategories.add(CATEGORIES.TYPE_COMPOSER_ATTRIBUTE);
            m_elements.put(CATEGORIES.TYPE_UNCATEGORIZED, unknownTypes.toArray(new IJavaElement[unknownTypes.size()]));
            break;
          case TYPE_COMPOSER_ENTRY:
            visitTypeComposerEntry(unknownTypes);
            m_visitedCategories.add(CATEGORIES.TYPE_COMPOSER_ENTRY);
            m_elements.put(CATEGORIES.TYPE_UNCATEGORIZED, unknownTypes.toArray(new IJavaElement[unknownTypes.size()]));
            break;
          case TYPE_FORM_HANDLER:
            visitTypeFormHandlers(unknownTypes);
            m_visitedCategories.add(CATEGORIES.TYPE_FORM_HANDLER);
            m_elements.put(CATEGORIES.TYPE_UNCATEGORIZED, unknownTypes.toArray(new IJavaElement[unknownTypes.size()]));
            break;
        }
      }
      catch (JavaModelException e) {
        ScoutSdk.logError("could build sturucted type '" + m_type.getFullyQualifiedName() + "'.", e);
      }
    }
  }

  /**
   * can be overwritten. Overwrites must ensure to super call after processing the working set.
   * 
   * @param workingSet
   * @throws JavaModelException
   */
  protected void visitFields(ArrayList<IJavaElement> workingSet) throws JavaModelException {
    ArrayList<IField> loggers = new ArrayList<IField>(2);
    ArrayList<IField> statics = new ArrayList<IField>();
    ArrayList<IField> members = new ArrayList<IField>();
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IField f = (IField) it.next();
      // static
      if ((f.getFlags() & Flags.AccStatic) != 0) {
        String fqn = getFullyQuallifiedTypeName(f.getTypeSignature(), getType());
        if (RuntimeClasses.IScoutLogger.equals(fqn)) {
          loggers.add(f);
          it.remove();
        }
        else {
          statics.add(f);
          it.remove();
        }
      }
      else {
        members.add(f);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.FIELD_LOGGER, loggers.toArray(new IField[loggers.size()]));
    m_elements.put(CATEGORIES.FIELD_STATIC, statics.toArray(new IField[statics.size()]));
    m_elements.put(CATEGORIES.FIELD_MEMBER, members.toArray(new IField[members.size()]));
    m_elements.put(CATEGORIES.FIELD_UNKNOWN, workingSet.toArray(new IField[workingSet.size()]));
  }

  private static String getFullyQuallifiedTypeName(String signature, IType jdtType) throws JavaModelException {
    if (Signature.getTypeArguments(signature).length > 0) {
      signature = Signature.getTypeErasure(signature);
    }
    if (SignatureUtility.getTypeSignatureKind(signature) == Signature.BASE_TYPE_SIGNATURE) {
      return Signature.getSignatureSimpleName(signature);
    }
    if (signature.startsWith("Q")) {
      String[][] resolvedTypeName = jdtType.resolveType(Signature.getSignatureSimpleName(signature));
      if (resolvedTypeName != null && resolvedTypeName.length == 1) {
        String fqName = resolvedTypeName[0][0];
        if (fqName != null && fqName.length() > 0) {
          fqName = fqName + ".";
        }
        fqName = fqName + resolvedTypeName[0][1];
        return fqName;
      }
    }
    return null;
  }

  protected void visitMethodConstructors(ArrayList<IJavaElement> workingSet) throws JavaModelException {
    TreeMap<CompositeObject, IMethod> constructors = new TreeMap<CompositeObject, IMethod>();
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IMethod method = (IMethod) it.next();
      if (method.isConstructor()) {
        CompositeObject key = createConstructorKey(method.getParameterTypes());
        constructors.put(key, method);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.METHOD_CONSTRUCTOR, constructors.values().toArray(new IMethod[constructors.size()]));
  }

  protected void visitMethodConfigExec(ArrayList<IJavaElement> workingSet, ITypeHierarchy superTypeHierarchy) throws JavaModelException {
    TreeMap<CompositeObject, IMethod> execMethods = new TreeMap<CompositeObject, IMethod>();
    IMethodFilter execMethodFilter = MethodFilters.getFilterWithAnnotation(TypeUtility.getType(RuntimeClasses.ConfigOperation));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IMethod method = (IMethod) it.next();
      IMethod visitedmethod = method;
      while (TypeUtility.exists(visitedmethod)) {
        if (execMethodFilter.accept(visitedmethod)) {
          CompositeObject key = new CompositeObject(method.getElementName(), method.getParameterNames().length, method);
          execMethods.put(key, method);
          it.remove();
          break;
        }
        visitedmethod = TypeUtility.getOverwrittenMethod(visitedmethod, superTypeHierarchy.getJdtHierarchy());

      }

    }
    m_elements.put(CATEGORIES.METHOD_CONFIG_EXEC, execMethods.values().toArray(new IMethod[execMethods.size()]));
  }

  protected void visitMethodConfigProperty(ArrayList<IJavaElement> workingSet, ITypeHierarchy superTypeHierarchy) throws JavaModelException {
    TreeMap<CompositeObject, IMethod> methods = new TreeMap<CompositeObject, IMethod>();
    IMethodFilter configPropertyMethodFilter = MethodFilters.getFilterWithAnnotation(TypeUtility.getType(RuntimeClasses.ConfigProperty));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IMethod method = (IMethod) it.next();
      IMethod visitedmethod = method;
      while (TypeUtility.exists(visitedmethod)) {
        if (configPropertyMethodFilter.accept(visitedmethod)) {
          CompositeObject key = new CompositeObject(method.getElementName(), method.getParameterNames().length, method);
          methods.put(key, method);
          it.remove();
          break;
        }
        visitedmethod = TypeUtility.getOverwrittenMethod(visitedmethod, superTypeHierarchy.getJdtHierarchy());

      }
    }
    m_elements.put(CATEGORIES.METHOD_CONFIG_PROPERTY, methods.values().toArray(new IMethod[methods.size()]));
  }

  protected void visitMethodFormDataBean(ArrayList<IJavaElement> workingSet) throws JavaModelException {
    TreeMap<CompositeObject, IMethod> methods = new TreeMap<CompositeObject, IMethod>();
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IMethod method = (IMethod) it.next();
      if (JdtUtility.hasAnnotation(method, RuntimeClasses.FormData)) {
        CompositeObject methodKey = createPropertyMethodKey(method);
        if (methodKey != null) {
          methods.put(methodKey, method);
          it.remove();
        }
        else {
          ScoutSdk.logWarning("could not parse property method '" + method.getElementName() + "'.");
        }
      }
    }
    m_elements.put(CATEGORIES.METHOD_FORM_DATA_BEAN, methods.values().toArray(new IMethod[methods.size()]));
  }

  protected void visitMethodOverridden(ArrayList<IJavaElement> workingSet, ITypeHierarchy superTypeHierarchy) throws JavaModelException {
    TreeMap<CompositeObject, IMethod> overriddenMethods = new TreeMap<CompositeObject, IMethod>();
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IMethod method = (IMethod) it.next();
      if (TypeUtility.getOverwrittenMethod(method, superTypeHierarchy.getJdtHierarchy()) != null) {
        CompositeObject key = new CompositeObject(method.getElementName(), method.getParameterNames().length, method);
        overriddenMethods.put(key, method);
        it.remove();
      }

    }
    m_elements.put(CATEGORIES.METHOD_OVERRIDDEN, overriddenMethods.values().toArray(new IMethod[overriddenMethods.size()]));
  }

  protected void visitMethodStartHandler(ArrayList<IJavaElement> workingSet) throws JavaModelException {
    TreeMap<CompositeObject, IMethod> startHandlerMethods = new TreeMap<CompositeObject, IMethod>();
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IMethod method = (IMethod) it.next();
      Matcher matcher = Pattern.compile("^start(.*)$").matcher(method.getElementName());
      if (matcher.find()) {
        String fieldName = matcher.group(1);
        if (TypeUtility.findInnerType(getType(), fieldName + SdkProperties.SUFFIX_FORM_HANDLER) != null) {
          CompositeObject key = new CompositeObject(method.getElementName(), method.getParameterNames().length, method);
          startHandlerMethods.put(key, method);
          it.remove();
        }
      }
    }
    m_elements.put(CATEGORIES.METHOD_START_HANDLER, startHandlerMethods.values().toArray(new IMethod[startHandlerMethods.size()]));
  }

  protected void visitMethodInnerTypeGetter(ArrayList<IJavaElement> workingSet) throws JavaModelException {
    TreeMap<CompositeObject, IMethod> fieldGetterMethods = new TreeMap<CompositeObject, IMethod>();
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IMethod method = (IMethod) it.next();
      Matcher matcher = Pattern.compile("^get(.*)$").matcher(method.getElementName());
      if (matcher.find()) {
        String fieldName = matcher.group(1);
        if (TypeUtility.findInnerType(getType(), fieldName) != null) {
          CompositeObject key = new CompositeObject(method.getElementName(), method.getParameterNames().length, method);
          fieldGetterMethods.put(key, method);
          it.remove();
        }
      }
    }
    m_elements.put(CATEGORIES.METHOD_INNER_TYPE_GETTER, fieldGetterMethods.values().toArray(new IMethod[fieldGetterMethods.size()]));
  }

  protected void visitMethodLocalBean(ArrayList<IJavaElement> workingSet) throws JavaModelException {
    TreeMap<CompositeObject, IMethod> localPropertyMethods = new TreeMap<CompositeObject, IMethod>();
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IMethod method = (IMethod) it.next();
      CompositeObject key = createPropertyMethodKey(method);
      if (key != null) {

        localPropertyMethods.put(key, method);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.METHOD_LOCAL_BEAN, localPropertyMethods.values().toArray(new IMethod[localPropertyMethods.size()]));
  }

  protected void visitMethodUncategorized(ArrayList<IMethod> workingSet) throws JavaModelException {
    TreeMap<CompositeObject, IMethod> methods = new TreeMap<CompositeObject, IMethod>();
    for (Iterator<IMethod> it = workingSet.iterator(); it.hasNext();) {
      IMethod method = it.next();
      CompositeObject key = new CompositeObject(method.getElementName(), method.getParameterNames().length, method);
      methods.put(key, method);
      it.remove();
    }
    m_elements.put(CATEGORIES.METHOD_UNCATEGORIZED, methods.values().toArray(new IMethod[methods.size()]));

  }

  protected void visitTypeFormFields(ArrayList<IJavaElement> workingSet) {
    TreeSet<IType> formFields = new TreeSet<IType>(ScoutTypeComparators.getOrderAnnotationComparator());
    ITypeFilter filter = TypeFilters.getMultiTypeFilter(TypeFilters.getClassFilter(), TypeFilters.getSubtypeFilter(TypeUtility.getType(RuntimeClasses.IFormField), m_typeHierarchy));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.accept(candidate)) {
        formFields.add(candidate);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.TYPE_FORM_FIELD, formFields.toArray(new IType[formFields.size()]));
  }

  protected void visitTypeColumns(ArrayList<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<IType>(ScoutTypeComparators.getOrderAnnotationComparator());
    ITypeFilter filter = TypeFilters.getMultiTypeFilter(TypeFilters.getClassFilter(), TypeFilters.getSubtypeFilter(TypeUtility.getType(RuntimeClasses.IColumn), m_typeHierarchy));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.accept(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.TYPE_COLUMN, types.toArray(new IType[types.size()]));
  }

  protected void visitTypeCodes(ArrayList<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<IType>(ScoutTypeComparators.getOrderAnnotationComparator());
    ITypeFilter filter = TypeFilters.getMultiTypeFilter(TypeFilters.getClassFilter(), TypeFilters.getSubtypeFilter(TypeUtility.getType(RuntimeClasses.ICode), m_typeHierarchy));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.accept(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.TYPE_CODE, types.toArray(new IType[types.size()]));
  }

  protected void visitTypeForms(ArrayList<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<IType>(ScoutTypeComparators.getOrderAnnotationComparator());
    ITypeFilter filter = TypeFilters.getMultiTypeFilter(TypeFilters.getClassFilter(), TypeFilters.getSubtypeFilter(TypeUtility.getType(RuntimeClasses.IForm), m_typeHierarchy));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.accept(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.TYPE_FORM, types.toArray(new IType[types.size()]));
  }

  protected void visitTypeTables(ArrayList<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<IType>(ScoutTypeComparators.getOrderAnnotationComparator());
    ITypeFilter filter = TypeFilters.getMultiTypeFilter(TypeFilters.getClassFilter(), TypeFilters.getSubtypeFilter(TypeUtility.getType(RuntimeClasses.ITable), m_typeHierarchy));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.accept(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.TYPE_TABLE, types.toArray(new IType[types.size()]));
  }

  /**
   * @param workingSet
   */
  private void visitTypeActivityMaps(ArrayList<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<IType>(TypeComparators.getTypeNameComparator());
    ITypeFilter filter = TypeFilters.getMultiTypeFilter(TypeFilters.getClassFilter(), TypeFilters.getSubtypeFilter(TypeUtility.getType(RuntimeClasses.IActivityMap), m_typeHierarchy));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.accept(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.TYPE_ACTIVITY_MAP, types.toArray(new IType[types.size()]));
  }

  protected void visitTypeTrees(ArrayList<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<IType>(ScoutTypeComparators.getOrderAnnotationComparator());
    ITypeFilter filter = TypeFilters.getMultiTypeFilter(TypeFilters.getClassFilter(), TypeFilters.getSubtypeFilter(TypeUtility.getType(RuntimeClasses.ITree), m_typeHierarchy));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.accept(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.TYPE_TREE, types.toArray(new IType[types.size()]));
  }

  protected void visitTypeCalendar(ArrayList<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<IType>(ScoutTypeComparators.getOrderAnnotationComparator());
    ITypeFilter filter = TypeFilters.getMultiTypeFilter(TypeFilters.getClassFilter(), TypeFilters.getSubtypeFilter(TypeUtility.getType(RuntimeClasses.ICalendar), m_typeHierarchy));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.accept(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.TYPE_CALENDAR, types.toArray(new IType[types.size()]));
  }

  protected void visitTypeCalendarItemProvider(ArrayList<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<IType>(ScoutTypeComparators.getOrderAnnotationComparator());
    ITypeFilter filter = TypeFilters.getMultiTypeFilter(TypeFilters.getClassFilter(), TypeFilters.getSubtypeFilter(TypeUtility.getType(RuntimeClasses.ICalendarItemProvider), m_typeHierarchy));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.accept(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.TYPE_CALENDAR_ITEM_PROVIDER, types.toArray(new IType[types.size()]));
  }

  protected void visitTypeWizards(ArrayList<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<IType>(ScoutTypeComparators.getOrderAnnotationComparator());
    ITypeFilter filter = TypeFilters.getMultiTypeFilter(TypeFilters.getClassFilter(), TypeFilters.getSubtypeFilter(TypeUtility.getType(RuntimeClasses.IWizard), m_typeHierarchy));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.accept(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.TYPE_WIZARD, types.toArray(new IType[types.size()]));
  }

  protected void visitTypeWizardSteps(ArrayList<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<IType>(ScoutTypeComparators.getOrderAnnotationComparator());
    ITypeFilter filter = TypeFilters.getMultiTypeFilter(TypeFilters.getClassFilter(), TypeFilters.getSubtypeFilter(TypeUtility.getType(RuntimeClasses.IWizardStep), m_typeHierarchy));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.accept(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.TYPE_WIZARD_STEP, types.toArray(new IType[types.size()]));
  }

  protected void visitTypeMenus(ArrayList<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<IType>(ScoutTypeComparators.getOrderAnnotationComparator());
    ITypeFilter filter = TypeFilters.getMultiTypeFilter(TypeFilters.getClassFilter(), TypeFilters.getSubtypeFilter(TypeUtility.getType(RuntimeClasses.IMenu), m_typeHierarchy));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.accept(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.TYPE_MENU, types.toArray(new IType[types.size()]));
  }

  protected void visitTypeViewbuttons(ArrayList<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<IType>(ScoutTypeComparators.getOrderAnnotationComparator());
    ITypeFilter filter = TypeFilters.getMultiTypeFilter(TypeFilters.getClassFilter(), TypeFilters.getSubtypeFilter(TypeUtility.getType(RuntimeClasses.IViewButton), m_typeHierarchy));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.accept(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.TYPE_VIEW_BUTTON, types.toArray(new IType[types.size()]));
  }

  protected void visitTypeToolbuttons(ArrayList<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<IType>(ScoutTypeComparators.getOrderAnnotationComparator());
    ITypeFilter filter = TypeFilters.getMultiTypeFilter(TypeFilters.getClassFilter(), TypeFilters.getSubtypeFilter(TypeUtility.getType(RuntimeClasses.IToolButton), m_typeHierarchy));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.accept(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.TYPE_TOOL_BUTTON, types.toArray(new IType[types.size()]));
  }

  protected void visitTypeKeystrokes(ArrayList<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<IType>(TypeComparators.getTypeNameComparator());
    ITypeFilter filter = TypeFilters.getMultiTypeFilter(TypeFilters.getClassFilter(), TypeFilters.getSubtypeFilter(TypeUtility.getType(RuntimeClasses.IKeyStroke), m_typeHierarchy));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.accept(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.TYPE_KEYSTROKE, types.toArray(new IType[types.size()]));
  }

  protected void visitTypeComposerAttribute(ArrayList<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<IType>(TypeComparators.getTypeNameComparator());
    ITypeFilter filter = TypeFilters.getMultiTypeFilter(TypeFilters.getClassFilter(), TypeFilters.getSubtypeFilter(TypeUtility.getType(RuntimeClasses.IComposerAttribute), m_typeHierarchy));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.accept(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.TYPE_COMPOSER_ATTRIBUTE, types.toArray(new IType[types.size()]));
  }

  protected void visitTypeComposerEntry(ArrayList<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<IType>(TypeComparators.getTypeNameComparator());
    ITypeFilter filter = TypeFilters.getMultiTypeFilter(TypeFilters.getClassFilter(), TypeFilters.getSubtypeFilter(TypeUtility.getType(RuntimeClasses.IComposerEntity), m_typeHierarchy));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.accept(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.TYPE_COMPOSER_ENTRY, types.toArray(new IType[types.size()]));
  }

  protected void visitTypeFormHandlers(ArrayList<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<IType>(TypeComparators.getTypeNameComparator());
    ITypeFilter filter = TypeFilters.getMultiTypeFilter(TypeFilters.getClassFilter(), TypeFilters.getSubtypeFilter(TypeUtility.getType(RuntimeClasses.IFormHandler), m_typeHierarchy));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.accept(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.TYPE_FORM_HANDLER, types.toArray(new IType[types.size()]));
  }

  private static CompositeObject createPropertyMethodKey(IMethod method) {
    if (TypeUtility.exists(method)) {
      Matcher matcher = PROPERTY_BEAN_REGEX.matcher(method.getElementName());
      if (matcher.find()) {
        int getSetOrder = 20;
        if (StringUtility.equalsIgnoreCase("get", matcher.group(1))) {
          getSetOrder = 1;
        }
        else if (StringUtility.equalsIgnoreCase("is", matcher.group(1))) {
          getSetOrder = 2;
        }
        else if (StringUtility.equalsIgnoreCase("set", matcher.group(1))) {
          getSetOrder = 3;
        }
        else if (StringUtility.equalsIgnoreCase("add", matcher.group(1))) {
          getSetOrder = 4;
        }
        else if (StringUtility.equalsIgnoreCase("remove", matcher.group(1))) {
          getSetOrder = 5;
        }
        else if (StringUtility.equalsIgnoreCase("clear", matcher.group(1))) {
          getSetOrder = 6;
        }
        else if (StringUtility.equalsIgnoreCase("delete", matcher.group(1))) {
          getSetOrder = 7;
        }
        String propName = matcher.group(2);
        CompositeObject key = new CompositeObject(propName, getSetOrder, method.getElementName(), method.getParameterTypes().length, method);
        return key;
      }
    }
    return null;
  }

  protected CompositeObject createConstructorKey(String[] parameterSignatures) {
    if (parameterSignatures == null) {
      parameterSignatures = new String[0];
    }
    StringBuilder b = new StringBuilder();
    for (String sig : parameterSignatures) {
      b.append(sig);
    }
    return new CompositeObject(parameterSignatures.length, b.toString());

  }

  @Override
  public void print(PrintStream printer) {
    printer.println("------ Structured type of '" + getType().getFullyQualifiedName() + "' ------------");
    for (CATEGORIES c : CATEGORIES.values()) {
      printCategory(printer, c);
    }
    printer.println("---------------------------------------------------------------------------");
  }

  private void printCategory(PrintStream printer, CATEGORIES category) {
    printer.println("category '" + category.name() + "'");
    for (IJavaElement e : getElements(category)) {
      printer.println("  - " + e.getElementName());
    }
  }

}
