/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.nls;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.core.model.api.Flags.isAbstract;
import static org.eclipse.scout.sdk.core.s.nls.properties.AbstractTranslationPropertiesFile.parseLanguageFromFileName;
import static org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment.toScoutProgress;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.scout.sdk.core.apidef.IClassNameSupplier;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.nls.ITranslationStore;
import org.eclipse.scout.sdk.core.s.nls.ITranslationStoreSupplier;
import org.eclipse.scout.sdk.core.s.nls.properties.EditableTranslationFile;
import org.eclipse.scout.sdk.core.s.nls.properties.ITranslationPropertiesFile;
import org.eclipse.scout.sdk.core.s.nls.properties.PropertiesTextProviderService;
import org.eclipse.scout.sdk.core.s.nls.properties.PropertiesTranslationStore;
import org.eclipse.scout.sdk.core.s.nls.properties.ReadOnlyTranslationFile;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment;
import org.eclipse.scout.sdk.s2e.environment.EclipseProgress;
import org.eclipse.scout.sdk.s2e.util.ApiHelper;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.scout.sdk.s2e.util.JdtUtils.PublicPrimaryTypeFilter;

/**
 * <h3>{@link EclipseTranslationStoreSupplier}</h3>
 *
 * @since 7.0.0
 */
public class EclipseTranslationStoreSupplier implements ITranslationStoreSupplier {

  @Override
  public Stream<ITranslationStore> visibleStoresForJavaModule(Path modulePath, IEnvironment env, IProgress progress) {
    var e = EclipseEnvironment.narrow(env);
    var p = toScoutProgress(progress);
    return e.findJavaProject(modulePath)
        .map(jp -> visibleTranslationStores(jp, e, p))
        .orElseGet(Stream::empty);
  }

  private static Stream<ITranslationStore> visibleTranslationStores(IJavaProject jp, EclipseEnvironment env, EclipseProgress progress) {
    progress.init(20, "Search properties text provider services.");

    var dynamicNlsTextProviderServices = resolveSubClasses(jp, IScoutApi::AbstractDynamicNlsTextProviderService, env, progress.newChild(10)).collect(toList());
    var loopProgress = progress
        .newChild(10)
        .setWorkRemaining(dynamicNlsTextProviderServices.size());

    return dynamicNlsTextProviderServices.stream()
        .map(svc -> createTranslationStore(svc, loopProgress))
        .flatMap(Optional::stream);
  }

  private static Stream<org.eclipse.scout.sdk.core.model.api.IType> resolveSubClasses(IJavaProject jp, Function<IScoutApi, IClassNameSupplier> nameFunction, EclipseEnvironment env, EclipseProgress progress) {
    var scoutApi = ApiHelper.scoutApiFor(jp, env);
    if (scoutApi.isEmpty()) {
      return Stream.empty();
    }

    Predicate<IType> filter = new PublicPrimaryTypeFilter() {
      @Override
      public boolean test(IType candidate) {
        try {
          // only accept non-abstract public primary classes with source available
          return super.test(candidate) && !isAbstract(candidate.getFlags()) && SourceRange.isAvailable(candidate.getSourceRange());
        }
        catch (JavaModelException e) {
          // this element seems to be corrupt -> ignore
          SdkLog.warning("Attempt to access source range of type '{}' failed. Type will be skipped.", candidate.getFullyQualifiedName(), e);
          return false;
        }
      }
    };

    var fqn = nameFunction.apply(scoutApi.get()).fqn();
    return JdtUtils.findTypesInStrictHierarchy(jp, fqn, progress.monitor(), filter).stream()
        .map(env::toScoutType);
  }

  @Override
  public Optional<ITranslationStore> createStoreForService(org.eclipse.scout.sdk.core.model.api.IType textService, IProgress progress) {
    progress.init(1, "Search properties text provider service.");
    return createTranslationStore(textService, progress);
  }

  @Override
  public Stream<org.eclipse.scout.sdk.core.model.api.IType> visibleTextContributorsForJavaModule(Path modulePath, IEnvironment env, IProgress progress) {
    progress.init(20, "Search UiTextContributors.");
    var e = EclipseEnvironment.narrow(env);
    var p = toScoutProgress(progress);
    return e.findJavaProject(modulePath)
        .map(jp -> resolveSubClasses(jp, IScoutApi::IUiTextContributor, e, p))
        .orElseGet(Stream::empty);
  }

  private static Optional<ITranslationStore> createTranslationStore(org.eclipse.scout.sdk.core.model.api.IType textProviderServiceType, IProgress progress) {
    return PropertiesTextProviderService.create(textProviderServiceType)
        .map(PropertiesTranslationStore::new)
        .filter(s -> loadStore(s, progress.newChild(1)))
        .map(Function.identity());
  }

  private static boolean loadStore(PropertiesTranslationStore store, IProgress progress) {
    var jdtType = EclipseEnvironment.toJdtType(store.service().type());
    if (!JdtUtils.exists(jdtType)) {
      SdkLog.warning("Type '{}' could not be found.", store.service().type().name());
      return false;
    }

    if (jdtType.isBinary()) {
      return loadStoreFromPlatform(jdtType, store, progress);
    }
    return loadStoreFromWorkspace(jdtType, store, progress);
  }

  private static boolean loadStoreFromWorkspace(IType jdtType, PropertiesTranslationStore store, IProgress progress) {
    try {
      store.load(filesFromWorkspace(jdtType, store.service()), progress);
      return true;
    }
    catch (JavaModelException e) {
      SdkLog.warning("Unable to load properties files of type '{}'.", jdtType.getFullyQualifiedName(), e);
      return false;
    }
  }

  private static boolean loadStoreFromPlatform(IType jdtType, PropertiesTranslationStore store, IProgress progress) {
    var r = JdtUtils.getSourceFolder(jdtType);
    if (!JdtUtils.exists(r)) {
      SdkLog.warning("Could not find text resource for type '{}'.", jdtType.getFullyQualifiedName());
      return false;
    }

    try {
      store.load(filesFromPlatform(r, store), progress);
      return true;
    }
    catch (JavaModelException e) {
      SdkLog.warning("Unable to load properties files of type '{}'.", jdtType.getFullyQualifiedName(), e);
      return false;
    }
  }

  private static Collection<ITranslationPropertiesFile> filesFromWorkspace(IJavaElement jdtType, PropertiesTextProviderService service) throws JavaModelException {
    IPath translationPath = new org.eclipse.core.runtime.Path(service.folder());
    return getFiles(jdtType.getJavaProject(), translationPath, service.filePrefix());
  }

  private static List<ITranslationPropertiesFile> getFiles(IJavaProject toLookAt, IPath path, String fileNamePrefix) throws JavaModelException {
    return getFoldersOfProject(toLookAt, path)
        .flatMap(EclipseTranslationStoreSupplier::filesInFolder)
        .map(file -> toEditableTranslationFile(file, fileNamePrefix))
        .flatMap(Optional::stream)
        .collect(toList());
  }

  private static Optional<ITranslationPropertiesFile> toEditableTranslationFile(IResource file, String fileNamePrefix) {
    return parseLanguageFromFileName(file.getName(), fileNamePrefix)
        .map(lang -> new EditableTranslationFile(file.getLocation().toFile().toPath(), lang));
  }

  private static Stream<IFolder> getFoldersOfProject(IJavaProject project, IPath path) throws JavaModelException {
    if (!JdtUtils.exists(project) || !project.getProject().isAccessible()) {
      return Stream.empty();
    }

    // check runtime dir
    var root = ResourcesPlugin.getWorkspace().getRoot();
    var clEntries = project.getRawClasspath();
    Collection<IFolder> folders = Arrays.stream(clEntries)
        .filter(entry -> entry.getEntryKind() == IClasspathEntry.CPE_SOURCE)
        .map(entry -> entry.getPath().append(path))
        .map(root::getFolder)
        .filter(Objects::nonNull)
        .filter(IResource::exists)
        .collect(toList());

    // check path relative to project
    var foundFolder = project.getProject().getFolder(path);
    if (foundFolder != null && foundFolder.exists()) {
      folders.add(foundFolder);
    }
    return folders.stream();
  }

  private static Stream<IFile> filesInFolder(IFolder folder) {
    try {
      return Arrays.stream(folder.members(IResource.NONE))
          .filter(member -> member instanceof IFile)
          .map(member -> (IFile) member);
    }
    catch (CoreException e) {
      throw new SdkException("Cannot read content of folder '{}'.", folder, e);
    }
  }

  private static Collection<ITranslationPropertiesFile> filesFromPlatform(IPackageFragmentRoot r, @SuppressWarnings("TypeMayBeWeakened") PropertiesTranslationStore store) throws JavaModelException {
    var delim = '.';
    var pckg = store.service().folder().replace(PropertiesTextProviderService.FOLDER_SEGMENT_DELIMITER, delim);
    var textFolder = r.getPackageFragment(pckg);
    if (!JdtUtils.exists(textFolder)) {
      SdkLog.warning("Folder '{}' could not be found in '{}'. Will be ignored.", store.service().folder(), r.getElementName());
      return emptyList();
    }

    Collection<ITranslationPropertiesFile> translationFiles = new ArrayList<>();
    var fileNamePrefix = store.service().filePrefix();
    for (var o : textFolder.getNonJavaResources()) {
      if (o instanceof IStorage) {
        var f = (IStorage) o;
        parseLanguageFromFileName(f.getName(), fileNamePrefix)
            .map(lang -> new ReadOnlyTranslationFile(() -> contentsOf(f), lang, f))
            .ifPresent(translationFiles::add);
      }
    }
    return translationFiles;
  }

  private static InputStream contentsOf(IStorage storage) {
    try {
      return storage.getContents();
    }
    catch (CoreException e) {
      throw new SdkException(e);
    }
  }
}
