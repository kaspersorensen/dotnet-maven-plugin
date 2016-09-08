package org.eobjects.build;

import java.io.File;

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
        final File[] projectDirectories = getPluginHelper().getProjectDirectories();
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

        final String projectJsonVersion = versionNode == null || versionNode.isMissingNode() ? ""
                : versionNode.asText();

        if (!version.equals(projectJsonVersion)) {
            root.put("version", version);
            getLog().info("Updating version '" + projectJsonVersion + "' to '" + version + "' in file: "
                    + projectJsonVersion);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(projectJsonFile, root);
        }
    }
}
