package org.eobjects.build;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "integration-test-verify", defaultPhase = LifecyclePhase.VERIFY)
public class DotnetIntegrationTestVerifyMojo extends AbstractDotnetTestMojo {

    @Parameter(property = "dotnet.integration.verify.enabled", required = false, defaultValue = "true")
    private boolean integrationTestVerifyEnabled;

    @Parameter(defaultValue = PluginHelper.PROPERTY_BUILD_DIR, readonly = true)
    private File buildDir;

    public void execute() throws MojoExecutionException, MojoFailureException {
        final Log log = getLog();
        if (!integrationTestVerifyEnabled) {
            log.debug("Disabled, skipping");
            return;
        }

        final File file = new File(buildDir, DotnetIntegrationTestRunMojo.RESULT_FILENAME);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line = reader.readLine();
                if (DotnetIntegrationTestRunMojo.RESULT_VALUE_SUCCESS.equals(line)) {
                    // everything OK
                    return;
                }

                while (line != null) {
                    log.warn(line);
                    line = reader.readLine();
                }
            } catch (Exception e) {
                throw new MojoExecutionException("Could not read from " + file, e);
            }
        }

        // error out with generic message (if successful we should have return'ed earlier)
        throw new MojoFailureException("Dotnet integration tests failed");
    }
}
