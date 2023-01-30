package de.cotto.javaconventions.plugins;

import com.github.spotbugs.snom.SpotBugsExtension;
import com.github.spotbugs.snom.SpotBugsPlugin;
import com.github.spotbugs.snom.SpotBugsTask;
import de.cotto.javaconventions.Utils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskCollection;

import static de.cotto.javaconventions.Utils.getResourceFile;

public abstract class SpotbugsPlugin implements Plugin<Project> {

    private static final String SPOTBUGS_EXCLUDE_XML = "/spotbugs-exclude.xml";

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(SpotBugsPlugin.class);

        SpotBugsExtension spotbugs = project.getExtensions().getByType(SpotBugsExtension.class);
        spotbugs.getExcludeFilter().set(project.file(getResourceFile(project, SPOTBUGS_EXCLUDE_XML)));
        spotbugs.getToolVersion().set(Utils.getVersionFromCatalog(project, "spotbugs"));

        TaskCollection<SpotBugsTask> spotbugsTasks = project.getTasks().withType(SpotBugsTask.class);
        spotbugsTasks.configureEach(task -> {
            task.getReports().create("xml").getRequired().set(false);
            task.getReports().create("html").getRequired().set(true);
        });
    }
}
