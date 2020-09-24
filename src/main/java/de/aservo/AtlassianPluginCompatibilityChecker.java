package de.aservo;

import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.io.File;
import java.util.Map;
import java.util.Properties;

import static de.aservo.Product.*;

public class AtlassianPluginCompatibilityChecker {

    public static void main(String[] args) throws Exception {
        final File directory;

        if (args.length > 0) {
            directory = new File(args[0]);
        } else {
            directory = null;
        }

        final File pomFile = new File(directory, "pom.xml");
        final File pomFileBackup = new File(pomFile.getAbsoluteFile()+ ".backup");
        FileUtils.copyFile(pomFile, pomFileBackup);

        final Model model = MavenPomFileUtil.read(pomFile);
        final Map<String, Plugin> pluginMap = model.getBuild().getPluginsAsMap();

        final Product product;
        final Plugin plugin;

        if (pluginMap.containsKey("com.atlassian.maven.plugins:jira-maven-plugin")) {
            product = JIRA;
            plugin = pluginMap.get("com.atlassian.maven.plugins:jira-maven-plugin");
        } else if (pluginMap.containsKey("com.atlassian.maven.plugins:maven-jira-plugin")) {
            product = JIRA;
            plugin = pluginMap.get("com.atlassian.maven.plugins:maven-jira-plugin");
        } else if (pluginMap.containsKey("com.atlassian.maven.plugins:crowd-maven-plugin")) {
            product = CROWD;
            plugin = pluginMap.get("com.atlassian.maven.plugins:crowd-maven-plugin");
        } else if (pluginMap.containsKey("com.atlassian.maven.plugins:maven-crowd-plugin")) {
            product = CROWD;
            plugin = pluginMap.get("com.atlassian.maven.plugins:maven-crowd-plugin");
        } else if (pluginMap.containsKey("com.atlassian.maven.plugins:bitbucket-maven-plugin")) {
            product = BITBUCKET;
            plugin = pluginMap.get("com.atlassian.maven.plugins:bitbucket-maven-plugin");
        } else if (pluginMap.containsKey("com.atlassian.maven.plugins:maven-bitbucket-plugin")) {
            product = BITBUCKET;
            plugin = pluginMap.get("com.atlassian.maven.plugins:maven-bitbucket-plugin");
        } else {
            return;
        }

        final String versionProperty = String.format("%s.version", product.toString().toLowerCase());
        final Properties properties = model.getProperties();
        final String productVersion = properties.getProperty(versionProperty);
        final VersionList versionList = new VersionList(new VersionFetcher(product, productVersion).getVersions());

        System.out.printf("Testing plugin %s implemented for %s %s for compatibility%n", model.getArtifactId(), product, productVersion);
        System.out.println();

        try {
            for (final String version : versionList) {
                properties.setProperty(versionProperty, version);
                model.setProperties(properties);
                MavenPomFileUtil.write(pomFile, model);

                final int exitCode = new MavenWrapperProcess().getProcessBuilder(pomFile).start().waitFor();

                if (exitCode == 0) {
                    System.out.printf("Version %s is compatible%n", version);
                } else {
                    System.out.printf("Version %s is NOT compatible%n", version);
                    versionList.setLastFailed();
                }
            }
        } finally {
            FileUtils.deleteQuietly(pomFile);
            FileUtils.moveFile(pomFileBackup, pomFile);
        }

        System.out.println();
        System.out.println("Last compatible version: " + versionList.getLastSuccessful());
    }

}
