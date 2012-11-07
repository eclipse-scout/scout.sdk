package org.eclipse.scout.sdk.ui.extensions;

import org.eclipse.scout.sdk.ui.view.outline.OutlinePasteTargetEvent;

public interface IPasteTargetDelegator {

  boolean performPaste(OutlinePasteTargetEvent event);
}
