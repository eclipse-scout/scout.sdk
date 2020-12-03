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
package org.eclipse.scout.sdk.s2i.publish

import org.eclipse.scout.sdk.core.util.Ensure
import org.eclipse.scout.sdk.core.util.Xml
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.BufferedInputStream
import java.nio.file.*
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import kotlin.system.exitProcess

const val XML_ATTRIBUTE_ID = "id"
const val XML_ATTRIBUTE_VERSION = "version"
const val XML_ATTRIBUTE_NAME = "name"
const val XML_ATTRIBUTE_URL = "url"
const val XML_TAG_NAME_PLUGIN = "plugin"
const val XML_TAG_NAME_DESCRIPTION = "description"
const val XML_TAG_NAME_CHANGE_NOTES = "change-notes"

const val PLUGIN_DESCRIPTOR_PATH = "META-INF/plugin.xml"
const val PLUGIN_REPO_FILE_NAME = "updatePlugins.xml"

const val PLUGINS_SUB_DIR_NAME = "plugins"


open class EnterprisePluginRepoPublisher(val pluginToDeploy: Path, val repoDir: Path, val repoUrl: String?) {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size < 2 || args[0].isBlank() || args[1].isBlank()) {
                printUsage()
                exitProcess(1)
            }

            val pluginToDeploy = Paths.get(args[0])
            if (!Files.isRegularFile(pluginToDeploy) || !Files.isReadable(pluginToDeploy)) {
                println("Plugin file '$pluginToDeploy' could not be found.")
                exitProcess(2)
            }

            val repoDir = Paths.get(args[1])
            if (!Files.isDirectory(repoDir)) {
                println("Repository directory '$repoDir' could not be found.")
                exitProcess(3)
            }

            val repoPath = if (args.size > 2) args[2] else null
            EnterprisePluginRepoPublisher(pluginToDeploy, repoDir, repoPath).publish()
        }

        fun printUsage() {
            val pathSep = System.getProperty("path.separator")
            println("usage:")
            println("java -cp kotlin-runtime.jar${pathSep}org.eclipse.scout.sdk.core.jar${pathSep}org.eclipse.scout.sdk.s2i.jar org.eclipse.scout.sdk.s2i.publish.EnterprisePluginRepoPublisher /path/to/plugin.zip /path/to/enterprise/repoDir [https://host/path-to-repo]")
        }
    }

    fun publish() {
        val pluginXml = ZipInputStream(BufferedInputStream(Files.newInputStream(pluginToDeploy))).use { findPluginXml(it) }
        if (pluginXml == null) {
            println("No plugin could be found in the zip '$pluginToDeploy'.")
            exitProcess(4)
        }

        val newZip = repoDir.relativize(copyNewPluginToRepo()).joinToString("/")
        val oldZip = modifyUpdatePluginsXml(pluginXml)

        if (!Objects.equals(newZip, oldZip)) {
            deleteOldPluginFromRepo(oldZip)
        }

        if (oldZip == null) {
            println("New plugin '${pluginXml.id}' (version '${pluginXml.version}') successfully published to enterprise plugin repository '$repoDir'.")
        } else {
            println("Plugin '${pluginXml.id}' (version '${pluginXml.version}') successfully published to enterprise plugin repository '$repoDir' replacing old version '$oldZip'.")
        }
    }

    fun copyNewPluginToRepo(): Path = Files.copy(pluginToDeploy, repoDir.resolve(PLUGINS_SUB_DIR_NAME).resolve(pluginToDeploy.fileName), StandardCopyOption.REPLACE_EXISTING)

    fun modifyUpdatePluginsXml(pluginXml: PluginXmlDescriptor): String? {
        val updatePluginsXml = repoDir.resolve(PLUGIN_REPO_FILE_NAME)
        val root = Xml.createDocumentBuilder()
                .parse(updatePluginsXml.toFile())
                .documentElement

        val id = pluginXml.id
        var oldUrl: String? = null
        val pluginNode = Xml.evaluateXPath("//plugin[@id='$id']", root).firstOrNull()
        if (pluginNode != null) {
            // remove existing
            oldUrl = pluginNode.getAttribute(XML_ATTRIBUTE_URL) // remember old path
            root.removeChild(pluginNode)
        }

        // add new entry
        root.appendChild(pluginXml.toPluginElement(root.ownerDocument, pluginToDeploy.fileName.toString(), repoUrl))
        cleanupXmlDocument(root.ownerDocument)
        writeXmlDocument(root.ownerDocument, updatePluginsXml)

        return oldUrl
    }

    fun cleanupXmlDocument(document: Document) {
        document.normalize()
        val root = document.documentElement
        removeTextChildNodes(root)
        for (i in 0 until root.childNodes.length) {
            removeTextChildNodes(root.childNodes.item(i))
        }
    }

    fun removeTextChildNodes(root: Node) {
        val toRemove = ArrayList<Node>()

        for (i in 0 until root.childNodes.length) {
            val child = root.childNodes.item(i)
            if (child.nodeType == Node.TEXT_NODE) {
                toRemove.add(child)
            }
        }
        toRemove.forEach { root.removeChild(it) }
    }

    fun writeXmlDocument(document: Document, updatePluginsXml: Path) {
        val transformer = Xml.createTransformer(true)

        Files.newOutputStream(updatePluginsXml, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING).use { out ->
            transformer.transform(DOMSource(document), StreamResult(out))
        }
    }

    /**
     * @param oldPluginZip The old content of the url attribute of the replaced plugin. May be an absolute URL or a local path relative to the repository root.
     */
    fun deleteOldPluginFromRepo(oldPluginZip: String?) {
        if (oldPluginZip == null) {
            return
        }
        val oldPluginZipNormalized = oldPluginZip.replace('\\', '/')
        val lastSlash = oldPluginZipNormalized.lastIndexOf('/')
        if (lastSlash < 1) {
            println("Cannot delete old plugin zip because the filename could not be calculated from old value '$oldPluginZip'.")
            return
        }
        val fileName = oldPluginZipNormalized.substring(lastSlash + 1)
        try {
            Files.delete(repoDir.resolve(PLUGINS_SUB_DIR_NAME).resolve(fileName))
        } catch (e: NoSuchFileException) {
            println("File $oldPluginZip cannot be deleted because it cannot be found. Skipping.")
        }
    }

    fun findPluginXml(stream: ZipInputStream): PluginXmlDescriptor? {
        var pluginXml: PluginXmlDescriptor?
        var ze: ZipEntry? = stream.nextEntry
        while (ze != null) {
            pluginXml = findPluginXml(stream, ze)
            if (pluginXml != null) {
                return pluginXml
            }
            ze = stream.nextEntry
        }
        return null
    }

    fun findPluginXml(stream: ZipInputStream, entry: ZipEntry): PluginXmlDescriptor? {
        val name = entry.name.toLowerCase()
        if (name == PLUGIN_DESCRIPTOR_PATH.toLowerCase()) {
            return parsePluginXml(stream)
        }
        if (name.endsWith(".jar") || name.endsWith(".zip")) {
            return findPluginXml(ZipInputStream(stream))
        }
        return null
    }

    fun parsePluginXml(stream: ZipInputStream): PluginXmlDescriptor {
        val root = Xml.createDocumentBuilder()
                .parse(stream)
                .documentElement
        val id = root.textContentOfChildTag(XML_ATTRIBUTE_ID)
        val version = root.textContentOfChildTag(XML_ATTRIBUTE_VERSION)
        val name = root.textContentOfChildTag(XML_ATTRIBUTE_NAME)
        val description = root.textContentOfChildTag(XML_TAG_NAME_DESCRIPTION)
        val changeNotes = root.textContentOfChildTag(XML_TAG_NAME_CHANGE_NOTES)
        return PluginXmlDescriptor(id, version, name, description, changeNotes)
    }

    fun Element.textContentOfChildTag(tagName: String): String =
            Xml.firstChildElement(this, tagName)
                    .map(Element::getTextContent)
                    .map(String::trim)
                    .orElseThrow {
                        Ensure.newFail("Tag $tagName could not be found.")
                    }
}

data class PluginXmlDescriptor(val id: String,
                               val version: String,
                               val name: String,
                               val description: String,
                               val changeNotes: String) {
    fun toPluginElement(ownerDocument: Document, fileName: String, repoUrl: String?): Element {
        val pluginElement = ownerDocument.createElement(XML_TAG_NAME_PLUGIN)
        pluginElement.setAttribute(XML_ATTRIBUTE_ID, id)

        var rootUrl = ""
        if (repoUrl?.isNotBlank() == true) {
            rootUrl = repoUrl
            if (!rootUrl.endsWith("/")) {
                rootUrl = "$rootUrl/"
            }
        }

        pluginElement.setAttribute(XML_ATTRIBUTE_URL, "$rootUrl$PLUGINS_SUB_DIR_NAME/$fileName")
        pluginElement.setAttribute(XML_ATTRIBUTE_VERSION, version)

        if (name.isNotBlank()) {
            val nameElement = ownerDocument.createElement(XML_ATTRIBUTE_NAME)
            nameElement.textContent = name
            pluginElement.appendChild(nameElement)
        }

        if (description.isNotBlank()) {
            val descriptionElement = ownerDocument.createElement(XML_TAG_NAME_DESCRIPTION)
            descriptionElement.textContent = description
            pluginElement.appendChild(descriptionElement)
        }

        if (changeNotes.isNotBlank()) {
            val changeNotesElement = ownerDocument.createElement(XML_TAG_NAME_CHANGE_NOTES)
            changeNotesElement.textContent = changeNotes
            pluginElement.appendChild(changeNotesElement)
        }

        return pluginElement
    }
}
