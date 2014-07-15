package org.gitcontrib.dataset;

import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class GitRepoTests {

    public static final String ORG = "dgutierr";
    public static final String MODULE = "Dashbuilder";
    public static final String REPO = "GitContrib";

    GitRepository repo;

    @Before
    public void setUp() throws Exception {
        repo = new GitRepository(ORG, MODULE, REPO, "https://github.com/dgutierr/gitcontrib.git", ".");
        if (!repo.isCloned()) {
            repo.clon();
        } else {
            repo.pull();
        }
    }

    @After
    public void tearDown() throws Exception {
        repo.delete();
    }

    @Test
    public void testReadCommits() throws Exception {
        assertThat(repo.isCloned()).isTrue();
        assertThat(repo.getOrganization()).isEqualTo(ORG);
        assertThat(repo.getModule()).isEqualTo(MODULE);
        assertThat(repo.getName()).isEqualTo(REPO);

        final int[] count = new int[] {0};
        repo.navigate(new GitCommitWalker() {

            public void commitEntry(RevCommit commit) {
                count[0]++;
            }
        });
        assertThat(count[0]).isGreaterThan(0);
    }
}

