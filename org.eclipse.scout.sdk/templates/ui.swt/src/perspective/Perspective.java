package @@BUNDLE_SWT_NAME@@.perspective;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {

  public static final String ID = Perspective.class.getName();

  @Override
	public void createInitialLayout(IPageLayout layout) {
    layout.setEditorAreaVisible(false);
  }
}
