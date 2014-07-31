Git Contrib
===========

Web application for reporting and monitoring the activity of Git repositories.
 
Steps to build & run
---------------------------
 
Clone the project

    git clone git@github.com:dgutierr/gitcontrib.git
    
Build the project

    cd gitcontrib
    mvn clean install

Run the app

    cd gitcontrib-webapp
    mvn gwt:run

Login

    admin / admin

(Git client, Maven 3.x and Java 1.6+ are required)

Git repositories setup
---------------------------

The current version of Gitcontrib comes with a default dashboard displaying several indicators about a set
or predefines Git repositories. The following two files contain a registry of the repos that fed the dashboard:

* **gitcontrib-webapp/src/main/resources/gitcontrib_repos_local.properties**

Contains a registry of all the local repositories to read logs from

* **gitcontrib-webapp/src/main/resources/gitcontrib_repos_remote.properties**

Contains a registry of all the remote repositories to clone.

The application will look into the local registry first and it will clone it if not found. The app may last a little bit
When started for the very fisrt time because of the remote git clone process. It will last longer as more remote
repositories are defined.

The file **gitcontrib-webapp/src/main/resources/gitcontrib_author_mappings.properties** is used to map GIT users to
real author names. This is useful in case a contributor has more than one alias or to fix typo errors as well.


