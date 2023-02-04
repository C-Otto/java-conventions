package de.cotto.javaconventions;

import de.cotto.javaconventions.plugins.CheckstylePlugin;
import de.cotto.javaconventions.plugins.CpdPlugin;
import de.cotto.javaconventions.plugins.ErrorPronePlugin;
import de.cotto.javaconventions.plugins.IntegrationTestsPlugin;
import de.cotto.javaconventions.plugins.JacocoPlugin;
import de.cotto.javaconventions.plugins.MutationTestsPlugin;
import de.cotto.javaconventions.plugins.PmdPlugin;
import de.cotto.javaconventions.plugins.SpotbugsPlugin;
import de.cotto.javaconventions.plugins.TestsPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.JavaResolutionConsistency;
import org.gradle.api.tasks.compile.JavaCompile;

import static org.gradle.api.JavaVersion.VERSION_17;

@SuppressWarnings("UnstableApiUsage")
public abstract class JavaConventionsPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(CheckstylePlugin.class);
        project.getPluginManager().apply(CpdPlugin.class);
        project.getPluginManager().apply(PmdPlugin.class);
        project.getPluginManager().apply(ErrorPronePlugin.class);
        project.getPluginManager().apply(JacocoPlugin.class);
        project.getPluginManager().apply(TestsPlugin.class);
        project.getPluginManager().apply(MutationTestsPlugin.class);
        project.getPluginManager().apply(SpotbugsPlugin.class);
        project.getPluginManager().apply(IntegrationTestsPlugin.class);

        project.getDependencies().add(
                JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME,
                project.getDependencies().platform(Utils.getPlatform(project))
        );

        JavaPluginExtension java = project.getExtensions().getByType(JavaPluginExtension.class);
        java.setSourceCompatibility(VERSION_17);
        java.setTargetCompatibility(VERSION_17);
        java.consistentResolution(JavaResolutionConsistency::useCompileClasspathVersions);

        project.getTasks().withType(JavaCompile.class).configureEach(
                compileTask -> compileTask.getOptions().getCompilerArgs().add("-Werror")
        );
    }
}
