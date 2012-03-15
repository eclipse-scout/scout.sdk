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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Types;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.wsdl.extensions.schema.SchemaReference;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility.WsdlArtefact.TypeEnum;

public final class SchemaUtility {

  private SchemaUtility() {
  }

  public static Artefact[] getArtefacts(IFile wsdlFile, boolean includeRootWsdl) {
    return getArtefacts(JaxWsSdkUtility.toFile(wsdlFile), includeRootWsdl);
  }

  public static Artefact[] getArtefacts(File wsdlFile, boolean includeRootWsdl) {
    if (wsdlFile == null || !wsdlFile.exists()) {
      return new Artefact[0];
    }

    Definition wsdlDefinition = loadWsdlDefinition(wsdlFile);
    if (wsdlDefinition == null) {
      return new Artefact[0];
    }
    List<Artefact> artefacts = new ArrayList<Artefact>();

    // root WSDL file
    WsdlArtefact rootWsdlArtefact = new WsdlArtefact(wsdlFile, TypeEnum.RootWsdl, wsdlDefinition);
    artefacts.add(rootWsdlArtefact);
    // referenced WSDL files
    artefacts.addAll(Arrays.asList(getReferencedWsdlResourcesRec(wsdlFile, wsdlDefinition)));

    // set inline schemas to WSDL resources and resolve referenced schema resources
    Artefact[] wsdlArtefacts = artefacts.toArray(new Artefact[artefacts.size()]);
    for (Artefact artefact : wsdlArtefacts) {
      if (!(artefact instanceof WsdlArtefact)) {
        continue;
      }
      WsdlArtefact wsdlArtefact = (WsdlArtefact) artefact;

      if (wsdlArtefact.getWsdlDefintion() == null) {
        JaxWsSdk.logWarning("Unexpected: WSDL definition should not be null '" + wsdlArtefact + "'");
        continue;
      }

      Types types = wsdlArtefact.getWsdlDefintion().getTypes();
      if (types != null) {
        List<Schema> inlineSchemas = new ArrayList<Schema>();
        for (Object type : types.getExtensibilityElements()) {
          if (type instanceof Schema) {
            Schema schema = (Schema) type;
            // inline schema
            inlineSchemas.add(schema);
            // referenced schema resources
            artefacts.addAll(Arrays.asList(getReferencedSchemaResourcesRec(wsdlArtefact.getFile(), schema)));
          }
        }
        wsdlArtefact.setInlineSchemas(inlineSchemas.toArray(new Schema[inlineSchemas.size()]));
      }
    }

    if (!includeRootWsdl) {
      artefacts.remove(rootWsdlArtefact);
    }
    JaxWsSdkUtility.removeDuplicateEntries(artefacts);
    return artefacts.toArray(new Artefact[artefacts.size()]);
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

  private static WsdlArtefact[] getReferencedWsdlResourcesRec(File parentWsdlFile, Definition parentWsdlDefinition) {
    if (parentWsdlFile == null || !parentWsdlFile.exists() || parentWsdlDefinition == null) {
      return new WsdlArtefact[0];
    }

    List<WsdlArtefact> wsdlArtefacts = new ArrayList<WsdlArtefact>();
    Map<?, ?> importMap = parentWsdlDefinition.getImports();

    for (Object importValue : importMap.values()) {
      if (importValue instanceof List<?>) {
        List<?> importList = (List<?>) importValue;
        for (Object importObject : importList) {
          if (importObject instanceof Import) {
            Import importDirective = (Import) importObject;

            Definition wsdlDefinition = importDirective.getDefinition();
            File wsdlFile = toFile(parentWsdlFile.getParentFile(), importDirective.getLocationURI());
            if (wsdlFile != null) {
              wsdlArtefacts.add(new WsdlArtefact(wsdlFile, TypeEnum.ReferencedWsdl, wsdlDefinition));

              // recursion
              wsdlArtefacts.addAll(Arrays.asList(getReferencedWsdlResourcesRec(wsdlFile, wsdlDefinition)));
            }
          }
        }
      }
    }

    return wsdlArtefacts.toArray(new WsdlArtefact[wsdlArtefacts.size()]);
  }

  private static SchemaArtefact[] getReferencedSchemaResourcesRec(File file, Schema schema) {
    if (file == null || !file.exists() || schema == null) {
      return new SchemaArtefact[0];
    }
    File folder = file.getParentFile();
    List<Artefact> artefacts = new ArrayList<Artefact>();

    // Included schemas
    List<?> includes = schema.getIncludes();
    if (includes != null && includes.size() > 0) {
      for (Object include : includes) {
        if (include instanceof SchemaReference) {
          SchemaReference schemaInclude = (SchemaReference) include;

          File referencedSchemaFile = toFile(folder, schemaInclude.getSchemaLocationURI());
          if (referencedSchemaFile != null) {
            Schema referencedSchema = schemaInclude.getReferencedSchema();
            SchemaIncludeArtefact artefact = new SchemaIncludeArtefact(referencedSchemaFile, referencedSchema);
            artefacts.add(artefact);

            // recursion
            artefacts.addAll(Arrays.asList(getReferencedSchemaResourcesRec(referencedSchemaFile, referencedSchema)));
          }
        }
      }
    }

    // Imported schemas
    Map<?, ?> importMap = schema.getImports();
    if (importMap != null && importMap.size() > 0) {
      for (Object importObject : importMap.values()) {
        if (importObject instanceof List<?>) {
          List<?> importList = (List<?>) importObject;

          for (Object importDirective : importList) {
            if (importDirective instanceof SchemaImport) {
              SchemaImport schemaImport = (SchemaImport) importDirective;
              File referencedSchemaFile = toFile(folder, schemaImport.getSchemaLocationURI());
              if (referencedSchemaFile != null) {
                Schema referencedSchema = schemaImport.getReferencedSchema();
                SchemaImportArtefact artefact = new SchemaImportArtefact(referencedSchemaFile, referencedSchema, schemaImport.getNamespaceURI());
                artefacts.add(artefact);

                // recursion
                artefacts.addAll(Arrays.asList(getReferencedSchemaResourcesRec(referencedSchemaFile, referencedSchema)));
              }
            }
          }
        }
      }
    }
    return artefacts.toArray(new SchemaArtefact[artefacts.size()]);
  }

  private static Definition loadWsdlDefinition(File file) {
    if (file == null || !file.exists()) {
      return null;
    }

    try {
      WSDLFactory factory = WSDLFactory.newInstance();
      WSDLReader reader = factory.newWSDLReader();
      return reader.readWSDL(file.getAbsolutePath());
    }
    catch (Exception e) {
      JaxWsSdk.logError("Could not load WSDL file '" + file.getAbsolutePath() + "'", e);
    }
    return null;
  }

  private static File toFile(File folder, String locationUri) {
    if (locationUri == null) {
      return null;
    }
    if (folder != null && folder.exists()) {
      File file = new File(folder, locationUri);
      if (file.exists()) {
        return file;
      }
    }
    File file = new File(locationUri);
    if (file.exists()) {
      return file;
    }
    return null;
  }

  public static abstract class Artefact {
    private File m_file;

    public Artefact(File file) {
      m_file = file;
    }

    public File getFile() {
      return m_file;
    }

    public void setFile(File file) {
      m_file = file;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((m_file == null) ? 0 : m_file.hashCode());
      return result;
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
      Artefact other = (Artefact) obj;
      if (m_file == null) {
        if (other.m_file != null) {
          return false;
        }
      }
      else if (!m_file.equals(other.m_file)) {
        return false;
      }
      return true;
    }
  }

  public static class WsdlArtefact extends Artefact {
    private TypeEnum m_typeEnum;
    private Definition m_wsdlDefintion;
    private Schema[] m_inlineSchemas = new Schema[0];

    public WsdlArtefact(File wsdlFile, TypeEnum typeEnum, Definition wsdlDefintion) {
      super(wsdlFile);
      m_typeEnum = typeEnum;
      m_wsdlDefintion = wsdlDefintion;
    }

    public TypeEnum getTypeEnum() {
      return m_typeEnum;
    }

    public void setTypeEnum(TypeEnum typeEnum) {
      m_typeEnum = typeEnum;
    }

    public Definition getWsdlDefintion() {
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

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((m_typeEnum == null) ? 0 : m_typeEnum.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!super.equals(obj)) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      WsdlArtefact other = (WsdlArtefact) obj;
      if (m_typeEnum != other.m_typeEnum) {
        return false;
      }
      return true;
    }

    public static enum TypeEnum {
      RootWsdl, ReferencedWsdl;
    }
  }

  public static abstract class SchemaArtefact extends Artefact {
    private Schema m_schema;

    public SchemaArtefact(File schemaFile, Schema schema) {
      super(schemaFile);
      m_schema = schema;
    }

    public Schema getSchema() {
      return m_schema;
    }

    public void setSchema(Schema schema) {
      m_schema = schema;
    }
  }

  public static class SchemaImportArtefact extends SchemaArtefact {
    private String m_namespaceUri;

    public SchemaImportArtefact(File schemaFile, Schema schema, String namespaceUri) {
      super(schemaFile, schema);
      m_namespaceUri = namespaceUri;
    }

    public String getNamespaceUri() {
      return m_namespaceUri;
    }

    public void setNamespaceUri(String namespaceUri) {
      m_namespaceUri = namespaceUri;
    }
  }

  public static class SchemaIncludeArtefact extends SchemaArtefact {

    public SchemaIncludeArtefact(File schemaFile, Schema schema) {
      super(schemaFile, schema);
    }
  }
}
