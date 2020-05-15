package de.aservo;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class VersionList implements Iterable<String> {

    public static final String VERSION_FORMAT = "%d.%d.%d";

    private final TreeSet<DefaultArtifactVersion> versions;

    private DefaultArtifactVersion lastSuccessfulVersion = null;
    private DefaultArtifactVersion versionToTest = null;

    private boolean majorFailed = false;
    private boolean minorFailed = false;
    private boolean incrementalFailed = false;

    public VersionList(
            final Collection<String> versions) {

        this.versions = versions.stream()
                .map(DefaultArtifactVersion::new)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public VersionList(
            final Collection<String> versions,
            final String startVersion) {

        final TreeSet<DefaultArtifactVersion> versionTreeSet = versions.stream()
                .map(DefaultArtifactVersion::new)
                .collect(Collectors.toCollection(TreeSet::new));

        this.versions = new TreeSet<>(versionTreeSet.subSet(
                new DefaultArtifactVersion(startVersion), versionTreeSet.first()));
    }

    public void setLastFailed() {
        if (!majorFailed && versionToTest.equals(getLowerMajor(lastSuccessfulVersion))) {
            majorFailed = true;
        } else if (!minorFailed && versionToTest.equals(getLowerMinor(lastSuccessfulVersion))) {
            minorFailed = true;
        } else if (!incrementalFailed && versionToTest.equals(versions.lower(lastSuccessfulVersion))) {
            incrementalFailed = true;
        }

        versionToTest = null;
    }

    private DefaultArtifactVersion getLowerMajor(
            final DefaultArtifactVersion version) {

        // if we already have a main major version, find the next version that is lower and then the major for that one
        if (version.getMinorVersion() == 0 && version.getIncrementalVersion() == 0) {
            final DefaultArtifactVersion lowerVersion = versions.lower(version);

            // is there still a lower version?
            if (lowerVersion == null) {
                return null;
            }
            // is the lower version already a major version?
            else if (lowerVersion.getMinorVersion() == 0 && lowerVersion.getIncrementalVersion() == 0) {
                return lowerVersion;
            }

            // find the next lower major from that lower version
            return getLowerMajor(lowerVersion);
        }

        // otherwise, just construct the main major version
        final DefaultArtifactVersion majorVersion = new Version(version.getMajorVersion(), 0, 0);

        if (versions.contains(majorVersion)) {
            return majorVersion;
        }

        return null;
    }

    private DefaultArtifactVersion getLowerMinor(
            final DefaultArtifactVersion version) {

        // if we already have a main minor version, find the next version that is lower and then the minor for that one
        if (version.getIncrementalVersion() == 0) {
            final DefaultArtifactVersion lowerVersion = versions.lower(version);

            // is there still a lower version?
            if (lowerVersion == null) {
                return null;
            }
            // is the lower version already a minor version?
            else if (lowerVersion.getIncrementalVersion() == 0) {
                return lowerVersion;
            }

            return getLowerMinor(lowerVersion);
        }

        // otherwise, just construct the main major version
        final DefaultArtifactVersion minorVersion = new Version(version.getMajorVersion(), version.getMinorVersion(), 0);

        if (versions.contains(minorVersion)) {
            return minorVersion;
        }

        return null;
    }

    @Override
    public Iterator<String> iterator() {
        return new Iterator<String>() {

            @Override
            public boolean hasNext() {
                return !versions.isEmpty() && !incrementalFailed;
            }

            @Override
            public String next() {
                if (versionToTest != null) {
                    lastSuccessfulVersion = versionToTest;
                }

                versionToTest = null;

                if (lastSuccessfulVersion == null) {
                    versionToTest = versions.last();
                } else if (!majorFailed) {
                    versionToTest = getLowerMajor(lastSuccessfulVersion);
                } else if (!minorFailed) {
                    versionToTest = getLowerMinor(lastSuccessfulVersion);
                } else if (!incrementalFailed) {
                    versionToTest = versions.lower(lastSuccessfulVersion);
                }

                assert versionToTest != null;

                return versionToTest.toString();
            }
        };
    }

    public String getLastSuccessful() {
        if (lastSuccessfulVersion != null) {
            return lastSuccessfulVersion.toString();
        }

        return null;
    }

    /**
     * Helper class to facilitate creating {@link DefaultArtifactVersion}'s
     */
    private class Version extends DefaultArtifactVersion {
        public Version(int major, int minor, int incremental) {
            super(String.format(VERSION_FORMAT, major, minor, incremental));
        }
    }

}
