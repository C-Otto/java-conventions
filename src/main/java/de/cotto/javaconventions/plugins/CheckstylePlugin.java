package de.cotto.javaconventions.plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.quality.Checkstyle;
import org.gradle.api.plugins.quality.CheckstyleExtension;

import static de.cotto.javaconventions.Utils.getResourceFile;
import static de.cotto.javaconventions.Utils.getVersion;
import static de.cotto.javaconventions.Utils.shouldRunBeforeTests;

public abstract class CheckstylePlugin implements Plugin<Project> {

    private static final String CHECKSTYLE_XML = "/checkstyle.xml";

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(org.gradle.api.plugins.quality.CheckstylePlugin.class);

        CheckstyleExtension checkstyle = project.getExtensions().getByType(CheckstyleExtension.class);
        checkstyle.setMaxWarnings(0);
        checkstyle.setToolVersion(getVersion(project, "checkstyle"));
        checkstyle.setConfigFile(getResourceFile(project, CHECKSTYLE_XML));

        project.getConfigurations().all(
                configuration -> configuration.resolutionStrategy(
                        resolutionStrategy -> resolutionStrategy.capabilitiesResolution(
                                capabilitiesResolution -> capabilitiesResolution.withCapability("com.google.collections:google-collections", capability -> capability.select("com.google.guava:guava:0")
                                )
                        )
                )
        );

        shouldRunBeforeTests(project, Checkstyle.class);
    }
}
