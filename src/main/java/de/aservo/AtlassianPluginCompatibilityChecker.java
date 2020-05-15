package de.aservo;

import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Model;

import java.io.File;
import java.util.Properties;

public class AtlassianPluginCompatibilityChecker {

    public static void main(String[] args) throws Exception {
        final File directory;

        final String property = args[0];

        if (args.length > 1) {
            directory = new File(args[1]);
        } else {
            directory = null;
        }

        final File pomFile = new File(directory, "pom.xml");
        final File pomFileBackup = new File(pomFile.getAbsoluteFile()+ ".backup");
        FileUtils.copyFile(pomFile, pomFileBackup);

        final Model model = MavenPomFileUtil.read(pomFile);
        final Properties properties = model.getProperties();
        final String product = property.split("\\.")[0];
        final String productVersion = properties.getProperty(property);
        final VersionList versionList = new VersionList(new VersionFetcher(product, productVersion).getVersions());

        System.out.println(String.format("Testing plugin %s implemented for %s %s for compatibility", model.getArtifactId(), product, productVersion));
        System.out.println();

        for (final String version : versionList) {
            properties.setProperty(property, version);
            model.setProperties(properties);
            MavenPomFileUtil.write(pomFile, model);

            final int exitCode = new MavenWrapperProcess().getProcessBuilder(pomFile).start().waitFor();

            if (exitCode == 0) {
                System.out.println(String.format("Version %s is compatible", version));
            } else {
                System.out.println(String.format("Version %s is NOT compatible", version));
                versionList.setLastFailed();
            }
        }

        System.out.println();
        System.out.println("Last compatible version: " + versionList.getLastSuccessful());

        FileUtils.deleteQuietly(pomFile);
        FileUtils.moveFile(pomFileBackup, pomFile);
    }

}
