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
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
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
  private static final Pattern START_HANDLER_REGEX = Pattern.compile("^start(.*)$");
  private static final Pattern METHOD_INNER_TYPE_GETTER_REGEX = Pattern.compile("^get(.*)$");

  private final IType m_type;
  private final EnumSet<CATEGORIES> m_enabledCategories;
  private final EnumSet<CATEGORIES> m_visitedCategories;
  private final Map<CATEGORIES, List<? extends IJavaElement>> m_elements;
  private final ITypeHierarchy m_typeHierarchy;

  public ScoutStructuredType(IType type, EnumSet<CATEGORIES> enabledCategories) {
    this(type, enabledCategories, null);
  }

  public ScoutStructuredType(IType type, EnumSet<CATEGORIES> enabledCategories, ITypeHierarchy localHierarchy) {
    m_type = type;
    m_enabledCategories = enabledCategories;
    m_visitedCategories = EnumSet.noneOf(CATEGORIES.class);
    m_elements = new HashMap<CATEGORIES, List<? extends IJavaElement>>();
    if (localHierarchy == null) {
      m_typeHierarchy = TypeUtility.getLocalTypeHierarchy(type);
    }
    else {
      m_typeHierarchy = localHierarchy;
    }

    // initially put all into unknown categories
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
    }
    catch (JavaModelException e) {
      ScoutSdk.logError("could not build structured type of '" + type.getFullyQualifiedName() + "'.", e);
    }

    m_elements.put(CATEGORIES.FIELD_UNKNOWN, fields);
    m_elements.put(CATEGORIES.ENUM, enums);
    m_elements.put(CATEGORIES.METHOD_UNCATEGORIZED, methods);
    m_elements.put(CATEGORIES.TYPE_UNCATEGORIZED, types);
  }

  public IType getType() {
    return m_type;
  }

  protected List<? extends IJavaElement> getElementsInternal(CATEGORIES category) {
    cache(category);
    return m_elements.get(category);
  }

  @Override
  public List<IJavaElement> getElements(CATEGORIES category) {
    return CollectionUtility.arrayList(getElementsInternal(category));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends IJavaElement> List<T> getElements(CATEGORIES category, Class<T> clazz) {
    List<? extends IJavaElement> elements = getElementsInternal(category);
    if (elements == null) {
      return CollectionUtility.arrayList();
    }

    List<T> result = new ArrayList<T>(elements.size());
    for (IJavaElement e : elements) {
      result.add((T) e);
    }
    return result;
  }

  @Override
  public IJavaElement getSiblingMethodConfigGetConfigured(String methodName) {
    List<? extends IJavaElement> configProperties = getElementsInternal(CATEGORIES.METHOD_CONFIG_PROPERTY);
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
    List<? extends IJavaElement> references = getElementsInternal(CATEGORIES.METHOD_CONFIG_EXEC);
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
    List<? extends IJavaElement> references = getElementsInternal(CATEGORIES.METHOD_INNER_TYPE_GETTER);
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
    List<? extends IJavaElement> references = getElementsInternal(CATEGORIES.METHOD_START_HANDLER);
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
    List<? extends IJavaElement> types = getElementsInternal(CATEGORIES.TYPE_KEYSTROKE);
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
    List<? extends IJavaElement> attributes = getElementsInternal(CATEGORIES.TYPE_COMPOSER_ATTRIBUTE);
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
    List<? extends IJavaElement> entities = getElementsInternal(CATEGORIES.TYPE_COMPOSER_ENTRY);
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
    List<? extends IJavaElement> formHandlers = getElementsInternal(CATEGORIES.TYPE_FORM_HANDLER);
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
        List<? extends IJavaElement> elements = getElementsInternal(methodCategories[i]);
        if (CollectionUtility.hasElements(elements)) {
          return CollectionUtility.firstElement(elements);
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
        ArrayList<IJavaElement> unknownMethods = CollectionUtility.arrayList(m_elements.get(CATEGORIES.METHOD_UNCATEGORIZED));
        ArrayList<IJavaElement> unknownTypes = CollectionUtility.arrayList(m_elements.get(CATEGORIES.TYPE_UNCATEGORIZED));
        switch (category) {
          case FIELD_LOGGER:
          case FIELD_STATIC:
          case FIELD_MEMBER:
            visitFields(CollectionUtility.arrayList(m_elements.get(CATEGORIES.FIELD_UNKNOWN)));
            m_visitedCategories.add(CATEGORIES.FIELD_LOGGER);
            m_visitedCategories.add(CATEGORIES.FIELD_STATIC);
            m_visitedCategories.add(CATEGORIES.FIELD_MEMBER);
            break;
          case METHOD_CONSTRUCTOR:
            visitMethodConstructors(unknownMethods);
            m_visitedCategories.add(CATEGORIES.METHOD_CONSTRUCTOR);
            m_elements.put(CATEGORIES.METHOD_UNCATEGORIZED, unknownMethods);
            break;
          case METHOD_CONFIG_PROPERTY:
            visitMethodConfigProperty(unknownMethods, m_typeHierarchy);
            m_visitedCategories.add(CATEGORIES.METHOD_CONFIG_PROPERTY);
            m_elements.put(CATEGORIES.METHOD_UNCATEGORIZED, unknownMethods);
            break;
          case METHOD_CONFIG_EXEC:
            visitMethodConfigExec(unknownMethods, m_typeHierarchy);
            m_visitedCategories.add(CATEGORIES.METHOD_CONFIG_EXEC);
            m_elements.put(CATEGORIES.METHOD_UNCATEGORIZED, unknownMethods);
            break;
          case METHOD_FORM_DATA_BEAN:
            m_visitedCategories.add(CATEGORIES.METHOD_FORM_DATA_BEAN);
            visitMethodFormDataBean(unknownMethods);
            m_elements.put(CATEGORIES.METHOD_UNCATEGORIZED, unknownMethods);
            break;
          case METHOD_OVERRIDDEN:
            visitMethodOverridden(unknownMethods, m_typeHierarchy);
            m_visitedCategories.add(CATEGORIES.METHOD_OVERRIDDEN);
            m_elements.put(CATEGORIES.METHOD_UNCATEGORIZED, unknownMethods);
            break;
          case METHOD_START_HANDLER:
            visitMethodStartHandler(unknownMethods);
            m_visitedCategories.add(CATEGORIES.METHOD_START_HANDLER);
            m_elements.put(CATEGORIES.METHOD_UNCATEGORIZED, unknownMethods);
            break;
          case METHOD_INNER_TYPE_GETTER:
            visitMethodInnerTypeGetter(unknownMethods);
            m_visitedCategories.add(CATEGORIES.METHOD_INNER_TYPE_GETTER);
            m_elements.put(CATEGORIES.METHOD_UNCATEGORIZED, unknownMethods);
            break;
          case METHOD_LOCAL_BEAN:
            visitMethodLocalBean(unknownMethods);
            m_visitedCategories.add(CATEGORIES.METHOD_LOCAL_BEAN);
            m_elements.put(CATEGORIES.METHOD_UNCATEGORIZED, unknownMethods);
            break;
          case TYPE_FORM_FIELD:
            visitTypeFormFields(unknownTypes);
            m_visitedCategories.add(CATEGORIES.TYPE_FORM_FIELD);
            m_elements.put(CATEGORIES.TYPE_UNCATEGORIZED, unknownTypes);
            break;
          case TYPE_COLUMN:
            visitTypeColumns(unknownTypes);
            m_visitedCategories.add(CATEGORIES.TYPE_COLUMN);
            m_elements.put(CATEGORIES.TYPE_UNCATEGORIZED, unknownTypes);
            break;
          case TYPE_CODE:
            visitTypeCodes(unknownTypes);
            m_visitedCategories.add(CATEGORIES.TYPE_CODE);
            m_elements.put(CATEGORIES.TYPE_UNCATEGORIZED, unknownTypes);
            break;
          case TYPE_FORM:
            visitTypeForms(unknownTypes);
            m_visitedCategories.add(CATEGORIES.TYPE_FORM);
            m_elements.put(CATEGORIES.TYPE_UNCATEGORIZED, unknownTypes);
            break;
          case TYPE_TABLE:
            visitTypeTables(unknownTypes);
            m_visitedCategories.add(CATEGORIES.TYPE_TABLE);
            m_elements.put(CATEGORIES.TYPE_UNCATEGORIZED, unknownTypes);
            break;
          case TYPE_ACTIVITY_MAP:
            visitTypeActivityMaps(unknownTypes);
            m_visitedCategories.add(CATEGORIES.TYPE_ACTIVITY_MAP);
            m_elements.put(CATEGORIES.TYPE_UNCATEGORIZED, unknownTypes);
            break;
          case TYPE_TREE:
            visitTypeTrees(unknownTypes);
            m_visitedCategories.add(CATEGORIES.TYPE_TREE);
            m_elements.put(CATEGORIES.TYPE_UNCATEGORIZED, unknownTypes);
            break;
          case TYPE_CALENDAR:
            visitTypeCalendar(unknownTypes);
            m_visitedCategories.add(CATEGORIES.TYPE_CALENDAR);
            m_elements.put(CATEGORIES.TYPE_UNCATEGORIZED, unknownTypes);
            break;
          case TYPE_CALENDAR_ITEM_PROVIDER:
            visitTypeCalendarItemProvider(unknownTypes);
            m_visitedCategories.add(CATEGORIES.TYPE_CALENDAR_ITEM_PROVIDER);
            m_elements.put(CATEGORIES.TYPE_UNCATEGORIZED, unknownTypes);
            break;
          case TYPE_WIZARD:
            visitTypeWizards(unknownTypes);
            m_visitedCategories.add(CATEGORIES.TYPE_WIZARD);
            m_elements.put(CATEGORIES.TYPE_UNCATEGORIZED, unknownTypes);
            break;
          case TYPE_WIZARD_STEP:
            visitTypeWizardSteps(unknownTypes);
            m_visitedCategories.add(CATEGORIES.TYPE_WIZARD_STEP);
            m_elements.put(CATEGORIES.TYPE_UNCATEGORIZED, unknownTypes);
            break;
          case TYPE_MENU:
            visitTypeMenus(unknownTypes);
            m_visitedCategories.add(CATEGORIES.TYPE_MENU);
            m_elements.put(CATEGORIES.TYPE_UNCATEGORIZED, unknownTypes);
            break;
          case TYPE_VIEW_BUTTON:
            visitTypeViewbuttons(unknownTypes);
            m_visitedCategories.add(CATEGORIES.TYPE_VIEW_BUTTON);
            m_elements.put(CATEGORIES.TYPE_UNCATEGORIZED, unknownTypes);
            break;
          case TYPE_TOOL_BUTTON:
            visitTypeToolbuttons(unknownTypes);
            m_visitedCategories.add(CATEGORIES.TYPE_TOOL_BUTTON);
            m_elements.put(CATEGORIES.TYPE_UNCATEGORIZED, unknownTypes);
            break;
          case TYPE_KEYSTROKE:
            visitTypeKeystrokes(unknownTypes);
            m_visitedCategories.add(CATEGORIES.TYPE_KEYSTROKE);
            m_elements.put(CATEGORIES.TYPE_UNCATEGORIZED, unknownTypes);
            break;
          case TYPE_COMPOSER_ATTRIBUTE:
            visitTypeComposerAttribute(unknownTypes);
            m_visitedCategories.add(CATEGORIES.TYPE_COMPOSER_ATTRIBUTE);
            m_elements.put(CATEGORIES.TYPE_UNCATEGORIZED, unknownTypes);
            break;
          case TYPE_COMPOSER_ENTRY:
            visitTypeDataModelEntry(unknownTypes);
            m_visitedCategories.add(CATEGORIES.TYPE_COMPOSER_ENTRY);
            m_elements.put(CATEGORIES.TYPE_UNCATEGORIZED, unknownTypes);
            break;
          case TYPE_FORM_HANDLER:
            visitTypeFormHandlers(unknownTypes);
            m_visitedCategories.add(CATEGORIES.TYPE_FORM_HANDLER);
            m_elements.put(CATEGORIES.TYPE_UNCATEGORIZED, unknownTypes);
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
    List<IJavaElement> loggers = new ArrayList<IJavaElement>(2);
    List<IJavaElement> statics = new ArrayList<IJavaElement>();
    List<IJavaElement> members = new ArrayList<IJavaElement>();

    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IField f = (IField) it.next();
      // static
      if ((f.getFlags() & Flags.AccStatic) != 0) {
        String fqn = getFullyQualifiedTypeName(f.getTypeSignature(), getType());
        if (IRuntimeClasses.IScoutLogger.equals(fqn)) {
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
    m_elements.put(CATEGORIES.FIELD_LOGGER, loggers);
    m_elements.put(CATEGORIES.FIELD_STATIC, statics);
    m_elements.put(CATEGORIES.FIELD_MEMBER, members);
    m_elements.put(CATEGORIES.FIELD_UNKNOWN, workingSet);
  }

  private static String getFullyQualifiedTypeName(String signature, IType jdtType) throws JavaModelException {
    if (Signature.getTypeArguments(signature).length > 0) {
      signature = Signature.getTypeErasure(signature);
    }
    if (SignatureUtility.getTypeSignatureKind(signature) == Signature.BASE_TYPE_SIGNATURE) {
      return Signature.getSignatureSimpleName(signature);
    }
    if (SignatureUtility.isUnresolved(signature)) {
      String simpleName = Signature.getSignatureSimpleName(signature);
      return TypeUtility.getReferencedTypeFqn(jdtType, simpleName, false);
    }
    return null;
  }

  protected void visitMethodConstructors(ArrayList<IJavaElement> workingSet) throws JavaModelException {
    TreeMap<CompositeObject, IJavaElement> constructors = new TreeMap<CompositeObject, IJavaElement>();
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IMethod method = (IMethod) it.next();
      if (method.isConstructor()) {
        CompositeObject key = createConstructorKey(method.getParameterTypes());
        constructors.put(key, method);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.METHOD_CONSTRUCTOR, CollectionUtility.arrayList(constructors.values()));
  }

  protected void visitMethodConfigExec(ArrayList<IJavaElement> workingSet, ITypeHierarchy superTypeHierarchy) throws CoreException {
    TreeMap<CompositeObject, IJavaElement> execMethods = new TreeMap<CompositeObject, IJavaElement>();
    IMethodFilter execMethodFilter = MethodFilters.getFilterWithAnnotation(TypeUtility.getType(IRuntimeClasses.ConfigOperation));
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
        visitedmethod = TypeUtility.getOverwrittenMethod(visitedmethod, superTypeHierarchy);

      }
    }
    m_elements.put(CATEGORIES.METHOD_CONFIG_EXEC, CollectionUtility.arrayList(execMethods.values()));
  }

  protected void visitMethodConfigProperty(ArrayList<IJavaElement> workingSet, ITypeHierarchy superTypeHierarchy) throws CoreException {
    TreeMap<CompositeObject, IJavaElement> methods = new TreeMap<CompositeObject, IJavaElement>();
    IMethodFilter configPropertyMethodFilter = MethodFilters.getFilterWithAnnotation(TypeUtility.getType(IRuntimeClasses.ConfigProperty));
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
        visitedmethod = TypeUtility.getOverwrittenMethod(visitedmethod, superTypeHierarchy);
      }
    }
    m_elements.put(CATEGORIES.METHOD_CONFIG_PROPERTY, CollectionUtility.arrayList(methods.values()));
  }

  protected void visitMethodFormDataBean(ArrayList<IJavaElement> workingSet) throws JavaModelException {
    TreeMap<CompositeObject, IJavaElement> methods = new TreeMap<CompositeObject, IJavaElement>();
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IMethod method = (IMethod) it.next();
      if (JdtUtility.hasAnnotation(method, IRuntimeClasses.FormData)) {
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
    m_elements.put(CATEGORIES.METHOD_FORM_DATA_BEAN, CollectionUtility.arrayList(methods.values()));
  }

  protected void visitMethodOverridden(ArrayList<IJavaElement> workingSet, ITypeHierarchy superTypeHierarchy) throws JavaModelException {
    TreeMap<CompositeObject, IJavaElement> overriddenMethods = new TreeMap<CompositeObject, IJavaElement>();
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IMethod method = (IMethod) it.next();
      if (TypeUtility.getOverwrittenMethod(method, superTypeHierarchy) != null) {
        CompositeObject key = new CompositeObject(method.getElementName(), method.getParameterNames().length, method);
        overriddenMethods.put(key, method);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.METHOD_OVERRIDDEN, CollectionUtility.arrayList(overriddenMethods.values()));
  }

  protected void visitMethodStartHandler(ArrayList<IJavaElement> workingSet) throws JavaModelException {
    TreeMap<CompositeObject, IJavaElement> startHandlerMethods = new TreeMap<CompositeObject, IJavaElement>();
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IMethod method = (IMethod) it.next();
      Matcher matcher = START_HANDLER_REGEX.matcher(method.getElementName());
      if (matcher.find()) {
        String fieldName = matcher.group(1);
        if (TypeUtility.findInnerType(getType(), fieldName + SdkProperties.SUFFIX_FORM_HANDLER) != null) {
          CompositeObject key = new CompositeObject(method.getElementName(), method.getParameterNames().length, method);
          startHandlerMethods.put(key, method);
          it.remove();
        }
      }
    }
    m_elements.put(CATEGORIES.METHOD_START_HANDLER, CollectionUtility.arrayList(startHandlerMethods.values()));
  }

  protected void visitMethodInnerTypeGetter(ArrayList<IJavaElement> workingSet) throws JavaModelException {
    TreeMap<CompositeObject, IJavaElement> fieldGetterMethods = new TreeMap<CompositeObject, IJavaElement>();
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IMethod method = (IMethod) it.next();
      Matcher matcher = METHOD_INNER_TYPE_GETTER_REGEX.matcher(method.getElementName());
      if (matcher.find()) {
        String fieldName = matcher.group(1);
        if (TypeUtility.findInnerType(getType(), fieldName) != null) {
          CompositeObject key = new CompositeObject(method.getElementName(), method.getParameterNames().length, method);
          fieldGetterMethods.put(key, method);
          it.remove();
        }
      }
    }
    m_elements.put(CATEGORIES.METHOD_INNER_TYPE_GETTER, CollectionUtility.arrayList(fieldGetterMethods.values()));
  }

  protected void visitMethodLocalBean(ArrayList<IJavaElement> workingSet) throws JavaModelException {
    TreeMap<CompositeObject, IJavaElement> localPropertyMethods = new TreeMap<CompositeObject, IJavaElement>();
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IMethod method = (IMethod) it.next();
      CompositeObject key = createPropertyMethodKey(method);
      if (key != null) {

        localPropertyMethods.put(key, method);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.METHOD_LOCAL_BEAN, CollectionUtility.arrayList(localPropertyMethods.values()));
  }

  protected void visitMethodUncategorized(ArrayList<IMethod> workingSet) throws JavaModelException {
    TreeMap<CompositeObject, IJavaElement> methods = new TreeMap<CompositeObject, IJavaElement>();
    for (Iterator<IMethod> it = workingSet.iterator(); it.hasNext();) {
      IMethod method = it.next();
      CompositeObject key = new CompositeObject(method.getElementName(), method.getParameterNames().length, method);
      methods.put(key, method);
      it.remove();
    }
    m_elements.put(CATEGORIES.METHOD_UNCATEGORIZED, CollectionUtility.arrayList(methods.values()));

  }

  protected void visitTypeFormFields(ArrayList<IJavaElement> workingSet) {
    TreeSet<IType> formFields = new TreeSet<IType>(ScoutTypeComparators.getOrderAnnotationComparator());
    ITypeFilter filter = TypeFilters.getMultiTypeFilterAnd(TypeFilters.getClassFilter(), TypeFilters.getSubtypeFilter(TypeUtility.getType(IRuntimeClasses.IFormField), m_typeHierarchy));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.accept(candidate)) {
        formFields.add(candidate);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.TYPE_FORM_FIELD, CollectionUtility.arrayList(formFields));
  }

  protected void visitTypeColumns(ArrayList<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<IType>(ScoutTypeComparators.getOrderAnnotationComparator());
    ITypeFilter filter = TypeFilters.getMultiTypeFilterAnd(TypeFilters.getClassFilter(), TypeFilters.getSubtypeFilter(TypeUtility.getType(IRuntimeClasses.IColumn), m_typeHierarchy));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.accept(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.TYPE_COLUMN, CollectionUtility.arrayList(types));
  }

  protected void visitTypeCodes(ArrayList<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<IType>(ScoutTypeComparators.getOrderAnnotationComparator());
    ITypeFilter filter = TypeFilters.getMultiTypeFilterAnd(TypeFilters.getClassFilter(), TypeFilters.getSubtypeFilter(TypeUtility.getType(IRuntimeClasses.ICode), m_typeHierarchy));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.accept(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.TYPE_CODE, CollectionUtility.arrayList(types));
  }

  protected void visitTypeForms(ArrayList<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<IType>(ScoutTypeComparators.getOrderAnnotationComparator());
    ITypeFilter filter = TypeFilters.getMultiTypeFilterAnd(TypeFilters.getClassFilter(), TypeFilters.getSubtypeFilter(TypeUtility.getType(IRuntimeClasses.IForm), m_typeHierarchy));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.accept(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.TYPE_FORM, CollectionUtility.arrayList(types));
  }

  protected void visitTypeTables(ArrayList<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<IType>(ScoutTypeComparators.getOrderAnnotationComparator());
    ITypeFilter filter = TypeFilters.getMultiTypeFilterAnd(TypeFilters.getClassFilter(), TypeFilters.getSubtypeFilter(TypeUtility.getType(IRuntimeClasses.ITable), m_typeHierarchy));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.accept(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.TYPE_TABLE, CollectionUtility.arrayList(types));
  }

  /**
   * @param workingSet
   */
  private void visitTypeActivityMaps(ArrayList<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<IType>(TypeComparators.getTypeNameComparator());
    ITypeFilter filter = TypeFilters.getMultiTypeFilterAnd(TypeFilters.getClassFilter(), TypeFilters.getSubtypeFilter(TypeUtility.getType(IRuntimeClasses.IActivityMap), m_typeHierarchy));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.accept(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.TYPE_ACTIVITY_MAP, CollectionUtility.arrayList(types));
  }

  protected void visitTypeTrees(ArrayList<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<IType>(ScoutTypeComparators.getOrderAnnotationComparator());
    ITypeFilter filter = TypeFilters.getMultiTypeFilterAnd(TypeFilters.getClassFilter(), TypeFilters.getSubtypeFilter(TypeUtility.getType(IRuntimeClasses.ITree), m_typeHierarchy));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.accept(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.TYPE_TREE, CollectionUtility.arrayList(types));
  }

  protected void visitTypeCalendar(ArrayList<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<IType>(ScoutTypeComparators.getOrderAnnotationComparator());
    ITypeFilter filter = TypeFilters.getMultiTypeFilterAnd(TypeFilters.getClassFilter(), TypeFilters.getSubtypeFilter(TypeUtility.getType(IRuntimeClasses.ICalendar), m_typeHierarchy));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.accept(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.TYPE_CALENDAR, CollectionUtility.arrayList(types));
  }

  protected void visitTypeCalendarItemProvider(ArrayList<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<IType>(ScoutTypeComparators.getOrderAnnotationComparator());
    ITypeFilter filter = TypeFilters.getMultiTypeFilterAnd(TypeFilters.getClassFilter(), TypeFilters.getSubtypeFilter(TypeUtility.getType(IRuntimeClasses.ICalendarItemProvider), m_typeHierarchy));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.accept(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.TYPE_CALENDAR_ITEM_PROVIDER, CollectionUtility.arrayList(types));
  }

  protected void visitTypeWizards(ArrayList<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<IType>(ScoutTypeComparators.getOrderAnnotationComparator());
    ITypeFilter filter = TypeFilters.getMultiTypeFilterAnd(TypeFilters.getClassFilter(), TypeFilters.getSubtypeFilter(TypeUtility.getType(IRuntimeClasses.IWizard), m_typeHierarchy));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.accept(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.TYPE_WIZARD, CollectionUtility.arrayList(types));
  }

  protected void visitTypeWizardSteps(ArrayList<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<IType>(ScoutTypeComparators.getOrderAnnotationComparator());
    ITypeFilter filter = TypeFilters.getMultiTypeFilterAnd(TypeFilters.getClassFilter(), TypeFilters.getSubtypeFilter(TypeUtility.getType(IRuntimeClasses.IWizardStep), m_typeHierarchy));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.accept(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.TYPE_WIZARD_STEP, CollectionUtility.arrayList(types));
  }

  protected void visitTypeMenus(ArrayList<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<IType>(ScoutTypeComparators.getOrderAnnotationComparator());
    ITypeFilter filter = TypeFilters.getMultiTypeFilterAnd(TypeFilters.getClassFilter(), TypeFilters.getSubtypeFilter(TypeUtility.getType(IRuntimeClasses.IMenu), m_typeHierarchy));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.accept(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.TYPE_MENU, CollectionUtility.arrayList(types));
  }

  protected void visitTypeViewbuttons(ArrayList<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<IType>(ScoutTypeComparators.getOrderAnnotationComparator());
    ITypeFilter filter = TypeFilters.getMultiTypeFilterAnd(TypeFilters.getClassFilter(), TypeFilters.getSubtypeFilter(TypeUtility.getType(IRuntimeClasses.IViewButton), m_typeHierarchy));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.accept(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.TYPE_VIEW_BUTTON, CollectionUtility.arrayList(types));
  }

  protected void visitTypeToolbuttons(ArrayList<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<IType>(ScoutTypeComparators.getOrderAnnotationComparator());
    ITypeFilter filter = TypeFilters.getMultiTypeFilterAnd(TypeFilters.getClassFilter(), TypeFilters.getSubtypeFilter(TypeUtility.getType(IRuntimeClasses.IToolButton), m_typeHierarchy));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.accept(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.TYPE_TOOL_BUTTON, CollectionUtility.arrayList(types));
  }

  protected void visitTypeKeystrokes(ArrayList<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<IType>(TypeComparators.getTypeNameComparator());
    ITypeFilter filter = TypeFilters.getMultiTypeFilterAnd(TypeFilters.getClassFilter(), TypeFilters.getSubtypeFilter(TypeUtility.getType(IRuntimeClasses.IKeyStroke), m_typeHierarchy));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.accept(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.TYPE_KEYSTROKE, CollectionUtility.arrayList(types));
  }

  protected void visitTypeComposerAttribute(ArrayList<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<IType>(TypeComparators.getTypeNameComparator());
    @SuppressWarnings("deprecation")
    ITypeFilter filter = TypeFilters.getMultiTypeFilterAnd(TypeFilters.getClassFilter(), TypeFilters.getSubtypeFilter(TypeUtility.getType(IRuntimeClasses.IComposerAttribute), m_typeHierarchy));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.accept(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.TYPE_COMPOSER_ATTRIBUTE, CollectionUtility.arrayList(types));
  }

  protected void visitTypeDataModelEntry(ArrayList<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<IType>(TypeComparators.getTypeNameComparator());
    ITypeFilter filter = TypeFilters.getMultiTypeFilterAnd(TypeFilters.getClassFilter(), TypeFilters.getSubtypeFilter(TypeUtility.getType(IRuntimeClasses.IDataModelEntity), m_typeHierarchy));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.accept(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.TYPE_COMPOSER_ENTRY, CollectionUtility.arrayList(types));
  }

  protected void visitTypeFormHandlers(ArrayList<IJavaElement> workingSet) {
    TreeSet<IType> types = new TreeSet<IType>(TypeComparators.getTypeNameComparator());
    ITypeFilter filter = TypeFilters.getMultiTypeFilterAnd(TypeFilters.getClassFilter(), TypeFilters.getSubtypeFilter(TypeUtility.getType(IRuntimeClasses.IFormHandler), m_typeHierarchy));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.accept(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(CATEGORIES.TYPE_FORM_HANDLER, CollectionUtility.arrayList(types));
  }

  private static CompositeObject createPropertyMethodKey(IMethod method) {
    if (TypeUtility.exists(method)) {
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
