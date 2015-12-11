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
package org.eclipse.scout.sdk.core.s.structured;

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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.s.model.ScoutTypeComparators;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.util.CompositeObject;
import org.eclipse.scout.sdk.core.util.Filters;
import org.eclipse.scout.sdk.core.util.IFilter;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.core.util.TypeFilters;

public class StructuredType implements IStructuredType {

  private static final Pattern PROPERTY_BEAN_REGEX = Pattern.compile("^(get|set|is|add|remove|clear|delete)(.*)$");
  private static final Pattern START_HANDLER_REGEX = Pattern.compile("^start(.*)$");
  private static final Pattern METHOD_INNER_TYPE_GETTER_REGEX = Pattern.compile("^get(.*)$");

  private static final IFilter<IType> CLASS_FILTER = new IFilter<IType>() {
    @Override
    public boolean evaluate(IType type) {
      if (StringUtils.isBlank(type.elementName())) {
        return false; // anonymous type
      }
      int flags = type.flags();
      return !Flags.isAbstract(flags) && !Flags.isInterface(flags) && !Flags.isDeprecated(flags);
    }
  };

  private final IType m_type;
  private final EnumSet<Categories> m_enabledCategories;
  private final EnumSet<Categories> m_visitedCategories;
  private final Map<Categories, List<? extends IJavaElement>> m_elements;

  public StructuredType(IType type, EnumSet<Categories> enabledCategories) {
    m_type = type;
    m_enabledCategories = enabledCategories;
    m_visitedCategories = EnumSet.noneOf(Categories.class);
    m_elements = new HashMap<>();

    // initially put all into unknown categories
    List<IField> fields = type.fields().list();
    List<IType> enums = type.innerTypes().withFlags(Flags.AccEnum).list();
    List<IMethod> methods = type.methods().list();
    List<IType> types = type.innerTypes().withFilter(new IFilter<IType>() {
      @Override
      public boolean evaluate(IType element) {
        return !Flags.isEnum(element.flags());
      }
    }).list();

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
    return getSibling(methodName, Categories.METHOD_CONFIG_PROPERTY);
  }

  @Override
  public IJavaElement getSiblingMethodConfigExec(String methodName) {
    return getSibling(methodName, Categories.METHOD_CONFIG_EXEC);
  }

  @Override
  public IJavaElement getSiblingMethodFieldGetter(String methodName) {
    return getSibling(methodName, Categories.METHOD_INNER_TYPE_GETTER);
  }

  @Override
  public IJavaElement getSiblingMethodStartHandler(String methodName) {
    return getSibling(methodName, Categories.METHOD_START_HANDLER);
  }

  @Override
  public IJavaElement getSiblingTypeKeyStroke(String keyStrokeName) {
    return getSibling(keyStrokeName, Categories.TYPE_KEYSTROKE);
  }

  @Override
  public IJavaElement getSiblingComposerAttribute(String attributeName) {
    return getSibling(attributeName, Categories.TYPE_COMPOSER_ATTRIBUTE);
  }

  @Override
  public IJavaElement getSiblingComposerEntity(String entityName) {
    return getSibling(entityName, Categories.TYPE_COMPOSER_ENTRY);
  }

  @Override
  public IJavaElement getSiblingTypeFormHandler(String formHandlerName) {
    return getSibling(formHandlerName, Categories.TYPE_FORM_HANDLER, Categories.TYPE_UNCATEGORIZED);
  }

  protected IJavaElement getSibling(String siblingName, Categories... categories) {
    for (Categories cat : categories) {
      List<? extends IJavaElement> references = getElementsInternal(cat);
      if (references != null && !references.isEmpty()) {
        for (IJavaElement reference : references) {
          if (reference.elementName().compareTo(siblingName) > 0) {
            return reference;
          }
        }
        return references.get(references.size() - 1);
      }
      IJavaElement sibling = getSibling(cat);
      if (sibling != null) {
        return sibling;
      }
    }

    return null;
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
      else if (methodCategories[i].equals(category)) {
        search = true;
      }
    }
    return null;
  }

  protected final void cache(Categories category) {
    if (m_enabledCategories.contains(category) && !m_visitedCategories.contains(category)) {
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
          visitMethodConfigProperty(unknownMethods);
          m_visitedCategories.add(Categories.METHOD_CONFIG_PROPERTY);
          m_elements.put(Categories.METHOD_UNCATEGORIZED, unknownMethods);
          break;
        case METHOD_CONFIG_EXEC:
          visitMethodConfigExec(unknownMethods);
          m_visitedCategories.add(Categories.METHOD_CONFIG_EXEC);
          m_elements.put(Categories.METHOD_UNCATEGORIZED, unknownMethods);
          break;
        case METHOD_FORM_DATA_BEAN:
          m_visitedCategories.add(Categories.METHOD_FORM_DATA_BEAN);
          visitMethodFormDataBean(unknownMethods);
          m_elements.put(Categories.METHOD_UNCATEGORIZED, unknownMethods);
          break;
        case METHOD_OVERRIDDEN:
          visitMethodOverridden(unknownMethods);
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
  }

  /**
   * can be overwritten. Overwrites must ensure to super call after processing the working set.
   *
   * @param workingSet
   */
  protected void visitFields(List<IJavaElement> workingSet) {
    List<IJavaElement> loggers = new ArrayList<>(2);
    List<IJavaElement> statics = new ArrayList<>();
    List<IJavaElement> members = new ArrayList<>();

    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IField f = (IField) it.next();
      // static
      if ((f.flags() & Flags.AccStatic) != 0) {
        String fieldDataType = Signature.toString(f.dataType().signature());
        if (Signature.getSimpleName(IScoutRuntimeTypes.Logger).equals(fieldDataType) || IScoutRuntimeTypes.Logger.equals(fieldDataType)) {
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

  protected void visitMethodConstructors(List<IJavaElement> workingSet) {
    Map<CompositeObject, IJavaElement> constructors = new TreeMap<>();
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IMethod method = (IMethod) it.next();
      if (method.isConstructor()) {
        CompositeObject key = createConstructorKey(method.parameters().list());
        constructors.put(key, method);
        it.remove();
      }
    }
    m_elements.put(Categories.METHOD_CONSTRUCTOR, new ArrayList<>(constructors.values()));
  }

  protected void visitMethodConfigExec(List<IJavaElement> workingSet) {
    Map<CompositeObject, IJavaElement> execMethods = new TreeMap<>();
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IMethod method = (IMethod) it.next();
      IMethod visitedMethod = method;
      while (visitedMethod != null) {
        if (visitedMethod.annotations().withName(IScoutRuntimeTypes.ConfigOperation).existsAny()) {
          CompositeObject key = new CompositeObject(method.elementName(), method.parameters().list().size(), method);
          execMethods.put(key, method);
          it.remove();
          break;
        }
        visitedMethod = getOverwrittenMethod(visitedMethod);
      }
    }
    m_elements.put(Categories.METHOD_CONFIG_EXEC, new ArrayList<>(execMethods.values()));
  }

  protected void visitMethodConfigProperty(List<IJavaElement> workingSet) {
    Map<CompositeObject, IJavaElement> methods = new TreeMap<>();
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IMethod method = (IMethod) it.next();
      IMethod visitedMethod = method;
      while (visitedMethod != null) {
        if (visitedMethod.annotations().withName(IScoutRuntimeTypes.ConfigProperty).existsAny()) {
          CompositeObject key = new CompositeObject(method.elementName(), method.parameters().list().size(), method);
          methods.put(key, method);
          it.remove();
          break;
        }
        visitedMethod = getOverwrittenMethod(visitedMethod);
      }
    }
    m_elements.put(Categories.METHOD_CONFIG_PROPERTY, new ArrayList<>(methods.values()));
  }

  protected void visitMethodFormDataBean(List<IJavaElement> workingSet) {
    Map<CompositeObject, IJavaElement> methods = new TreeMap<>();
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IMethod method = (IMethod) it.next();
      if (method.annotations().withName(IScoutRuntimeTypes.FormData).existsAny()) {
        CompositeObject methodKey = createPropertyMethodKey(method);
        if (methodKey != null) {
          methods.put(methodKey, method);
          it.remove();
        }
        else {
          SdkLog.warning("could not parse property method '" + method.elementName() + "'.");
        }
      }
    }
    m_elements.put(Categories.METHOD_FORM_DATA_BEAN, new ArrayList<>(methods.values()));
  }

  protected void visitMethodOverridden(List<IJavaElement> workingSet) {
    Map<CompositeObject, IJavaElement> overriddenMethods = new TreeMap<>();
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IMethod method = (IMethod) it.next();
      if (getOverwrittenMethod(method) != null) {
        CompositeObject key = new CompositeObject(method.elementName(), method.parameters().list().size(), method);
        overriddenMethods.put(key, method);
        it.remove();
      }
    }
    m_elements.put(Categories.METHOD_OVERRIDDEN, new ArrayList<>(overriddenMethods.values()));
  }

  protected void visitMethodStartHandler(List<IJavaElement> workingSet) {
    Map<CompositeObject, IJavaElement> startHandlerMethods = new TreeMap<>();
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IMethod method = (IMethod) it.next();
      Matcher matcher = START_HANDLER_REGEX.matcher(method.elementName());
      if (matcher.find()) {
        String fieldName = matcher.group(1);
        if (getType().innerTypes().withRecursiveInnerTypes(true).withSimpleName(fieldName + ISdkProperties.SUFFIX_FORM_HANDLER).existsAny()) {
          CompositeObject key = new CompositeObject(method.elementName(), method.parameters().list().size(), method);
          startHandlerMethods.put(key, method);
          it.remove();
        }
      }
    }
    m_elements.put(Categories.METHOD_START_HANDLER, new ArrayList<>(startHandlerMethods.values()));
  }

  protected void visitMethodInnerTypeGetter(List<IJavaElement> workingSet) {
    Map<CompositeObject, IJavaElement> fieldGetterMethods = new TreeMap<>();
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IMethod method = (IMethod) it.next();
      Matcher matcher = METHOD_INNER_TYPE_GETTER_REGEX.matcher(method.elementName());
      if (matcher.find()) {
        String fieldName = matcher.group(1);
        if (getType().innerTypes().withRecursiveInnerTypes(true).withSimpleName(fieldName).existsAny()) {
          CompositeObject key = new CompositeObject(method.elementName(), method.parameters().list().size(), method);
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

  protected void visitMethodUncategorized(ArrayList<IMethod> workingSet) {
    Map<CompositeObject, IJavaElement> methods = new TreeMap<>();
    for (Iterator<IMethod> it = workingSet.iterator(); it.hasNext();) {
      IMethod method = it.next();
      CompositeObject key = new CompositeObject(method.elementName(), method.parameters().list().size(), method);
      methods.put(key, method);
      it.remove();
    }
    m_elements.put(Categories.METHOD_UNCATEGORIZED, new ArrayList<>(methods.values()));

  }

  protected void visitTypeFormFields(List<IJavaElement> workingSet) {
    Set<IType> formFields = new TreeSet<>(ScoutTypeComparators.getOrderAnnotationComparator(false));
    IFilter<IType> filter = Filters.and(CLASS_FILTER, TypeFilters.instanceOf(IScoutRuntimeTypes.IFormField));
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
    Set<IType> types = new TreeSet<>(ScoutTypeComparators.getOrderAnnotationComparator(false));
    IFilter<IType> filter = Filters.and(CLASS_FILTER, TypeFilters.instanceOf(IScoutRuntimeTypes.IColumn));
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
    Set<IType> types = new TreeSet<>(ScoutTypeComparators.getOrderAnnotationComparator(false));
    IFilter<IType> filter = Filters.and(CLASS_FILTER, TypeFilters.instanceOf(IScoutRuntimeTypes.ICode));
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
    Set<IType> types = new TreeSet<>(ScoutTypeComparators.getOrderAnnotationComparator(false));
    IFilter<IType> filter = Filters.and(CLASS_FILTER, TypeFilters.instanceOf(IScoutRuntimeTypes.IForm));
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
    Set<IType> types = new TreeSet<>(ScoutTypeComparators.getOrderAnnotationComparator(false));
    IFilter<IType> filter = Filters.and(CLASS_FILTER, TypeFilters.instanceOf(IScoutRuntimeTypes.ITable));
    for (Iterator<IJavaElement> it = workingSet.iterator(); it.hasNext();) {
      IType candidate = (IType) it.next();
      if (filter.evaluate(candidate)) {
        types.add(candidate);
        it.remove();
      }
    }
    m_elements.put(Categories.TYPE_TABLE, new ArrayList<>(types));
  }

  protected void visitTypeTrees(List<IJavaElement> workingSet) {
    Set<IType> types = new TreeSet<>(ScoutTypeComparators.getOrderAnnotationComparator(false));
    IFilter<IType> filter = Filters.and(CLASS_FILTER, TypeFilters.instanceOf(IScoutRuntimeTypes.ITree));
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
    Set<IType> types = new TreeSet<>(ScoutTypeComparators.getOrderAnnotationComparator(false));
    IFilter<IType> filter = Filters.and(CLASS_FILTER, TypeFilters.instanceOf(IScoutRuntimeTypes.ICalendar));
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
    Set<IType> types = new TreeSet<>(ScoutTypeComparators.getOrderAnnotationComparator(false));
    IFilter<IType> filter = Filters.and(CLASS_FILTER, TypeFilters.instanceOf(IScoutRuntimeTypes.ICalendarItemProvider));
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
    Set<IType> types = new TreeSet<>(ScoutTypeComparators.getOrderAnnotationComparator(false));
    IFilter<IType> filter = Filters.and(CLASS_FILTER, TypeFilters.instanceOf(IScoutRuntimeTypes.IWizard));
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
    Set<IType> types = new TreeSet<>(ScoutTypeComparators.getOrderAnnotationComparator(false));
    IFilter<IType> filter = Filters.and(CLASS_FILTER, TypeFilters.instanceOf(IScoutRuntimeTypes.IWizardStep));
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
    Set<IType> types = new TreeSet<>(ScoutTypeComparators.getOrderAnnotationComparator(false));
    IFilter<IType> filter = Filters.and(CLASS_FILTER, TypeFilters.instanceOf(IScoutRuntimeTypes.IMenu));
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
    Set<IType> types = new TreeSet<>(ScoutTypeComparators.getOrderAnnotationComparator(false));
    IFilter<IType> filter = Filters.and(CLASS_FILTER, TypeFilters.instanceOf(IScoutRuntimeTypes.IViewButton));
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
    Set<IType> types = new TreeSet<>(ScoutTypeComparators.getOrderAnnotationComparator(false));
    IFilter<IType> filter = Filters.and(CLASS_FILTER, TypeFilters.instanceOf(IScoutRuntimeTypes.IToolButton));
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
    TreeSet<IType> types = new TreeSet<>(ScoutTypeComparators.getTypeNameComparator());
    IFilter<IType> filter = Filters.and(CLASS_FILTER, TypeFilters.instanceOf(IScoutRuntimeTypes.IKeyStroke));
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
    TreeSet<IType> types = new TreeSet<>(ScoutTypeComparators.getTypeNameComparator());
    IFilter<IType> filter = Filters.and(CLASS_FILTER, TypeFilters.instanceOf(IScoutRuntimeTypes.IDataModelAttribute));
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
    TreeSet<IType> types = new TreeSet<>(ScoutTypeComparators.getTypeNameComparator());
    IFilter<IType> filter = Filters.and(CLASS_FILTER, TypeFilters.instanceOf(IScoutRuntimeTypes.IDataModelEntity));
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
    TreeSet<IType> types = new TreeSet<>(ScoutTypeComparators.getTypeNameComparator());
    IFilter<IType> filter = Filters.and(CLASS_FILTER, TypeFilters.instanceOf(IScoutRuntimeTypes.IFormHandler));
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
    if (method != null) {
      Matcher matcher = PROPERTY_BEAN_REGEX.matcher(method.elementName());
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
        CompositeObject key = new CompositeObject(propName, getSetOrder, method.elementName(), method.parameters().list().size(), method);
        return key;
      }
    }
    return null;
  }

  private static IMethod getOverwrittenMethod(final IMethod method) {
    final String refSig = SignatureUtils.createMethodIdentifier(method);
    return method.declaringType().methods().withSuperClasses(true).withFilter(new IFilter<IMethod>() {
      @Override
      public boolean evaluate(IMethod element) {
        if (method.equals(element)) {
          return false;
        }
        return refSig.equals(SignatureUtils.createMethodIdentifier(element));
      }
    }).first();
  }

  protected CompositeObject createConstructorKey(List<IMethodParameter> list) {
    if (list == null) {
      return new CompositeObject(0, "");
    }

    StringBuilder b = new StringBuilder();
    for (IMethodParameter p : list) {
      b.append(p.dataType().name());
    }
    return new CompositeObject(list.size(), b.toString());
  }

  public void print(PrintStream printer) {
    printer.println("------ Structured type of '" + getType().name() + "' ------------");
    for (Categories c : Categories.values()) {
      printCategory(printer, c);
    }
    printer.println("---------------------------------------------------------------------------");
  }

  private void printCategory(PrintStream printer, Categories category) {
    printer.println("category '" + category.name() + "'");
    for (IJavaElement e : getElements(category)) {
      printer.println("  - " + e.elementName());
    }
  }
}
