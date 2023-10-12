package de.cotto.javaconventions.plugins;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.testing.Test;
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension;
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification;
import org.gradle.testing.jacoco.tasks.JacocoReport;
import org.gradle.testing.jacoco.tasks.rules.JacocoViolationRule;

import java.math.BigDecimal;

import static de.cotto.javaconventions.Utils.getVersion;
import static de.cotto.javaconventions.Utils.mustRunAfterTests;

public abstract class JacocoPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(JavaPlugin.class);
        project.getPluginManager().apply(org.gradle.testing.jacoco.plugins.JacocoPlugin.class);

        JacocoPluginExtension jacoco = project.getExtensions().getByType(JacocoPluginExtension.class);
        jacoco.setToolVersion(getVersion(project, "jacoco"));

        mustRunAfterTests(project, JacocoReport.class);

        TaskCollection<JacocoCoverageVerification> jacocoCoverageVerifications = project.getTasks().withType(JacocoCoverageVerification.class);
        jacocoCoverageVerifications.configureEach(verificationTask -> {
            verificationTask.dependsOn(project.getTasks().withType(Test.class));
            verificationTask.dependsOn(project.getTasks().withType(JacocoReport.class));
            verificationTask.dependsOn(project.getTasks().withType(CheckForExecutionDataTask.class));
            setExecutionDataPath(project, verificationTask.getExecutionData());

            verificationTask.getViolationRules().rule(rule("BRANCH", 0.9));
            verificationTask.getViolationRules().rule(rule("INSTRUCTION", 0.9));
            verificationTask.getViolationRules().rule(rule("METHOD", 0.9));
            verificationTask.getViolationRules().rule(rule("CLASS", 1.0));
        });

        project.getTasks().register("checkForExecutionData", CheckForExecutionDataTask.class)
                .configure(task -> {
                    task.getProjectName().set( project.getName());
                    task.dependsOn(project.getTasks().withType(Test.class));
                    setExecutionDataPath(project, task.getExecutionData());
                });

        project.getTasks().named("check").configure(
                checkTask -> checkTask.dependsOn(jacocoCoverageVerifications)
        );
    }

    private void setExecutionDataPath(Project project, ConfigurableFileCollection fileCollection) {
        ConfigurableFileTree buildDirectory = project.fileTree(project.getLayout().getBuildDirectory());
        fileCollection.setFrom(buildDirectory.include("/jacoco/*.exec"));
    }

    private Action<JacocoViolationRule> rule(String counter, double minimum) {
        return rule -> rule.limit(limit -> {
            limit.setValue("COVEREDRATIO");
            limit.setCounter(counter);
            limit.setMinimum(BigDecimal.valueOf(minimum));
        });
    }

    public static abstract class CheckForExecutionDataTask extends DefaultTask {
        @Input
        public abstract Property<String> getProjectName();

        @InputFiles
        public abstract ConfigurableFileCollection getExecutionData();

        @TaskAction
        public void check() {
            if (getExecutionData().isEmpty()) {
                throw new GradleException("No tests found for " + getProjectName().get());
            }
        }
    }
}
