package de.aservo;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class MavenPomFileUtil {

    public static Model read(
            final File file) throws IOException, XmlPullParserException {

        return new MavenXpp3Reader().read(new FileReader(file));
    }

    public static void write(
            final File pomFile,
            final Model model) throws IOException {

        new MavenXpp3Writer().write(new FileWriter(pomFile), model);
    }

    private MavenPomFileUtil() {}

}
