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
package org.eclipse.scout.sdk.compatibility.v44.internal;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.pde.core.target.ITargetDefinition;
import org.eclipse.pde.core.target.ITargetHandle;
import org.eclipse.pde.core.target.ITargetLocation;
import org.eclipse.pde.core.target.ITargetPlatformService;
import org.eclipse.pde.core.target.LoadTargetDefinitionJob;
import org.eclipse.pde.core.target.NameVersionDescriptor;
import org.eclipse.pde.internal.core.target.ExternalFileTargetHandle;
import org.eclipse.pde.internal.core.target.IUBundleContainer;
import org.eclipse.pde.internal.core.target.WorkspaceFileTargetHandle;
import org.eclipse.scout.sdk.compatibility.P2Utility;
import org.eclipse.scout.sdk.compatibility.PlatformVersionUtility;
import org.eclipse.scout.sdk.compatibility.internal.FeatureDefinition;
import org.eclipse.scout.sdk.compatibility.internal.ScoutCompatibilityActivator;
import org.eclipse.scout.sdk.compatibility.internal.service.ITargetPlatformCompatService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@SuppressWarnings("restriction")
public class TargetPlatformCompatService implements ITargetPlatformCompatService {

  private interface ITargetFileModification {
    void contribute(List<ITargetLocation> liveList, Map<IUBundleContainer, Set<FeatureDefinition>> features, ITargetPlatformService svc) throws CoreException;
  }

  @Override
  public IStatus resolveTargetPlatform(IFile targetFile, boolean loadPlatform, IProgressMonitor monitor) throws CoreException {
    ITargetPlatformService targetService = ScoutCompatibilityActivator.getDefault().acquireService(ITargetPlatformService.class);
    ITargetHandle handle = targetService.getTarget(targetFile);
    ITargetDefinition def = handle.getTargetDefinition();
    return resolveTarget(def, loadPlatform, monitor);
  }

  @Override
  public URI getCurrentTargetFile() throws CoreException {
    ITargetPlatformService targetService = ScoutCompatibilityActivator.getDefault().acquireService(ITargetPlatformService.class);
    ITargetHandle workspaceTargetHandle = targetService.getWorkspaceTargetHandle();
    if (workspaceTargetHandle instanceof WorkspaceFileTargetHandle && workspaceTargetHandle.exists()) {
      IFile f = ((WorkspaceFileTargetHandle) workspaceTargetHandle).getTargetFile();
      if (f != null && f.exists()) {
        return f.getLocationURI();
      }
    }
    else if (workspaceTargetHandle instanceof ExternalFileTargetHandle && workspaceTargetHandle.exists()) {
      return ((ExternalFileTargetHandle) workspaceTargetHandle).getLocation();
    }
    return null;
  }

  @Override
  public IStatus resolveTargetPlatform(Set<File> absolutePaths, String targetName, boolean loadPlatform, IProgressMonitor monitor) throws CoreException {
    ITargetPlatformService tpService = ScoutCompatibilityActivator.getDefault().acquireService(ITargetPlatformService.class);
    ITargetDefinition targetDef = tpService.newTarget();
    targetDef.setName(targetName);

    List<ITargetLocation> bundleContainers = new ArrayList<ITargetLocation>();
    for (File dir : absolutePaths) {
      bundleContainers.add(tpService.newDirectoryLocation(dir.getAbsolutePath()));
    }
    targetDef.setTargetLocations(bundleContainers.toArray(new ITargetLocation[bundleContainers.size()]));
    targetDef.setArch(Platform.getOSArch());
    targetDef.setOS(Platform.getOS());
    targetDef.setWS(Platform.getWS());
    targetDef.setNL(Platform.getNL());
    tpService.saveTargetDefinition(targetDef);

    return resolveTarget(targetDef, loadPlatform, monitor);
  }

  private IStatus resolveTarget(ITargetDefinition def, boolean loadPlatform, IProgressMonitor monitor) {
    IStatus result = def.resolve(monitor);
    if (loadPlatform && result.isOK()) {
      LoadTargetDefinitionJob loadJob = new LoadTargetDefinitionJob(def);
      result = loadJob.run(monitor);
    }
    return result;
  }

  @Override
  public void removeInstallableUnitsFromTarget(IFile targetFile, final String[] unitIds) throws CoreException {
    modifyTargetFile(targetFile, new ITargetFileModification() {
      @Override
      public void contribute(List<ITargetLocation> liveList, Map<IUBundleContainer, Set<FeatureDefinition>> features, ITargetPlatformService svc) throws CoreException {
        for (Set<FeatureDefinition> featureOfContainer : features.values()) {
          Iterator<FeatureDefinition> it = featureOfContainer.iterator();
          while (it.hasNext()) {
            FeatureDefinition def = it.next();
            for (String featureToRemove : unitIds) {
              if (featureToRemove != null && featureToRemove.equals(def.id)) {
                it.remove();
              }
            }
          }
        }
      }
    });
  }

  @Override
  public void addDirectoryLocationToTarget(IFile targetFile, final String[] dirs) throws CoreException {
    modifyTargetFile(targetFile, new ITargetFileModification() {
      @Override
      public void contribute(List<ITargetLocation> liveList, Map<IUBundleContainer, Set<FeatureDefinition>> features, ITargetPlatformService svc) {
        for (String dir : dirs) {
          if (dir != null && dir.trim().length() > 0) {
            liveList.add(svc.newDirectoryLocation(dir));
          }
        }
      }
    });
  }

  private static IUBundleContainer getContainer(String uri, Set<IUBundleContainer> containers) throws URISyntaxException {
    URI searchUri = URIUtil.fromString(uri);
    for (IUBundleContainer container : containers) {
      for (URI repo : container.getRepositories()) {
        if (URIUtil.sameURI(searchUri, repo)) {
          return container;
        }
      }
    }
    return null;
  }

  @Override
  public void addInstallableUnitToTarget(IFile targetFile, final String unitId, final String version, final String repository, final IProgressMonitor monitor) throws CoreException {
    modifyTargetFile(targetFile, new ITargetFileModification() {
      @Override
      public void contribute(List<ITargetLocation> liveList, Map<IUBundleContainer, Set<FeatureDefinition>> features, ITargetPlatformService svc) throws CoreException {
        try {
          String ver = version;
          if (ver == null || ver.trim().length() < 1) {
            ver = P2Utility.getLatestVersion(unitId, URIUtil.fromString(repository), monitor);
          }

          IUBundleContainer container = getContainer(repository, features.keySet());
          FeatureDefinition fd = new FeatureDefinition();
          fd.id = unitId;
          fd.version = ver;
          if (container == null) {
            URI uri = URIUtil.fromString(repository);
            IUBundleContainer newIULocation = (IUBundleContainer) svc.newIULocation(new String[]{unitId}, new String[]{ver}, new URI[]{uri}, 0);
            Set<FeatureDefinition> set = new HashSet<FeatureDefinition>(1);
            set.add(fd);
            features.put(newIULocation, set);
          }
          else {
            features.get(container).add(fd);
          }
        }
        catch (URISyntaxException e) {
          throw new CoreException(new Status(IStatus.ERROR, ScoutCompatibilityActivator.PLUGIN_ID, "Unable to parse url '" + repository + "'.", e));
        }
      }
    });
  }

  private List<NameVersionDescriptor> getFeatures(IUBundleContainer iubc) throws CoreException {
    final String INSTALLABLE_UNIT = "unit"; // from org.eclipse.pde.internal.core.target.TargetDefinitionPersistenceHelper.INSTALLABLE_UNIT
    final String ATTR_ID = "id"; // from org.eclipse.pde.internal.core.target.TargetDefinitionPersistenceHelper.ATTR_ID
    final String ATTR_VERSION = "version"; // from org.eclipse.pde.internal.core.target.TargetDefinitionPersistenceHelper.ATTR_VERSION
    try {
      String xml = iubc.serialize();
      DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = docBuilder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
      NodeList childNodes = document.getDocumentElement().getChildNodes();
      ArrayList<NameVersionDescriptor> result = new ArrayList<NameVersionDescriptor>(childNodes.getLength());
      for (int i = 0; i < childNodes.getLength(); i++) {
        Node n = childNodes.item(i);
        if (n.getNodeType() == Node.ELEMENT_NODE && INSTALLABLE_UNIT.equals(n.getNodeName())) {
          Element e = (Element) n;
          if (e.hasAttribute(ATTR_ID)) {
            String id = e.getAttribute(ATTR_ID);
            String version = e.getAttribute(ATTR_VERSION);
            result.add(new NameVersionDescriptor(id, version));
          }
        }
      }
      return result;
    }
    catch (Exception e) {
      throw new CoreException(new Status(IStatus.ERROR, ScoutCompatibilityActivator.PLUGIN_ID, "Unable to parse installable units.", e));
    }
  }

  /**
   * workaround for bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=432494
   */
  private Set<FeatureDefinition> getValueOf(Map<IUBundleContainer, Set<FeatureDefinition>> map, IUBundleContainer objectToFind) {
    for (Entry<IUBundleContainer, Set<FeatureDefinition>> entry : map.entrySet()) {
      if (objectToFind != null && objectToFind.equals(entry.getKey())) {
        return entry.getValue();
      }
    }
    return null;
  }

  private void modifyTargetFile(IFile targetFile, ITargetFileModification modification) throws CoreException {
    ITargetPlatformService svc = ScoutCompatibilityActivator.getDefault().acquireService(ITargetPlatformService.class);
    ITargetHandle handle = svc.getTarget(targetFile);
    ITargetDefinition td = handle.getTargetDefinition();
    ITargetLocation[] features = td.getTargetLocations();
    int size = 1;
    if (features != null) {
      size += features.length;
    }

    Map<IUBundleContainer, Set<FeatureDefinition>> p2FeatureMap = new HashMap<IUBundleContainer, Set<FeatureDefinition>>(size);
    List<ITargetLocation> locations = new ArrayList<ITargetLocation>(size);
    if (features != null) {
      for (ITargetLocation container : features) {
        if (container instanceof IUBundleContainer) {
          IUBundleContainer iubc = (IUBundleContainer) container;
          List<NameVersionDescriptor> fs = getFeatures(iubc);

          Set<FeatureDefinition> featureSet = getValueOf(p2FeatureMap, iubc);
          if (featureSet == null) {
            featureSet = new HashSet<FeatureDefinition>(fs.size());
            p2FeatureMap.put(iubc, featureSet);
          }

          for (NameVersionDescriptor f : fs) {
            FeatureDefinition fd = new FeatureDefinition();
            fd.id = f.getId();
            fd.version = f.getVersion();
            featureSet.add(fd);
          }
        }
        else {
          locations.add(container);
        }
      }
    }

    modification.contribute(locations, p2FeatureMap, svc);

    for (Entry<IUBundleContainer, Set<FeatureDefinition>> entry : p2FeatureMap.entrySet()) {
      if (!entry.getValue().isEmpty()) {
        String[] unitIds = new String[entry.getValue().size()];
        String[] versions = new String[entry.getValue().size()];
        int i = 0;
        for (FeatureDefinition f : entry.getValue()) {
          unitIds[i] = f.id;
          if (f.version == null) {
            versions[i] = PlatformVersionUtility.EMPTY_VERSION_STR;
          }
          else {
            versions[i] = f.version;
          }
          i++;
        }

        locations.add(svc.newIULocation(unitIds, versions, entry.getKey().getRepositories(), IUBundleContainer.INCLUDE_CONFIGURE_PHASE | IUBundleContainer.INCLUDE_SOURCE));
      }
    }

    td.setTargetLocations(locations.toArray(new ITargetLocation[locations.size()]));
    svc.saveTargetDefinition(td);
  }
}
