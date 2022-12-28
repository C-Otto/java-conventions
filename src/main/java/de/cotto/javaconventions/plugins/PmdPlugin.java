package de.cotto.javaconventions.plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.quality.Pmd;
import org.gradle.api.plugins.quality.PmdExtension;

import java.util.List;

import static de.cotto.javaconventions.Utils.getVersionFromCatalog;
import static de.cotto.javaconventions.Utils.shouldRunBeforeTests;

// TODO include actual configuration (pmd-ruleset.xml), as binary plugin?
public abstract class PmdPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(org.gradle.api.plugins.quality.PmdPlugin.class);

        PmdExtension pmd = project.getExtensions().getByType(PmdExtension.class);
        pmd.setToolVersion(getVersionFromCatalog(project, "pmdCpd"));
        pmd.setConsoleOutput(true);
        pmd.setRuleSetFiles(project.files(project.getRootDir() + "/config/pmd-ruleset.xml"));
        pmd.setRuleSets(List.of());

        shouldRunBeforeTests(project, Pmd.class);
    }
}
