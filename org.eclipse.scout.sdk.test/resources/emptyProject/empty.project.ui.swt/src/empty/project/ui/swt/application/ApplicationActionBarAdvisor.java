package empty.project.ui.swt.application;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

/**
 * <h3>ApplicationActionBarAdvisor</h3> Used for menu contributions.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

  public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
    super(configurer);
  }

  @Override
  protected void fillMenuBar(IMenuManager menuBar) {
    menuBar.add(new MenuManager("", IWorkbenchActionConstants.M_FILE));
  }
}
