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
package org.eclipse.scout.sdk.ui.fields.proposal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.icon.IIconProvider;
import org.eclipse.scout.sdk.icon.ScoutIconDesc;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.fields.proposal.BundleTypeProposal;
import org.eclipse.scout.sdk.ui.internal.fields.proposal.ConstantFieldProposal;
import org.eclipse.scout.sdk.util.ScoutSourceUtilities;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * <h3>BCProposalFieldProperties</h3> ...
 * 
 * @see ScoutTypeDecorators
 */
public class ScoutProposalUtility {

  private static final ScoutProposalUtility instance = new ScoutProposalUtility();

  private ImageRegistry m_projectImageRegistiry;

  private ScoutProposalUtility() {
  }

//  public static SiblingProposal[] getSiblingProposals(IType parentType, IType superType) {
//    return instance.getSiblingProposalsImpl(parentType, superType);
//  }

  public static SiblingProposal[] getSiblingProposals(IType[] types) {
    ArrayList<SiblingProposal> siblings = new ArrayList<SiblingProposal>();
    for (IType t : types) {
      siblings.add(new SiblingProposal(t));
    }
    return siblings.toArray(new SiblingProposal[siblings.size()]);
  }

//  private SiblingProposal[] getSiblingProposalsImpl(IType parentType, IType superType) {
//    IRegion region = JavaCore.newRegion();
//    region.add(parentType);
//    ITypeHierarchy combindedHierarchy = ScoutSdk.getTypeHierarchyPrimaryTypes(superType).combinedTypeHierarchy(region);
//    ArrayList<SiblingProposal> siblings = new ArrayList<SiblingProposal>();
//    for (IType type : TypeUtility.getInnerTypes(parentType, TypeFilters.getMultiTypeFilter(TypeFilters.getClassFilter(), TypeFilters.getInHierarchyFilter(combindedHierarchy)), TypeComparators.getOrderAnnotationComparator())) {
//      siblings.add(new SiblingProposal(type));
//    }
//    if (siblings.size() > 0) {
//      siblings.add(SiblingProposal.SIBLING_END);
//    }
//    return siblings.toArray(new SiblingProposal[siblings.size()]);
//  }

  public static ITypeProposal[] getScoutTypeProposalsFor(IType... types) {
    ITypeProposal[] props = new ITypeProposal[types.length];
    for (int i = 0; i < types.length; i++) {
      props[i] = new TypeProposal(types[i]);
    }
    return props;
  }

  public static ScoutBundleProposal[] getBundleProposals(IScoutBundle... bundles) {
    ScoutBundleProposal[] props = new ScoutBundleProposal[bundles.length];
    for (int i = 0; i < bundles.length; i++) {
      props[i] = new ScoutBundleProposal(bundles[i]);
    }
    return props;
  }

  public static ITypeProposal[] getBcTypeProposalsFor(String... types) {
    ITypeProposal[] props = new ITypeProposal[types.length];
    for (int i = 0; i < types.length; i++) {
      props[i] = new TypeProposal(types[i]);
    }
    return props;
  }

  /**
   * the order of the proposal array will be the order the items are shown as proposals.
   * 
   * @return
   * @throws JavaModelException
   */
  public static ITypeProposal[] getBcTypeProposals() throws JavaModelException {
    return instance.getBcTypeProposalsImpl();
  }

  private ITypeProposal[] m_beanPropertyTypeProposals;

  private ITypeProposal[] getBcTypeProposalsImpl() throws JavaModelException {
    if (m_beanPropertyTypeProposals == null) {
      m_beanPropertyTypeProposals = new ITypeProposal[]{
          new TypeProposal(ScoutSdk.getType(ArrayList.class.getName())),
          new TypeProposal(ScoutSdk.getType(java.lang.Boolean.class.getName())),
          new TypeProposal(ScoutSdk.getType(Collection.class.getName())),
          new TypeProposal(ScoutSdk.getType(Date.class.getName())),
          new TypeProposal(ScoutSdk.getType(Double.class.getName())),
          new TypeProposal(ScoutSdk.getType(java.util.Enumeration.class.getName())),
          new TypeProposal(ScoutSdk.getType(java.lang.Float.class.getName())),
          new TypeProposal(ScoutSdk.getType(HashMap.class.getName())),
          new TypeProposal(ScoutSdk.getType(Integer.class.getName())),
          new TypeProposal(ScoutSdk.getType(List.class.getName())),
          new TypeProposal(ScoutSdk.getType(Long.class.getName())),
          new TypeProposal(ScoutSdk.getType(Map.class.getName())),
          new TypeProposal(ScoutSdk.getType(java.lang.Number.class.getName())),
          new TypeProposal(ScoutSdk.getType(Object.class.getName())),
          new TypeProposal(ScoutSdk.getType(java.lang.Runnable.class.getName())),
          new TypeProposal(ScoutSdk.getType(Set.class.getName())),
          new TypeProposal(ScoutSdk.getType(String.class.getName())),
          new TypeProposal(ScoutSdk.getType(TreeMap.class.getName())),
          new TypeProposal(ScoutSdk.getType(TreeSet.class.getName())),
          new TypeProposal(ScoutSdk.getType(java.util.Vector.class.getName()))};
    }
    return m_beanPropertyTypeProposals;
  }

  public static ITypeProposal[] getMenuShortListProposals() {
    return instance.getMenuShortListProposalsImpl();
  }

  private ITypeProposal[] m_menuShortListProposals;

  private ITypeProposal[] getMenuShortListProposalsImpl() {
    if (m_menuShortListProposals == null) {
      m_menuShortListProposals = new ITypeProposal[]{new TypeProposal(RuntimeClasses.AbstractMenu),
          new TypeProposal(RuntimeClasses.AbstractBookmarkMenu),
          new TypeProposal(RuntimeClasses.AbstractCheckBoxMenu)};
    }
    return m_menuShortListProposals;
  }

  // public static IBCTypeProposal[] getFormFieldShortListProposals(){
  // return instance.getFormFieldShortListProposalsImpl();
  // }
  //
  // private IBCTypeProposal[] m_formFieldShortListProposals;
  //
  // private IBCTypeProposal[] getFormFieldShortListProposalsImpl(){
  // if(m_formFieldShortListProposals==null){
  // m_formFieldShortListProposals=new IBCTypeProposal[]{new FormFieldTypeProposal(BCTypeDecorators.INDEX_BOOLEAN_FIELD, RuntimeClasses.AbstractBooleanField), new FormFieldTypeProposal(BCTypeDecorators.INDEX_BUTTON, RuntimeClasses.AbstractButton), new FormFieldTypeProposal(BCTypeDecorators.INDEX_CANCEL_BUTTON, RuntimeClasses.AbstractCancelButton), new FormFieldTypeProposal(BCTypeDecorators.INDEX_CLOSE_BUTTON, RuntimeClasses.AbstractCloseButton), new FormFieldTypeProposal(BCTypeDecorators.INDEX_OK_BUTTON, RuntimeClasses.AbstractOkButton), new FormFieldTypeProposal(BCTypeDecorators.INDEX_RESET_BUTTON, RuntimeClasses.AbstractResetButton), new FormFieldTypeProposal(BCTypeDecorators.INDEX_SAVE_BUTTON, RuntimeClasses.AbstractSaveButton), new FormFieldTypeProposal(BCTypeDecorators.INDEX_SEARCH_BUTTON, RuntimeClasses.AbstractSearchButton), new FormFieldTypeProposal(BCTypeDecorators.INDEX_CALENDAR_FIELD, RuntimeClasses.AbstractCalendarField), new FormFieldTypeProposal(BCTypeDecorators.INDEX_CHART_BOX, RuntimeClasses.AbstractChartBox), new FormFieldTypeProposal(BCTypeDecorators.INDEX_CHECK_BOX, RuntimeClasses.AbstractCheckBox), new FormFieldTypeProposal(BCTypeDecorators.INDEX_COMPOSER_FIELD, RuntimeClasses.AbstractComposerField), new FormFieldTypeProposal(BCTypeDecorators.INDEX_CUSTOM_FIELD, RuntimeClasses.AbstractCustomField), new FormFieldTypeProposal(BCTypeDecorators.INDEX_DATE_FIELD, RuntimeClasses.AbstractDateField), new FormFieldTypeProposal(BCTypeDecorators.INDEX_DOUBLE_FIELD, RuntimeClasses.AbstractDoubleField), new FormFieldTypeProposal(BCTypeDecorators.INDEX_FILE_CHOOSER_FIELD, RuntimeClasses.AbstractFileChooserField), new FormFieldTypeProposal(BCTypeDecorators.INDEX_GROUP_BOX, RuntimeClasses.AbstractGroupBox), new FormFieldTypeProposal(BCTypeDecorators.INDEX_HTML_FIELD, RuntimeClasses.AbstractHtmlField), new FormFieldTypeProposal(BCTypeDecorators.INDEX_IMAGE_FIELD, RuntimeClasses.AbstractImageField),
  // new FormFieldTypeProposal(BCTypeDecorators.INDEX_INTEGER_FIELD, RuntimeClasses.AbstractIntegerField), new FormFieldTypeProposal(BCTypeDecorators.INDEX_LABEL_FIELD, RuntimeClasses.AbstractLabelField), new FormFieldTypeProposal(BCTypeDecorators.INDEX_LIST_BOX, RuntimeClasses.AbstractListBox), new FormFieldTypeProposal(BCTypeDecorators.INDEX_LONG_FIELD, RuntimeClasses.AbstractLongField), new FormFieldTypeProposal(BCTypeDecorators.INDEX_MATRIX_FIELD, RuntimeClasses.AbstractMatrixField), new FormFieldTypeProposal(BCTypeDecorators.INDEX_PLANNER_FIELD, RuntimeClasses.AbstractPlannerField), new FormFieldTypeProposal(BCTypeDecorators.INDEX_RADIO_BUTTON_GROUP, RuntimeClasses.AbstractRadioButtonGroup), new FormFieldTypeProposal(BCTypeDecorators.INDEX_SEQUENCE_BOX, RuntimeClasses.AbstractSequenceBox), new FormFieldTypeProposal(BCTypeDecorators.INDEX_SMART_FIELD, RuntimeClasses.AbstractSmartField), new FormFieldTypeProposal(BCTypeDecorators.INDEX_STRING_FIELD, RuntimeClasses.AbstractStringField), new FormFieldTypeProposal(BCTypeDecorators.INDEX_TAB_BOX, RuntimeClasses.AbstractTabBox), new FormFieldTypeProposal(BCTypeDecorators.INDEX_TABLE_FIELD, RuntimeClasses.AbstractTableField), new FormFieldTypeProposal(BCTypeDecorators.INDEX_TIME_FIELD, RuntimeClasses.AbstractTimeField), new FormFieldTypeProposal(BCTypeDecorators.INDEX_TREE_BOX, RuntimeClasses.AbstractTreeBox), new FormFieldTypeProposal(BCTypeDecorators.INDEX_TREE_FIELD, RuntimeClasses.AbstractTreeField),
  // // virtual fields working with one of the upper classes
  //
  // new FormFieldTypeProposal(BCTypeDecorators.INDEX_SEQUENCE_BOX_DOUBLE, RuntimeClasses.AbstractSequenceBox), new FormFieldTypeProposal(BCTypeDecorators.INDEX_SEQUENCE_BOX_DATE, RuntimeClasses.AbstractSequenceBox), new FormFieldTypeProposal(BCTypeDecorators.INDEX_SEQUENCE_BOX_INTEGER, RuntimeClasses.AbstractSequenceBox), new FormFieldTypeProposal(BCTypeDecorators.INDEX_SEQUENCE_BOX_DATE_TIME, RuntimeClasses.AbstractSequenceBox), new FormFieldTypeProposal(BCTypeDecorators.INDEX_SEQUENCE_BOX_LONG, RuntimeClasses.AbstractSequenceBox), new FormFieldTypeProposal(BCTypeDecorators.INDEX_DATE_TIME_FIELD, RuntimeClasses.AbstractDateField),};
  // Arrays.sort(m_formFieldShortListProposals, new ContentProposalExComparator(false));
  // }
  // return m_formFieldShortListProposals;
  // }

  public static ConstantFieldProposal<Integer>[] getButtonDisplayTypeProposals() {
    return instance.getButtonDisplayTypeProposalsImpl();
  }

  private ConstantFieldProposal<Integer>[] m_buttonDisplayTypeProposals;

  @SuppressWarnings("unchecked")
  private ConstantFieldProposal<Integer>[] getButtonDisplayTypeProposalsImpl() {
    if (m_buttonDisplayTypeProposals == null) {
      IType buttonType = ScoutSdk.getType(RuntimeClasses.IButton);
      m_buttonDisplayTypeProposals = new ConstantFieldProposal[]{
          new ConstantFieldProposal<Integer>("Default", ScoutSdkUi.getImage(ScoutSdkUi.ButtonStyle), buttonType.getField("DISPLAY_STYLE_DEFAULT"), 0),
          new ConstantFieldProposal<Integer>("Radio", ScoutSdkUi.getImage(ScoutSdkUi.ButtonStyle), buttonType.getField("DISPLAY_STYLE_RADIO"), 2),
          new ConstantFieldProposal<Integer>("Toggle", ScoutSdkUi.getImage(ScoutSdkUi.ButtonStyle), buttonType.getField("DISPLAY_STYLE_TOGGLE"), 1),
          new ConstantFieldProposal<Integer>("Link", ScoutSdkUi.getImage(ScoutSdkUi.ButtonStyle), buttonType.getField("DISPLAY_STYLE_LINK"), 3)};
    }
    return m_buttonDisplayTypeProposals;
  }

  public static ConstantFieldProposal<String>[] getFormViewIdTypeProposals() {
    return instance.getFormViewIdTypeProposalsImpl();
  }

  private ConstantFieldProposal<String>[] m_formViewIdTypeProposals;

  @SuppressWarnings("unchecked")
  private ConstantFieldProposal<String>[] getFormViewIdTypeProposalsImpl() {
    if (m_formViewIdTypeProposals == null) {
      IType iformType = ScoutSdk.getType(RuntimeClasses.IForm);
      m_formViewIdTypeProposals = new ConstantFieldProposal[]{
          new ConstantFieldProposal<String>("Outline", ScoutSdkUi.getImage(ScoutSdkUi.Default), iformType.getField("VIEW_ID_OUTLINE"), "OUTLINE"),
          new ConstantFieldProposal<String>("Outline Selector", ScoutSdkUi.getImage(ScoutSdkUi.Default), iformType.getField("VIEW_ID_OUTLINE_SELECTOR"), "OUTLINE_SELECTOR"),
          new ConstantFieldProposal<String>("Page Table", ScoutSdkUi.getImage(ScoutSdkUi.Default), iformType.getField("VIEW_ID_PAGE_TABLE"), "PAGE_TABLE"),
          new ConstantFieldProposal<String>("Page Detail", ScoutSdkUi.getImage(ScoutSdkUi.Default), iformType.getField("VIEW_ID_PAGE_DETAIL"), "PAGE_DETAIL"),
          new ConstantFieldProposal<String>("Page Search", ScoutSdkUi.getImage(ScoutSdkUi.Default), iformType.getField("VIEW_ID_PAGE_SEARCH"), "PAGE_SEARCH"),
          new ConstantFieldProposal<String>("Editor", ScoutSdkUi.getImage(ScoutSdkUi.Default), iformType.getField("EDITOR_ID"), "EDITOR"),
          new ConstantFieldProposal<String>("North", ScoutSdkUi.getImage(ScoutSdkUi.Default), iformType.getField("VIEW_ID_N"), "N"),
          new ConstantFieldProposal<String>("North-East", ScoutSdkUi.getImage(ScoutSdkUi.Default), iformType.getField("VIEW_ID_NE"), "NE"),
          new ConstantFieldProposal<String>("East", ScoutSdkUi.getImage(ScoutSdkUi.Default), iformType.getField("VIEW_ID_E"), "E"),
          new ConstantFieldProposal<String>("South-East", ScoutSdkUi.getImage(ScoutSdkUi.Default), iformType.getField("VIEW_ID_SE"), "SE"),
          new ConstantFieldProposal<String>("South", ScoutSdkUi.getImage(ScoutSdkUi.Default), iformType.getField("VIEW_ID_S"), "S"),
          new ConstantFieldProposal<String>("South-West", ScoutSdkUi.getImage(ScoutSdkUi.Default), iformType.getField("VIEW_ID_SW"), "SW"),
          new ConstantFieldProposal<String>("West", ScoutSdkUi.getImage(ScoutSdkUi.Default), iformType.getField("VIEW_ID_W"), "W"),
          new ConstantFieldProposal<String>("NorthWest", ScoutSdkUi.getImage(ScoutSdkUi.Default), iformType.getField("VIEW_ID_NW"), "NW"),
          new ConstantFieldProposal<String>("Center", ScoutSdkUi.getImage(ScoutSdkUi.Default), iformType.getField("VIEW_ID_CENTER"), "C")};
    }
    return m_formViewIdTypeProposals;
  }

  public static ConstantFieldProposal<Integer>[] getButtonSystemTypeProposals() {
    return instance.getButtonSystemTypeProposalsImpl();
  }

  private ConstantFieldProposal<Integer>[] m_buttonSystemTypeProposals;

  @SuppressWarnings("unchecked")
  private ConstantFieldProposal<Integer>[] getButtonSystemTypeProposalsImpl() {
    if (m_buttonSystemTypeProposals == null) {
      IType buttonType = ScoutSdk.getType(RuntimeClasses.IButton);
      m_buttonSystemTypeProposals = new ConstantFieldProposal[]{
          new ConstantFieldProposal<Integer>("None", ScoutSdkUi.getImage(ScoutSdkUi.Default), buttonType.getField("SYSTEM_TYPE_NONE"), 0),
          new ConstantFieldProposal<Integer>("Cancel", ScoutSdkUi.getImage(ScoutSdkUi.Default), buttonType.getField("SYSTEM_TYPE_CANCEL"), 1),
          new ConstantFieldProposal<Integer>("Close", ScoutSdkUi.getImage(ScoutSdkUi.Default), buttonType.getField("SYSTEM_TYPE_CLOSE"), 2),
          new ConstantFieldProposal<Integer>("Ok", ScoutSdkUi.getImage(ScoutSdkUi.Default), buttonType.getField("SYSTEM_TYPE_OK"), 3),
          new ConstantFieldProposal<Integer>("Reset", ScoutSdkUi.getImage(ScoutSdkUi.Default), buttonType.getField("SYSTEM_TYPE_RESET"), 4),
          new ConstantFieldProposal<Integer>("Save", ScoutSdkUi.getImage(ScoutSdkUi.Default), buttonType.getField("SYSTEM_TYPE_SAVE"), 5),
          new ConstantFieldProposal<Integer>("Save Search", ScoutSdkUi.getImage(ScoutSdkUi.Default), buttonType.getField("SYSTEM_TYPE_SAVE_WITHOUT_MARKER_CHANGE"), 6),
          new ConstantFieldProposal<Integer>("Back", ScoutSdkUi.getImage(ScoutSdkUi.Default), buttonType.getField("SYSTEM_TYPE_WIZARD_BACK"), 7),
          new ConstantFieldProposal<Integer>("Next", ScoutSdkUi.getImage(ScoutSdkUi.Default), buttonType.getField("SYSTEM_TYPE_WIZARD_NEXT"), 8),
          new ConstantFieldProposal<Integer>("Finish", ScoutSdkUi.getImage(ScoutSdkUi.Default), buttonType.getField("SYSTEM_TYPE_WIZARD_FINISH"), 9),
          new ConstantFieldProposal<Integer>("Suspend", ScoutSdkUi.getImage(ScoutSdkUi.Default), buttonType.getField("SYSTEM_TYPE_WIZARD_SUSPEND"), 10)};
      Arrays.sort(m_buttonSystemTypeProposals, new ContentProposalExComparator(false));
    }
    return m_buttonSystemTypeProposals;
  }

  public static ConstantFieldProposal<Integer>[] getFormDisplayHintProposals() {
    return instance.getFormDisplayHintProposalsImpl();
  }

  private ConstantFieldProposal<Integer>[] m_formDisplayHintProposals;

  @SuppressWarnings("unchecked")
  private ConstantFieldProposal<Integer>[] getFormDisplayHintProposalsImpl() {
    if (m_formDisplayHintProposals == null) {
      IType formType = ScoutSdk.getType(RuntimeClasses.IForm);
      m_formDisplayHintProposals = new ConstantFieldProposal[]{
          new ConstantFieldProposal<Integer>("Dialog", ScoutSdkUi.getImage(ScoutSdkUi.Default), formType.getField("DISPLAY_HINT_DIALOG"), 0),
          new ConstantFieldProposal<Integer>("View", ScoutSdkUi.getImage(ScoutSdkUi.Default), formType.getField("DISPLAY_HINT_VIEW"), 20),};
    }
    return m_formDisplayHintProposals;
  }

  /**
   * @return
   */
  public static ConstantFieldProposal<Integer>[] getVerticalAlignmentProposals() {
    return instance.getVerticalAlignmentProposalsImpl();
  }

  private ConstantFieldProposal<Integer>[] m_verticalAlignmentProposals;

  @SuppressWarnings("unchecked")
  private ConstantFieldProposal<Integer>[] getVerticalAlignmentProposalsImpl() {
    if (m_verticalAlignmentProposals == null) {
      m_verticalAlignmentProposals = new ConstantFieldProposal[]{
          new ConstantFieldProposal<Integer>("Top", ScoutSdkUi.getImage(ScoutSdkUi.VerticalTop), null, -1),
          new ConstantFieldProposal<Integer>("Center", ScoutSdkUi.getImage(ScoutSdkUi.VerticalCenter), null, 0),
          new ConstantFieldProposal<Integer>("Bottom", ScoutSdkUi.getImage(ScoutSdkUi.VerticalBottom), null, 1)};
    }
    return m_verticalAlignmentProposals;
  }

  /**
   * @return
   */
  public static ConstantFieldProposal<Integer>[] getHorizontalAlignmentProposals() {
    return instance.getHorizontalAlignmentProposalsImpl();
  }

  private ConstantFieldProposal<Integer>[] m_horizontalAlignmentProposals;

  @SuppressWarnings("unchecked")
  private ConstantFieldProposal<Integer>[] getHorizontalAlignmentProposalsImpl() {
    if (m_horizontalAlignmentProposals == null) {
      m_horizontalAlignmentProposals = new ConstantFieldProposal[]{
          new ConstantFieldProposal<Integer>("Left", ScoutSdkUi.getImage(ScoutSdkUi.HorizontalLeft), null, -1),
          new ConstantFieldProposal<Integer>("Center", ScoutSdkUi.getImage(ScoutSdkUi.HorizontalCenter), null, 0),
          new ConstantFieldProposal<Integer>("Right", ScoutSdkUi.getImage(ScoutSdkUi.HorizontalRight), null, 1)};
    }
    return m_horizontalAlignmentProposals;
  }

  public static String getFieldName(IType field, String labelMethodName) {
    return ScoutSourceUtilities.getTranslatedMethodStringValue(field, labelMethodName);

  }

  public static ITypeProposal[] getPageShortListProposals() {
    return instance.getPageShortListProposalsImpl();
  }

  private ITypeProposal[] m_pageShortListProposals;

  private ITypeProposal[] getPageShortListProposalsImpl() {
    if (m_pageShortListProposals == null) {
      m_pageShortListProposals = new ITypeProposal[]{new TypeProposal(ScoutSdk.getType(RuntimeClasses.AbstractPageWithNodes)),
          new TypeProposal(ScoutSdk.getType(RuntimeClasses.AbstractPageWithTable))};
    }
    return m_pageShortListProposals;
  }

  public static BundleTypeProposal[] getAllBundleProposals() {
    return instance.getAllBundleProposalsImpl();
  }

  private BundleTypeProposal[] m_bundleTypes;

  private BundleTypeProposal[] getAllBundleProposalsImpl() {
    if (m_bundleTypes == null) {
      m_bundleTypes = new BundleTypeProposal[]{
          new BundleTypeProposal(ScoutIdeProperties.BUNDLE_TYPE_CLIENT, "Client"),
          new BundleTypeProposal(ScoutIdeProperties.BUNDLE_TYPE_TEST_CLIENT, "Client Test"),
          new BundleTypeProposal(ScoutIdeProperties.BUNDLE_TYPE_UI_SWING, "Client Swing"),
          new BundleTypeProposal(ScoutIdeProperties.BUNDLE_TYPE_UI_SWT, "Client SWT"),
          new BundleTypeProposal(ScoutIdeProperties.BUNDLE_TYPE_SHARED, "Shared"),
          new BundleTypeProposal(ScoutIdeProperties.BUNDLE_TYPE_SERVER, "Server"),
          new BundleTypeProposal(ScoutIdeProperties.BUNDLE_TYPE_UI_SWT_APPLICATION, "RCP Application (SWT)"),
          new BundleTypeProposal(ScoutIdeProperties.BUNDLE_TYPE_SERVER_APPLICATION, "Server Appliaction")
          };
    }
    return m_bundleTypes;
  }

  public static IconProposal[] getScoutIconProposals(Display display, IScoutBundle bundle) {
    return instance.getBCIconProposalsImpl(display, bundle);
  }

  private IconProposal[] getBCIconProposalsImpl(Display dispaly, IScoutBundle bundle) {
    if (m_projectImageRegistiry == null) {
      m_projectImageRegistiry = new ImageRegistry(dispaly);
    }
    IIconProvider iconProvider = bundle.findBestMatchIconProvider();
    ArrayList<IconProposal> proposals = new ArrayList<IconProposal>();
    for (ScoutIconDesc icon : iconProvider.getIcons()) {
      Image img = m_projectImageRegistiry.get(icon.getId());
      if (img == null) {
        ImageDescriptor imageDescriptor = icon.getImageDescriptor();
        m_projectImageRegistiry.put(icon.getId(), imageDescriptor);
        img = m_projectImageRegistiry.get(icon.getId());
      }
      proposals.add(new IconProposal(icon, img));
    }
    return proposals.toArray(new IconProposal[proposals.size()]);
  }

  /**
   * <h3>BCTypeProposalComparator</h3> The default comparator used to alphabetic asc ordering IContentProposalsEx.
   */
  public static class ContentProposalExComparator implements Comparator<IContentProposalEx> {
    private final boolean m_expertMode;

    public ContentProposalExComparator(boolean expertMode) {
      m_expertMode = expertMode;

    }

    public int compare(IContentProposalEx o1, IContentProposalEx o2) {
      if (o1 == null && o2 == null) {
        return 0;
      }
      else if (o2 == null) {
        return -1;
      }
      else if (o1 == null) {
        return 1;
      }
      else {
        return o1.getLabel(false, m_expertMode).compareTo(o2.getLabel(false, true));
      }

    }
  }

}
