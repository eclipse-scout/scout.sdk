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
package org.eclipse.scout.sdk.ui.internal.extensions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.ui.extensions.AbstractFormFieldWizard;
import org.eclipse.scout.sdk.ui.extensions.IFormFieldExtension;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.osgi.framework.Bundle;

public final class FormFieldExtensionPoint {
  private static final FormFieldExtensionPoint instance = new FormFieldExtensionPoint();

  private List<IFormFieldExtension> m_extensions;

  private FormFieldExtensionPoint() {
    init();
  }

  public static IFormFieldExtension[] getAllFormFieldExtensions() {
    return instance.getAllFormFieldExtensionsImpl();
  }

  private IFormFieldExtension[] getAllFormFieldExtensionsImpl() {
    return m_extensions.toArray(new IFormFieldExtension[m_extensions.size()]);
  }

  /**
   * To find the new wizard matches best to the given model type.
   * 
   * <pre>
   * Hierarchy:
   *  IFormField
   *    ISmartField
   * </pre>
   * 
   * createNewWizard(MySmartField) returns the SmartField new wizard if a smart field extensions is registered
   * otherwise it returns the form field new wizard.
   * 
   * @param modelType
   * @return the best match extensions new wizard.
   */
  public static AbstractWorkspaceWizard createNewWizard(IType modelType) {
    return instance.createNewWizardImpl(modelType);
  }

  private AbstractWorkspaceWizard createNewWizardImpl(IType modelType) {
    ITypeHierarchy superTypeHierarchy = ScoutSdkCore.getHierarchyCache().getSuperHierarchy(modelType);
    for (IFormFieldExtension ext : getSortedExtensions(modelType, superTypeHierarchy, -1)) {
      if (ext.getNewWizardClazz() != null) {
        return ext.createNewWizard();
      }
    }
    return null;
  }

  public static IFormFieldExtension findExtension(IType modelType, int maxDistance) {
    return instance.findExtensionImpl(modelType, maxDistance);
  }

  /**
   * @param modelType
   * @return
   */
  private IFormFieldExtension findExtensionImpl(IType modelType, int maxDistance) {
    ITypeHierarchy superTypeHierarchy = ScoutSdkCore.getHierarchyCache().getSuperHierarchy(modelType);
    IFormFieldExtension[] sortedExtensions = getSortedExtensions(modelType, superTypeHierarchy, maxDistance);
    if (sortedExtensions.length > 0) {
      return sortedExtensions[0];
    }
    return null;
  }

  /**
   * To find the node page matches best to the given model type.
   * 
   * <pre>
   * Hierarchy:
   *  IFormField
   *    ISmartField
   * </pre>
   * 
   * createNodePage(MySmartField) returns the SmartField node page if a smart field extensions is registered
   * otherwise it returns the form field node page.
   * 
   * @param modelType
   * @return the best match extensions node page.
   */
  public static IPage createNodePage(IType modelType, ITypeHierarchy formFieldHierarchy) {
    return instance.createNodePageImpl(modelType, formFieldHierarchy);
  }

  private IPage createNodePageImpl(IType modelType, ITypeHierarchy formFieldHierarchy) {
    for (IFormFieldExtension ext : getSortedExtensions(modelType, formFieldHierarchy, -1)) {
      if (ext.getNodePage() != null) {
        return ext.createNodePage();
      }
    }
    return null;
  }

  private IFormFieldExtension[] getSortedExtensions(IType modelType, ITypeHierarchy formFieldHierarchy, int maxDistance) {
    ArrayList<IFormFieldExtension> extensions = new ArrayList<IFormFieldExtension>();
    for (IFormFieldExtension ext : m_extensions) {
      if (maxDistance < 0) {
        HashSet<IType> allSubTypes = CollectionUtility.hashSet(formFieldHierarchy.getAllSubtypes(ext.getModelType()));
        allSubTypes.add(ext.getModelType());
        if (allSubTypes.contains(modelType)) {
          extensions.add(ext);
        }
      }
      else {
        if (distanceToIFormField(modelType, ext.getModelType(), 0, formFieldHierarchy, maxDistance) <= maxDistance) {
          extensions.add(ext);
        }
      }
    }
    return extensions.toArray(new IFormFieldExtension[extensions.size()]);
  }

  private void init() {
    TreeMap<CompositeObject, FormFieldExtension> formFieldExtensions = new TreeMap<CompositeObject, FormFieldExtension>();
    IExtensionRegistry reg = Platform.getExtensionRegistry();
    IExtensionPoint xp = reg.getExtensionPoint(ScoutSdkUi.PLUGIN_ID, "formField");
    IExtension[] extensions = xp.getExtensions();
    for (IExtension extension : extensions) {
      IConfigurationElement[] elements = extension.getConfigurationElements();
      for (IConfigurationElement element : elements) {
        if ("true".equalsIgnoreCase(element.getAttribute("active"))) {
          String name = element.getAttribute("name");
          String modClassName = element.getAttribute("model");
          if (!StringUtility.hasText(modClassName)) {
            ScoutSdkUi.logWarning("Could not find model in '" + extension.getUniqueIdentifier() + "'. Skiping this extension.");
            continue;
          }
          IType modelType = TypeUtility.getType(modClassName);
          if (!TypeUtility.exists(modelType)) {
            ScoutSdkUi.logError("FormFieldExtension: the model type '" + modClassName + "' can not be found.");
            break;
          }
          ITypeHierarchy superTypeHierarchy = null;
          superTypeHierarchy = ScoutSdkCore.getHierarchyCache().getSuperHierarchy(modelType);
          if (superTypeHierarchy == null) {
            ScoutSdkUi.logWarning("could not create super type hierarchy of '" + modelType.getFullyQualifiedName() + "'.");
            continue;
          }
          int distance = -distanceToIFormField(modelType, TypeUtility.getType(IRuntimeClasses.IFormField), 0, superTypeHierarchy);
          CompositeObject key = new CompositeObject(distance, modelType.getFullyQualifiedName());
          FormFieldExtension formFieldExtension = formFieldExtensions.get(key);
          if (formFieldExtension == null) {
            formFieldExtension = new FormFieldExtension(name, modelType);
            formFieldExtensions.put(key, formFieldExtension);
          }
          Bundle contributerBundle = Platform.getBundle(extension.getNamespaceIdentifier());
          Class<? extends AbstractFormFieldWizard> wizardClazz = getClassOfContribution(contributerBundle, element.getChildren("newWizard"), "wizard", AbstractFormFieldWizard.class);
          if (wizardClazz != null) {
            if (formFieldExtension.getNewWizardClazz() != null) {
              ScoutSdkUi.logWarning("double defined new wizard class.");
            }
            else {
              formFieldExtension.setNewWizardClazz(wizardClazz);
            }
          }
          String isInShortList = getAttributeOfContribution(contributerBundle, element.getChildren("newWizard"), "inShortList");
          formFieldExtension.setInShortList("true".equalsIgnoreCase(isInShortList));

          Class<? extends AbstractScoutTypePage> nodePageClazz = getClassOfContribution(contributerBundle, element.getChildren("nodePage"), "nodePage", AbstractScoutTypePage.class);
          if (nodePageClazz != null) {
            if (formFieldExtension.getNodePage() != null) {
              ScoutSdkUi.logWarning("double defined node page class.");
            }
            else {
              formFieldExtension.setNodePage(nodePageClazz);
            }
          }
        }
      }
    }
    m_extensions = new ArrayList<IFormFieldExtension>();
    for (IFormFieldExtension ext : formFieldExtensions.values()) {
      m_extensions.add(ext);
    }
  }

  @SuppressWarnings("unchecked")
  private <T> Class<? extends T> getClassOfContribution(Bundle bundle, IConfigurationElement[] elements, String attribute, Class<T> t) {
    Class<? extends T> clazz = null;
    if (bundle != null) {
      // wizard
      if (elements != null && elements.length == 1) {
        String clazzName = elements[0].getAttribute(attribute);
        if (!StringUtility.isNullOrEmpty(clazzName)) {
          try {
            clazz = (Class<? extends T>) bundle.loadClass(clazzName);
          }
          catch (Exception tt) {
            ScoutSdkUi.logWarning("could not load class of extension '" + elements[0].getName() + "'.", tt);
          }
        }
      }
    }
    return clazz;
  }

  private String getAttributeOfContribution(Bundle bundle, IConfigurationElement[] elements, String attribute) {
    String value = null;
    if (bundle != null) {
      // wizard
      if (elements != null && elements.length == 1) {
        value = elements[0].getAttribute(attribute);
      }
    }
    return value;
  }

  private int distanceToIFormField(IType visitee, IType superType, int dist, ITypeHierarchy superTypeHierarchy) throws IllegalArgumentException {
    return distanceToIFormField(visitee, superType, dist, superTypeHierarchy, Integer.MAX_VALUE);
  }

  private int distanceToIFormField(IType visitee, IType superType, int dist, ITypeHierarchy superTypeHierarchy, int maxDistance) throws IllegalArgumentException {
    if (visitee == null) {
      throw new IllegalArgumentException("try to determ the distance to IFormField of a instance not in subhierarchy of IFormField.");
    }
    if (dist > maxDistance) {
      return Integer.MAX_VALUE;
    }
    else if (superType.getFullyQualifiedName().equals(visitee.getFullyQualifiedName())) {
      return dist;
    }
    else {
      int locDist = 100000;
      IType superclass = superTypeHierarchy.getSuperclass(visitee);
      if (superclass != null) {
        locDist = distanceToIFormField(superclass, superType, (dist + 1), superTypeHierarchy, maxDistance);
      }
      Set<IType> interfaces = superTypeHierarchy.getSuperInterfaces(visitee);
      for (IType i : interfaces) {
        locDist = Math.min(locDist, distanceToIFormField(i, superType, (dist + 1), superTypeHierarchy, maxDistance));
      }
      dist = locDist;
      return dist;
    }
  }
}
