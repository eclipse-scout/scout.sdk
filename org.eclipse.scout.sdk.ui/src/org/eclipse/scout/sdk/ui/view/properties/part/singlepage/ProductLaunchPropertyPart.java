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
package org.eclipse.scout.sdk.ui.view.properties.part.singlepage;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.properties.part.ISection;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.ProductLaunchPresenter;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>ServicePropertyPart</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 22.07.2010
 */
public class ProductLaunchPropertyPart extends AbstractSinglePageSectionBasedViewPart {
  private static final String SECTION_ID_LINKS = "section.links";

  final IType basicPermission = TypeUtility.getType(RuntimeClasses.BasicPermission);
  final IType iForm = TypeUtility.getType(RuntimeClasses.IForm);

  public ProductLaunchPropertyPart() {
  }

  @Override
  protected void createSections() {
    // link area
    ISection linkSection = createSection(SECTION_ID_LINKS, Texts.get("Links"));
    fillLinkSection(linkSection.getSectionClient());
    super.createSections();
  }

  protected void fillLinkSection(Composite parent) {
    IScoutBundle bundle = getPage().getScoutResource();
    if (bundle != null) {
      IResource resource = bundle.getProject().findMember(SdkProperties.PRODUCT_FOLDER);
      if (resource != null && resource.exists() && resource.getType() == IResource.FOLDER) {
        // spider products
        IFolder productFolder = (IFolder) resource;
        P_ProductResourceVisitor productVisitor = new P_ProductResourceVisitor();
        try {
          productFolder.accept(productVisitor);
        }
        catch (CoreException e) {
          ScoutSdkUi.logError("error during visiting folder '" + productFolder.getFullPath() + "'.", e);
        }
        for (IFile productFile : productVisitor.getProductFiles()) {
          ProductLaunchPresenter p = new ProductLaunchPresenter(getFormToolkit(), parent, productFile, bundle);
          GridData layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
          layoutData.widthHint = 200;
          p.getContainer().setLayoutData(layoutData);
        }
      }
    }
  }

  private class P_ProductResourceVisitor implements IResourceVisitor {
    private ArrayList<IFile> m_productFiels = new ArrayList<IFile>();

    @Override
    public boolean visit(IResource resource) throws CoreException {
      if (resource.getType() == IResource.FILE && resource.getName().matches(".*\\.product")) {
        m_productFiels.add((IFile) resource);
      }
      else if (resource.getType() == IResource.FOLDER) {
        return true;
      }
      return false;
    }

    private IFile[] getProductFiles() {
      return m_productFiels.toArray(new IFile[m_productFiels.size()]);
    }
  }
}
