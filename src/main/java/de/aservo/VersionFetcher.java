package de.aservo;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.aservo.Product.*;

public class VersionFetcher {

    public static final String VERSION_EXPRESSION = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";

    public static final Map<Product, String> URLS;

    static {
        URLS = new HashMap<>();
        URLS.put(BITBUCKET, "https://packages.atlassian.com/mvn/maven-external/com/atlassian//bitbucket/server/bitbucket-parent/maven-metadata.xml");
        URLS.put(CROWD, "https://packages.atlassian.com/maven-external/com/atlassian/crowd/atlassian-crowd/maven-metadata.xml");
        URLS.put(JIRA, "https://packages.atlassian.com/mvn/maven-external/com/atlassian/jira/jira-api/maven-metadata.xml");
    }

    private final Product product;
    private final DefaultArtifactVersion maxVersion;

    public VersionFetcher(
            final Product product,
            final String productVersion) {

        this.product = product;
        this.maxVersion = new DefaultArtifactVersion(productVersion);
    }

    public List<String> getVersions() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        final Document document = documentBuilder.parse(new URL(URLS.get(product)).openStream());

        final String xPathExpression = "/metadata/versioning/versions/version";
        final XPath xPath = XPathFactory.newInstance().newXPath();
        final NodeList nodeList = (NodeList) xPath.compile(xPathExpression).evaluate(document, XPathConstants.NODESET);

        final List<String> versions = new ArrayList<>();

        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node node = nodeList.item(i);
            final String textContent = node.getTextContent();

            if (!textContent.matches(VERSION_EXPRESSION)) {
                continue;
            }

            final DefaultArtifactVersion version = new DefaultArtifactVersion(textContent);

            if (version.compareTo(maxVersion) >= 0) {
                continue;
            }

            versions.add(version.toString());
        }

        versions.sort(VERSION_COMPARATOR);
        return versions;
    }

    private static final Comparator<String> VERSION_COMPARATOR = new Comparator<String>() {
        @Override
        public int compare(final String v1, final String v2) {
            final DefaultArtifactVersion version1 = new DefaultArtifactVersion(v1);
            final DefaultArtifactVersion version2 = new DefaultArtifactVersion(v2);

            // the comparator sorts in reverse order
            return version2.compareTo(version1);
        }
    };

}
