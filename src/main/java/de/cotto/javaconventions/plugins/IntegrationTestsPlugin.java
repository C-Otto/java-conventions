package de.cotto.javaconventions.plugins;

import static de.cotto.javaconventions.Utils.getPlatform;

import com.adarshr.gradle.testlogger.TestLoggerExtension;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.attributes.TestSuiteType;
import org.gradle.api.plugins.jvm.JvmTestSuite;
import org.gradle.testing.base.TestingExtension;

@SuppressWarnings("UnstableApiUsage")
public abstract class IntegrationTestsPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(TestsPlugin.class);

        TestingExtension testing = project.getExtensions().getByType(TestingExtension.class);
        NamedDomainObjectProvider<JvmTestSuite> suite = testing.getSuites().register("integrationTest", JvmTestSuite.class);
        suite.configure(testSuite -> {
            testSuite.getTestType().set(TestSuiteType.INTEGRATION_TEST);
            testSuite.getDependencies().getImplementation().add(project.getDependencies().platform(getPlatform(project)));
            testSuite.getDependencies().getImplementation().add(testSuite.getDependencies().project());

            testSuite.getTargets().configureEach(
                    target -> target.getTestTask().configure(
                            testTask -> testTask.shouldRunAfter(testing.getSuites().named("test"))
                    )
            );

            TestLoggerExtension testLogger = project.getExtensions().getByType(TestLoggerExtension.class);
            testLogger.setSlowThreshold(2000L);
        });

        project.getTasks().named("check").configure(
                checkTask -> checkTask.dependsOn(suite)
        );
    }
}
