/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.internal.structuredtype;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.util.CompositeObject;
import org.eclipse.scout.sdk.core.util.IFilter;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.structuredtype.IStructuredType;
import org.eclipse.scout.sdk.s2e.util.JdtTypeFilters;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.scout.sdk.s2e.util.ScoutJdtTypeComparators;

public class ScoutStructuredType implements IStructuredType {

  private static final Pattern PROPERTY_BEAN_REGEX = Pattern.compile("^(get|set|is|add|remove|clear|delete)(.*)$");
  private static final Pattern START_HANDLER_REGEX = Pattern.compile("^start(.*)$");
  private static final Pattern METHOD_INNER_TYPE_GETTER_REGEX = Pattern.compile("^get(.*)$");

  private final IType m_type;
  private final EnumSet<Categories> m_enabledCategories;
  private final EnumSet<Categories> m_visitedCategories;
  private final Map<Categories, List<? extends IJavaElement>> m_elements;
  private final ITypeHierarchy m_typeHierarchy;

  public ScoutStructuredType(IType type, EnumSet<Categories> enabledCategories) {
    this(type, enabledCategories, null);
  }

  public ScoutStructuredType(IType type, EnumSet<Categories> enabledCategories, ITypeHierarchy localHierarchy) {
    m_type = type;
    m_enabledCategories = enabledCategories;
    m_visitedCategories = EnumSet.noneOf(Categories.class);
    m_elements = new HashMap<>();
    if (localHierarchy == null) {
      try {
        m_typeHierarchy = JdtUtils.getLocalTypeHierarchy(type);
      }
      catch (JavaModelException e) {
        throw new SdkException("Unable to build type hierarchy for type '" + type.getFullyQualifiedName() + "'.", e);
      }
    }
    else {
      m_typeHierarchy = localHierarchy;
    }

    // initially put all into unknown categories
    List<IJavaElement> fields = new ArrayList<>();
    List<IJavaElement> enums = new ArrayList<>();
    List<IJavaElement> methods = new ArrayList<>();
    List<IJavaElement> types = new ArrayList<>();
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
            SdkLog.error("Found not considered java element '" + childElement.getElementName() + "' of source builder for '" + getType().getFullyQualifiedName() + "'.");
            break;
        }
      }
    }
    catch (JavaModelException e) {
      SdkLog.error("could not build structured type of '" + type.getFullyQualifiedName() + "'.", e);
    }

    m_elements.put(Categories.FIELD_UNKNOWN, fields);
    m_elements.put(Categories.ENUM, enums);
    m_elements.put(Categories.METHOD_UNCATEGORIZED, methods);
    m_elements.put(Categories.TYPE_UNCATEGORIZED, types);
  }

  public IType getType() {
    return m_type;
  }

  protected List<? extends IJavaElement> getElementsInternal(Categories category) {
    cache(category);
    return m_elements.get(category);
  }

  @Override
  public List<IJavaElement> getElements(Categories category) {
    return new ArrayList<>(getElementsInternal(category));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends IJavaElement> List<T> getElements(Categories category, Class<T> clazz) {
    List<? extends IJavaElement> elements = getElementsInternal(category);
    if (elements == null) {
      return new ArrayList<>(0);
    }

    List<T> result = new ArrayList<>(elements.size());
    for (IJavaElement e : elements) {
      result.add((T) e);
    }
    return result;
  }

  @Override
  public IJavaElement getSiblingMethodConfigGetConfigured(String methodName) {
    List<? extends IJavaElement> configProperties = getElementsInternal(Categories.METHOD_CONFIG_PROPERTY);
    if (configProperties != null) {
      for (IJavaElement reference : configProperties) {
        if (reference.getElementName().compareTo(methodName) > 0) {
          return reference;
        }
      }
    }
    return getSibling(Categories.METHOD_CONFIG_PROPERTY);
  }

  @Override
  public IJavaElement getSiblingMethodConfigExec(String methodName) {
    List<? extends IJavaElement> references = getElementsInternal(Categories.METHOD_CONFIG_EXEC);
    if (references != null) {
      for (IJavaElement reference : references) {
        if (reference.getElementName().compareTo(methodName) > 0) {
          return reference;
        }
      }
    }
    return getSibling(Categories.METHOD_CONFIG_EXEC);
  }

  @Override
  public IJavaElement getSiblingMethodFieldGetter(String methodName) {
    List<? extends IJavaElement> references = getElementsInternal(Categories.METHOD_INNER_TYPE_GETTER);
    if (references != null) {
      for (IJavaElement reference : references) {
        if (reference.getElementName().compareTo(methodName) > 0) {
          return reference;
        }
      }
    }
    return getSibling(Categories.METHOD_INNER_TYPE_GETTER);
  }

  @Override
  public IJavaElement getSiblingMethodStartHandler(String methodName) {
    List<? extends IJavaElement> references = getElementsInternal(Categories.METHOD_START_HANDLER);
    if (references != null) {
      for (IJavaElement reference : references) {
        if (reference.getElementName().compareTo(methodName) > 0) {
          return reference;
        }
      }
    }
    return getSibling(Categories.METHOD_START_HANDLER);
  }

  @Override
  public IJavaElement getSiblingTypeKeyStroke(String keyStrokeName) {
    List<? extends IJavaElement> types = getElementsInternal(Categories.TYPE_KEYSTROKE);
    if (types != null) {
      for (IJavaElement fh : types) {
        if (fh.getElementName().compareTo(keyStrokeName) > 0) {
          return fh;
        }
      }
    }
    return getSibling(Categories.TYPE_KEYSTROKE);
  }

  @Override
  public IJavaElement getSiblingComposerAttribute(String attributeName) {
    List<? extends IJavaElement> attributes = getElementsInternal(Categories.TYPE_COMPOSER_ATTRIBUTE);
    if (attributes != null) {
      for (IJavaElement element : attributes) {
        if (element.getElementName().compareTo(attributeName) > 0) {
          return element;
        }
      }
    }
    return getSibling(Categories.TYPE_COMPOSER_ATTRIBUTE);
  }

  @Override
  public IJavaElement getSiblingComposerEntity(String entityName) {
    List<? extends IJavaElement> entities = getElementsInternal(Categories.TYPE_COMPOSER_ENTRY);
    if (entities != null) {
      for (IJavaElement element : entities) {
        if (element.getElementName().compareTo(entityName) > 0) {
          return element;
        }
      }
    }
    return getSibling(Categories.TYPE_COMPOSER_ENTRY);
  }

  @Override
  public IJavaElement getSiblingTypeFormHandler(String formHandlerName) {
    List<? extends IJavaElement> formHandlers = getElementsInternal(Categories.TYPE_FORM_HANDLER);
    if (formHandlers != null) {
      for (IJavaElement fh : formHandlers) {
        if (fh.getElementName().compareTo(formHandlerName) > 0) {
          return fh;
        }
      }
    }
    return getSibling(Categories.TYPE_UNCATEGORIZED);
  }

  @Override
  public IJavaElement getSibling(Categories category) {
    boolean search = false;
    Categories[] methodCategories = Categories.values();
    for (int i = 0; i < methodCategories.length; i++) {
      cache(methodCategories[i]);
      if (search) {
        List<? extends IJavaElement> elements = getElementsInternal(methodCategories[i]);
        if (elements != null && !elements.isEmpty()) {
          return elements.get(0);
        }
      }
      if (methodCategories[i].equals(category)) {
        search = true;
      }
    }
    return null;
  }

  protected final void cache(Categories category) {
    if (m_enabledCategories.contains(category) && !m_visitedCategories.contains(category)) {
      try {
        List<IJavaElement> unknownMethods = new ArrayList<>(m_elements.get(Categories.METHOD_UNCATEGORIZED));
        List<IJavaElement> unknownTypes = new ArrayList<>(m_elements.get(Categories.TYPE_UNCATEGORIZED));
        switch (category) {
          case FIELD_LOGGER:
          case FIELD_STATIC:
          case FIELD_MEMBER:
            visitFields(new ArrayList<>(m_elements.get(Categories.FIELD_UNKNOWN)));
            m_visitedCategories.add(Categories.FIELD_LOGGER);
            m_visitedCategories.add(Categories.FIELD_STATIC);
            m_visitedCategories.add(Categories.FIELD_MEMBER);
            break;
          case METHOD_CONSTRUCTOR:
            visitMethodConstructors(unknownMethods);
            m_visitedCategories.add(Categories.METHOD_CONSTRUCTOR);
            m_elements.put(Categories.METHOD_UNCATEGORIZED, unknownMethods);
            break;
          case METHOD_CONFIG_PROPERTY:
            visitMethodConfigProperty(unknownMethods, m_typeHierarchy);
            m_visitedCategories.add(Categories.METHOD_CONFIG_PROPERTY);
            m_elements.put(Categories.METHOD_UNCATEGORIZED, unknownMethods);
            break;
          case METHOD_CONFIG_EXEC:
            visitMethodConfigExec(unknownMethods, m_typeHierarchy);
            m_visitedCategories.add(Categories.METHOD_CONFIG_EXEC);
            m_elements.put(Categories.METHOD_UNCATEGORIZED, unknownMethods);
            break;
          case METHOD_FORM_DATA_BEAN:
            m_visitedCategories.add(Categories.METHOD_FORM_DATA_BEAN);
            visitMethodFormDataBean(unknownMethods);
            m_elements.put(Categories.METHOD_UNCATEGORIZED, unknownMethods);
            break;
          case METHOD_OVERRIDDEN:
            visitMethodOverridden(unknownMethods, m_typeHierarchy);
            m_visitedCategories.add(Categories.METHOD_OVERRIDDEN);
            m_elements.put(Categories.METHOD_UNCATEGORIZED, unknownMethods);
            break;
          case METHOD_START_HANDLER:
            visitMethodStartHandler(unknownMethods);
            m_visitedCategories.add(Categories.METHOD_START_HANDLER);
            m_elements.put(Categories.METHOD_UNCATEGORIZED, unknownMethods);
            break;
          case METHOD_INNER_TYPE_GETTER:
            visitMethodInnerTypeGetter(unknownMethods);
            m_visitedCategories.add(Categories.METHOD_INNER_TYPE_GETTER);
            m_elements.put(Categories.METHOD_UNCATEGORIZED, unknownMethods);
            break;
          case METHOD_LOCAL_BEAN:
            visitMethodLocalBean(unknownMethods);
            m_visitedCategories.add(Categories.METHOD_LOCAL_BEAN);
            m_elements.put(Categories.METHOD_UNCATEGORIZED, unknownMethods);
            break;
          case TYPE_FORM_FIELD:
            visitTypeFormFields(unknownTypes);
            m_visitedCategories.add(Categories.TYPE_FORM_FIELD);
            m_elements.put(Categories.TYPE_UNCATEGORIZED, unknownTypes);
            break;
          case TYPE_COLUMN:
            visitTypeColumns(unknownTypes);
            m_visitedCategories.add(Categories.TYPE_COLUMN);
            m_elements.put(Categories.TYPE_UNCATEGORIZED, unknownTypes);
            break;
          case TYPE_CODE:
            visitTypeCodes(unknownTypes);
            m_visitedCategories.add(Categories.TYPE_CODE);
            m_elements.put(Categories.TYPE_UNCATEGORIZED, unknownTypes);
            break;
          case TYPE_FORM:
            visitTypeForms(unknownTypes);
            m_visitedCategories.add(Categories.TYPE_FORM);
            m_elements.put(Categories.TYPE_UNCATEGORIZED, unknownTypes);
            break;
          case TYPE_TABLE:
            visitTypeTables(unknownTypes);
            m_visitedCategories.add(Categories.TYPE_TABLE);
            m_elements.put(Categories.TYPE_UNCATEGORIZED, unknownTypes);
            break;
          case TYPE_ACTIVITY_MAP:
            visitTypeActivityMaps(unknownTypes);
            m_visitedCategories.add(Categories.TYPE_ACTIVITY_MAP);
            m_elements.put(Categories.TYPE_UNCATEGORIZED, unknownTypes);
            break;
          case TYPE_TREE:
            visitTypeTrees(unknownTypes);
            m_visitedCategories.add(Categories.TYPE_TREE);
            m_elements.put(Categories.TYPE_UNCATEGORIZED, unknownTypes);
            break;
          case TYPE_CALENDAR:
            visitTypeCalendar(unknownTypes);
            m_visitedCategories.add(Categories.TYPE_CALENDAR);
            m_elements.put(Categories.TYPE_UNCATEGORIZED, unknownTypes);
            break;
          case TYPE_CALENDAR_ITEM_PROVIDER:
            visitTypeCalendarItemProvider(unknownTypes);
            m_visitedCategories.add(Categories.TYPE_CALENDAR_ITEM_PROVIDER);
            m_elements.put(Categories.TYPE_UNCATEGORIZED, unknownTypes);
            break;
          case TYPE_WIZARD:
            visitTypeWizards(unknownTypes);
            m_visitedCategories.add(Categories.TYPE_WIZARD);
            m_elements.put(Categories.TYPE_UNCATEGORIZED, unknownTypes);
            break;
          case TYPE_WIZARD_STEP:
            visitTypeWizardSteps(unknownTypes);
            m_visitedCategories.add(Categories.TYPE_WIZARD_STEP);
            m_elements.put(Categories.TYPE_UNCATEGORIZED, unknownTypes);
            break;
          case TYPE_MENU:
            visitTypeMenus(unknownTypes);
            m_visitedCategories.add(Categories.TYPE_MENU);
            m_elements.put(Categories.TYPE_UNCATEGORIZED, unknownTypes);
            break;
          case TYPE_VIEW_BUTTON:
            visitTypeViewbuttons(unknownTypes);
            m_visitedCategories.add(Categories.TYPE_VIEW_BUTTON);
            m_elements.put(Categories.TYPE_UNCATEGORIZED, unknownTypes);
            break;
          case TYPE_TOOL_BUTTON:
            visitTypeToolbuttons(unknownTypes);
            m_visitedCategories.add(Categories.TYPE_TOOL_BUTTON);
            m_elements.put(Categories.TYPE_UNCATEGORIZED, unknownTypes);
            break;
          case TYPE_KEYSTROKE:
            visitTypeKeystrokes(unknownTypes);
            m_visitedCategories.add(Categories.TYPE_KEYSTROKE);
            m_elements.put(Categories.TYPE_UNCATEGORIZED, unknownTypes);
            break;
          case TYPE_COMPOSER_ATTRIBUTE:
            visitTypeComposerAttribute(unknownTypes);
            m_visitedCategories.add(Categories.TYPE_COMPOSER_ATTRIBUTE);
            m_elements.put(Categories.TYPE_UNCATEGORIZED, unknownTypes);
            break;
          case TYPE_COMPOSER_ENTRY:
            visitTypeDataModelEntry(unknownTypes);
            m_visitedCategories.add(Categories.TYPE_COMPOSER_ENTRY);
            m_elements.put(Categories.TYPE_UNCATEGORIZED, unknownTypes);
            break;
          case TYPE_FORM_HANDLER:
            visitTypeFormHandlers(unknownTypes);
            m_visitedCategories.add(Categories.TYPE_FORM_HANDLER);
            m_elements.put(Categories.TYPE_UNCATEGORIZED, unknownTypes);
            break;
          case FIELD_UNKNOWN:
            break;
          case METHOD_UNCATEGORIZED:
            break;
          case TYPE_UNCATEGORIZED:
            break;
          case ENUM:
            break;
        }
      }
      catch (CoreException e) {
        SdkLog.error("could build sturucted type '" + m_type.getFullyQualifiedName() + "'.", e);
      }
    }
  }

  /**
   * can be overwritten. Overwrites must ensure to super call after processing the working set.
   *
   * @param workingSet
   * @throws JavaModelException
   */
  protected void visitFields(List<IJavaElement> workingSet) throws JavaModelException {
    List<IJavaElement> loggers = new ArrayList<>(2);
    List<IJavaElement> statics = new ArrayList<>();
    List<IJavaElement> members = new ArrayList<>();

    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IField f = (IField) it.next();
      // static
      if ((f.getFlags() & Flags.AccStatic) != 0) {
        String fieldDataType = Signature.toString(f.getTypeSignature());
        if (Signature.getSimpleName(IScoutRuntimeTypes.IScoutLogger).equals(fieldDataType) || IScoutRuntimeTypes.IScoutLogger.equals(fieldDataType)) {
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
    m_elements.put(Categories.FIELD_LOGGER, loggers);
    m_elements.put(Categories.FIELD_STATIC, statics);
    m_elements.put(Categories.FIELD_MEMBER, members);
    m_elements.put(Categories.FIELD_UNKNOWN, workingSet);
  }

  protected void visitMethodConstructors(List<IJavaElement> workingSet) throws JavaModelException {
    Map<CompositeObject, IJavaElement> constructors = new TreeMap<>();
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IMethod method = (IMethod) it.next();
      if (method.isConstructor()) {
        CompositeObject key = createConstructorKey(method.getParameterTypes());
        constructors.put(key, method);
        it.remove();
      }
    }
    m_elements.put(Categories.METHOD_CONSTRUCTOR, new ArrayList<>(constructors.values()));
  }

  protected void visitMethodConfigExec(List<IJavaElement> workingSet, ITypeHierarchy superTypeHierarchy) throws CoreException {
    Map<CompositeObject, IJavaElement> execMethods = new TreeMap<>();
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IMethod method = (IMethod) it.next();
      IMethod visitedMethod = method;
      while (JdtUtils.exists(visitedMethod)) {
        if (JdtUtils.getAnnotation(visitedMethod, IScoutRuntimeTypes.ConfigOperation) != null) {
          CompositeObject key = new CompositeObject(method.getElementName(), method.getParameterNames().length, method);
          execMethods.put(key, method);
          it.remove();
          break;
        }
        visitedMethod = getOverwrittenMethod(visitedMethod, superTypeHierarchy);
      }
    }
    m_elements.put(Categories.METHOD_CONFIG_EXEC, new ArrayList<>(execMethods.values()));
  }

  protected void visitMethodConfigProperty(List<IJavaElement> workingSet, ITypeHierarchy superTypeHierarchy) throws CoreException {
    Map<CompositeObject, IJavaElement> methods = new TreeMap<>();
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IMethod method = (IMethod) it.next();
      IMethod visitedMethod = method;
      while (JdtUtils.exists(visitedMethod)) {
        if (JdtUtils.getAnnotation(visitedMethod, IScoutRuntimeTypes.ConfigProperty) != null) {
          CompositeObject key = new CompositeObject(method.getElementName(), method.getParameterNames().length, method);
          methods.put(key, method);
          it.remove();
          break;
        }
        visitedMethod = getOverwrittenMethod(visitedMethod, superTypeHierarchy);
      }
    }
    m_elements.put(Categories.METHOD_CONFIG_PROPERTY, new ArrayList<>(methods.values()));
  }

  protected void visitMethodFormDataBean(List<IJavaElement> workingSet) {
    Map<CompositeObject, IJavaElement> methods = new TreeMap<>();
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IMethod method = (IMethod) it.next();
      if (JdtUtils.getAnnotation(method, IScoutRuntimeTypes.FormData) != null) {
        CompositeObject methodKey = createPropertyMethodKey(method);
        if (methodKey != null) {
          methods.put(methodKey, method);
          it.remove();
        }
        else {
          SdkLog.warning("could not parse property method '" + method.getElementName() + "'.");
        }
      }
    }
    m_elements.put(Categories.METHOD_FORM_DATA_BEAN, new ArrayList<>(methods.values()));
  }

  protected void visitMethodOverridden(List<IJavaElement> workingSet, ITypeHierarchy superTypeHierarchy) throws JavaModelException {
    Map<CompositeObject, IJavaElement> overriddenMethods = new TreeMap<>();
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IMethod method = (IMethod) it.next();
      if (getOverwrittenMethod(method, superTypeHierarchy) != null) {
        CompositeObject key = new CompositeObject(method.getElementName(), method.getParameterNames().length, method);
        overriddenMethods.put(key, method);
        it.remove();
      }
    }
    m_elements.put(Categories.METHOD_OVERRIDDEN, new ArrayList<>(overriddenMethods.values()));
  }

  protected void visitMethodStartHandler(List<IJavaElement> workingSet) throws JavaModelException {
    Map<CompositeObject, IJavaElement> startHandlerMethods = new TreeMap<>();
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IMethod method = (IMethod) it.next();
      Matcher matcher = START_HANDLER_REGEX.matcher(method.getElementName());
      if (matcher.find()) {
        String fieldName = matcher.group(1);
        if (JdtUtils.findInnerType(getType(), fieldName + ISdkProperties.SUFFIX_FORM_HANDLER) != null) {
          CompositeObject key = new CompositeObject(method.getElementName(), method.getParameterNames().length, method);
          startHandlerMethods.put(key, method);
          it.remove();
        }
      }
    }
    m_elements.put(Categories.METHOD_START_HANDLER, new ArrayList<>(startHandlerMethods.values()));
  }

  protected void visitMethodInnerTypeGetter(List<IJavaElement> workingSet) throws JavaModelException {
    Map<CompositeObject, IJavaElement> fieldGetterMethods = new TreeMap<>();
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IMethod method = (IMethod) it.next();
      Matcher matcher = METHOD_INNER_TYPE_GETTER_REGEX.matcher(method.getElementName());
      if (matcher.find()) {
        String fieldName = matcher.group(1);
        if (JdtUtils.findInnerType(getType(), fieldName) != null) {
          CompositeObject key = new CompositeObject(method.getElementName(), method.getParameterNames().length, method);
          fieldGetterMethods.put(key, method);
          it.remove();
        }
      }
    }
    m_elements.put(Categories.METHOD_INNER_TYPE_GETTER, new ArrayList<>(fieldGetterMethods.values()));
  }

  protected void visitMethodLocalBean(List<IJavaElement> workingSet) {
    Map<CompositeObject, IJavaElement> localPropertyMethods = new TreeMap<>();
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IMethod method = (IMethod) it.next();
      CompositeObject key = createPropertyMethodKey(method);
      if (key != null) {

        localPropertyMethods.put(key, method);
        it.remove();
      }
    }
    m_elements.put(Categories.METHOD_LOCAL_BEAN, new ArrayList<>(localPropertyMethods.values()));
  }

  protected void visitMethodUncategorized(ArrayList<IMethod> workingSet) throws JavaModelException {
    Map<CompositeObject, IJavaElement> methods = new TreeMap<>();
    for (Iterator<IMethod> it = workingSet.iterator(); it.hasNext();) {
      IMethod method = it.next();
      CompositeObject key = new CompositeObject(method.getElementName(), method.getParameterNames().length, method);
      methods.put(key, method);
      it.remove();
    }
    m_elements.put(Categories.METHOD_UNCATEGORIZED, new ArrayList<>(methods.values()));

  }

  protected void visitTypeFormFields(List<IJavaElement> workingSet) {
    Set<IType> formFields = new TreeSet<>(ScoutJdtTypeComparators.getOrderAnnotationComparator(false));
    IFilter<IType> filter = JdtTypeFilters.getMultiFilterAnd(JdtTypeFilters.getClassFilter(), JdtTypeFilters.getSubtypeFilter(IScoutRuntimeTypes.IFormField));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.evaluate(candidate)) {
        formFields.add(candidate);
        it.remove();
      }
    }
    m_elements.put(Categories.TYPE_FORM_FIELD, new ArrayList<>(formFields));
  }

  protected void visitTypeColumns(List<IJavaElement> workingSet) {
    Set<IType> types = new TreeSet<>(ScoutJdtTypeComparators.getOrderAnnotationComparator(false));
    IFilter<IType> filter = JdtTypeFilters.getMultiFilterAnd(JdtTypeFilters.getClassFilter(), JdtTypeFilters.getSubtypeFilter(IScoutRuntimeTypes.IColumn));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.evaluate(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(Categories.TYPE_COLUMN, new ArrayList<>(types));
  }

  protected void visitTypeCodes(List<IJavaElement> workingSet) {
    Set<IType> types = new TreeSet<>(ScoutJdtTypeComparators.getOrderAnnotationComparator(false));
    IFilter<IType> filter = JdtTypeFilters.getMultiFilterAnd(JdtTypeFilters.getClassFilter(), JdtTypeFilters.getSubtypeFilter(IScoutRuntimeTypes.ICode));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.evaluate(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(Categories.TYPE_CODE, new ArrayList<>(types));
  }

  protected void visitTypeForms(List<IJavaElement> workingSet) {
    Set<IType> types = new TreeSet<>(ScoutJdtTypeComparators.getOrderAnnotationComparator(false));
    IFilter<IType> filter = JdtTypeFilters.getMultiFilterAnd(JdtTypeFilters.getClassFilter(), JdtTypeFilters.getSubtypeFilter(IScoutRuntimeTypes.IForm));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.evaluate(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(Categories.TYPE_FORM, new ArrayList<>(types));
  }

  protected void visitTypeTables(List<IJavaElement> workingSet) {
    Set<IType> types = new TreeSet<>(ScoutJdtTypeComparators.getOrderAnnotationComparator(false));
    IFilter<IType> filter = JdtTypeFilters.getMultiFilterAnd(JdtTypeFilters.getClassFilter(), JdtTypeFilters.getSubtypeFilter(IScoutRuntimeTypes.ITable));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.evaluate(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(Categories.TYPE_TABLE, new ArrayList<>(types));
  }

  /**
   * @param workingSet
   */
  private void visitTypeActivityMaps(List<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<>(ScoutJdtTypeComparators.getTypeNameComparator());
    IFilter<IType> filter = JdtTypeFilters.getMultiFilterAnd(JdtTypeFilters.getClassFilter(), JdtTypeFilters.getSubtypeFilter(IScoutRuntimeTypes.IActivityMap));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.evaluate(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(Categories.TYPE_ACTIVITY_MAP, new ArrayList<>(types));
  }

  protected void visitTypeTrees(List<IJavaElement> workingSet) {
    Set<IType> types = new TreeSet<>(ScoutJdtTypeComparators.getOrderAnnotationComparator(false));
    IFilter<IType> filter = JdtTypeFilters.getMultiFilterAnd(JdtTypeFilters.getClassFilter(), JdtTypeFilters.getSubtypeFilter(IScoutRuntimeTypes.ITree));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.evaluate(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(Categories.TYPE_TREE, new ArrayList<>(types));
  }

  protected void visitTypeCalendar(List<IJavaElement> workingSet) {
    Set<IType> types = new TreeSet<>(ScoutJdtTypeComparators.getOrderAnnotationComparator(false));
    IFilter<IType> filter = JdtTypeFilters.getMultiFilterAnd(JdtTypeFilters.getClassFilter(), JdtTypeFilters.getSubtypeFilter(IScoutRuntimeTypes.ICalendar));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.evaluate(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(Categories.TYPE_CALENDAR, new ArrayList<>(types));
  }

  protected void visitTypeCalendarItemProvider(List<IJavaElement> workingSet) {
    Set<IType> types = new TreeSet<>(ScoutJdtTypeComparators.getOrderAnnotationComparator(false));
    IFilter<IType> filter = JdtTypeFilters.getMultiFilterAnd(JdtTypeFilters.getClassFilter(), JdtTypeFilters.getSubtypeFilter(IScoutRuntimeTypes.ICalendarItemProvider));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.evaluate(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(Categories.TYPE_CALENDAR_ITEM_PROVIDER, new ArrayList<>(types));
  }

  protected void visitTypeWizards(List<IJavaElement> workingSet) {
    Set<IType> types = new TreeSet<>(ScoutJdtTypeComparators.getOrderAnnotationComparator(false));
    IFilter<IType> filter = JdtTypeFilters.getMultiFilterAnd(JdtTypeFilters.getClassFilter(), JdtTypeFilters.getSubtypeFilter(IScoutRuntimeTypes.IWizard));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.evaluate(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(Categories.TYPE_WIZARD, new ArrayList<>(types));
  }

  protected void visitTypeWizardSteps(List<IJavaElement> workingSet) {
    Set<IType> types = new TreeSet<>(ScoutJdtTypeComparators.getOrderAnnotationComparator(false));
    IFilter<IType> filter = JdtTypeFilters.getMultiFilterAnd(JdtTypeFilters.getClassFilter(), JdtTypeFilters.getSubtypeFilter(IScoutRuntimeTypes.IWizardStep));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.evaluate(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(Categories.TYPE_WIZARD_STEP, new ArrayList<>(types));
  }

  protected void visitTypeMenus(List<IJavaElement> workingSet) {
    Set<IType> types = new TreeSet<>(ScoutJdtTypeComparators.getOrderAnnotationComparator(false));
    IFilter<IType> filter = JdtTypeFilters.getMultiFilterAnd(JdtTypeFilters.getClassFilter(), JdtTypeFilters.getSubtypeFilter(IScoutRuntimeTypes.IMenu));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.evaluate(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(Categories.TYPE_MENU, new ArrayList<>(types));
  }

  protected void visitTypeViewbuttons(List<IJavaElement> workingSet) {
    Set<IType> types = new TreeSet<>(ScoutJdtTypeComparators.getOrderAnnotationComparator(false));
    IFilter<IType> filter = JdtTypeFilters.getMultiFilterAnd(JdtTypeFilters.getClassFilter(), JdtTypeFilters.getSubtypeFilter(IScoutRuntimeTypes.IViewButton));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.evaluate(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(Categories.TYPE_VIEW_BUTTON, new ArrayList<>(types));
  }

  protected void visitTypeToolbuttons(List<IJavaElement> workingSet) {
    Set<IType> types = new TreeSet<>(ScoutJdtTypeComparators.getOrderAnnotationComparator(false));
    IFilter<IType> filter = JdtTypeFilters.getMultiFilterAnd(JdtTypeFilters.getClassFilter(), JdtTypeFilters.getSubtypeFilter(IScoutRuntimeTypes.IToolButton));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.evaluate(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(Categories.TYPE_TOOL_BUTTON, new ArrayList<>(types));
  }

  protected void visitTypeKeystrokes(List<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<>(ScoutJdtTypeComparators.getTypeNameComparator());
    IFilter<IType> filter = JdtTypeFilters.getMultiFilterAnd(JdtTypeFilters.getClassFilter(), JdtTypeFilters.getSubtypeFilter(IScoutRuntimeTypes.IKeyStroke));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.evaluate(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(Categories.TYPE_KEYSTROKE, new ArrayList<>(types));
  }

  protected void visitTypeComposerAttribute(List<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<>(ScoutJdtTypeComparators.getTypeNameComparator());
    IFilter<IType> filter = JdtTypeFilters.getMultiFilterAnd(JdtTypeFilters.getClassFilter(), JdtTypeFilters.getSubtypeFilter(IScoutRuntimeTypes.IDataModelAttribute));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.evaluate(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(Categories.TYPE_COMPOSER_ATTRIBUTE, new ArrayList<>(types));
  }

  protected void visitTypeDataModelEntry(List<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<>(ScoutJdtTypeComparators.getTypeNameComparator());
    IFilter<IType> filter = JdtTypeFilters.getMultiFilterAnd(JdtTypeFilters.getClassFilter(), JdtTypeFilters.getSubtypeFilter(IScoutRuntimeTypes.IDataModelEntity));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.evaluate(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(Categories.TYPE_COMPOSER_ENTRY, new ArrayList<>(types));
  }

  protected void visitTypeFormHandlers(List<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<>(ScoutJdtTypeComparators.getTypeNameComparator());
    IFilter<IType> filter = JdtTypeFilters.getMultiFilterAnd(JdtTypeFilters.getClassFilter(), JdtTypeFilters.getSubtypeFilter(IScoutRuntimeTypes.IFormHandler));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.evaluate(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(Categories.TYPE_FORM_HANDLER, new ArrayList<>(types));
  }

  private static CompositeObject createPropertyMethodKey(IMethod method) {
    if (JdtUtils.exists(method)) {
      Matcher matcher = PROPERTY_BEAN_REGEX.matcher(method.getElementName());
      if (matcher.find()) {
        int getSetOrder = 20;
        if ("get".equalsIgnoreCase(matcher.group(1))) {
          getSetOrder = 1;
        }
        else if ("is".equalsIgnoreCase(matcher.group(1))) {
          getSetOrder = 2;
        }
        else if ("set".equalsIgnoreCase(matcher.group(1))) {
          getSetOrder = 3;
        }
        else if ("add".equalsIgnoreCase(matcher.group(1))) {
          getSetOrder = 4;
        }
        else if ("remove".equalsIgnoreCase(matcher.group(1))) {
          getSetOrder = 5;
        }
        else if ("clear".equalsIgnoreCase(matcher.group(1))) {
          getSetOrder = 6;
        }
        else if ("delete".equalsIgnoreCase(matcher.group(1))) {
          getSetOrder = 7;
        }
        String propName = matcher.group(2);
        CompositeObject key = new CompositeObject(propName, getSetOrder, method.getElementName(), method.getParameterTypes().length, method);
        return key;
      }
    }
    return null;
  }

  private static IMethod getOverwrittenMethod(IMethod method, ITypeHierarchy supertypeHierarchy) throws JavaModelException {
    IType supertype = supertypeHierarchy.getSuperclass(method.getDeclaringType());
    IFilter<IMethod> overrideFilter = getSuperMethodFilter(method);
    while (JdtUtils.exists(supertype)) {
      IMethod superMethod = JdtUtils.getFirstMethod(supertype, overrideFilter);
      if (superMethod != null) {
        return superMethod;
      }
      supertype = supertypeHierarchy.getSuperclass(supertype);
    }
    return null;
  }

  protected CompositeObject createConstructorKey(String[] parameterSignatures) {
    if (parameterSignatures == null) {
      return new CompositeObject(0, "");
    }

    StringBuilder b = new StringBuilder();
    for (String sig : parameterSignatures) {
      b.append(sig);
    }
    return new CompositeObject(parameterSignatures.length, b.toString());
  }

  public void print(PrintStream printer) {
    printer.println("------ Structured type of '" + getType().getFullyQualifiedName() + "' ------------");
    for (Categories c : Categories.values()) {
      printCategory(printer, c);
    }
    printer.println("---------------------------------------------------------------------------");
  }

  private void printCategory(PrintStream printer, Categories category) {
    printer.println("category '" + category.name() + "'");
    for (IJavaElement e : getElements(category)) {
      printer.println("  - " + e.getElementName());
    }
  }

  private static IFilter<IMethod> getSuperMethodFilter(final IMethod ref) {
    return new IFilter<IMethod>() {
      @Override
      public boolean evaluate(IMethod candidate) {
        if (JdtUtils.exists(candidate) && candidate.getElementName().equals(ref.getElementName())) {
          String[] candidateParameters = candidate.getParameterTypes();
          String[] methodParameters = ref.getParameterTypes();
          if (methodParameters.length == candidateParameters.length) {
            for (int i = 0; i < candidateParameters.length; i++) {
              String cParam = candidateParameters[i];
              String mParam = methodParameters[i];
              int cArrCount = Signature.getArrayCount(cParam);
              int mArrCount = Signature.getArrayCount(mParam);
              if (cArrCount != mArrCount) {
                return false;
              }
              cParam = cParam.substring(cArrCount);
              mParam = mParam.substring(mArrCount);
              mParam = Signature.getTypeErasure(mParam);
              cParam = Signature.getTypeErasure(cParam);
              if (Signature.getTypeSignatureKind(cParam) == ISignatureConstants.TYPE_VARIABLE_SIGNATURE || Signature.getTypeSignatureKind(mParam) == ISignatureConstants.TYPE_VARIABLE_SIGNATURE) {
                continue;
              }
              else if (!Signature.getSignatureSimpleName(mParam).equals(Signature.getSignatureSimpleName(cParam))) {
                return false;
              }
            }
            return true;
          }
        }
        return false;
      }
    };
  }
}
