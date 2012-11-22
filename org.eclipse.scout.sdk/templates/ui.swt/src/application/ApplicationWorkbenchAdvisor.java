package @@BUNDLE_SWT_NAME@@.application;

import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import @@BUNDLE_SWT_NAME@@.perspective.Perspective;

/** <h3>ApplicationWorkbenchAdvisor</h3>
 *  Used for getting the initial perspective.
 */
public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

  @Override
  public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
  	return new ApplicationWorkbenchWindowAdvisor(configurer);
  }

  @Override
	public String getInitialWindowPerspectiveId() {
		return Perspective.ID;
	}
}
