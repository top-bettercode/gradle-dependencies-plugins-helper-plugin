/*
 * Copyright (c) 2017 Peter Wu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.bestwu.gdf.maven

import com.google.common.base.Charsets.UTF_8
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.SAXException
import org.xml.sax.SAXParseException
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.*
import javax.xml.XMLConstants
import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException
import javax.xml.bind.Unmarshaller
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.validation.Schema
import javax.xml.validation.SchemaFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathExpression
import javax.xml.xpath.XPathExpressionException
import javax.xml.xpath.XPathFactory

class MavenDependenciesDeserializerImpl : MavenDependenciesDeserializer {

    private val schema: Schema
        get() {
            val schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
            val resource = this.javaClass.getResource("/dependency.xsd")
            val schema: Schema
            try {
                schema = schemaFactory.newSchema(resource)
            } catch (e: SAXException) {
                throw IllegalStateException(e)
            }

            return schema
        }

    @Throws(UnsupportedContentException::class)
    override fun deserialize(mavenDependencyXml: String): List<MavenDependency> {
        val wrapperMavenDependencyXml = wrapWithRoot(mavenDependencyXml)
        val doc = parseDocument(wrapperMavenDependencyXml)
        val nodeList = findNodes(doc)
        return unmarshall(nodeList)
    }

    private fun wrapWithRoot(mavenDependencyXml: String): String {
        return String.format("<root>%s</root>", mavenDependencyXml)
    }

    @Throws(UnsupportedContentException::class)
    private fun parseDocument(wrapperMavenDependencyXml: String): Document {
        try {
            val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            return builder.parse(ByteArrayInputStream(wrapperMavenDependencyXml.toByteArray(UTF_8)))
        } catch (e: ParserConfigurationException) {
            throw UnsupportedContentException()
        } catch (e: SAXException) {
            throw UnsupportedContentException()
        } catch (e: IOException) {
            throw UnsupportedContentException()
        }

    }

    @Throws(UnsupportedContentException::class)
    private fun findNodes(doc: Document): NodeList {
        val xPath = XPathFactory.newInstance().newXPath()
        val xPathExpression: XPathExpression
        try {
            xPathExpression = xPath.compile("/root/dependency | /root/dependencies/dependency")
        } catch (e: XPathExpressionException) {
            throw IllegalStateException("Invalid XPath expression. ", e)
        }

        try {
            return xPathExpression.evaluate(doc, XPathConstants.NODESET) as NodeList
        } catch (e: XPathExpressionException) {
            throw UnsupportedContentException()
        }

    }

    @Throws(UnsupportedContentException::class)
    private fun unmarshall(nodeList: NodeList): List<MavenDependency> {
        val schema = schema
        try {
            val jaxbContext = JAXBContext.newInstance(MavenDependency::class.java)
            val unmarshaller = jaxbContext.createUnmarshaller()
            unmarshaller.schema = schema
            val dependencies = ArrayList<MavenDependency>()
            for (i in 0 until nodeList.length) {
                val dependency = unmarshall(nodeList.item(i), unmarshaller)
                dependencies.add(dependency)
            }
            return dependencies
        } catch (e: JAXBException) {
            if (e.linkedException is SAXParseException) {
                throw DependencyValidationException(e.linkedException.message?:"")
            }
            throw UnsupportedContentException()
        }

    }

    @Throws(JAXBException::class)
    private fun unmarshall(node: Node, unmarshaller: Unmarshaller): MavenDependency {
        return unmarshaller.unmarshal(node) as MavenDependency
    }

}
