package de.cotto.javaconventions.plugins;

import static de.cotto.javaconventions.Utils.getPlatform;

import com.adarshr.gradle.testlogger.TestLoggerExtension;
import com.adarshr.gradle.testlogger.TestLoggerPlugin;
import com.adarshr.gradle.testlogger.theme.ThemeType;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.jvm.JvmTestSuite;
import org.gradle.api.tasks.testing.Test;
import org.gradle.api.tasks.testing.TestDescriptor;
import org.gradle.api.tasks.testing.TestListener;
import org.gradle.api.tasks.testing.TestResult;
import org.gradle.testing.base.TestingExtension;

@SuppressWarnings("UnstableApiUsage")
public abstract class TestsPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(JavaPlugin.class);
        project.getPluginManager().apply(TestLoggerPlugin.class);

        TestingExtension testing = project.getExtensions().getByType(TestingExtension.class);
        testing.getSuites().withType(JvmTestSuite.class).configureEach(testSuite -> {
            testSuite.useJUnitJupiter();
            testSuite.getDependencies().getImplementation().add(project.getDependencies().platform(getPlatform(project)));
            testSuite.getDependencies().getImplementation().add("nl.jqno.equalsverifier:equalsverifier");
            testSuite.getDependencies().getImplementation().add("org.assertj:assertj-core");
        });

        project.getTasks().withType(Test.class).configureEach(testTask -> {
            testTask.addTestListener(new FailIfIgnoredTestListener());

                testTask.getSystemProperties().put(
                    "junit.jupiter.displayname.generator.default",
                    "org.junit.jupiter.api.DisplayNameGenerator$ReplaceUnderscores"
            );
        });

        TestLoggerExtension testLogger = project.getExtensions().getByType(TestLoggerExtension.class);
        testLogger.setTheme(ThemeType.STANDARD_PARALLEL);
        testLogger.setSlowThreshold(1000L);
        testLogger.setShowSimpleNames(true);
        testLogger.setShowOnlySlow(true);
    }

    private static class FailIfIgnoredTestListener extends NoopListener {
        @Override
        public void afterTest(TestDescriptor testDescriptor, TestResult result) {
            if (result.getSkippedTestCount() > 0) {
                throw new GradleException("Do not ignore test cases! Ignored test: " + testDescriptor);
            }
        }
    }

    private static abstract class NoopListener implements TestListener {
        @Override
        public void beforeSuite(TestDescriptor suite) {
        }

        @Override
        public void afterSuite(TestDescriptor suite, TestResult result) {
        }

        @Override
        public void beforeTest(TestDescriptor testDescriptor) {
        }

        @Override
        public void afterTest(TestDescriptor testDescriptor, TestResult result) {
        }
    }
}
