package org.eobjects.build;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.maven.plugin.logging.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CsProjFile implements DotnetProjectFile {

    private static final Set<String> KNOWN_TEST_RUNNER_DEPENDENCIES;

    static {
        KNOWN_TEST_RUNNER_DEPENDENCIES = new HashSet<>();
        KNOWN_TEST_RUNNER_DEPENDENCIES.add("xunit.runner.visualstudio");
        KNOWN_TEST_RUNNER_DEPENDENCIES.add("MSTest.TestAdapter");
    }

    private final File file;
    private final Log log;
    private Document document;

    public CsProjFile(File file, Log log) {
        this.file = file;
        this.log = log;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public boolean isTestProject() {
        final List<DotnetProjectDependency> dependencies = getDependencies();
        for (DotnetProjectDependency d : dependencies) {
            if (KNOWN_TEST_RUNNER_DEPENDENCIES.contains(d.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getVersion() {
        final String version = (String) xpath("/Project/Version", XPathConstants.STRING);
        if ("".equals(version)) {
            return null;
        }
        return version;
    }

    @Override
    public void setVersion(String version) {
        final Node versionNode = (Node) xpath("/Project/Version", XPathConstants.NODE);

        final boolean removeVersion = version == null || version.isEmpty();
        if (versionNode == null) {
            if (!removeVersion) {
                final Element newChild = getDocument().createElement("Version");
                newChild.setTextContent(version);
                final Node projectNode = getDocument().getFirstChild();
                final Node firstChild = projectNode.getFirstChild();
                projectNode.insertBefore(newChild, firstChild);
            }
        } else {
            if (removeVersion) {
                getDocument().getFirstChild().removeChild(versionNode);
            } else {
                versionNode.setTextContent(version);
            }
        }
    }

    @Override
    public List<DotnetProjectDependency> getDependencies() {
        final List<DotnetProjectDependency> list = new ArrayList<>();

        final NodeList packageRefElems = (NodeList) xpath("/Project/ItemGroup/PackageReference",
                XPathConstants.NODESET);

        if (packageRefElems != null && packageRefElems.getLength() > 0) {
            for (int i = 0; i < packageRefElems.getLength(); i++) {
                final Element packageRef = (Element) packageRefElems.item(i);
                final String name = packageRef.getAttribute("Include");
                final String version = packageRef.getAttribute("Version");
                list.add(new DotnetProjectDependency(name, version));
            }
        }

        return list;
    }

    @Override
    public void setDependencyVersion(DotnetProjectDependency dependency, String version) {
        final NodeList packageRefElems = (NodeList) xpath("/Project/ItemGroup/PackageReference",
                XPathConstants.NODESET);

        if (packageRefElems != null && packageRefElems.getLength() > 0) {
            for (int i = 0; i < packageRefElems.getLength(); i++) {
                final Element packageRef = (Element) packageRefElems.item(i);
                final String name = packageRef.getAttribute("Include");
                if (name.equals(dependency.getName())) {
                    if (version == null || "".equals(version)) {
                        version = "*";
                    }
                    packageRef.setAttribute("Version", version);
                }
            }
        }
    }

    @Override
    public void saveChanges() {
        try {
            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            final Source xmlSource = new DOMSource(document);
            final Result outputTarget = new StreamResult(file);
            transformer.transform(xmlSource, outputTarget);
        } catch (Exception e) {
            log.warn("Failed to save '" + file + "' as XML.");
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }

    }

    public Document getDocument() {
        if (document == null) {
            try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
                final DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                document = documentBuilder.parse(is);
            } catch (Exception e) {
                log.warn("Failed to parse '" + file + "' as XML.");
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new RuntimeException(e);
            }
        }
        return document;
    }

    private Object xpath(String expression, QName returnType) {
        final XPath xPath = XPathFactory.newInstance().newXPath();
        try {
            return xPath.evaluate(expression, getDocument(), returnType);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Failure to process XPath expression: " + expression, e);
        }
    }
}
