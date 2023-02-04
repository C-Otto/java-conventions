package de.cotto.javaconventions.plugins;

import info.solidsoft.gradle.pitest.PitestPlugin;
import info.solidsoft.gradle.pitest.PitestPluginExtension;
import info.solidsoft.gradle.pitest.PitestTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.testing.Test;

import java.util.List;

import static de.cotto.javaconventions.Utils.getVersion;

public abstract class MutationTestsPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(PitestPlugin.class);
        project.getPluginManager().apply(TestsPlugin.class);

        PitestPluginExtension pitest = project.getExtensions().getByType(PitestPluginExtension.class);
        pitest.getPitestVersion().set(getVersion(project, "pitest"));
        pitest.getJunit5PluginVersion().set(getVersion(project, "pitestJunit5Plugin"));
        pitest.getTargetClasses().set(List.of("de.cotto.*"));
        pitest.getOutputFormats().set(List.of("XML", "HTML"));
        pitest.getTimestampedReports().set(false);
        pitest.getFailWhenNoMutations().set(false);
        pitest.getExcludedMethods().set(List.of("hashCode"));
        pitest.getThreads().set(Math.max(Runtime.getRuntime().availableProcessors() / 2, 1));
        pitest.getTestStrengthThreshold().set(100);

        TaskCollection<PitestTask> pitestTasks = project.getTasks().withType(PitestTask.class);
        TaskCollection<Test> testTasks = project.getTasks().withType(Test.class);
        pitestTasks.forEach(
                pitestTask -> pitestTask.dependsOn(testTasks)
        );
    }
}
