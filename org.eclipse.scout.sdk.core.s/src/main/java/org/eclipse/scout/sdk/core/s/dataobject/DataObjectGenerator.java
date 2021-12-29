/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.dataobject;

import static org.eclipse.scout.sdk.core.s.generator.method.ScoutDoMethodGenerator.createConvenienceMethods;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.builder.java.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.builder.java.JavaBuilderContextFunction;
import org.eclipse.scout.sdk.core.generator.annotation.AnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.field.FieldGenerator;
import org.eclipse.scout.sdk.core.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.s.ISdkConstants;
import org.eclipse.scout.sdk.core.s.apidef.IScoutAnnotationApi;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.dataobject.DataObjectNode.DataObjectNodeKind;
import org.eclipse.scout.sdk.core.s.generator.method.ScoutDoMethodGenerator;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * A generator for data objects
 */
public class DataObjectGenerator<TYPE extends DataObjectGenerator<TYPE>> extends PrimaryTypeGenerator<TYPE> {

  private String m_namespace;
  private String m_typeVersion;
  private final Map<String, NodeEntry> m_nodes = new LinkedHashMap<>();

  @Override
  protected void setup() {
    typeVersion()
        .map(tv -> AnnotationGenerator.create()
            .withAnnotationNameFrom(IScoutApi.class, IScoutAnnotationApi::TypeVersion)
            .withElementFrom(IScoutApi.class, api -> api.TypeVersion().valueElementName(), b -> b.context().requireApi(IScoutApi.class).TypeVersion().buildValue(b, tv)))
        .ifPresent(this::withAnnotation);

    namespace()
        .map(ns -> AnnotationGenerator.create()
            .withAnnotationNameFrom(IScoutApi.class, IScoutAnnotationApi::TypeName)
            .withElementFrom(IScoutApi.class, api -> api.TypeName().valueElementName(), b -> b.stringLiteral(buildTypeName(ns, b.context()))))
        .ifPresent(this::withAnnotation);

    withSuperClassFrom(IScoutApi.class, api -> api.DoEntity().fqn());

    if (!m_nodes.isEmpty()) {
      appendDoNodes();
      appendGeneratedComment();
      withPreProcessor((e, c) -> appendConvenienceMethods(c));
    }
  }

  protected void appendDoNodes() {
    var num = new AtomicInteger();
    nodes().forEach(n -> withMethod(ScoutDoMethodGenerator.createDoNodeFunc(n.name(), n.kind(), n.dataTypeFunc()), "D", num.incrementAndGet()));
  }

  protected void appendGeneratedComment() {
    withField(FieldGenerator.create()
        .withComment(b -> b.append(ScoutDoMethodGenerator.convenienceMethodsMarkerComment(b.context().lineDelimiter()))), "M", 0);
  }

  protected void appendConvenienceMethods(IJavaBuilderContext context) {
    var num = new AtomicInteger();
    nodes()
        .forEach(nodeEntry -> createConvenienceMethods(nodeEntry.name(), nodeEntry.kind(), nodeEntry.dataTypeFunc().apply(context), false, this, context)
            .forEach(g -> this
                .withoutMethod(g.identifier(context), context) // remove existing method with this signature (if existing)
                .withMethod(g, "M", num.incrementAndGet())));
  }

  protected String buildTypeName(String namespace, IJavaBuilderContext context) {
    var pageParamDoName = Strings.removeSuffix(elementName(context).orElseThrow(() -> newFail("DataObject name missing.")), ISdkConstants.SUFFIX_DO);
    return namespace + JavaTypes.C_DOT + pageParamDoName;
  }

  protected Stream<NodeEntry> nodes() {
    return m_nodes.values().stream();
  }

  /**
   * Appends a DoNode to this builder. The corresponding DoNode method and all convenience methods are created
   * automatically.
   * 
   * @param name
   *          The name of the DoNode. Must not be {@code null} or blank.
   * @param kind
   *          The {@link DataObjectNodeKind} of the DoNode. Must not be {@code null}.
   * @param dataType
   *          The datatype of the DoNode. If the kind is {@link DataObjectNodeKind#LIST}, {@link DataObjectNodeKind#SET}
   *          or {@link DataObjectNodeKind#COLLECTION} the datatype always describes the type of an element without the
   *          collection type itself. Must not be {@code null} or blank.
   * @return This generator.
   */
  public TYPE withNode(String name, DataObjectNodeKind kind, String dataType) {
    Ensure.notBlank(dataType);
    return withNodeFunc(name, kind, JavaBuilderContextFunction.orNull(dataType));
  }

  /**
   * Appends a DoNode to this builder. The corresponding DoNode method and all convenience methods are created
   * automatically.
   * 
   * @param name
   *          The name of the DoNode. Must not be {@code null} or blank.
   * @param kind
   *          The {@link DataObjectNodeKind} of the DoNode. Must not be {@code null}.
   * @param api
   *          The {@link IApiSpecification} from which the datatype should be retrieved. May be {@code null} in case the
   *          dataTypeFunc can handle a {@code null} input.
   * @param dataTypeFunc
   *          The datatype of the DoNode. If the kind is {@link DataObjectNodeKind#LIST}, {@link DataObjectNodeKind#SET}
   *          or {@link DataObjectNodeKind#COLLECTION} the datatype always describes the type of an element without the
   *          collection type itself. Must not be {@code null} or blank.
   * @return This generator.
   */
  public <API extends IApiSpecification> TYPE withNodeFrom(String name, DataObjectNodeKind kind, Class<API> api, Function<API, String> dataTypeFunc) {
    return withNodeFunc(name, kind, new ApiFunction<>(api, dataTypeFunc));
  }

  /**
   * Appends a DoNode to this builder. The corresponding DoNode method and all convenience methods are created
   * automatically.
   * 
   * @param name
   *          The name of the DoNode. Must not be {@code null} or blank.
   * @param kind
   *          The {@link DataObjectNodeKind} of the DoNode. Must not be {@code null}.
   * @param dataTypeFunc
   *          A function that retrieves the datatype based on the running {@link IJavaBuilderContext}. If the kind is
   *          {@link DataObjectNodeKind#LIST}, {@link DataObjectNodeKind#SET} or {@link DataObjectNodeKind#COLLECTION}
   *          the datatype always describes the type of an element without the collection type itself. Must not be
   *          {@code null} or blank.
   * @return This generator.
   */
  public TYPE withNodeFunc(String name, DataObjectNodeKind kind, Function<IJavaBuilderContext, String> dataTypeFunc) {
    Ensure.notBlank(name);
    Ensure.notNull(dataTypeFunc);
    Ensure.notNull(kind);
    m_nodes.put(name, new NodeEntry(name, kind, JavaBuilderContextFunction.create(dataTypeFunc)));
    return thisInstance();
  }

  public Optional<String> typeVersion() {
    return Strings.notBlank(m_typeVersion);
  }

  /**
   * Specifies the type version to use in the @TypeVersion annotation.
   * 
   * @param typeVersion
   *          The new type version. If {@code null}, no @TypeVersion annotation is created.
   * @return This generator.
   */
  public TYPE withTypeVersion(String typeVersion) {
    m_typeVersion = typeVersion;
    return thisInstance();
  }

  public Optional<String> namespace() {
    return Strings.notEmpty(m_namespace);
  }

  /**
   * Specifies the namespace this DO belongs to. The namespace is used as prefix for the @TypeName annotation. The
   * suffix is automatically appended based on the type name of this DO.
   * 
   * @param namespace
   *          The namespace or {@code null} if no @TypeName annotation should be generated for this DO.
   * @return This generator.
   */
  public TYPE withNamespace(String namespace) {
    m_namespace = namespace;
    return thisInstance();
  }

  private static final class NodeEntry {
    private final String m_name;
    private final DataObjectNodeKind m_kind;
    private final JavaBuilderContextFunction<String> m_dataType;

    public NodeEntry(String name, DataObjectNodeKind kind, JavaBuilderContextFunction<String> dataType) {
      m_name = name;
      m_kind = kind;
      m_dataType = dataType;
    }

    public DataObjectNodeKind kind() {
      return m_kind;
    }

    public String name() {
      return m_name;
    }

    public JavaBuilderContextFunction<String> dataTypeFunc() {
      return m_dataType;
    }
  }
}
