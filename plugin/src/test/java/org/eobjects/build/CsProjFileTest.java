package org.eobjects.build;

import static org.assertj.core.api.Assertions.*;

import java.io.File;

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
        
        assertThat(file3_webApp.getDependencies()).contains(new DotnetProjectDependency("Nancy", "2.0.0-clinteastwood"));
    }

    @Test
    public void testGetVersion() {
        assertThat(file1_xUnit.getVersion()).isNull();
        assertThat(file2_msTest.getVersion()).isNull();
        assertThat(file3_webApp.getVersion()).isNotNull().isEqualTo("1.3.1");
    }

    @Test
    public void testIsTestProject() {
        assertThat(file1_xUnit.isTestProject()).isTrue();
        assertThat(file2_msTest.isTestProject()).isTrue();
        assertThat(file3_webApp.isTestProject()).isFalse();
    }
}
