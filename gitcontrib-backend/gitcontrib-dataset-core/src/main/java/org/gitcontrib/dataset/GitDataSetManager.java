/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gitcontrib.dataset;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetBuilder;
import org.dashbuilder.dataset.DataSetFactory;
import org.dashbuilder.dataset.DataSetManager;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.uberfire.commons.services.cdi.Startup;

@Startup
@ApplicationScoped
public class GitDataSetManager {

    public static final String GITCONTRIB_ALL = "gitcontrib_all";
    public static final String GITCONTRIB_ORG = "gitcontrib_org_";
    public static final String GITCONTRIB_REPO = "gitcontrib_repo_";
    public static final String GITCONTRIB_MODULE = "gitcontrib_module_";

    /**
     * Map holding the alive repository instances.
     */
    private Map<String,GitRepository> repositoryMap = new HashMap<String,GitRepository>();

    /**
     * Map holding alias to author name mappings.
     */
    private Properties authorMappings = new Properties();

    @Inject
    private Logger log;

    @Inject
    private DataSetManager dataSetManager;

    @PostConstruct
    private void init() throws Exception {
        initAuthorMappings();
        initRepositories("gitcontrib_repos_local.properties", false);
        initRepositories("gitcontrib_repos_remote.properties", true);
        initDataSets();
    }

    private void initAuthorMappings() throws Exception {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("gitcontrib_author_mappings.properties");
        if (is != null) {
            authorMappings.load(is);
        }
    }

    private void initRepositories(String fileName, boolean remote) throws Exception {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
        if (is != null) {
            Properties props = new Properties();
            props.load(is);

            String relativePath = props.getProperty("relativePath");
            String clonePath = props.getProperty("clonePath");
            if (clonePath == null) clonePath = "./repositories";

            for (String key : props.stringPropertyNames()) {
                if (key.equals("relativePath") || key.equals("clonePath")) continue;

                String value = props.getProperty(key);
                if (getRepository(key) != null) {
                    log.info("Repository already added. Skipping: " + key);
                } else {
                    try {
                        String[] repoFields = StringUtils.split(value, ";");
                        String repoRef = repoFields[2].trim();
                        if (remote) addRepository(repoFields[0].trim(), repoFields[1].trim(), key, repoRef, clonePath);
                        else addRepository(repoFields[0].trim(), repoFields[1].trim(), key, new File(relativePath, repoRef));
                    } catch (Exception e) {
                        log.error("Git repository initialization error: " + key);
                        log.error(e.getMessage());
                    }
                }
            }
        }
    }

    private void initDataSets() throws Exception {
        final DataSetBuilder dsBuilder = DataSetFactory.newDataSetBuilder()
                .label("organization")
                .label("module")
                .label("repository")
                .label("author")
                .label("message")
                .date("date");

        for (final String name : getRepositoryNames()) {
            GitRepository repo = getRepository(name);
            final String org = repo.getOrganization();
            final String module = repo.getModule();

            repo.navigate(new GitCommitWalker() {

                public void commitEntry(RevCommit commit) {
                    String alias = commit.getAuthorIdent().getName();
                    String author = authorMappings.getProperty(alias);
                    if (author == null) author = alias;
                    String msg = commit.getShortMessage();
                    Date date = commit.getAuthorIdent().getWhen();
                    dsBuilder.row(org, module, name, author, msg, date);
                }
            });
        }

        DataSet dataSet = dsBuilder.buildDataSet();
        dataSet.setUUID(GITCONTRIB_ALL);
        dataSetManager.registerDataSet(dataSet);
    }

    public GitRepository addRepository(String organization, String module, String name, String url, String clonePath) throws Exception {
        log.info("Adding remote repository: " + url);
        GitRepository repo = new GitRepository(organization, module, name, url, clonePath);
        if (!repo.isCloned()) {
            repo.clon();
        } else {
            repo.pull();
        }
        repositoryMap.put(name, repo);
        return repo;
    }

    public GitRepository addRepository(String organization, String module, String name, File directory) throws Exception {
        log.info("Adding local repository: " + directory.getAbsolutePath());
        GitRepository repo = new GitRepository(organization, module, name, directory);
        if (!repo.isCloned()) {
            log.error("Repository does not exists: " + directory.getAbsolutePath());
        } else {
            repositoryMap.put(name, repo);
        }
        return repo;
    }

    public Set<String> getRepositoryNames() {
        return repositoryMap.keySet();
    }

    public GitRepository getRepository(String name) {
        return repositoryMap.get(name);
    }
}
