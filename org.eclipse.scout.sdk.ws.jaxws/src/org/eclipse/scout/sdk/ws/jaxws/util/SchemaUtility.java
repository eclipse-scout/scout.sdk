/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.util;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Types;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.wsdl.extensions.schema.SchemaReference;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility.WsdlArtefact.TypeEnum;

public final class SchemaUtility {

  private SchemaUtility() {
  }

  public static void visitArtefacts(IFile wsdlFile, ISchemaArtefactVisitor<IFile> visitor) {
    visit(visitor, new EclipseFileHandle(wsdlFile));
  }

  public static void visitArtefacts(File wsdlFile, ISchemaArtefactVisitor<File> visitor) {
    visit(visitor, new JavaFileHandle(wsdlFile));
  }

  private static <T> void visit(ISchemaArtefactVisitor<T> visitor, IFileHandle<T> wsdlFileHandle) {
    if (wsdlFileHandle == null || !wsdlFileHandle.exists()) {
      return;
    }

    Definition wsdlDefinition = JaxWsSdkUtility.loadWsdlDefinition(wsdlFileHandle);
    if (wsdlDefinition == null) {
      return;
    }

    // root WSDL file
    WsdlArtefact<T> rootWsdlArtefact = new WsdlArtefact<T>(wsdlFileHandle, TypeEnum.RootWsdl, wsdlDefinition);
    visitor.onRootWsdlArtefact(rootWsdlArtefact);

    Set<WsdlArtefact<T>> wsdlArtefacts = new HashSet<WsdlArtefact<T>>();
    wsdlArtefacts.add(rootWsdlArtefact);

    // referenced WSDL files
    Set<WsdlArtefact<T>> referencedWsdlArtefacts = getReferencedWsdlResourcesRec(wsdlFileHandle, wsdlDefinition);
    wsdlArtefacts.addAll(referencedWsdlArtefacts);
    for (WsdlArtefact<T> referencedWsdlArtefact : referencedWsdlArtefacts) {
      visitor.onReferencedWsdlArtefact(referencedWsdlArtefact);
    }

    // set inline schemas to WSDL resources and resolve referenced schema resources
    for (WsdlArtefact<T> wsdlArtefact : wsdlArtefacts) {
      if (wsdlArtefact.getWsdlDefinition() == null) {
        JaxWsSdk.logWarning("Unexpected: WSDL definition should not be null '" + wsdlArtefact + "'");
        continue;
      }

      Types types = wsdlArtefact.getWsdlDefinition().getTypes();
      if (types != null) {
        Set<Schema> inlineSchemas = new HashSet<Schema>();
        for (Object type : types.getExtensibilityElements()) {
          if (type instanceof Schema) {
            Schema schema = (Schema) type;
            // inline schema
            inlineSchemas.add(schema);
            // referenced schema resources
            visitReferencedSchemaResources(visitor, wsdlArtefact.getFileHandle(), schema);
          }
        }
        wsdlArtefact.setInlineSchemas(inlineSchemas.toArray(new Schema[inlineSchemas.size()]));
      }
    }
  }

  public static String getSchemaTargetNamespace(Schema schema) {
    if (schema == null) {
      return null;
    }
    if (schema.getElement().hasAttribute("targetNamespace")) {
      return schema.getElement().getAttribute("targetNamespace");
    }
    return null;
  }

  private static <T> Set<WsdlArtefact<T>> getReferencedWsdlResourcesRec(IFileHandle<T> parentWsdlFileHandle, Definition parentWsdlDefinition) {
    if (parentWsdlFileHandle == null || !parentWsdlFileHandle.exists()) {
      return Collections.emptySet();
    }

    IFileHandle<T> folder = parentWsdlFileHandle.getParent();

    Set<WsdlArtefact<T>> wsdlArtefacts = new HashSet<WsdlArtefact<T>>();
    Map<?, ?> importMap = parentWsdlDefinition.getImports();

    for (Object importValue : importMap.values()) {
      if (importValue instanceof List<?>) {
        List<?> importList = (List<?>) importValue;
        for (Object importObject : importList) {
          if (importObject instanceof Import) {
            Import importDirective = (Import) importObject;

            Definition wsdlDefinition = importDirective.getDefinition();
            IFileHandle<T> wsdlFileHandle = folder.getChild(new Path(importDirective.getLocationURI()));

            if (wsdlFileHandle != null) {
              wsdlArtefacts.add(new WsdlArtefact<T>(wsdlFileHandle, TypeEnum.ReferencedWsdl, wsdlDefinition));

              // recursion
              wsdlArtefacts.addAll(getReferencedWsdlResourcesRec(wsdlFileHandle, wsdlDefinition));
            }
          }
        }
      }
    }

    return wsdlArtefacts;
  }

  private static <T> void visitReferencedSchemaResources(ISchemaArtefactVisitor<T> visitor, IFileHandle<T> fileHandle, Schema schema) {
    if (fileHandle == null || !fileHandle.exists() || schema == null) {
      return;
    }
    IFileHandle<T> parentResouce = fileHandle.getParent();

    // Included schemas
    List<?> includes = schema.getIncludes();
    if (includes != null && !includes.isEmpty()) {
      for (Object include : includes) {
        if (!(include instanceof SchemaReference)) {
          continue;
        }
        SchemaReference schemaInclude = (SchemaReference) include;
        String schemaLocationURI = schemaInclude.getSchemaLocationURI();
        if (!StringUtility.hasText(schemaLocationURI)) {
          continue;
        }

        IFileHandle<T> referencedSchemaResource = parentResouce.getChild(new Path(schemaLocationURI));
        if (referencedSchemaResource != null) {
          Schema referencedSchema = schemaInclude.getReferencedSchema();
          SchemaIncludeArtefact<T> artefact = new SchemaIncludeArtefact<T>(referencedSchemaResource, referencedSchema);
          visitor.onSchemaIncludeArtefact(artefact);

          // recursion
          visitReferencedSchemaResources(visitor, referencedSchemaResource, referencedSchema);
        }
      }
    }

    // Imported schemas
    Map<?, ?> importMap = schema.getImports();
    if (importMap != null && !importMap.isEmpty()) {
      for (Object importObject : importMap.values()) {
        if (!(importObject instanceof List<?>)) {
          continue;
        }
        for (Object importDirective : (List<?>) importObject) {
          if (!(importDirective instanceof SchemaImport)) {
            continue;
          }
          SchemaImport schemaImport = (SchemaImport) importDirective;
          String schemaLocationURI = schemaImport.getSchemaLocationURI();
          if (!StringUtility.hasText(schemaLocationURI)) {
            continue;
          }

          IFileHandle<T> referencedSchemaResource = parentResouce.getChild(new Path(schemaLocationURI));
          if (referencedSchemaResource != null) {
            Schema referencedSchema = schemaImport.getReferencedSchema();
            SchemaImportArtefact<T> artefact = new SchemaImportArtefact<T>(referencedSchemaResource, referencedSchema, schemaImport.getNamespaceURI());
            visitor.onSchemaImportArtefact(artefact);

            // recursion
            visitReferencedSchemaResources(visitor, referencedSchemaResource, referencedSchema);
          }
        }
      }
    }
  }

  public static abstract class Artefact<T> {
    private IFileHandle<T> m_fileHandle;

    public Artefact(IFileHandle<T> fileHandle) {
      m_fileHandle = fileHandle;
    }

    public IFileHandle<T> getFileHandle() {
      return m_fileHandle;
    }

    public void setFileHandle(IFileHandle<T> resource) {
      m_fileHandle = resource;
    }

    @Override
    public int hashCode() {
      return m_fileHandle.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      return m_fileHandle.equals(((Artefact) obj).m_fileHandle);
    }
  }

  public static class WsdlArtefact<T> extends Artefact<T> {
    private TypeEnum m_typeEnum;
    private Definition m_wsdlDefintion;
    private Schema[] m_inlineSchemas = new Schema[0];

    public WsdlArtefact(IFileHandle<T> wsdlFileHandle, TypeEnum typeEnum, Definition wsdlDefintion) {
      super(wsdlFileHandle);
      m_typeEnum = typeEnum;
      m_wsdlDefintion = wsdlDefintion;
    }

    public TypeEnum getTypeEnum() {
      return m_typeEnum;
    }

    public void setTypeEnum(TypeEnum typeEnum) {
      m_typeEnum = typeEnum;
    }

    public Definition getWsdlDefinition() {
      return m_wsdlDefintion;
    }

    public void setWsdlDefintion(Definition wsdlDefintion) {
      m_wsdlDefintion = wsdlDefintion;
    }

    public Schema[] getInlineSchemas() {
      if (m_inlineSchemas == null) {
        return new Schema[0];
      }
      else {
        return m_inlineSchemas;
      }
    }

    public void setInlineSchemas(Schema[] inlineSchemas) {
      m_inlineSchemas = inlineSchemas;
    }

    public static enum TypeEnum {
      RootWsdl, ReferencedWsdl;
    }
  }

  public static abstract class SchemaArtefact<T> extends Artefact<T> {
    private Schema m_schema;

    public SchemaArtefact(IFileHandle<T> schemaResource, Schema schema) {
      super(schemaResource);
      m_schema = schema;
    }

    public Schema getSchema() {
      return m_schema;
    }

    public void setSchema(Schema schema) {
      m_schema = schema;
    }
  }

  public static class SchemaImportArtefact<T> extends SchemaArtefact<T> {
    private String m_namespaceUri;

    public SchemaImportArtefact(IFileHandle<T> schemaFileHandle, Schema schema, String namespaceUri) {
      super(schemaFileHandle, schema);
      m_namespaceUri = namespaceUri;
    }

    public String getNamespaceUri() {
      return m_namespaceUri;
    }

    public void setNamespaceUri(String namespaceUri) {
      m_namespaceUri = namespaceUri;
    }
  }

  public static class SchemaIncludeArtefact<T> extends SchemaArtefact<T> {

    public SchemaIncludeArtefact(IFileHandle<T> schemaFileHandle, Schema schema) {
      super(schemaFileHandle, schema);
    }
  }
}
