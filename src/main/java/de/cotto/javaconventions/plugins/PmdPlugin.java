package de.cotto.javaconventions.plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.quality.Pmd;
import org.gradle.api.plugins.quality.PmdExtension;

import java.util.List;

import static de.cotto.javaconventions.Utils.getResourceFile;
import static de.cotto.javaconventions.Utils.getVersion;
import static de.cotto.javaconventions.Utils.shouldRunBeforeTests;

public abstract class PmdPlugin implements Plugin<Project> {
    private static final String PMD_RULESET_XML = "/pmd-ruleset.xml";

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(org.gradle.api.plugins.quality.PmdPlugin.class);

        PmdExtension pmd = project.getExtensions().getByType(PmdExtension.class);
        pmd.setToolVersion(getVersion(project, "pmdCpd"));
        pmd.setConsoleOutput(true);
        pmd.setRuleSetFiles(project.files(getResourceFile(project, PMD_RULESET_XML)));
        pmd.setRuleSets(List.of());

        shouldRunBeforeTests(project, Pmd.class);
    }
}
