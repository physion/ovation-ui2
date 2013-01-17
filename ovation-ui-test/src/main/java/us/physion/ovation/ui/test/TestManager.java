/*
 * Copyright (c) 2012. Physion Consulting LLC
 * All rights reserved.
 */

package us.physion.ovation.ui.test;

/**
 * TestManager for Ovation unit tests. This class provides setupDatabase (to be called in JUnit TestCase setUp())
 * and teardownDatabase (to be called in TestCase tearDown()).
 *
 * setupDatabase builds an isolated test database which is wiped (all entities deleted) in teardownDatabase.
 */
public class TestManager extends TestManagerBase {

    private String licenseText;
    private String insitution;
    private String group;
    private String connectionFile;
    private String userName;
    private String userPassword;

    /**
     * Constructs a new TestManager. A valid Ovation license must be provided for database creation.
     * @param connectionFile desired test database connection file path
     * @param insitution licensed institution
     * @param group licensed group
     * @param licenseText license text
     * @param userName initial database user name
     * @param userPassword initial database user password
     */
    public TestManager(String connectionFile, String insitution, String group, String licenseText, String userName, String userPassword) {
        this.licenseText = licenseText;
        this.insitution = insitution;
        this.group = group;
        this.connectionFile = connectionFile;
        this.userName = userName;
        this.userPassword = userPassword;
    }


    @Override
    public String getLicenseText() {
        return licenseText;
    }

    @Override
    public String getLicenseInstitution() {
        return insitution;
    }

    @Override
    public String getLicenseGroup() {
        return group;
    }

    @Override
    public String getConnectionFile() {
        return connectionFile;
    }

    @Override
    public String getFirstUserName() {
        return userName;
    }

    @Override
    public String getFirstUserPassword() {
        return userPassword;
    }
}
