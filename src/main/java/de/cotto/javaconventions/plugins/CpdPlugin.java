package de.cotto.javaconventions.plugins;

import de.aaschmid.gradle.plugins.cpd.Cpd;
import de.aaschmid.gradle.plugins.cpd.CpdExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import static de.cotto.javaconventions.Utils.getVersionFromCatalog;
import static de.cotto.javaconventions.Utils.shouldRunBeforeTests;

@SuppressWarnings("UnstableApiUsage")
public abstract class CpdPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(de.aaschmid.gradle.plugins.cpd.CpdPlugin.class);

        CpdExtension cpd = project.getExtensions().getByType(CpdExtension.class);
        cpd.setToolVersion(getVersionFromCatalog(project, "pmdCpd"));

        shouldRunBeforeTests(project, Cpd.class);
    }
}
