package org.eobjects.build;

import java.io.File;
import java.util.Iterator;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Mojo(name = "update-versions")
public class DotnetVersionUpdateMojo extends AbstractDotnetMojo {

    @Parameter(defaultValue = "${project.version}", readonly = true)
    private String version;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void execute() throws MojoExecutionException, MojoFailureException {
        final File[] projectDirectories = getPluginHelper().getProjectDirectories(false);
        for (File directory : projectDirectories) {
            final File projectJsonFile = new File(directory, "project.json");
            try {
                updateVersion(projectJsonFile);
            } catch (Exception e) {
                throw new MojoExecutionException("Failed to update version in file: " + projectJsonFile, e);
            }
        }
    }

    private void updateVersion(File projectJsonFile) throws Exception {
        final ObjectNode root = (ObjectNode) objectMapper.readTree(projectJsonFile);
        final JsonNode versionNode = root.get("version");

        boolean updatesMade = false;

        final String projectJsonVersion = versionNode == null || versionNode.isMissingNode() ? ""
                : versionNode.asText();
        if (needsUpdating(projectJsonVersion, false)) {
            root.put("version", version);
            getLog().info("Updating module version to: " + version);
            updatesMade = true;
        }

        final ObjectNode dependenciesNode = (ObjectNode) root.get("dependencies");
        final Iterator<String> dependencyNames = dependenciesNode.fieldNames();
        while (dependencyNames.hasNext()) {
            final String dependencyName = dependencyNames.next();
            final JsonNode dependencyValue = dependenciesNode.get(dependencyName);
            if (dependencyValue.isTextual()) {
                if (needsUpdating(dependencyValue.asText())) {
                    dependenciesNode.put(dependencyName, version);
                    getLog().info("Updating dependency '" + dependencyName + "' to version: " + version);
                    updatesMade = true;
                }
            } else if (dependencyValue.isObject()) {
                final ObjectNode dependencyObject = (ObjectNode) dependencyValue;
                final JsonNode dependencyVersionNode = dependencyObject.get("version");
                if (needsUpdating(dependencyVersionNode.asText())) {
                    dependencyObject.put("version", version);
                    getLog().info("Updating dependency '" + dependencyName + "' to version: " + version);
                    updatesMade = true;
                }
            }
        }

        if (updatesMade) {
            getLog().info("Writing updated file (version '" + version + "'): " + projectJsonVersion);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(projectJsonFile, root);
        }
    }

    private boolean needsUpdating(String aVersion) {
        return needsUpdating(aVersion, true);
    }

    private boolean needsUpdating(String aVersion, boolean onlySnapshots) {
        if (aVersion == null) {
            return false;
        }
        if (onlySnapshots && !aVersion.toUpperCase().endsWith("-SNAPSHOT")) {
            return false;
        }
        return !version.equals(aVersion);
    }
}
