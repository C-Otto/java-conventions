package de.cotto.javaconventions.plugins;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.testing.Test;
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension;
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification;
import org.gradle.testing.jacoco.tasks.JacocoReport;
import org.gradle.testing.jacoco.tasks.rules.JacocoViolationRule;

import javax.inject.Inject;
import java.math.BigDecimal;

import static de.cotto.javaconventions.Utils.getVersionFromCatalog;
import static de.cotto.javaconventions.Utils.mustRunAfterTests;

public abstract class JacocoPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(org.gradle.testing.jacoco.plugins.JacocoPlugin.class);

        JacocoPluginExtension jacoco = project.getExtensions().getByType(JacocoPluginExtension.class);
        jacoco.setToolVersion(getVersionFromCatalog(project, "jacoco"));

        mustRunAfterTests(project, JacocoReport.class);

        project.getTasks().withType(JacocoCoverageVerification.class).configureEach(verificationTask -> {
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
                .configure(task -> setExecutionDataPath(project, task.getExecutionData()));
        mustRunAfterTests(project, CheckForExecutionDataTask.class);
    }

    private void setExecutionDataPath(Project project, ConfigurableFileCollection fileCollection) {
        fileCollection.setFrom(project.fileTree(project.getBuildDir()).include("/jacoco/*.exec"));
    }

    private Action<JacocoViolationRule> rule(String counter, double minimum) {
        return rule -> rule.limit(limit -> {
            limit.setValue("COVEREDRATIO");
            limit.setCounter(counter);
            limit.setMinimum(BigDecimal.valueOf(minimum));
        });
    }

    public static class CheckForExecutionDataTask extends DefaultTask {
        private final Project project;

        @Inject
        public CheckForExecutionDataTask(Project project) {
            this.project = project;
        }

        @TaskAction
        public void check() {
            if (getExecutionData().isEmpty()) {
                throw new GradleException("No tests found for " + project);
            }
        }

        @InputFiles
        public ConfigurableFileCollection getExecutionData() {
            return project.files();
        }

    }
}
