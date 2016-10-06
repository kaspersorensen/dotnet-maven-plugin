package org.eobjects.build;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "integration-test-run", defaultPhase = LifecyclePhase.INTEGRATION_TEST)
public class DotnetIntegrationTestRunMojo extends AbstractDotnetTestMojo {

    public static final String RESULT_VALUE_SUCCESS = "Success";
    public static final String RESULT_FILENAME = "dotnet-maven-plugin-integration-test-result.txt";

    @Parameter(property = "dotnet-integration-test-run-enabled", required = false, defaultValue = "true")
    private boolean enabled;

    @Parameter(defaultValue = PluginHelper.PROPERTY_BUILD_DIR, readonly = true)
    private File buildDir;

    public void execute() {
        if (!enabled) {
            getLog().debug("Disabled, skipping");
            return;
        }

        try {
            executeInternal();
            writeSuccess();
        } catch (Exception e) {
            writeFailure(e);
        }
    }

    private void writeSuccess() {
        final File file = new File(buildDir, RESULT_FILENAME);
        if (file.exists()) {
            // remove content
            try (final FileWriter writer = new FileWriter(file)) {
                writer.write(RESULT_VALUE_SUCCESS);
            } catch (IOException ioException) {
                // nothing we can do here - we really don't want to interrupt
                // the build because the 'verify' phase has to clean up later.
                getLog().error("Unexpected IOException while writing to " + file, ioException);
            }
        }
    }

    private void writeFailure(Exception e) {
        buildDir.mkdirs();
        final File file = new File(buildDir, RESULT_FILENAME);
        try {
            file.createNewFile();
        } catch (IOException ioException) {
            getLog().error("Unexpected IOException while creating file " + file, ioException);
        }
        try (final FileWriter writer = new FileWriter(file)) {
            try (final PrintWriter printWriter = new PrintWriter(writer)) {
                e.printStackTrace(printWriter);
            }
        } catch (IOException ioException) {
            // nothing we can do here - we really don't want to interrupt
            // the build because the 'verify' phase has to clean up later.
            getLog().error("Unexpected IOException while writing to " + file, ioException);
        }
    }
}
