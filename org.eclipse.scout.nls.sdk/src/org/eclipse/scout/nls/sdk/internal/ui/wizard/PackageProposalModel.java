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
package org.eclipse.scout.nls.sdk.internal.ui.wizard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.scout.nls.sdk.internal.jdt.NlsJdtHandler;
import org.eclipse.swt.graphics.Image;

public class PackageProposalModel extends LabelProvider implements IContentProposalProvider {

  private LabelProvider m_packageLabelProvider = new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);
  private TreeSet<String> m_proposalSorting = new TreeSet<String>();
  private HashMap<String, PackageProposal> m_proposals = new HashMap<String, PackageProposal>();

  public PackageProposalModel() {

  }

  public void setProject(IProject root) {
    m_proposals.clear();
    for (IPackageFragment frag : NlsJdtHandler.getPluginPackages(root)) {
      PackageProposal p = new PackageProposal(frag);
      m_proposals.put(p.getContent(), p);
      m_proposalSorting.add(p.getContent());
    }
  }

  public IContentProposal[] getProposals(String contents, int position) {
    ArrayList<PackageProposal> list = new ArrayList<PackageProposal>();
    for (String proptext : m_proposalSorting) {
      if (proptext.startsWith(contents)) {
        list.add(m_proposals.get(proptext));
      }
    }
    return list.toArray(new IContentProposal[list.size()]);
  }

  @Override
  public Image getImage(Object element) {
    PackageProposal prop = (PackageProposal) element;
    return m_packageLabelProvider.getImage(prop.getPackage());
  }

  @Override
  public String getText(Object element) {
    PackageProposal prop = (PackageProposal) element;
    return m_packageLabelProvider.getText(prop.getPackage());
  }

}
