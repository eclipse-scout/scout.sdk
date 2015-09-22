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
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility.WsdlArtifact.TypeEnum;

public final class SchemaUtility {

  private SchemaUtility() {
  }

  public static void visitArtifacts(IFile wsdlFile, ISchemaArtifactVisitor<IFile> visitor) {
    visit(visitor, new EclipseFileHandle(wsdlFile));
  }

  public static void visitArtifacts(File wsdlFile, ISchemaArtifactVisitor<File> visitor) {
    visit(visitor, new JavaFileHandle(wsdlFile));
  }

  private static <T> void visit(ISchemaArtifactVisitor<T> visitor, IFileHandle<T> wsdlFileHandle) {
    if (wsdlFileHandle == null || !wsdlFileHandle.exists()) {
      return;
    }

    Definition wsdlDefinition = JaxWsSdkUtility.loadWsdlDefinition(wsdlFileHandle);
    if (wsdlDefinition == null) {
      return;
    }

    // root WSDL file
    WsdlArtifact<T> rootWsdlArtifact = new WsdlArtifact<T>(wsdlFileHandle, TypeEnum.ROOT_WSDL, wsdlDefinition);
    visitor.onRootWsdlArtifact(rootWsdlArtifact);

    Set<WsdlArtifact<T>> wsdlArtifacts = new HashSet<WsdlArtifact<T>>();
    wsdlArtifacts.add(rootWsdlArtifact);

    // referenced WSDL files
    Set<WsdlArtifact<T>> referencedWsdlArtifacts = getReferencedWsdlResourcesRec(wsdlFileHandle, wsdlDefinition);
    wsdlArtifacts.addAll(referencedWsdlArtifacts);
    for (WsdlArtifact<T> referencedWsdlArtifact : referencedWsdlArtifacts) {
      visitor.onReferencedWsdlArtifact(referencedWsdlArtifact);
    }

    // set inline schemas to WSDL resources and resolve referenced schema resources
    for (WsdlArtifact<T> wsdlArtifact : wsdlArtifacts) {
      if (wsdlArtifact.getWsdlDefinition() == null) {
        JaxWsSdk.logWarning("Unexpected: WSDL definition should not be null '" + wsdlArtifact + "'");
        continue;
      }

      Types types = wsdlArtifact.getWsdlDefinition().getTypes();
      if (types != null) {
        Set<Schema> inlineSchemas = new HashSet<Schema>();
        for (Object type : types.getExtensibilityElements()) {
          if (type instanceof Schema) {
            Schema schema = (Schema) type;
            // inline schema
            inlineSchemas.add(schema);
            // referenced schema resources
            visitReferencedSchemaResources(visitor, wsdlArtifact.getFileHandle(), schema);
          }
        }
        wsdlArtifact.setInlineSchemas(inlineSchemas.toArray(new Schema[inlineSchemas.size()]));
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

  private static <T> Set<WsdlArtifact<T>> getReferencedWsdlResourcesRec(IFileHandle<T> parentWsdlFileHandle, Definition parentWsdlDefinition) {
    if (parentWsdlFileHandle == null || !parentWsdlFileHandle.exists()) {
      return Collections.emptySet();
    }

    IFileHandle<T> folder = parentWsdlFileHandle.getParent();

    Set<WsdlArtifact<T>> wsdlArtifacts = new HashSet<WsdlArtifact<T>>();
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
              wsdlArtifacts.add(new WsdlArtifact<T>(wsdlFileHandle, TypeEnum.REFERENCED_WSDL, wsdlDefinition));

              // recursion
              wsdlArtifacts.addAll(getReferencedWsdlResourcesRec(wsdlFileHandle, wsdlDefinition));
            }
          }
        }
      }
    }

    return wsdlArtifacts;
  }

  private static <T> void visitReferencedSchemaResources(ISchemaArtifactVisitor<T> visitor, IFileHandle<T> fileHandle, Schema schema) {
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
          SchemaIncludeArtifact<T> artifact = new SchemaIncludeArtifact<T>(referencedSchemaResource, referencedSchema);
          visitor.onSchemaIncludeArtifact(artifact);

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
            SchemaImportArtifact<T> artifact = new SchemaImportArtifact<T>(referencedSchemaResource, referencedSchema, schemaImport.getNamespaceURI());
            visitor.onSchemaImportArtifact(artifact);

            // recursion
            visitReferencedSchemaResources(visitor, referencedSchemaResource, referencedSchema);
          }
        }
      }
    }
  }

  public abstract static class AbstractArtifact<T> {
    private IFileHandle<T> m_fileHandle;

    public AbstractArtifact(IFileHandle<T> fileHandle) {
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
      return m_fileHandle.equals(((AbstractArtifact) obj).m_fileHandle);
    }
  }

  public static class WsdlArtifact<T> extends AbstractArtifact<T> {
    private TypeEnum m_typeEnum;
    private Definition m_wsdlDefintion;
    private Schema[] m_inlineSchemas = new Schema[0];

    public WsdlArtifact(IFileHandle<T> wsdlFileHandle, TypeEnum typeEnum, Definition wsdlDefintion) {
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
      ROOT_WSDL,
      REFERENCED_WSDL;
    }
  }

  public abstract static class AbstractSchemaArtifact<T> extends AbstractArtifact<T> {
    private Schema m_schema;

    public AbstractSchemaArtifact(IFileHandle<T> schemaResource, Schema schema) {
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

  public static class SchemaImportArtifact<T> extends AbstractSchemaArtifact<T> {
    private String m_namespaceUri;

    public SchemaImportArtifact(IFileHandle<T> schemaFileHandle, Schema schema, String namespaceUri) {
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

  public static class SchemaIncludeArtifact<T> extends AbstractSchemaArtifact<T> {

    public SchemaIncludeArtifact(IFileHandle<T> schemaFileHandle, Schema schema) {
      super(schemaFileHandle, schema);
    }
  }
}