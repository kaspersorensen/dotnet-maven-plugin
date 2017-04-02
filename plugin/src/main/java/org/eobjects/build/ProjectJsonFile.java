package org.eobjects.build;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ProjectJsonFile implements DotnetProjectFile {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final File file;
    private final Log log;
    private ObjectNode root;

    public ProjectJsonFile(File file, Log log) {
        this.file = file;
        this.log = log;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public boolean isTestProject() {
        final JsonNode testRunnerNode = getRoot().get("testRunner");
        return testRunnerNode != null && !testRunnerNode.isMissingNode();
    }

    @Override
    public String getVersion() {
        final JsonNode versionNode = getRoot().get("version");
        return versionNode == null || versionNode.isMissingNode() ? "" : versionNode.asText();
    }

    private ObjectNode getRoot() {
        if (root == null) {
            try {
                root = (ObjectNode) objectMapper.readTree(file);
            } catch (Exception e) {
                log.warn("Failed to parse '" + file + "' as JSON.");
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new RuntimeException(e);
            }
        }
        return root;
    }

    @Override
    public void setVersion(String version) {
        getRoot().put("version", version);
    }

    @Override
    public List<DotnetProjectDependency> getDependencies() {
        final List<DotnetProjectDependency> dependencies = new ArrayList<>();
        final ObjectNode dependenciesNode = (ObjectNode) root.get("dependencies");
        final Iterator<String> dependencyNames = dependenciesNode.fieldNames();
        while (dependencyNames.hasNext()) {
            final String dependencyName = dependencyNames.next();
            final JsonNode dependencyValue = dependenciesNode.get(dependencyName);
            if (dependencyValue.isTextual()) {
                dependencies.add(new DotnetProjectDependency(dependencyName, dependencyValue.asText()));
            } else if (dependencyValue.isObject()) {
                final ObjectNode dependencyObject = (ObjectNode) dependencyValue;
                final JsonNode dependencyVersionNode = dependencyObject.get("version");
                dependencies.add(new DotnetProjectDependency(dependencyName, dependencyVersionNode.asText()));
            }
        }
        return dependencies;
    }

    @Override
    public void setDependencyVersion(DotnetProjectDependency dependency, String version) {
        // TODO: Implement
    }

    @Override
    public void saveChanges() {
        if (root == null) {
            // no changes made for sure
            return;
        }
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, root);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
    }

}
