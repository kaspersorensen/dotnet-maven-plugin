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
        String version = (String) xpath("/Project/PropertyGroup/VersionPrefix", XPathConstants.STRING);
        String vSuffix = (String) xpath("/Project/PropertyGroup/VersionSuffix", XPathConstants.STRING);

        /* The <Version> tag overrides the Prefix/Suffix pair */
        if ("".equals(version)) {
            version = (String) xpath("/Project/PropertyGroup/Version", XPathConstants.STRING);
            if (!"".equals(version)) {
                vSuffix = "";
            }
        }

        if (!"".equals(vSuffix)) {
            version += "-" + vSuffix;
        }

        if ("".equals(version)) {
            return null;
        }
        return version;
    }

    public void setVersionPart(String element, String version, String pairWithElement) {
        final Node versionNode = (Node) xpath("/Project/PropertyGroup/" + element, XPathConstants.NODE);

        final boolean removeVersion = version == null || version.isEmpty();
        if (versionNode == null) {
            if (!removeVersion) {
                /*
                 * Need to create a new node. Try to pair it with the named element if possible.
                 */
                final Node pairNode = (Node) xpath("/Project/PropertyGroup/" + pairWithElement, XPathConstants.NODE);

                Element parentPropGroup = null;
                if (pairNode == null) {
                    parentPropGroup = getDocument().createElement("PropertyGroup");
                    final Node projectNode = (Node) xpath("/Project", XPathConstants.NODE);
                    projectNode.insertBefore(parentPropGroup, projectNode.getFirstChild());
                } else {
                    parentPropGroup = (Element) pairNode.getParentNode();
                }

                final Element newVersion = getDocument().createElement(element);
                newVersion.setTextContent(version);
                parentPropGroup.appendChild(newVersion);
            }
        } else {
            if (removeVersion) {
                final Node versionParent = versionNode.getParentNode();
                versionParent.removeChild(versionNode);
                if (versionParent.getChildNodes().getLength() == 0) {
                    versionParent.getParentNode().removeChild(versionParent);
                }
            } else {
                versionNode.setTextContent(version);
            }
        }
    }

    @Override
    public void setVersion(String version) {
        /*
         * Dotnet .csproj versions are stored in a pair of properties now: VersionPrefix VersionSuffix
         * 
         * The Version element is still supported and overrides the new pair. Here we'll parse off the last portion and
         * make that the suffix.
         */
        String versionPrefix = version;
        String versionSuffix = "";

        if (version != null && !version.isEmpty()) {
            int iSuffix = version.lastIndexOf("-");
            if (-1 != iSuffix) {
                versionSuffix = version.substring(iSuffix + 1);
                versionPrefix = version.substring(0, iSuffix);
            }
        }

        setVersionPart("VersionPrefix", versionPrefix, "VersionSuffix");
        setVersionPart("VersionSuffix", versionSuffix, "VersionPrefix");
        // Remove the Version tag if it exists.
        setVersionPart("Version", null, null);
    }

    @Override
    public List<DotnetProjectDependency> getDependencies() {
        final List<DotnetProjectDependency> list = new ArrayList<>();

        final NodeList packageRefElems =
                (NodeList) xpath("/Project/ItemGroup/PackageReference", XPathConstants.NODESET);

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
        final NodeList packageRefElems =
                (NodeList) xpath("/Project/ItemGroup/PackageReference", XPathConstants.NODESET);

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
