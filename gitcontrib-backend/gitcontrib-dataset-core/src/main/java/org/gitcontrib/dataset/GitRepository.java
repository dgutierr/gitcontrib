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
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitRepository {

    protected String name;
    protected String url;
    protected String module;
    protected String organization;
    protected File directory;
    protected Date lastUpdateDate;

    private Logger log = LoggerFactory.getLogger(this.getClass().getName());

    public GitRepository(String organization, String module, String name, String url, String clonePath) {
        this.organization = organization;
        this.module = module;
        this.name = name;
        this.url = url;
        this.directory = new File(clonePath, organization + File.separator + name);
    }

    public GitRepository(String organization, String module, String name, File directory) {
        this.organization = organization;
        this.module = module;
        this.name = name;
        this.directory = directory;
    }

    public String getOrganization() {
        return organization;
    }

    public String getModule() {
        return module;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public File getDirectory() {
        return directory;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public boolean isCloned() {
        return directory.exists();
    }

    public void clon() throws Exception {
        log.info("Cloning repository: " + url);

        // prepare a new folder for the cloned repository
        log.info("Cloning into: " + directory.getAbsolutePath());
        directory.delete();

        // then clone
        Git.cloneRepository()
                .setURI(url)
                .setDirectory(directory)
                .call();

        log.info("Cloned successfully: " + url);
        lastUpdateDate = new Date();
    }

    public void delete() throws Exception {
        if (directory != null) {
            FileUtils.deleteDirectory(directory);
            File parent = directory.getParentFile();
            if (parent.listFiles().length == 0) FileUtils.deleteDirectory(parent);
            parent = parent.getParentFile();
            if (parent.listFiles().length == 0) FileUtils.deleteDirectory(parent);
        }
    }

    public void pull() throws Exception {
        log.info("Pulling changes into: " + directory.getAbsolutePath());

        Repository repository = open();
        new Git(repository).pull().call();
        repository.close();
        lastUpdateDate = new Date();
    }

    public void navigate(GitCommitWalker logWalker) throws Exception {
        Repository repository = open();
        try {
            Git git = new Git(repository);
            Iterable<RevCommit> commits = git.log().call();
            for (RevCommit commit : commits) {
                logWalker.commitEntry(commit);
            }
        } finally {
            repository.close();
        }
    }

    protected Repository open() throws Exception {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = builder.setGitDir(new File(directory, ".git"))
                .build();

        return repository;
    }
}
