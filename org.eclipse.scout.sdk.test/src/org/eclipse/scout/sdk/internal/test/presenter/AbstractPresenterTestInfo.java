package org.eclipse.scout.sdk.internal.test.presenter;

import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractMethodPresenter;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.swt.widgets.Composite;

public abstract class AbstractPresenterTestInfo {

  private final String m_typeFqn;

  public AbstractPresenterTestInfo(String typeFqn) {
    m_typeFqn = typeFqn;
  }

  /**
   * used to initialize the presenter test info. the call of this method is not part of the performance testing.
   */
  public void init(ConfigurationMethod testMethod) {

  }

  /**
   * Creates and initializes a presenter for the given method.
   * 
   * @param toolkit
   *          The toolkit to use.
   * @param parent
   *          The parent composite
   * @param m
   *          the method for which the presenter should be created.
   * @return The created and initialized presenter.
   */
  public abstract AbstractMethodPresenter createPresenter(PropertyViewFormToolkit toolkit, Composite parent, ConfigurationMethod m);

  /**
   * Specifies the maximum allowed duration in nanoseconds to create and initialize this presenter (call of the
   * <code>createPresenter</code> method).<br>
   * If the <code>createPresenter</code> takes longer than the value returned by this method, the test for this
   * presenter will fail.
   * 
   * @return The maximum accepted duration (nanoseconds) of <code>createPresenter</code>.
   */
  public abstract long getMaxPresenterCreationDuration();

  public final String getTestTypeFqn() {
    return m_typeFqn;
  }
}
