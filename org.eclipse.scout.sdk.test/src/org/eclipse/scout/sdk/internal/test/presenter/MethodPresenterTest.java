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
package org.eclipse.scout.sdk.internal.test.presenter;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.TuningUtility;
import org.eclipse.scout.sdk.internal.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.internal.test.Activator;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.BooleanPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.ButtonDisplayStylePresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.ButtonSystemTypePresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.CodeTypeProposalPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.ColorPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.DoublePresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.FontPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.FormDisplayHintPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.FormViewIdPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.HorizontalAlignmentPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.IconPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.IntegerPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.LabelHorizontalAlignmentPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.LabelPositionPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.LongPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.LookupCallProposalPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.LookupServiceProposalPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.MasterFieldPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.NlsTextPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.OutlinesPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.SearchFormPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.StringPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.VerticalAglinmentPresenter;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractMethodPresenter;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.scout.sdk.workspace.type.config.ConfigPropertyType;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class MethodPresenterTest extends AbstractScoutSdkTest {

  private static final String SHARED_PROJECT = "presenter.test.shared";
  private static final String SERVER_PROJECT = "presenter.test.server";
  private static final String CLIENT_PROJECT = "presenter.test.client";

  private static PropertyViewFormToolkit default_toolkit;
  private static Composite default_parent;
  private static Properties reference_max_durations;

  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    setupWorkspace("resources/presenter", SHARED_PROJECT, SERVER_PROJECT, CLIENT_PROJECT);

    default_toolkit = new PropertyViewFormToolkit(Display.getDefault());
    default_parent = Display.getDefault().getActiveShell();

    String hostname = InetAddress.getLocalHost().getHostName().toLowerCase();
    InputStream is = null;
    try {
      is = getInputStream("resources/presenter/refDurations/" + hostname + ".properties");
      if (is == null) {
        is = getInputStream("resources/presenter/refDurations/default.properties");
      }
      reference_max_durations = new Properties();
      reference_max_durations.load(is);
    }
    finally {
      if (is != null) {
        try {
          is.close();
        }
        catch (Exception e) {
        }
      }
    }
  }

  private static InputStream getInputStream(String pathToResource) throws IOException {
    URL resource = FileLocator.find(Activator.getDefault().getBundle(), new Path(pathToResource), null);
    InputStream is = null;
    if (resource != null) {
      is = resource.openStream();
    }
    return is;
  }

  @AfterClass
  public static void cleanUpWorkspace() throws Exception {
    clearWorkspace();
    default_toolkit = null;
    default_parent = null;
    reference_max_durations = null;
    TuningUtility.finishAll(true);
  }

  private void doPresenterTest(AbstractPresenterTestInfo testInfo) throws Exception {
    IType testType = TypeUtility.getType(testInfo.getTestTypeFqn());
    Assert.assertTrue(TypeUtility.exists(testType));

    ConfigPropertyType configPropertyType = new ConfigPropertyType(testType);
    List<ConfigurationMethod> configPropertyMethods = configPropertyType.getConfigurationMethods(ConfigurationMethod.PROPERTY_METHOD);
    Assert.assertNotNull(configPropertyMethods);
    Assert.assertTrue(configPropertyMethods.size() > 0);

    ArrayList<ConfigurationMethod> implementedMethods = new ArrayList<ConfigurationMethod>(configPropertyMethods.size());
    for (ConfigurationMethod method : configPropertyMethods) {
      if (method != null) {
        if (method.isImplemented()) {
          implementedMethods.add(method);
        }
      }
    }
    Assert.assertTrue(implementedMethods.size() == 1);

    ConfigurationMethod testMethod = implementedMethods.get(0);

    testInfo.init(testMethod);

    long duration = -1;
    AbstractMethodPresenter presenter = null;
    try {
      TuningUtility.startTimer();
      presenter = testInfo.createPresenter(default_toolkit, default_parent, testMethod);
    }
    finally {
      duration = TuningUtility.stopTimer("", false, false);
    }
    Assert.assertNotNull(presenter);
    Assert.assertTrue(duration > 0);
    Assert.assertTrue("Creation of presenter '" + presenter + "' took " + duration + "ns but should be faster than " + testInfo.getMaxPresenterCreationDuration() + "ns.", duration < testInfo.getMaxPresenterCreationDuration());
  }

  private static long getReferenceDuration(String key) {
    try {
      return Long.parseLong(reference_max_durations.getProperty(key, "0"));
    }
    catch (NumberFormatException e) {
      return 0;
    }
  }

  @Test
  public void testBooleanPresenter() throws Exception {
    doPresenterTest(new AbstractPresenterTestInfo("presenter.test.client.ui.forms.DesktopForm.MainBox.BooleanPresenterTestField") {

      @Override
      public long getMaxPresenterCreationDuration() {
        return getReferenceDuration("testBooleanPresenter");
      }

      @Override
      public AbstractMethodPresenter createPresenter(PropertyViewFormToolkit toolkit, Composite parent, ConfigurationMethod m) {
        BooleanPresenter presenter = new BooleanPresenter(toolkit, parent);
        presenter.setMethod(m);
        return presenter;
      }
    });
  }

  @Test
  public void testOutlinesPresenter() throws Exception {
    doPresenterTest(new AbstractPresenterTestInfo("presenter.test.client.ui.desktop.Desktop") {

      @Override
      public long getMaxPresenterCreationDuration() {
        return getReferenceDuration("testOutlinesPresenter");
      }

      @Override
      public AbstractMethodPresenter createPresenter(PropertyViewFormToolkit toolkit, Composite parent, ConfigurationMethod m) {
        OutlinesPresenter presenter = new OutlinesPresenter(toolkit, parent);
        presenter.setMethod(m);
        return presenter;
      }
    });
  }

  @Test
  public void testCodeTypeProposalPresenter() throws Exception {
    doPresenterTest(new AbstractPresenterTestInfo("presenter.test.client.ui.forms.DesktopForm.MainBox.MasterFieldPresenterTestField") {

      @Override
      public long getMaxPresenterCreationDuration() {
        return getReferenceDuration("testCodeTypeProposalPresenter");
      }

      @Override
      public AbstractMethodPresenter createPresenter(PropertyViewFormToolkit toolkit, Composite parent, ConfigurationMethod m) {
        CodeTypeProposalPresenter presenter = new CodeTypeProposalPresenter(toolkit, parent);
        presenter.setMethod(m);
        return presenter;
      }
    });
  }

  @Test
  public void testLookupCallProposalPresenter() throws Exception {
    doPresenterTest(new AbstractPresenterTestInfo("presenter.test.client.ui.forms.DesktopForm.MainBox.LookupCallProposalPresenterTestField") {

      @Override
      public long getMaxPresenterCreationDuration() {
        return getReferenceDuration("testLookupCallProposalPresenter");
      }

      @Override
      public AbstractMethodPresenter createPresenter(PropertyViewFormToolkit toolkit, Composite parent, ConfigurationMethod m) {
        LookupCallProposalPresenter presenter = new LookupCallProposalPresenter(toolkit, parent);
        presenter.setMethod(m);
        return presenter;
      }
    });
  }

  @Test
  public void testLookupServiceProposalPresenter() throws Exception {
    doPresenterTest(new AbstractPresenterTestInfo("presenter.test.shared.services.lookup.TestLookupCall") {

      @Override
      public long getMaxPresenterCreationDuration() {
        return getReferenceDuration("testLookupServiceProposalPresenter");
      }

      @Override
      public AbstractMethodPresenter createPresenter(PropertyViewFormToolkit toolkit, Composite parent, ConfigurationMethod m) {
        LookupServiceProposalPresenter presenter = new LookupServiceProposalPresenter(toolkit, parent);
        presenter.setMethod(m);
        return presenter;
      }
    });
  }

  @Test
  public void testMasterFieldProposalPresenter() throws Exception {
    doPresenterTest(new AbstractPresenterTestInfo("presenter.test.client.ui.forms.DesktopForm.MainBox.MasterFieldPresenterTestField") {

      @Override
      public long getMaxPresenterCreationDuration() {
        return getReferenceDuration("testMasterFieldProposalPresenter");
      }

      @Override
      public AbstractMethodPresenter createPresenter(PropertyViewFormToolkit toolkit, Composite parent, ConfigurationMethod m) {
        MasterFieldPresenter presenter = new MasterFieldPresenter(toolkit, parent);
        presenter.setMethod(m);
        return presenter;
      }
    });
  }

  @Test
  public void testSearchFormProposalPresenter() throws Exception {
    doPresenterTest(new AbstractPresenterTestInfo("presenter.test.client.ui.desktop.outlines.pages.TestTablePage") {

      @Override
      public long getMaxPresenterCreationDuration() {
        return getReferenceDuration("testSearchFormProposalPresenter");
      }

      @Override
      public AbstractMethodPresenter createPresenter(PropertyViewFormToolkit toolkit, Composite parent, ConfigurationMethod m) {
        SearchFormPresenter presenter = new SearchFormPresenter(toolkit, parent);
        presenter.setMethod(m);
        return presenter;
      }
    });
  }

  @Test
  public void testButtonDisplayStylePresenter() throws Exception {
    doPresenterTest(new AbstractPresenterTestInfo("presenter.test.client.ui.forms.DesktopForm.MainBox.DisplayStylePresenterTestButton") {

      @Override
      public long getMaxPresenterCreationDuration() {
        return getReferenceDuration("testButtonDisplayStylePresenter");
      }

      @Override
      public AbstractMethodPresenter createPresenter(PropertyViewFormToolkit toolkit, Composite parent, ConfigurationMethod m) {
        ButtonDisplayStylePresenter presenter = new ButtonDisplayStylePresenter(toolkit, parent);
        presenter.setMethod(m);
        return presenter;
      }
    });
  }

  @Test
  public void testButtonSystemTypePresenter() throws Exception {
    doPresenterTest(new AbstractPresenterTestInfo("presenter.test.client.ui.forms.DesktopForm.MainBox.SystemTypePresenterTestButton") {

      @Override
      public long getMaxPresenterCreationDuration() {
        return getReferenceDuration("testButtonSystemTypePresenter");
      }

      @Override
      public AbstractMethodPresenter createPresenter(PropertyViewFormToolkit toolkit, Composite parent, ConfigurationMethod m) {
        ButtonSystemTypePresenter presenter = new ButtonSystemTypePresenter(toolkit, parent);
        presenter.setMethod(m);
        return presenter;
      }
    });
  }

  @Test
  public void testFormDisplayHintPresenter() throws Exception {
    doPresenterTest(new AbstractPresenterTestInfo("presenter.test.client.ui.forms.DesktopForm") {

      @Override
      public long getMaxPresenterCreationDuration() {
        return getReferenceDuration("testFormDisplayHintPresenter");
      }

      @Override
      public AbstractMethodPresenter createPresenter(PropertyViewFormToolkit toolkit, Composite parent, ConfigurationMethod m) {
        FormDisplayHintPresenter presenter = new FormDisplayHintPresenter(toolkit, parent);
        presenter.setMethod(m);
        return presenter;
      }
    });
  }

  @Test
  public void testFormViewIdPresenter() throws Exception {
    doPresenterTest(new AbstractPresenterTestInfo("presenter.test.client.ui.forms.ViewIdTestForm") {

      @Override
      public long getMaxPresenterCreationDuration() {
        return getReferenceDuration("testFormViewIdPresenter");
      }

      @Override
      public AbstractMethodPresenter createPresenter(PropertyViewFormToolkit toolkit, Composite parent, ConfigurationMethod m) {
        FormViewIdPresenter presenter = new FormViewIdPresenter(toolkit, parent);
        presenter.setMethod(m);
        return presenter;
      }
    });
  }

  @Test
  public void testHorizontalAlignmentPresenter() throws Exception {
    doPresenterTest(new AbstractPresenterTestInfo("presenter.test.client.ui.forms.DesktopForm.MainBox.HorizontalAlignmentPresenterTestBox") {

      @Override
      public long getMaxPresenterCreationDuration() {
        return getReferenceDuration("testHorizontalAlignmentPresenter");
      }

      @Override
      public AbstractMethodPresenter createPresenter(PropertyViewFormToolkit toolkit, Composite parent, ConfigurationMethod m) {
        HorizontalAlignmentPresenter presenter = new HorizontalAlignmentPresenter(toolkit, parent);
        presenter.setMethod(m);
        return presenter;
      }
    });
  }

  @Test
  public void testIconPresenter() throws Exception {
    doPresenterTest(new AbstractPresenterTestInfo("presenter.test.client.ui.forms.DesktopForm.MainBox.IconPresenterField") {

      @Override
      public long getMaxPresenterCreationDuration() {
        return getReferenceDuration("testIconPresenter");
      }

      @Override
      public AbstractMethodPresenter createPresenter(PropertyViewFormToolkit toolkit, Composite parent, ConfigurationMethod m) {
        IconPresenter presenter = new IconPresenter(toolkit, parent);
        presenter.setMethod(m);
        return presenter;
      }
    });
  }

  @Test
  public void testVerticalAlignmentPresenter() throws Exception {
    doPresenterTest(new AbstractPresenterTestInfo("presenter.test.client.ui.forms.DesktopForm.MainBox.VerticalAlignmentPresenterTestBox") {

      @Override
      public long getMaxPresenterCreationDuration() {
        return getReferenceDuration("testVerticalAlignmentPresenter");
      }

      @Override
      public AbstractMethodPresenter createPresenter(PropertyViewFormToolkit toolkit, Composite parent, ConfigurationMethod m) {
        VerticalAglinmentPresenter presenter = new VerticalAglinmentPresenter(toolkit, parent);
        presenter.setMethod(m);
        return presenter;
      }
    });
  }

  @Test
  public void testColorPresenter() throws Exception {
    doPresenterTest(new AbstractPresenterTestInfo("presenter.test.client.ui.forms.DesktopForm.MainBox.ColorPresenterTestBox") {

      @Override
      public long getMaxPresenterCreationDuration() {
        return getReferenceDuration("testColorPresenter");
      }

      @Override
      public AbstractMethodPresenter createPresenter(PropertyViewFormToolkit toolkit, Composite parent, ConfigurationMethod m) {
        ColorPresenter presenter = new ColorPresenter(toolkit, parent);
        presenter.setMethod(m);
        return presenter;
      }
    });
  }

  @Test
  public void testDoublePresenter() throws Exception {
    doPresenterTest(new AbstractPresenterTestInfo("presenter.test.client.ui.forms.DesktopForm.MainBox.DoublePresenterTestBox") {

      @Override
      public long getMaxPresenterCreationDuration() {
        return getReferenceDuration("testDoublePresenter");
      }

      @Override
      public AbstractMethodPresenter createPresenter(PropertyViewFormToolkit toolkit, Composite parent, ConfigurationMethod m) {
        DoublePresenter presenter = new DoublePresenter(toolkit, parent);
        presenter.setMethod(m);
        return presenter;
      }
    });
  }

  @Test
  public void testFontPresenter() throws Exception {
    doPresenterTest(new AbstractPresenterTestInfo("presenter.test.client.ui.forms.DesktopForm.MainBox.FontPresenterTestBox") {

      @Override
      public long getMaxPresenterCreationDuration() {
        return getReferenceDuration("testFontPresenter");
      }

      @Override
      public AbstractMethodPresenter createPresenter(PropertyViewFormToolkit toolkit, Composite parent, ConfigurationMethod m) {
        FontPresenter presenter = new FontPresenter(toolkit, parent);
        presenter.setMethod(m);
        return presenter;
      }
    });
  }

  @Test
  public void testIntegerPresenter() throws Exception {
    doPresenterTest(new AbstractPresenterTestInfo("presenter.test.client.ui.forms.DesktopForm.MainBox.IntegerPresenterTestBox") {

      @Override
      public long getMaxPresenterCreationDuration() {
        return getReferenceDuration("testIntegerPresenter");
      }

      @Override
      public AbstractMethodPresenter createPresenter(PropertyViewFormToolkit toolkit, Composite parent, ConfigurationMethod m) {
        IntegerPresenter presenter = new IntegerPresenter(toolkit, parent);
        presenter.setMethod(m);
        return presenter;
      }
    });
  }

  @Test
  public void testLongPresenter() throws Exception {
    doPresenterTest(new AbstractPresenterTestInfo("presenter.test.client.ui.forms.DesktopForm.MainBox.LongPresenterTestField") {

      @Override
      public long getMaxPresenterCreationDuration() {
        return getReferenceDuration("testLongPresenter");
      }

      @Override
      public AbstractMethodPresenter createPresenter(PropertyViewFormToolkit toolkit, Composite parent, ConfigurationMethod m) {
        LongPresenter presenter = new LongPresenter(toolkit, parent);
        presenter.setMethod(m);
        return presenter;
      }
    });
  }

  @Test
  public void testStringPresenter() throws Exception {
    doPresenterTest(new AbstractPresenterTestInfo("presenter.test.client.ui.forms.DesktopForm.MainBox.StringPresenterTestField") {

      @Override
      public long getMaxPresenterCreationDuration() {
        return getReferenceDuration("testStringPresenter");
      }

      @Override
      public AbstractMethodPresenter createPresenter(PropertyViewFormToolkit toolkit, Composite parent, ConfigurationMethod m) {
        StringPresenter presenter = new StringPresenter(toolkit, parent);
        presenter.setMethod(m);
        return presenter;
      }
    });
  }

  @Test
  public void testNlsTextProposalPresenter() throws Exception {
    doPresenterTest(new AbstractPresenterTestInfo("presenter.test.client.ui.forms.DesktopForm.MainBox.NlsTextProposalPresenterTestField") {

      @Override
      public void init(ConfigurationMethod testMethod) {
        // ensure that the NLS project is initialized for the client. this ensures the NLS project creation itself is not part of the
        // performance tests as this has nothing to do with the presenter and is cached after first retrieval.
        ScoutTypeUtility.findNlsProject(testMethod.getType());
      }

      @Override
      public long getMaxPresenterCreationDuration() {
        return getReferenceDuration("testNlsTextProposalPresenter");
      }

      @Override
      public AbstractMethodPresenter createPresenter(PropertyViewFormToolkit toolkit, Composite parent, ConfigurationMethod m) {
        NlsTextPresenter presenter = new NlsTextPresenter(toolkit, parent);
        presenter.setMethod(m);
        return presenter;
      }
    });
  }

  @Test
  public void testLabelPositionPresenter() throws Exception {
    doPresenterTest(new AbstractPresenterTestInfo("presenter.test.client.ui.forms.DesktopForm.MainBox.LabelPositionPresenterTestField") {

      @Override
      public long getMaxPresenterCreationDuration() {
        return getReferenceDuration("testLabelPositionPresenter");
      }

      @Override
      public AbstractMethodPresenter createPresenter(PropertyViewFormToolkit toolkit, Composite parent, ConfigurationMethod m) {
        LabelPositionPresenter presenter = new LabelPositionPresenter(toolkit, parent);
        presenter.setMethod(m);
        return presenter;
      }
    });
  }

  @Test
  public void testLabelHorizontalAlignmentPresenter() throws Exception {
    doPresenterTest(new AbstractPresenterTestInfo("presenter.test.client.ui.forms.DesktopForm.MainBox.LabelHorizontalAlignmentTestField") {

      @Override
      public long getMaxPresenterCreationDuration() {
        return getReferenceDuration("testLabelHorizontalAlignmentPresenter");
      }

      @Override
      public AbstractMethodPresenter createPresenter(PropertyViewFormToolkit toolkit, Composite parent, ConfigurationMethod m) {
        LabelHorizontalAlignmentPresenter presenter = new LabelHorizontalAlignmentPresenter(toolkit, parent);
        presenter.setMethod(m);
        return presenter;
      }
    });
  }
}
