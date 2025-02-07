/*
 * Copyright (c) 2009--2016 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.frontend.xmlrpc.packages;

import com.redhat.rhn.FaultException;
import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.Row;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.rhnpackage.PackageSource;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.PackageFileDto;
import com.redhat.rhn.frontend.dto.PackageOverview;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.InvalidPackageArchException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchPackageException;
import com.redhat.rhn.frontend.xmlrpc.PackageDownloadException;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;
import com.redhat.rhn.frontend.xmlrpc.RhnXmlRpcServer;
import com.redhat.rhn.manager.download.DownloadManager;
import com.redhat.rhn.manager.rhnpackage.PackageManager;

import com.suse.manager.api.ReadOnly;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PackagesHandler
 * @apidoc.namespace packages
 * @apidoc.doc Methods to retrieve information about the Packages contained
 * within this server.
 */
public class PackagesHandler extends BaseHandler {

    private static Logger logger = LogManager.getLogger(PackagesHandler.class);
    private static float freeMemCoeff = 0.9f;

    /**
     * Get Details - Retrieves the details for a given package
     * @param loggedInUser The current user
     * @param pid The id of the package you're looking for
     * @return Returns a Map containing the details of the package
     * @throws FaultException A FaultException is thrown if the errata corresponding to
     * pid cannot be found.
     *
     * @apidoc.doc Retrieve details for the package with the ID.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "pid")
     * @apidoc.returntype
     *   #struct_begin("package")
     *       #prop("int", "id")
     *       #prop("string", "name")
     *       #prop("string", "epoch")
     *       #prop("string", "version")
     *       #prop("string", "release")
     *       #prop("string", "arch_label")
     *       #prop_array("providing_channels", "string",
     *          "Channel label providing this package.")
     *       #prop("string", "build_host")
     *       #prop("string", "description")
     *       #prop("string", "checksum")
     *       #prop("string", "checksum_type")
     *       #prop("string", "vendor")
     *       #prop("string", "summary")
     *       #prop("string", "cookie")
     *       #prop("string", "license")
     *       #prop("string", "file")
     *       #prop("string", "build_date")
     *       #prop("string", "last_modified_date")
     *       #prop("string", "size")
     *       #prop_desc("string", "path", "The path on the #product() server's file system that
     *              the package resides.")
     *       #prop("string", "payload_size")
     *    #struct_end()
     */
    @ReadOnly
    public Map<String, Object> getDetails(User loggedInUser, Integer pid) throws FaultException {
        // Get the logged in user
        Package pkg = lookupPackage(loggedInUser, pid);

        return PackageHelper.packageToMap(pkg, loggedInUser);
    }

    /**
     * List of Channels that provide a given package
     * @param loggedInUser The current user
     * @param pid The id of the package in question
     * @return Returns an array of maps representing a channel
     * @throws FaultException A FaultException is thrown if the errata corresponding to
     * pid cannot be found.
     *
     * @apidoc.doc List the channels that provide the a package.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "pid")
     * @apidoc.returntype
     * #return_array_begin()
     *   #struct_begin("channel")
     *     #prop("string", "label")
     *     #prop("string", "parent_label")
     *     #prop("string", "name")
     *   #struct_end()
     * #array_end()
     */
    @ReadOnly
    public Object[] listProvidingChannels(User loggedInUser, Integer pid)
            throws FaultException {
        //Get the logged in user
        Package pkg = lookupPackage(loggedInUser, pid);

        DataResult dr = PackageManager.orgPackageChannels(loggedInUser.getOrg().getId(),
                                                         pkg.getId());
        return dr.toArray();
    }

    /**
     * List of Errata that provide the given package.
     * @param loggedInUser The current user
     * @param pid The id of the package in question
     * @return Returns an array of maps representing an erratum
     * @throws FaultException A FaultException is thrown if the errata corresponding to
     * pid cannot be found.
     *
     * @apidoc.doc List the errata providing the a package.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "pid")
     * @apidoc.returntype
     * #return_array_begin()
     *   #struct_begin("errata")
     *     #prop("string", "advisory")
     *     #prop("string", "issue_date")
     *     #prop("string", "last_modified_date")
     *     #prop("string", "update_date")
     *     #prop("string", "synopsis")
     *     #prop("string", "type")
     *   #struct_end()
     * #array_end()
     */
    @ReadOnly
    public Object[] listProvidingErrata(User loggedInUser, Integer pid)
            throws FaultException {
        // Get the logged in user
        Package pkg = lookupPackage(loggedInUser, pid);

        DataResult dr = PackageManager.providingErrata(loggedInUser.getOrg().getId(),
                                                       pkg.getId());

        return dr.toArray();
    }

    /**
     * Get a list of files associated with a package
     * @param loggedInUser The current user
     * @param pid The id of the package you're looking for
     * @return Returns an Array of maps representing a file
     * @throws FaultException A FaultException is thrown if the errata corresponding to
     * pid cannot be found.
     *
     * @apidoc.doc List the files associated with a package.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "pid")
     * @apidoc.returntype
     *   #return_array_begin()
     *     #struct_begin("file info")
     *       #prop("string", "path")
     *       #prop("string", "type")
     *       #prop("string", "last_modified_date")
     *       #prop("string", "checksum")
     *       #prop("string", "checksum_type")
     *       #prop("int", "size")
     *       #prop("string", "linkto")
     *     #struct_end()
     *   #array_end()
     */
    @ReadOnly
    public Object[] listFiles(User loggedInUser, Integer pid) throws FaultException {
        // Get the logged in user
        Package pkg = lookupPackage(loggedInUser, pid);

        List<PackageFileDto> dr = PackageManager.packageFiles(pkg.getId());

        List<Map<String, Object>> returnList = new ArrayList<>();

        /*
         * Loop through the data result and merge the data into the correct format
         */
        for (PackageFileDto file : dr) {
            Map<String, Object> row = new HashMap<>();

            // Default items (mtime and file_size cannot be null)
            row.put("path", StringUtils.defaultString(file.getName()));
            row.put("last_modified_date", file.getMtime());
            row.put("size", file.getFileSize());
            row.put("linkto", StringUtils.defaultString(file.getLinkto()));
            row.put("checksum", StringUtils.defaultString(file.getChecksum()));
            row.put("checksum_type", StringUtils.defaultString(file.getChecksumtype()));

            // Determine the file_type
            if (file.getChecksum() != null) {
                row.put("type", "file");
            }
            else {
                if (file.getLinkto() != null) {
                    row.put("type", "symlink");
                }
                else {
                    row.put("type", "directory");
                }
            }

            returnList.add(row);
        }

        return returnList.toArray();
    }

    /**
     * Gets the change log for a given package
     * @param loggedInUser The current user
     * @param pid The id of the package you're looking for
     * @return Returns a string with the changelog
     * @throws FaultException A FaultException is thrown if the errata corresponding to
     * pid cannot be found.
     *
     * @apidoc.doc List the change log for a package.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "pid")
     * @apidoc.returntype
     *   string
     */
    @ReadOnly
    public String listChangelog(User loggedInUser, Integer pid) throws FaultException {
        // Get the logged in user
        Package pkg = lookupPackage(loggedInUser, pid);

        return PackageManager.getPackageChangeLog(pkg);
    }

    /**
     * List dependencies for a given package.
     * @param loggedInUser The current user
     * @param pid The package id for the package in question
     * @return Returns an array of maps representing a dependency
     * @throws FaultException A FaultException is thrown if the errata corresponding to
     * pid cannot be found.
     *
     * @apidoc.doc List the dependencies for a package.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "pid")
     * @apidoc.returntype
     *   #return_array_begin()
     *     #struct_begin("dependency")
     *       #prop("string", "dependency")
     *       #prop_desc("string", "dependency_type", "One of the following:")
     *         #options()
     *           #item("requires")
     *           #item("conflicts")
     *           #item("obsoletes")
     *           #item("provides")
     *           #item("recommends")
     *           #item("suggests")
     *           #item("supplements")
     *           #item("enhances")
     *           #item("predepends")
     *           #item("breaks")
     *         #options_end()
     *       #prop("string", "dependency_modifier")
     *     #struct_end()
     *   #array_end()
     */
    @ReadOnly
    public Object[] listDependencies(User loggedInUser, Integer pid) throws FaultException {
        // Get the logged in user
        Package pkg = lookupPackage(loggedInUser, pid);

        // The list we'll eventually return
        List<Map<String, String>> returnList = new ArrayList<>();

        /*
         * Loop through each of the types of dependencies and create a map representing the
         * dependency to add to the returnList
         */
        for (int i = 0; i < PackageManager.DEPENDENCY_TYPES.length; i++) {
            String depType = PackageManager.DEPENDENCY_TYPES[i];
            DataResult<Row> dr = getDependencies(depType, pkg);

            // In the off chance we get null back, we should skip the next loop
            if (dr == null || dr.isEmpty()) {
                continue;
            }

            /*
             * Loop through each item in the dependencies data result, adding each row
             * to the returnList
             */
            for (Row map : dr) {
                Map<String, String> row = new HashMap<>(); // The map we'll put into returnList

                String name = (String) map.get("name");
                String version = (String) map.get("version");
                Long sense = (Long) map.get("sense");

                row.put("dependency", StringUtils.defaultString(name));
                row.put("dependency_type", depType);

                // If the version for this dep isn't null, we need to calculate the modifier
                String depmod = " ";
                if (version != null) {
                    depmod = StringUtils.defaultString(
                            PackageManager.getDependencyModifier(sense, version));
                }
                row.put("dependency_modifier", depmod);
                returnList.add(row);
            }
        }

        return returnList.toArray();
    }

    /**
     * Removes a package from the system based on package id
     * @param loggedInUser The current user
     * @param pid package id
     * @throws FaultException something bad happens
     * @return 1 on success.
     *
     * @apidoc.doc Remove a package from #product().
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "pid")
     * @apidoc.returntype #return_int_success()
     */
    public int removePackage(User loggedInUser, Integer pid) throws FaultException {
        if (!loggedInUser.hasRole(RoleFactory.ORG_ADMIN)) {
            throw new PermissionCheckFailureException();
        }
        Package pkg = lookupPackage(loggedInUser, pid);
        if (pkg == null) {
            throw new NoSuchPackageException();
        }
        try {
            PackageManager.schedulePackageRemoval(loggedInUser, pkg);
        }
        catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

        return 1;
    }

    /**
     * Removes a source package based on source package id
     * @param loggedInUser The current user
     * @param psid package source id
     * @throws FaultException something bad happens
     * @return 1 on success.
     *
     * @apidoc.doc Remove a source package.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "psid", "package source ID")
     * @apidoc.returntype #return_int_success()
     */
    public int removeSourcePackage(User loggedInUser, Integer psid) throws FaultException {
        if (!loggedInUser.hasRole(RoleFactory.ORG_ADMIN)) {
            throw new PermissionCheckFailureException();
        }
        PackageSource pkg = PackageFactory.lookupPackageSourceByIdAndOrg(
                psid.longValue(), loggedInUser.getOrg());
        if (pkg == null) {
            throw new NoSuchPackageException();
        }
        try {
            PackageManager.schedulePackageSourceRemoval(loggedInUser, pkg);
        }
        catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

        return 1;
    }

    /**
     * List all source packages
     * @param loggedInUser The current user
     * @return Returns an array of source packages
     * @throws FaultException A FaultException is thrown
     * when user does not have permissions.
     * @apidoc.doc List all source packages in user's organization.
     * @apidoc.param #session_key()
     * @apidoc.returntype
     * #return_array_begin()
     *   #struct_begin("source_package")
     *     #prop("int", "id")
     *     #prop("string", "name")
     *   #struct_end()
     * #array_end()
     */
    @ReadOnly
    public Object[] listSourcePackages(User loggedInUser) throws FaultException {

        DataResult<PackageOverview> dr =
                PackageManager.listCustomPackages(loggedInUser.getOrg().getId(), true);
        return dr.toArray();
    }

    /**
     * Private helper method to get a DataResult object for a given dependency type
     * @param type The type in question
     * @param pkg The package in question
     * @return Returns the data result containing the dependencies of a specific type for a
     * given package.
     */
    private DataResult<Row> getDependencies(String type, Package pkg) {
        if (type.equals("requires")) {
            return PackageManager.packageRequires(pkg.getId());
        }
        else if (type.equals("provides")) {
            return PackageManager.packageProvides(pkg.getId());
        }
        else if (type.equals("obsoletes")) {
            return PackageManager.packageObsoletes(pkg.getId());
        }
        else if (type.equals("conflicts")) {
            return PackageManager.packageConflicts(pkg.getId());
        }
        else if (type.equals("recommends")) {
            return PackageManager.packageRecommends(pkg.getId());
        }
        else if (type.equals("suggests")) {
            return PackageManager.packageSuggests(pkg.getId());
        }
        else if (type.equals("supplements")) {
            return PackageManager.packageSupplements(pkg.getId());
        }
        else if (type.equals("enhances")) {
            return PackageManager.packageEnhances(pkg.getId());
        }
        else if ("predepends".equals(type)) {
            return PackageManager.packagePreDepends(pkg.getId());
        }
        else if ("breaks".equals(type)) {
            return PackageManager.packageBreaks(pkg.getId());
        }
        return null;
    }

    /**
     * @param user The logged in user
     * @param pid The id for the package
     * @return Returns the package or a fault exception
     * @throws FaultException Occurs when the package is not found
     */
    private Package lookupPackage(User user, Integer pid) throws FaultException {
        Package pkg = PackageManager.lookupByIdAndUser(pid.longValue(), user);

        /*
         * PackageManager.lookupByIdAndUser() could return null, so we need to check
         * and throw a no_such_package exception if the package was not found.
         */
        if (pkg == null) {
            throw new FaultException(-208, "no_such_package",
                    "The package '" + pid + "' cannot be found.");
        }

        return pkg;
    }


    /**
     * Lookup the details for packages with the given name, version,
     * release, architecture label, and (optionally) epoch.
     * @param loggedInUser The current user
     * @param name - name of the package to search for
     * @param version - version of the package to search for
     * @param release release of the package to search for
     * @param epoch if set to something other than an empty string (""), strict
     *          matching will be used and the epoch string must be correct.  If set
     *          to an empty string, if the epoch is null or there is only one NEVRA
     *          combination, it will be returned.  (Empty string is recommended.)
     * @param archLabel the arch to search for
     * @return the Package object requested
     *
     * @apidoc.doc Lookup the details for packages with the given name, version,
     *          release, architecture label, and (optionally) epoch.
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "name")
     * @apidoc.param #param("string", "version")
     * @apidoc.param #param("string", "release")
     * @apidoc.param #param_desc("string", "epoch",
     *          "If set to something other than empty string,
     *          strict matching will be used and the epoch string must be correct.
     *          If set to an empty string, if the epoch is null or there is only one
     *          NVRA combination, it will be returned.  (Empty string is recommended.)")
     * @apidoc.param #param("string", "archLabel")
     * @apidoc.returntype
     *   #return_array_begin()
     *     $PackageSerializer
     *   #array_end()
     */
    @ReadOnly
    public List<Package> findByNvrea(User loggedInUser, String name, String version,
            String release, String epoch, String archLabel) {
        PackageArch arch = PackageFactory.lookupPackageArchByLabel(archLabel);

        if (arch == null) {
             throw new InvalidPackageArchException(archLabel);
        }
        if (epoch.equals("")) {
            epoch = null;
        }
        return PackageFactory.lookupByNevra(loggedInUser.getOrg(), name, version, release, epoch, arch);
    }

    /**
     * get a package's download url
     * @param loggedInUser The current user
     * @param pid the package id
     * @return the url
     *
     * @apidoc.doc Retrieve the url that can be used to download a package.
     *      This will expire after a certain time period.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "pid")
     * @apidoc.returntype
     *  string - the download url
     *
     */
    @ReadOnly
    public String getPackageUrl(User loggedInUser, Integer pid) {
        Package pkg = lookupPackage(loggedInUser, pid);
        return RhnXmlRpcServer.getProtocol() + "://" +
            RhnXmlRpcServer.getServerName() +
            DownloadManager.getPackageDownloadPath(pkg, loggedInUser);
    }


    /**
     * download a binary package
     * @param loggedInUser The current user
     * @param pid the package id
     * @return  a byte array of the package
     * @throws IOException if there is an exception

     * @apidoc.doc Retrieve the package file associated with a package.
     * (Consider using <a href ="#getPackageUrl">packages.getPackageUrl</a>
     * for larger files.)
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "pid")
     * @apidoc.returntype #array_single("byte", "binary object - package file")
     */
    @ReadOnly
    public byte[] getPackage(User loggedInUser, Integer pid) throws IOException {
        Package pkg = lookupPackage(loggedInUser, pid);
        String path = Config.get().getString(ConfigDefaults.MOUNT_POINT) + "/" +
            pkg.getPath();
        File file = new File(path);

        if (file.length() > freeMemCoeff * Runtime.getRuntime().freeMemory()) {
            throw new PackageDownloadException("api.package.download.toolarge");
        }

        byte[] toReturn = new byte[(int) file.length()];
        BufferedInputStream br = new BufferedInputStream(new FileInputStream(file));
        if (br.read(toReturn) != file.length()) {
            throw new PackageDownloadException("api.package.download.ioerror");
        }
        return toReturn;
    }

}
