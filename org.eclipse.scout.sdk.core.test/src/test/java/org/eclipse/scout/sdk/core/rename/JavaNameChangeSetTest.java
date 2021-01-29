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
package org.eclipse.scout.sdk.core.rename;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test {@link JavaNameChangeSet}
 */
public class JavaNameChangeSetTest {

    private static void assertReplacement(String actual, String expected) {
        assertEquals(expected, actual);
    }

    @Test
    public void testSimpleScript() {
        JavaNameChangeSet set = new JavaNameChangeSet();
        set.renameQualifiedName("a.b.c.A", "u.v.B");

        assertReplacement(set.getQualifiedNameReplacement("a.b.c.Z"), null);
        assertReplacement(set.getQualifiedNameReplacement("x.y.A"), null);
        assertReplacement(set.getQualifiedNameReplacement("a.b.c.A"), "u.v.B");
    }

    @Test
    public void testTransientScript() {
        JavaNameChangeSet set = new JavaNameChangeSet();
        set.renameQualifiedName("a.b.c.A", "a.b.c.B");
        set.renamePackage("a.b.c", "u.v");
        set.renameQualifiedName("u.v.B", "u.v.C");

        assertReplacement(set.getQualifiedNameReplacement("a.b.c.A"), "u.v.C");
        assertReplacement(set.getQualifiedNameReplacement("u.v.B"), "u.v.C");

        set.renameMember("u.v.W#fun1", "fun2");
        set.renameMember("u.v.W#fun2", "fun3");
        set.renameMember("u.v.W#fun3", "fun4");

        assertReplacement(set.getMemberReplacement("u.v.W#fun1"), "fun4");
        assertReplacement(set.getMemberReplacement("u.v.W#fun2"), "fun4");
        assertReplacement(set.getMemberReplacement("u.v.W#fun3"), "fun4");

        set.renameQualifiedName("u.v.W", "u.v.X");
        set.renameMember("u.v.X#fun4", "fun5");
        assertReplacement(set.getQualifiedNameReplacement("u.v.W"), "u.v.X");
        assertReplacement(set.getMemberReplacement("u.v.X#fun1"), null);
        assertReplacement(set.getMemberReplacement("u.v.X#fun4"), "fun5");
    }

    @Test
    public void testScriptIssue1() throws IOException {
        JavaNameChangeSet set = new JavaNameChangeSet();
        //set.addScript(loadScript("ticket-245457-RENAME-Java.txt"));
        set.addScript(loadScript("ticket-245457-fixed-RENAME-Java.txt"));

        assertReplacement(set.getQualifiedNameReplacement("com.bsiag.crm.client.core.person.PersonForm"), "com.bsiag.crm.client.core.customer.CustomerForm");
        assertReplacement(set.getQualifiedNameReplacement("com.bsiag.crm.shared.core.person.PersonFormData"), "com.bsiag.crm.shared.core.customer.CustomerFormData");

        assertReplacement(set.getMemberReplacement("com.bsiag.crm.client.core.customer.CustomerForm#getPersonKey"), "getCustomerKey");
        assertReplacement(set.getMemberReplacement("com.bsiag.crm.shared.core.customer.CustomerFormData#getPersonKey"), "getCustomerKey");

        assertReplacement(set.getQualifiedNameReplacement("com.bsiag.crm.shared.core.person.PersonKey"), "com.bsiag.crm.shared.core.customer.CustomerKey");
        assertReplacement(set.getQualifiedNameReplacement("com.bsiag.crm.shared.core.person.CustomerKey"), "com.bsiag.crm.shared.core.customer.CustomerKey");
        assertReplacement(set.getQualifiedNameReplacement("com.bsiag.crm.shared.core.customer.PersonKey"), null);
        assertReplacement(set.getMemberReplacement("com.bsiag.crm.shared.core.customer.CustomerKey#getDirectoryPersonKey"), "getDirectoryCustomerKey");
        assertReplacement(set.getMemberReplacement("com.bsiag.crm.shared.core.customer.CustomerKey#toPersonKey"), "toCustomerKey");
    }

    private String loadScript(String name) throws IOException {
        StringBuilder buf = new StringBuilder();
        try (InputStream in = getClass().getResourceAsStream(name)) {
            int ch;
            while ((ch = in.read()) >= 0) buf.append((char) ch);
        }
        return buf.toString();
    }
}
