package de.cotto.javaconventions;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.resources.TextResource;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.testing.Test;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

public class Utils {
    private static final String VERSIONS_PROPERTIES_FILE = "/versions.properties";

    public static String getPlatform(Project project) {
        return "de.c-otto:java-platform:" + getVersion(project, "platform");
    }

    public static String getVersion(Project project, String alias) {
        Properties properties = getProperties(project);
        return properties.get(alias).toString();
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

    private static Properties getProperties(Project project) {
        Properties properties = new Properties();
        String propertiesString = readResourceFile(project, VERSIONS_PROPERTIES_FILE);
        try {
            properties.load(new StringReader(propertiesString));
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        return properties;
    }

    private static TaskCollection<Test> testTasks(Project project) {
        return tasksWithType(project, Test.class);
    }

    private static <T extends Task> TaskCollection<T> tasksWithType(Project project, Class<T> taskType) {
        return project.getTasks().withType(taskType);
    }

    public static String readResourceFile(Project project, String resourcePath) {
        return getTextResource(project, resourcePath).asString();
    }

    public static File getResourceFile(Project project, String resourcePath) {
        String targetName = new File(resourcePath).getName();
        File temporaryFile = getTextResource(project, resourcePath).asFile();
        Path target = new File(temporaryFile.getParentFile(), targetName).toPath();
        try {
            Files.deleteIfExists(target);
            Files.copy(temporaryFile.toPath(), target);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new File(temporaryFile.getParentFile(), targetName);
    }

    private static TextResource getTextResource(Project project, String resourcePath) {
        URL uri = getResourceUri(resourcePath);
        return project.getResources().getText().fromUri(uri);
    }

    private static URL getResourceUri(String resourcePath) {
        return Objects.requireNonNull(Utils.class.getResource(resourcePath));
    }
}
