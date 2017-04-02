package org.eobjects.build;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.maven.plugin.logging.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
        final NodeList versionNodes = getDocument().getElementsByTagName("Version");
        if (versionNodes == null || versionNodes.getLength() == 0) {
            return null;
        }
        return versionNodes.item(0).getTextContent();
    }

    @Override
    public void setVersion(String version) {
        // TODO: proper implementation
        throw new UnsupportedOperationException("Updating project version with .csproj files is not yet supported");
    }

    @Override
    public List<DotnetProjectDependency> getDependencies() {
        final List<DotnetProjectDependency> list = new ArrayList<>();

        final NodeList itemGroupElems = getDocument().getElementsByTagName("ItemGroup");
        if (itemGroupElems != null && itemGroupElems.getLength() > 0) {
            final Element itemGroup = (Element) itemGroupElems.item(0);
            final NodeList packageRefElems = itemGroup.getElementsByTagName("PackageReference");
            if (packageRefElems != null && packageRefElems.getLength() > 0) {
                for (int i = 0; i < packageRefElems.getLength(); i++) {
                    final Element packageRef = (Element) packageRefElems.item(i);
                    final String name = packageRef.getAttribute("Include");
                    final String version = packageRef.getAttribute("Version");
                    list.add(new DotnetProjectDependency(name, version));
                }
            }
        }

        return list;
    }

    @Override
    public void setDependencyVersion(DotnetProjectDependency dependency, String version) {
        // TODO: proper implementation
        throw new UnsupportedOperationException("Updating dependency version with .csproj files is not yet supported");
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
}
