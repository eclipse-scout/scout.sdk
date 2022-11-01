/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2i.publish

import com.fasterxml.jackson.databind.ObjectMapper
import com.jetbrains.plugin.blockmap.core.BlockMap
import com.jetbrains.plugin.blockmap.core.FileHash
import org.eclipse.scout.sdk.core.util.Ensure.newFail
import org.eclipse.scout.sdk.core.util.Xml
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.*
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.system.exitProcess

const val XML_ATTRIBUTE_ID = "id"
const val XML_ATTRIBUTE_VERSION = "version"
const val XML_ATTRIBUTE_NAME = "name"
const val XML_ATTRIBUTE_URL = "url"
const val XML_TAG_NAME_PLUGIN = "plugin"
const val XML_TAG_NAME_DESCRIPTION = "description"
const val XML_TAG_NAME_CHANGE_NOTES = "change-notes"
const val XML_TAG_NAME_ROOT = "plugins"

const val PLUGIN_DESCRIPTOR_PATH = "META-INF/plugin.xml"
const val PLUGIN_REPO_FILE_NAME = "updatePlugins.xml"

const val PLUGINS_SUB_DIR_NAME = "plugins"

const val BLOCKMAP_ZIP_SUFFIX = ".blockmap.zip"
const val BLOCKMAP_FILENAME = "blockmap.json"
const val HASH_FILENAME_SUFFIX = ".hash.json"


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
            println(
                "java -cp blockmap-1.0.5.jar${pathSep}jackson-annotations-2.13.4.jar${pathSep}jackson-core-2.13.4.jar${pathSep}jackson-databind-2.13.4.2.jar${pathSep}" + // Blockmap Libs
                        "kotlin-stdlib-1.7.20.jar${pathSep}kotlin-stdlib-common-1.7.20.jar${pathSep}kotlin-stdlib-jdk8-1.7.20.jar${pathSep}" + // Kotlin libs
                        "org.eclipse.scout.sdk.core-13.0.0-SNAPSHOT.jar${pathSep}org.eclipse.scout.sdk.s2i-13.0.0-SNAPSHOT.jar" + // Scout SDK libs
                        " org.eclipse.scout.sdk.s2i.publish.EnterprisePluginRepoPublisher /path/to/plugin.zip /path/to/enterprise/repoDir [https://host/path-to-repo]"
            )
        }
    }

    fun publish() {
        val pluginXml = ZipInputStream(BufferedInputStream(Files.newInputStream(pluginToDeploy))).use { findPluginXml(it) }
        if (pluginXml == null) {
            println("No plugin could be found in the zip '$pluginToDeploy'.")
            exitProcess(4)
        }

        val deployedPlugin = copyNewPluginToRepo()
        writeBlockMapFiles(deployedPlugin)

        val newZip = repoDir.relativize(deployedPlugin).joinToString("/")
        val oldZip = modifyUpdatePluginsXml(pluginXml)
        if (!Objects.equals(newZip, oldZip)) {
            deleteOldPluginFromRepo(oldZip)
            removeOldBlockMapFilesFromRepo(deployedPlugin, pluginXml.id)
        }

        if (oldZip == null) {
            println("New plugin '${pluginXml.id}' (version '${pluginXml.version}') successfully published to enterprise plugin repository '$repoDir'.")
        } else {
            println("Plugin '${pluginXml.id}' (version '${pluginXml.version}') successfully published to enterprise plugin repository '$repoDir' replacing old version '$oldZip'.")
        }
    }

    fun removeOldBlockMapFilesFromRepo(deployedPlugin: Path, pluginId: String) {
        val newPluginFileName = deployedPlugin.fileName.toString()
        Files.list(deployedPlugin.parent)
            .filter { isOldBlockMapFile(it, newPluginFileName, pluginId) }
            .forEach { Files.delete(it) }
    }

    fun isOldBlockMapFile(candidate: Path, newPluginFileName: String, pluginId: String): Boolean {
        val candidateFileName = candidate.fileName.toString()
        if (!candidateFileName.startsWith(pluginId)) {
            return false // not this plugin
        }

        val isBlockMapFile = candidateFileName.endsWith(BLOCKMAP_ZIP_SUFFIX) || candidateFileName.endsWith(HASH_FILENAME_SUFFIX)
        if (!isBlockMapFile) {
            return false
        }

        // only blockmap files that do not belong to the new plugin
        return candidateFileName != newPluginFileName + BLOCKMAP_ZIP_SUFFIX && candidateFileName != newPluginFileName + HASH_FILENAME_SUFFIX
    }

    fun writeBlockMapFiles(deployedPlugin: Path) {
        val pluginFileName = deployedPlugin.fileName.toString()

        val map = Files.newInputStream(deployedPlugin).use { ObjectMapper().writeValueAsBytes(BlockMap(it)) }
        val blockMapFile = deployedPlugin.parent.resolve(pluginFileName + BLOCKMAP_ZIP_SUFFIX)
        ZipOutputStream(BufferedOutputStream(Files.newOutputStream(blockMapFile, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))).use {
            val entry = ZipEntry(BLOCKMAP_FILENAME)
            entry.size = map.size.toLong()
            it.putNextEntry(entry)
            it.write(map)
            it.closeEntry()
        }

        val hash = Files.newInputStream(pluginToDeploy).use { ObjectMapper().writeValueAsString(FileHash(it)).toByteArray(StandardCharsets.UTF_8) }
        val hashFile = deployedPlugin.parent.resolve(pluginFileName + HASH_FILENAME_SUFFIX)
        BufferedOutputStream(Files.newOutputStream(hashFile, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))
            .use { it.write(hash) }
    }

    fun copyNewPluginToRepo(): Path {
        val targetDir = repoDir.resolve(PLUGINS_SUB_DIR_NAME)
        Files.createDirectories(targetDir)
        return Files.copy(pluginToDeploy, targetDir.resolve(pluginToDeploy.fileName), StandardCopyOption.REPLACE_EXISTING)
    }

    fun modifyUpdatePluginsXml(pluginXml: PluginXmlDescriptor): String? {
        val updatePluginsXml = repoDir.resolve(PLUGIN_REPO_FILE_NAME)
        val root = getOrCreatePluginsXml(updatePluginsXml)

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
        Xml.writeDocument(root.ownerDocument, true, updatePluginsXml)

        return oldUrl
    }

    fun getOrCreatePluginsXml(updatePluginsXml: Path): Element {
        if (Files.isRegularFile(updatePluginsXml)) {
            // exists already
            return Xml.get(updatePluginsXml).documentElement
        }

        // create a new updatePlugins.xml
        val doc = Xml.createDocumentBuilder().newDocument()
        val root = doc.createElement(XML_TAG_NAME_ROOT)
        doc.appendChild(root)
        return root
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

    /**
     * @param oldPluginZip The old content of the url attribute of the replaced plugin. Can be an absolute URL or a local path relative to the repository root.
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
        val name = entry.name.lowercase(Locale.US)
        if (name == PLUGIN_DESCRIPTOR_PATH.lowercase(Locale.US)) {
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
            .orElseThrow { newFail("Tag $tagName could not be found.") }
}

data class PluginXmlDescriptor(
    val id: String,
    val version: String,
    val name: String,
    val description: String,
    val changeNotes: String
) {
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
