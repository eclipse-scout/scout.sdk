package org.eclipse.scout.sdk.internal.test.presenter;

import java.util.ArrayList;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.TuningUtility;
import org.eclipse.scout.sdk.test.AbstractScoutSdkTest;
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
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.LongPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.LookupCallProposalPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.LookupServiceProposalPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.MasterFieldPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.NlsTextPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.OutlinesPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.SearchFormPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.StringPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.VerticalAglinmentPresenter;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractMethodPresenter;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.config.ConfigPropertyType;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class MethodPresenterTest extends AbstractScoutSdkTest {

  private final static String SHARED_PROJECT = "presenter.test.shared";
  private final static String SERVER_PROJECT = "presenter.test.server";
  private final static String CLIENT_PROJECT = "presenter.test.client";

  private static FormToolkit default_toolkit;
  private static Composite default_parent;

  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    setupWorkspace("presenter", SHARED_PROJECT, SERVER_PROJECT, CLIENT_PROJECT);

    default_toolkit = new FormToolkit(Display.getDefault());
    default_parent = Display.getDefault().getActiveShell();
  }

  @AfterClass
  public static void cleanUpWorkspace() throws Exception {
    deleteProjects(SHARED_PROJECT, SERVER_PROJECT, CLIENT_PROJECT);
    default_toolkit = null;
    default_parent = null;
    TuningUtility.finishAll(true);
  }

  private void doPresenterTest(AbstractPresenterTestInfo testInfo) throws Exception {
    IType testType = TypeUtility.getType(testInfo.getTestTypeFqn());
    Assert.assertTrue(TypeUtility.exists(testType));

    ConfigPropertyType configPropertyType = new ConfigPropertyType(testType);
    ConfigurationMethod[] configPropertyMethods = configPropertyType.getConfigurationMethods(ConfigurationMethod.PROPERTY_METHOD);
    Assert.assertNotNull(configPropertyMethods);
    Assert.assertTrue(configPropertyMethods.length > 0);

    ArrayList<ConfigurationMethod> implementedMethods = new ArrayList<ConfigurationMethod>(configPropertyMethods.length);
    for (ConfigurationMethod method : configPropertyMethods) {
      if (method != null) {
        if (method.isImplemented()) {
          implementedMethods.add(method);
        }
      }
    }
    Assert.assertTrue(implementedMethods.size() == 1);

    long duration = -1;
    AbstractMethodPresenter presenter = null;
    try {
      TuningUtility.startTimer();
      presenter = testInfo.createPresenter(default_toolkit, default_parent, implementedMethods.get(0));
    }
    finally {
      duration = TuningUtility.stopTimer("", false, false);
    }

    Assert.assertNotNull(presenter);
    Assert.assertTrue(duration > 0);
    if (duration > testInfo.getMaxPresenterCreationDuration()) {
      throw new Exception("Creation of presenter '" + presenter + "' took " + duration + "ns but should be faster than " + testInfo.getMaxPresenterCreationDuration() + "ns.");
    }
  }

  @Test
  public void testBooleanPresenter() throws Exception {
    doPresenterTest(new AbstractPresenterTestInfo("presenter.test.client.ui.forms.DesktopForm.MainBox.BooleanPresenterTestField") {

      @Override
      public long getMaxPresenterCreationDuration() {
        return 100 * 1000 * 1000; //100ms
      }

      @Override
      public AbstractMethodPresenter createPresenter(FormToolkit toolkit, Composite parent, ConfigurationMethod m) {
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
        return 400 * 1000 * 1000;
      }

      @Override
      public AbstractMethodPresenter createPresenter(FormToolkit toolkit, Composite parent, ConfigurationMethod m) {
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
        return 1100 * 1000 * 1000;
      }

      @Override
      public AbstractMethodPresenter createPresenter(FormToolkit toolkit, Composite parent, ConfigurationMethod m) {
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
        return 500 * 1000 * 1000;
      }

      @Override
      public AbstractMethodPresenter createPresenter(FormToolkit toolkit, Composite parent, ConfigurationMethod m) {
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
        return 300 * 1000 * 1000;
      }

      @Override
      public AbstractMethodPresenter createPresenter(FormToolkit toolkit, Composite parent, ConfigurationMethod m) {
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
        return 400 * 1000 * 1000;
      }

      @Override
      public AbstractMethodPresenter createPresenter(FormToolkit toolkit, Composite parent, ConfigurationMethod m) {
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
        return 400 * 1000 * 1000;
      }

      @Override
      public AbstractMethodPresenter createPresenter(FormToolkit toolkit, Composite parent, ConfigurationMethod m) {
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
        return 100 * 1000 * 1000;
      }

      @Override
      public AbstractMethodPresenter createPresenter(FormToolkit toolkit, Composite parent, ConfigurationMethod m) {
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
        return 100 * 1000 * 1000;
      }

      @Override
      public AbstractMethodPresenter createPresenter(FormToolkit toolkit, Composite parent, ConfigurationMethod m) {
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
        return 100 * 1000 * 1000;
      }

      @Override
      public AbstractMethodPresenter createPresenter(FormToolkit toolkit, Composite parent, ConfigurationMethod m) {
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
        return 100 * 1000 * 1000;
      }

      @Override
      public AbstractMethodPresenter createPresenter(FormToolkit toolkit, Composite parent, ConfigurationMethod m) {
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
        return 100 * 1000 * 1000;
      }

      @Override
      public AbstractMethodPresenter createPresenter(FormToolkit toolkit, Composite parent, ConfigurationMethod m) {
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
        return 500 * 1000 * 1000;
      }

      @Override
      public AbstractMethodPresenter createPresenter(FormToolkit toolkit, Composite parent, ConfigurationMethod m) {
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
        return 100 * 1000 * 1000;
      }

      @Override
      public AbstractMethodPresenter createPresenter(FormToolkit toolkit, Composite parent, ConfigurationMethod m) {
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
        return 100 * 1000 * 1000;
      }

      @Override
      public AbstractMethodPresenter createPresenter(FormToolkit toolkit, Composite parent, ConfigurationMethod m) {
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
        return 100 * 1000 * 1000;
      }

      @Override
      public AbstractMethodPresenter createPresenter(FormToolkit toolkit, Composite parent, ConfigurationMethod m) {
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
        return 100 * 1000 * 1000;
      }

      @Override
      public AbstractMethodPresenter createPresenter(FormToolkit toolkit, Composite parent, ConfigurationMethod m) {
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
        return 100 * 1000 * 1000;
      }

      @Override
      public AbstractMethodPresenter createPresenter(FormToolkit toolkit, Composite parent, ConfigurationMethod m) {
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
        return 100 * 1000 * 1000;
      }

      @Override
      public AbstractMethodPresenter createPresenter(FormToolkit toolkit, Composite parent, ConfigurationMethod m) {
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
        return 100 * 1000 * 1000;
      }

      @Override
      public AbstractMethodPresenter createPresenter(FormToolkit toolkit, Composite parent, ConfigurationMethod m) {
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
      public long getMaxPresenterCreationDuration() {
        return 100 * 1000 * 1000;
      }

      @Override
      public AbstractMethodPresenter createPresenter(FormToolkit toolkit, Composite parent, ConfigurationMethod m) {
        NlsTextPresenter presenter = new NlsTextPresenter(toolkit, parent);
        presenter.setMethod(m);
        return presenter;
      }
    });
  }
}
