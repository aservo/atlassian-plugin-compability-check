package de.aservo;

public enum Product {

    BITBUCKET,
    CROWD,
    CONFLUENCE,
    JIRA;

    @Override
    public String toString() {
        final String name = super.toString();
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }

}
