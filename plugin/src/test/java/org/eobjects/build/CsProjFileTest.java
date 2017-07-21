package org.eobjects.build;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.junit.Test;

public class CsProjFileTest {

    final DotnetProjectFile file1_xUnit = new CsProjFile(new File("src/test/resources/example1-xunit.csproj"), null);
    final DotnetProjectFile file2_msTest = new CsProjFile(new File("src/test/resources/example2-mstest.csproj"), null);
    final DotnetProjectFile file3_webApp = new CsProjFile(new File("src/test/resources/example3-webapp.csproj"), null);

    @Test
    public void testGetDependencies() {
        assertThat(file1_xUnit.getDependencies()).isNotEmpty().hasSize(3);
        assertThat(file2_msTest.getDependencies()).isNotEmpty().hasSize(3);
        assertThat(file3_webApp.getDependencies()).isNotEmpty().hasSize(5);

        assertThat(file3_webApp.getDependencies())
                .contains(new DotnetProjectDependency("Nancy", "2.0.0-clinteastwood"));
    }

    @Test
    public void testGetVersion() {
        assertThat(file1_xUnit.getVersion()).isEqualTo("LegacyVersionToBeRemoved");
        assertThat(file2_msTest.getVersion()).isNull();
        assertThat(file3_webApp.getVersion()).isNotNull().isEqualTo("1.3.1-alpha");
    }

    @Test
    public void testIsTestProject() {
        assertThat(file1_xUnit.isTestProject()).isTrue();
        assertThat(file2_msTest.isTestProject()).isTrue();
        assertThat(file3_webApp.isTestProject()).isFalse();
    }

    @Test
    public void testUpdateDependency() throws Exception {
        final File testFile = new File("target/testUpdateDependency.csproj");
        if (testFile.exists()) {
            testFile.delete();
        }
        Files.copy(file1_xUnit.getFile().toPath(), testFile.toPath());

        assertThat(getXUnitVersion(testFile)).isEqualTo("2.2.0-beta4-build3444");

        final DotnetProjectFile projectFile = new CsProjFile(testFile, null);

        projectFile.setDependencyVersion(new DotnetProjectDependency("xunit"), "1.2.3");
        projectFile.saveChanges();
        assertThat(getXUnitVersion(testFile)).isEqualTo("1.2.3");

        projectFile.setDependencyVersion(new DotnetProjectDependency("xunit"), null);
        projectFile.saveChanges();
        assertThat(getXUnitVersion(testFile)).isEqualTo("*");

        projectFile.setDependencyVersion(new DotnetProjectDependency("xunit"), "1.2.4");
        projectFile.saveChanges();
        assertThat(getXUnitVersion(testFile)).isEqualTo("1.2.4");

        projectFile.setDependencyVersion(new DotnetProjectDependency("xunit"), "1.2.5");
        projectFile.saveChanges();
        assertThat(getXUnitVersion(testFile)).isEqualTo("1.2.5");
    }

    private String getXUnitVersion(File testFile) {
        final DotnetProjectFile projectFile = new CsProjFile(testFile, null);
        final List<DotnetProjectDependency> dependencies = projectFile.getDependencies();
        for (DotnetProjectDependency dependency : dependencies) {
            if (dependency.getName().equals("xunit")) {
                return dependency.getVersion();
            }
        }
        return null;
    }

    @Test
    public void testUpdateVersion() throws Exception {
        final File testFile = new File("target/testUpdateVersion.csproj");
        if (testFile.exists()) {
            testFile.delete();
        }
        Files.copy(file1_xUnit.getFile().toPath(), testFile.toPath());

        assertThat(getVersion(testFile)).isEqualTo("LegacyVersionToBeRemoved");

        final DotnetProjectFile projectFile = new CsProjFile(testFile, null);
        projectFile.setVersion("42.1337.0");
        projectFile.saveChanges();

        assertThat(getVersion(testFile)).isEqualTo("42.1337.0");

        projectFile.setVersion("42.1337.1");
        projectFile.saveChanges();

        assertThat(getVersion(testFile)).isEqualTo("42.1337.1");

        projectFile.setVersion(null);
        projectFile.saveChanges();

        assertThat(getVersion(testFile)).isNull();

        projectFile.setVersion("42.1337.2-alpha");
        projectFile.saveChanges();

        assertThat(getVersion(testFile)).isEqualTo("42.1337.2-alpha");

        projectFile.setVersion("42.1337.3-");
        projectFile.saveChanges();

        assertThat(getVersion(testFile)).isEqualTo("42.1337.3");

        projectFile.setVersion(null);
        projectFile.saveChanges();

        assertThat(getVersion(testFile)).isNull();

        projectFile.setVersion("42.1337.4");
        projectFile.saveChanges();
        projectFile.setVersion("42.1337.4-beta");
        projectFile.saveChanges();

        assertThat(getVersion(testFile)).isEqualTo("42.1337.4-beta");

        projectFile.setVersion("-dev");
        projectFile.saveChanges();

        assertThat(getVersion(testFile)).isEqualTo("-dev");
    }

    private String getVersion(File testFile) {
        return new CsProjFile(testFile, null).getVersion();
    }
}
