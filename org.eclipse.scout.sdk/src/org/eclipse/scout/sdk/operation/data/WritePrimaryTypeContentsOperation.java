package org.eclipse.scout.sdk.operation.data;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * Operation for writing the contents of a primary type (i.e. a complete {@link ICompilationUnit}).
 * 
 * @since 3.10.0-M1
 */
public class WritePrimaryTypeContentsOperation implements IOperation {
  private final IType m_type;
  private final String m_icuSource;

  public WritePrimaryTypeContentsOperation(IType type, String icuSource) {
    m_type = type;
    m_icuSource = icuSource;
  }

  @Override
  public String getOperationName() {
    return "Writing contents of '" + m_type.getElementName() + "'.";
  }

  @Override
  public void validate() throws IllegalArgumentException {
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    ICompilationUnit icu = m_type.getCompilationUnit();
    if (icu != null) {
      try {
        icu.becomeWorkingCopy(monitor);

        // store new derived type content to buffer
        icu.getBuffer().setContents(ScoutUtility.cleanLineSeparator(m_icuSource, icu));

        // format buffer and organize imports
        JavaElementFormatOperation formatSourceOperation = new JavaElementFormatOperation(icu, true);
        formatSourceOperation.validate();
        formatSourceOperation.run(monitor, workingCopyManager);

        // save buffer
        icu.getBuffer().save(monitor, true);
        icu.commitWorkingCopy(true, monitor);
      }
      catch (Exception e) {
        ScoutSdk.logError("could not store derived model class '" + m_type.getFullyQualifiedName() + "'.", e);
      }
      finally {
        icu.discardWorkingCopy();
      }
    }
  }
}
