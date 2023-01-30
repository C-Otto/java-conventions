package de.cotto.javaconventions;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.MinimalExternalModuleDependency;
import org.gradle.api.artifacts.VersionCatalog;
import org.gradle.api.artifacts.VersionCatalogsExtension;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.testing.Test;

import java.io.File;
import java.net.URL;
import java.util.Objects;

public class Utils {
    public static MinimalExternalModuleDependency getFromCatalog(Project project, String alias) {
        VersionCatalog libs = getLibs(project);
        return libs.findLibrary(alias).orElseThrow().get();
    }

    public static String getVersionFromCatalog(Project project, String alias) {
        VersionCatalog libs = getLibs(project);
        return libs.findVersion(alias).orElseThrow().getRequiredVersion();
    }

    public static <T extends Task> void shouldRunBeforeTests(Project project, Class<T> taskType) {
        TaskCollection<T> tasks = project.getTasks().withType(taskType);
        testTasks(project).forEach(
                testTask -> testTask.shouldRunAfter(tasks)
        );
    }

    public static <T extends Task> void mustRunAfterTests(Project project, Class<T> taskType) {
        TaskCollection<Test> testTasks = testTasks(project);
        tasksWithType(project, taskType).forEach(
                task -> task.mustRunAfter(testTasks)
        );
    }

    private static VersionCatalog getLibs(Project project) {
        return project.getExtensions().getByType(VersionCatalogsExtension.class).named("libs");
    }

    private static TaskCollection<Test> testTasks(Project project) {
        return tasksWithType(project, Test.class);
    }

    private static <T extends Task> TaskCollection<T> tasksWithType(Project project, Class<T> taskType) {
        return project.getTasks().withType(taskType);
    }

    public static File getResourceFile(Project project, String resourcePath) {
        URL uri = Objects.requireNonNull(Utils.class.getResource(resourcePath));
        return project.getResources().getText().fromUri(uri).asFile();
    }
}
