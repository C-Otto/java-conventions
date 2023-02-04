package de.cotto.javaconventions.plugins;

import static de.cotto.javaconventions.Utils.getPlatform;

import java.util.List;
import net.ltgt.gradle.errorprone.CheckSeverity;
import net.ltgt.gradle.errorprone.ErrorProneOptions;
import net.ltgt.gradle.nullaway.NullAwayExtension;
import net.ltgt.gradle.nullaway.NullAwayOptions;
import net.ltgt.gradle.nullaway.NullAwayPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.tasks.compile.JavaCompile;

public abstract class ErrorPronePlugin implements Plugin<Project> {

    private static final String ANNOTATED_PACKAGES = "de.cotto";

    @Override
    public void apply(Project project) {
        PluginManager pluginManager = project.getPluginManager();
        DependencyHandler dependencies = project.getDependencies();
        ExtensionContainer extensions = project.getExtensions();

        pluginManager.apply(net.ltgt.gradle.errorprone.ErrorPronePlugin.class);
        pluginManager.apply(NullAwayPlugin.class);

        NullAwayExtension nullAwayExtension = extensions.getByType(NullAwayExtension.class);
        nullAwayExtension.getAnnotatedPackages().add(ANNOTATED_PACKAGES);

        project.getTasks().withType(JavaCompile.class).configureEach(
                compileTask -> {
                    ErrorProneOptions errorProne = getExtension(compileTask.getOptions(), ErrorProneOptions.class);
                    configureErrorProne(errorProne);

                    NullAwayOptions nullAway = getExtension(errorProne, NullAwayOptions.class);
                    configureNullAway(nullAway);
                }
        );

        String configurationName = net.ltgt.gradle.errorprone.ErrorPronePlugin.CONFIGURATION_NAME;
        dependencies.add(configurationName, dependencies.platform(getPlatform(project)));
        dependencies.add(configurationName, "com.google.errorprone:error_prone_core");
        dependencies.add(configurationName, "com.uber.nullaway:nullaway");
    }

    private void configureErrorProne(ErrorProneOptions errorProne) {
        errorProne.disable("EqualsGetClass");
        errorProne.getExcludedPaths().set(".*/generated/.*");
    }

    private void configureNullAway(NullAwayOptions nullAway) {
        List.of(
                "org.mockito.Mock",
                "org.mockito.InjectMocks",
                "org.junit.jupiter.api.io.TempDir",
                "org.springframework.boot.test.mock.mockito.MockBean",
                "org.springframework.beans.factory.annotation.Autowired",
                "org.mockito.Captor",
                "org.springframework.beans.factory.annotation.Value"
        ).forEach(nullAway.getExcludedFieldAnnotations()::add);

        nullAway.getExcludedClassAnnotations()
                .add("org.springframework.boot.context.properties.ConfigurationProperties");

        nullAway.getSeverity().set(CheckSeverity.ERROR);
    }

    private <T> T getExtension(Object source, Class<T> clazz) {
        return ((ExtensionAware) source).getExtensions().getByType(clazz);
    }
}
