# version-class
A Gradle [Plugin](https://plugins.gradle.org/plugin/edu.sc.seis.version-class) to create a class containing version info based on the project.

Generates a Java class in `build/generated-src/version/java/<group>/<name>/BuildVersion.java`, adds this directory to the java sourceset
and makes this a dependency of compileJava.



The output class looks like:
```
package  gui.test.testProject;

/**
 * Simple class for storing the version derived from the gradle build.gradle file.
 *
 */
public class BuildVersion {

    private static final String version = "0.1";
    private static final String name = "testProject";
    private static final String group = "gui.test";
    private static final String date = "2023-11-20T17:13:22Z";
    private static final String git_revision = "";
    private static final String git_short_sha = "";
    private static final String git_sha = "";
    private static final String git_date = "";

    /** returns the version of the project from the gradle build.gradle file. */
    public static String getVersion() {
        return version;
    }
    /** returns the name of the project from the gradle build.gradle file. */
    public static String getName() {
        return name;
    }
    /** returns the group of the project from the gradle build.gradle file. */
    public static String getGroup() {
        return group;
    }mple class for storing the version derived from the gradle build.gradle fil    /** returns the date this file was generated, usually the last date that the project was modified. */
    public static String getDate() {
        return date;
    }c class BuildVersion {
    /** returns the git revision when this file was generated, usually the last git revision when the project was modified. */
    public static String getGitRevision() {0.1";
        return git_revision;ing name = "testProject";
    }rivate static final String group = "gui.test";
    /** returns the short git sha when this file was generated, usually the last git revision when the project was modified. */
    public static String getGitShortSha() {n = "";
        return git_short_sha;ng git_short_sha = "";
    }rivate static final String git_sha = "";
    /** returns the git sha when this file was generated, usually the last git revision when the project was modified. */
    public static String getGitSha() {
        return git_sha;sion of the project from the gradle build.gradle file. */
    }ublic static String getVersion() {
    /** returns the git date when this file was generated, usually the last git revision when the project was modified. */
    public static String getGitDate() {
        return git_date; of the project from the gradle build.gradle file. */
    }t/build/generated-src/version/java/gui/test/testProject/BuildVersion.java
    public static String getDetailedVersion() {
        String out = getGroup()+":"+getName()+":"+getVersion()+" "+getDate();
        if (git_revision.length() > 0) {
            out += " ("+getGitRevision()+" "+getGitShortSha()+")";
        }
        return out;
    }

    public static String getVersionAsJSON() {
        String N = "\n";
        String out = "{\n";
        out += "  \"version\": \""+getVersion()+"\","+N;
        out += "  \"name\": \""+getName()+"\","+N;
        out += "  \"group\": \""+getGroup()+"\","+N;
        out += "  \"date\": \""+getDate()+"\","+N;
        out += "  \"git\": {"+N;
        out += "    \"revision\": \""+getGitRevision()+"\","+N;
        out += "    \"shortsha\": \""+getGitShortSha()+"\","+N;
        out += "    \"sha\": \""+getGitSha()+"\","+N;
        out += "    \"date\": \""+getGitDate()+"\""+N;
        out += "  }"+N;
        out += "}";
        return out;
    }
}

```
