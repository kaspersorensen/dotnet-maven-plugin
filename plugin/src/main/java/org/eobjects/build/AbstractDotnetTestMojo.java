package org.eobjects.build;

import java.io.File;

import org.apache.maven.plugin.MojoFailureException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public abstract class AbstractDotnetTestMojo extends AbstractDotnetMojo {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void executeInternal() throws MojoFailureException {
        final PluginHelper helper = getPluginHelper();
        for (File subDirectory : helper.getProjectDirectories()) {
            if (isTestRunnable(subDirectory)) {
                helper.executeCommand(subDirectory, "dotnet", "test", "-c", helper.getBuildConfiguration());
            }
        }
    }

    private boolean isTestRunnable(File subDirectory) {
        // check if there is a 'testRunner' attribute defined in project.json
        final File projectJsonFile = new File(subDirectory, "project.json");
        final ObjectNode root;
        try {
            root = (ObjectNode) objectMapper.readTree(projectJsonFile);
        } catch (Exception e) {
            getLog().warn("Failed to parse '" + projectJsonFile
                    + "' in order to determine if a 'testRunner' attribute is set.");
            return false;
        }
        final JsonNode testRunnerNode = root.get("testRunner");
        return testRunnerNode != null && !testRunnerNode.isMissingNode();
    }
}
