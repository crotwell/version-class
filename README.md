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
    private static final String date = "2018-05-21T16:16:10Z";

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
    }
    /** returns the date this file was generated, usually the last date that the project was modified. */
    public static String getDate() {
        return date;
    }
    public static String getDetailedVersion() {
        return getGroup()+":"+getName()+":"+getVersion()+" "+getDate();
    }
}

```
