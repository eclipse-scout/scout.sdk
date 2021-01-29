/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.rename;

import org.eclipse.scout.sdk.core.log.SdkLog;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaNameChangeSet {
    private static final int GROUP_INDEX_MOVE = 2;
    private static final int GROUP_INDEX_RENAME_CLASS = 6;
    private static final int GROUP_INDEX_RENAME_MEMBER = 10;
    private static final int GROUP_INDEX_RENAME_PACKAGE = 15;
    private static final int GROUP_INDEX_REGEX = 18;
    private static final int GROUP_INDEX_MOVE_JPA = 22;
    private static final int GROUP_INDEX_RENAME_JPA = 26;
    private static final int GROUP_INDEX_RENAME_JPA_MEMBER = 30;

    private static final Pattern TEXT_COMMAND_PATTERN = Pattern.compile(""
            + "(" + //1
            "(move) ([a-z0-9.]+)\\.([A-Z_]\\w*) to ([a-z0-9.]+)" + //2,3,4,5
            "|"
            + "(rename) ([\\w.]+)\\.([A-Z_]\\w*) to (\\w+)" + //6,7,8,9
            "|"
            + "(rename) ([\\w.]+)\\.([A-Z_]\\w*)#(\\w+) to (\\w+)" + //10,11,12,13,14
            "|"
            + "(rename) ([a-z0-9.]+) to ([a-z0-9.]+)" + //15,16,17
            "|"
            + "(regex) (.+) to (.+?)(?: \\[files: (.*)\\])?" + //18,19,20,21
            "|"
            + "(move jpa) ([a-z0-9.]+)\\.([A-Z_]\\w*) to ([a-z0-9.]+)" + //22,23,24,25
            "|"
            + "(rename jpa) ([a-z0-9.]+)\\.([A-Z_]\\w*) to (\\w+)" + //26,27,28,29
            "|"
            + "(rename jpa) ([a-z0-9.]+)\\.([A-Z_]\\w*)#(\\w+) to (\\w+)" + //30,31,32,33,34
            ")");

    //a.b.c -> x.y.z (sorted by longest last)
    private final NavigableMap<String, String> m_renamePackage = new TreeMap<>();
    //a.b.c.Foo -> a.b.c.Bar
    private final Map<String, String> m_renameQualifiedName = new HashMap<>();
    //a.b.c.Foo#fieldFoo -> fieldBar
    //a.b.c.Foo#getFoo -> getBar
    private final Map<String, String> m_renameMember = new HashMap<>();
    // a.b.c.Dummy.Foo -> Bar
    private final Map<String, String> m_renameInnerClass = new HashMap<>();
    private final List<RegexRenaming> m_regexRenamings = new ArrayList<>();

    public static String capitalize(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static void trace(String map, String key, String value) {
        //System.out.println("TRACE\t" + map + "\t" + key + "\t" + value);
    }

    @SuppressWarnings("squid:ForLoopCounterChangedCheck")
    private static String processPreserveCaseMarker(String s, boolean upper) {
        StringBuilder buf = new StringBuilder();
        char[] a = s.toCharArray();
        for (int i = 0; i < a.length; i++) {
            if (a[i] == '\\' && a[i + 1] == 'p') {
                buf.append(upper ? Character.toUpperCase(a[i + 2]) : Character.toLowerCase(a[i + 2]));
                i += 2;
                continue;
            }
            buf.append(a[i]);
        }
        return buf.toString();
    }

    private static String joinRegexWithOr(Set<String> tokens) {
        StringBuilder buf = new StringBuilder();
        for (String s : tokens) {
            buf.append(s);
            buf.append('|');
        }
        buf.setLength(buf.length() - 1);
        return buf.toString();
    }

    public void addScript(String script) {
        for (String line : script.split("[\\n\\r]")) {
            line = line.trim();
            Matcher mat = TEXT_COMMAND_PATTERN.matcher(line);
            if (!mat.matches()) {
                continue;
            }

            trace("+scriptLine", line, "");
            if (mat.group(GROUP_INDEX_MOVE) != null) {
                //move
                String oldPackageName = mat.group(GROUP_INDEX_MOVE + 1);
                String className = mat.group(GROUP_INDEX_MOVE + 2);
                String newPackageName = mat.group(GROUP_INDEX_MOVE + 3);
                this.renameQualifiedName(oldPackageName + "." + className, newPackageName + "." + className);
            } else if (mat.group(GROUP_INDEX_RENAME_CLASS) != null) {
                //rename
                String fullyQualifiedPackageAndPossibleOuterClassName = mat.group(GROUP_INDEX_RENAME_CLASS + 1);
                String oldClassName = mat.group(GROUP_INDEX_RENAME_CLASS + 2);
                String newClassName = mat.group(GROUP_INDEX_RENAME_CLASS + 3);
                if (fullyQualifiedPackageAndPossibleOuterClassName.matches("[a-z0-9.]+")) {
                    // top-level class
                    this.renameQualifiedName(fullyQualifiedPackageAndPossibleOuterClassName + "." + oldClassName, fullyQualifiedPackageAndPossibleOuterClassName + "." + newClassName);
                } else {
                    // inner class
                    this.renameInnerClass(fullyQualifiedPackageAndPossibleOuterClassName + "." + oldClassName, newClassName);
                }
            } else if (mat.group(GROUP_INDEX_RENAME_MEMBER) != null) {
                //rename
                String packageName = mat.group(GROUP_INDEX_RENAME_MEMBER + 1);
                String className = mat.group(GROUP_INDEX_RENAME_MEMBER + 2);
                String oldMemberName = mat.group(GROUP_INDEX_RENAME_MEMBER + 3);
                String newMemberName = mat.group(GROUP_INDEX_RENAME_MEMBER + 4);
                this.renameMember(packageName + "." + className + "#" + oldMemberName, newMemberName);
            } else if (mat.group(GROUP_INDEX_RENAME_PACKAGE) != null) {
                //rename
                String oldPackageName = mat.group(GROUP_INDEX_RENAME_PACKAGE + 1);
                String newPackageName = mat.group(GROUP_INDEX_RENAME_PACKAGE + 2);
                this.renamePackage(oldPackageName, newPackageName);
            } else if (mat.group(GROUP_INDEX_REGEX) != null) {
                //regex
                String regex = mat.group(GROUP_INDEX_REGEX + 1);
                String replacement = mat.group(GROUP_INDEX_REGEX + 2);
                String filePathRegex = mat.group(GROUP_INDEX_REGEX + 3);
                String toText = " to ";
                if (!regex.contains(toText) && !replacement.contains(toText) && (filePathRegex == null || !filePathRegex.contains(toText))) {
                    // no additional " to " string in regex or replacement or files
                    this.renameRegex(regex, replacement, filePathRegex);
                } else {
                    SdkLog.warning("Invalid regex: multiple ' to ': " + line);
                }
            } else if (mat.group(GROUP_INDEX_MOVE_JPA) != null) {
                //move jpa table
                String oldPackageName = mat.group(GROUP_INDEX_MOVE_JPA + 1);
                String className = mat.group(GROUP_INDEX_MOVE_JPA + 2);
                String newPackageName = mat.group(GROUP_INDEX_MOVE_JPA + 3);

                String oldEntityFqName = oldPackageName + "." + className;
                String newEntityFqName = newPackageName + "." + className;
                String oldMetaEntityFqName = oldPackageName + ".I" + className;
                String newMetaEntityFqName = newPackageName + ".I" + className;

                this.renameQualifiedName(oldMetaEntityFqName, newMetaEntityFqName);
                this.renameQualifiedName(oldEntityFqName, newEntityFqName);
                this.renameQualifiedName(oldEntityFqName + "_", newEntityFqName + "_");
            } else if (mat.group(GROUP_INDEX_RENAME_JPA) != null) {
                //rename jpa table
                String packageName = mat.group(GROUP_INDEX_RENAME_JPA + 1);
                String oldClassName = mat.group(GROUP_INDEX_RENAME_JPA + 2);
                String newClassName = mat.group(GROUP_INDEX_RENAME_JPA + 3);

                String oldEntityFqName = packageName + "." + oldClassName;
                String newEntityFqName = packageName + "." + newClassName;
                String oldMetaEntityFqName = packageName + ".I" + oldClassName;
                String newMetaEntityFqName = packageName + ".I" + newClassName;

                this.renameQualifiedName(oldMetaEntityFqName, newMetaEntityFqName);
                this.renameQualifiedName(oldEntityFqName, newEntityFqName);
                this.renameQualifiedName(oldEntityFqName + "_", newEntityFqName + "_");
            } else if (mat.group(GROUP_INDEX_RENAME_JPA_MEMBER) != null) {
                //rename jpa member
                String packageName = mat.group(GROUP_INDEX_RENAME_JPA_MEMBER + 1);
                String className = mat.group(GROUP_INDEX_RENAME_JPA_MEMBER + 2);
                String propOld = mat.group(GROUP_INDEX_RENAME_JPA_MEMBER + 3);
                String propNew = mat.group(GROUP_INDEX_RENAME_JPA_MEMBER + 4);

                String entityFqName = packageName + "." + className;
                String metaEntityFqName = packageName + ".I" + className;

                this.renameMember(entityFqName + "_#" + propOld, propNew);
                this.renameMember(entityFqName + "_#" + propOld, propNew);
                String joinPrefix = "join";
                if (propOld.startsWith(joinPrefix) && propNew.startsWith(joinPrefix)) {
                    String oldPropWithoutPrefix = propOld.substring(joinPrefix.length());
                    String newPropWithoutPrefix = propNew.substring(joinPrefix.length());
                    this.renameMember(entityFqName + "#get" + capitalize(oldPropWithoutPrefix), "get" + capitalize(newPropWithoutPrefix));
                    this.renameMember(entityFqName + "#getBsi" + capitalize(oldPropWithoutPrefix), "get" + capitalize(newPropWithoutPrefix));
                    this.renameMember(entityFqName + "#getCrm" + capitalize(oldPropWithoutPrefix), "get" + capitalize(newPropWithoutPrefix));
                } else {
                    this.renameMember(metaEntityFqName + "#is" + capitalize(propOld), "is" + capitalize(propNew));
                    this.renameMember(metaEntityFqName + "#get" + capitalize(propOld), "get" + capitalize(propNew));
                    this.renameMember(metaEntityFqName + "#set" + capitalize(propOld), "set" + capitalize(propNew));
                    this.renameMember(entityFqName + "#is" + capitalize(propOld), "is" + capitalize(propNew));
                    this.renameMember(entityFqName + "#get" + capitalize(propOld), "get" + capitalize(propNew));
                    this.renameMember(entityFqName + "#set" + capitalize(propOld), "set" + capitalize(propNew));
                }
            }
        }
    }

    /**
     * a.b.c - x.y.z
     */
    public void renamePackage(String oldName, String newName) {
        if (oldName.equals(newName)) {
            return;
        }
        String oldNameDot = oldName + ".";
        String newNameDot = newName + ".";
        for (Map.Entry<String, String> e : m_renamePackage.entrySet()) {
            if (e.getValue().equals(oldName) || e.getValue().startsWith(oldNameDot)) {
                trace("-renamePackage", e.getKey(), e.getValue());
                e.setValue(e.getValue().replace(oldName, newName));
                trace("+renamePackage", e.getKey(), e.getValue());
            }
        }
        trace("+renamePackage", oldName, newName);
        m_renamePackage.put(oldName, newName);
        for (Map.Entry<String, String> e : new HashMap<>(m_renameQualifiedName).entrySet()) {
            if (e.getValue().startsWith(oldNameDot)) {
                trace("-renameQualifiedName", e.getKey(), e.getValue());
                trace("+renameQualifiedName", e.getKey(), e.getValue().replace(oldNameDot, newNameDot));
                m_renameQualifiedName.put(e.getKey(), e.getValue().replace(oldNameDot, newNameDot));
                trace("+renameQualifiedName", e.getKey().replace(oldNameDot, newNameDot), e.getValue().replace(oldNameDot, newNameDot));
                m_renameQualifiedName.put(e.getKey().replace(oldNameDot, newNameDot), e.getValue().replace(oldNameDot, newNameDot));
            }
        }
        for (Map.Entry<String, String> e : new HashMap<>(m_renameMember).entrySet()) {
            if (e.getValue().startsWith(oldNameDot)) {
                trace("-renameMember", e.getKey(), e.getValue());
                trace("+renameMember", e.getKey(), e.getValue().replace(oldNameDot, newNameDot));
                m_renameMember.put(e.getKey(), e.getValue().replace(oldNameDot, newNameDot));
                trace("+renameMember", e.getKey().replace(oldNameDot, newNameDot), e.getValue().replace(oldNameDot, newNameDot));
                m_renameMember.put(e.getKey().replace(oldNameDot, newNameDot), e.getValue().replace(oldNameDot, newNameDot));
            }
        }
        for (Map.Entry<String, String> e : new HashMap<>(m_renameInnerClass).entrySet()) {
            if (e.getValue().startsWith(oldNameDot)) {
                trace("-renameInnerClass", e.getKey(), e.getValue());
                trace("+renameInnerClass", e.getKey(), e.getValue().replace(oldNameDot, newNameDot));
                m_renameInnerClass.put(e.getKey(), e.getValue().replace(oldNameDot, newNameDot));
                trace("+renameInnerClass", e.getKey().replace(oldNameDot, newNameDot), e.getValue().replace(oldNameDot, newNameDot));
                m_renameInnerClass.put(e.getKey().replace(oldNameDot, newNameDot), e.getValue().replace(oldNameDot, newNameDot));
            }
        }
    }

    /**
     * a.b.c.Foo - a.b.c.Bar
     */
    public void renameQualifiedName(String oldName, String newName) {
        if (oldName.equals(newName)) {
            return;
        }
        for (Map.Entry<String, String> e : m_renameQualifiedName.entrySet()) {
            if (e.getValue().equals(oldName)) {
                trace("-renameQualifiedName", e.getKey(), e.getValue());
                e.setValue(newName);
                trace("+renameQualifiedName", e.getKey(), e.getValue());
            }
        }
        trace("+renameQualifiedName", oldName, newName);
        m_renameQualifiedName.put(oldName, newName);
        for (Map.Entry<String, String> e : new HashMap<>(m_renameMember).entrySet()) {
            if (e.getValue().startsWith(oldName)) {
                trace("-renameMember", e.getKey(), e.getValue());
                trace("+renameMember", e.getKey(), e.getValue().replace(oldName, newName));
                m_renameMember.put(e.getKey(), e.getValue().replace(oldName, newName));
                trace("+renameMember", e.getKey().replace(oldName, newName), e.getValue().replace(oldName, newName));
                m_renameMember.put(e.getKey().replace(oldName, newName), e.getValue().replace(oldName, newName));
            }
        }
        for (Map.Entry<String, String> e : new HashMap<>(m_renameInnerClass).entrySet()) {
            if (e.getValue().startsWith(oldName)) {
                trace("-renameInnerClass", e.getKey(), e.getValue());
                trace("qrenameInnerClass", e.getKey(), e.getValue().replace(oldName, newName));
                m_renameInnerClass.put(e.getKey(), e.getValue().replace(oldName, newName));
                trace("+renameInnerClass", e.getKey().replace(oldName, newName), e.getValue().replace(oldName, newName));
                m_renameInnerClass.put(e.getKey().replace(oldName, newName), e.getValue().replace(oldName, newName));
            }
        }
    }

    /**
     * a.b.c.Foo#fieldFoo - fieldBar
     * <p>
     * a.b.c.Foo#getFoo - getBar
     */
    public void renameMember(String qualifiedOldName, String newName) {
        String oldQualifier = qualifiedOldName.substring(0, qualifiedOldName.lastIndexOf('#'));
        String oldName = qualifiedOldName.substring(qualifiedOldName.lastIndexOf('#') + 1);
        if (oldName.equals(newName)) {
            return;
        }
        //Existing mapping is 'a.b.c.Foo#fun1 -> fun2' and new mapping is 'a.b.c.Foo#fun2 -> fun3'
        //Thus transitive replace 'a.b.c.Foo#fun1 -> fun2' by 'a.b.c.Foo#fun1 -> fun3'
        for (Map.Entry<String, String> e : m_renameMember.entrySet()) {
            String prevQualifier = e.getKey().substring(0, e.getKey().lastIndexOf('#'));
            String prevValue = e.getValue();
            // current key is 'a.b.c.Foo#fun1', value is 'fun2', now check if it matches the new selector key 'a.b.c.Foo#fun2'
            if (qualifiedOldName.equals(prevQualifier + "#" + prevValue)) {
                trace("-renameMember", e.getKey(), e.getValue());
                e.setValue(newName);
                trace("+renameMember", e.getKey(), e.getValue());
            }
        }
        trace("+renameMember", qualifiedOldName, newName);
        m_renameMember.put(qualifiedOldName, newName);
    }

    /**
     * a.b.c.Dummy.Foo -> Bar
     */
    public void renameInnerClass(String qualifiedOldName, String newName) {
        String oldName = qualifiedOldName.substring(qualifiedOldName.lastIndexOf('.') + 1);
        if (oldName.equals(newName)) {
            return;
        }
        for (Map.Entry<String, String> e : m_renameInnerClass.entrySet()) {
            if (e.getValue().equals(oldName)) {
                trace("-renameInnerClass", e.getKey(), e.getValue());
                e.setValue(newName);
                trace("+renameInnerClass", e.getKey(), e.getValue());
            }
        }
        trace("+renameInnerClass", qualifiedOldName, newName);
        m_renameInnerClass.put(qualifiedOldName, newName);
    }

    /**
     * regex - replacement
     * <p>
     * Example with special RegEx "\p": preserve annotation
     * <p>
     * rename "\pMyTest" to "\pHello" MyTest => Hello, m_myTest => m_hello, myTest => hello
     */
    public void renameRegex(String pattern, String replacement, String filePathRegex) {
        if (pattern.contains("\\p")) {
            renameRegex(processPreserveCaseMarker(pattern, true), processPreserveCaseMarker(replacement, true), filePathRegex);
            renameRegex(processPreserveCaseMarker(pattern, false), processPreserveCaseMarker(replacement, false), filePathRegex);
            renameRegex("m_" + processPreserveCaseMarker(pattern, false), "m_" + processPreserveCaseMarker(replacement, false), filePathRegex);
            return;
        }
        Pattern filePathPattern = filePathRegex == null || filePathRegex.length() == 0 ? null : Pattern.compile(filePathRegex);
        trace("+renameRegex", pattern, replacement);
        m_regexRenamings.add(new RegexRenaming(Pattern.compile(pattern), replacement, filePathPattern));
    }

    /**
     * a.b.c.Foo - x.y.z.Bar
     */
    public String getQualifiedNameReplacement(String qName) {
        if (qName == null) {
            return null;
        }
        String r = m_renameQualifiedName.get(qName);
        if (r != null) {
            return r;
        }
        for (Map.Entry<String, String> e : m_renamePackage.descendingMap().entrySet()) {
            if (qName.startsWith(e.getKey() + ".")) {
                return qName.replace(e.getKey(), e.getValue());
            }
        }
        return null;
    }

    /**
     * a.b.c.Foo#fieldFoo - fieldBar
     * <p>
     * a.b.c.Foo#getFoo - getBar
     */
    public String getMemberReplacement(String qualifiedMemberName) {
        if (qualifiedMemberName == null) {
            SdkLog.info("LUP: null null1");
            return null;
        }
        String r = m_renameMember.get(qualifiedMemberName);
        if (r != null) {
            SdkLog.info("LUP: " + qualifiedMemberName + " " + r);
            return r;
        }
        SdkLog.info("LUP: " + qualifiedMemberName + " null2");
        return null;
    }

    /**
     * a.b.c.Dummy.Foo -> Bar
     */
    public String getInnerClassReplacement(String qualifiedInnerClassName) {
        if (qualifiedInnerClassName == null) {
            SdkLog.info("LUP: null null1");
            return null;
        }
        String r = m_renameInnerClass.get(qualifiedInnerClassName);
        if (r != null) {
            SdkLog.info("LUP: " + qualifiedInnerClassName + " " + r);
            return r;
        }
        SdkLog.info("LUP: " + qualifiedInnerClassName + " null2");
        return null;
    }

    public String getRegexReplacement(String filePath, String text) {
        for (RegexRenaming bean : m_regexRenamings) {
            Pattern pattern = bean.getTextPattern();
            String replacement = bean.getReplacement();
            Pattern filePathPattern = bean.getFilePathRegex();
            if (filePathPattern == null || filePathPattern.matcher(filePath).matches()) {
                // file path pattern is not defined or matches

                Matcher matcher = pattern.matcher(text);
                if (matcher.matches()) {
                    SdkLog.info("LUP (regex): " + text + " " + replacement);
                    // a match
                    return matcher.replaceAll(replacement);
                }
            }
        }
        return null;
    }

    /**
     * @return the search pattern finding all occurrences of qualified names
     */
    public Pattern createAffectedNonJavaTokenPattern() {
        Set<String> tokens = new HashSet<>();
        for (String s : m_renamePackage.keySet()) {
            tokens.add(Pattern.quote(s) + "\\.[A-Z_]\\w*");
        }
        for (String s : m_renameQualifiedName.keySet()) {
            tokens.add(Pattern.quote(s));
        }
        if (tokens.isEmpty() && m_regexRenamings.isEmpty()) {
            return Pattern.compile("XX_NO_MATCH_XX");
        }

        return addRegexRenamingPatterns(tokens);
    }

    /**
     * @return the search pattern finding all occurrences of names, field names, regex in *.java files
     */
    public Pattern createAffectedJavaTokenPattern() {
        Set<String> tokens = new HashSet<>();
        for (String s : m_renamePackage.keySet()) {
            tokens.add(Pattern.quote(s) + "\\.[A-Z_]\\w*");
        }
        for (String s : m_renameQualifiedName.keySet()) {
            tokens.add(Pattern.quote(s));
        }
        for (String s : m_renameMember.keySet()) {
            tokens.add(s.substring(s.lastIndexOf('#') + 1));
        }
        for (String s : m_renameInnerClass.keySet()) {
            tokens.add(s.substring(s.lastIndexOf('.') + 1));
        }
        if (tokens.isEmpty() && m_regexRenamings.isEmpty()) {
            return Pattern.compile("XX_NO_MATCH_XX");
        }

        return addRegexRenamingPatterns(tokens);
    }

    private Pattern addRegexRenamingPatterns(Set<String> tokens) {
        String patternString = !tokens.isEmpty() ? "(?<!\\w)(?:" + joinRegexWithOr(tokens) + ")(?!\\w)" : null;

        if (m_regexRenamings.size() > 0) {
            // regex renamings do not require to have a non \w character before or after
            // thus they are added separately
            Set<String> regexTokens = new HashSet<>();
            for (RegexRenaming bean : m_regexRenamings) {
                regexTokens.add("(?:" + bean.getTextPattern().pattern() + ")");
            }
            patternString = (patternString != null ? "(?:" + patternString + ")|" : "") + joinRegexWithOr(regexTokens);
        }
        return Pattern.compile(patternString);
    }

    public static class RegexRenaming {
        private final Pattern m_textPattern;
        private final String m_replacement;
        private final Pattern m_filePathPattern;

        public RegexRenaming(Pattern textPattern, String replacement, Pattern filePathPattern) {
            m_textPattern = textPattern;
            m_replacement = replacement;
            m_filePathPattern = filePathPattern;
        }

        public Pattern getTextPattern() {
            return m_textPattern;
        }

        public String getReplacement() {
            return m_replacement;
        }

        public Pattern getFilePathRegex() {
            return m_filePathPattern;
        }
    }
}
