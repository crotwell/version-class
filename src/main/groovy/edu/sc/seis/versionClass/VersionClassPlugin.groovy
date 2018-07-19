package edu.sc.seis.versionClass;

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.plugins.JavaPlugin
import java.util.Date;
import java.text.SimpleDateFormat;

class VersionClassPlugin implements Plugin<Project>  {

        VersionClassPlugin() {
        }

        def String getVersionString(Project project) {
            if (project.version.endsWith("-SNAPSHOT")) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HHmmss")
                return project.version+"_"+sdf.format(new Date())
            }
            return project.version
        }

        def getGenSrc() {
            return 'generated-src/version'
        }

        def getGenSrcDir(Project project) {
            return new File(project.buildDir, getGenSrc())
        }

        def getBuildVersionFilename(Project project) {
            return "java/"+project.group.replace('.','/')+"/"+project.name.replace('-','/')+"/BuildVersion.java"
        }

        def String taskName() {
            return 'makeVersionClass'
        }

        def void apply(Project project) {
            project.getPlugins().apply( JavaPlugin.class )
            def generatedSrcDir = getGenSrcDir(project)
            def buildVersionFilename = getBuildVersionFilename(project)

            def makeVersionClassTask = project.task(taskName()) {
              doLast {
                def df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                df.setTimeZone(TimeZone.getTimeZone("UTC"));
                def now = df.format(new Date());
                def git_revision = ""
                def git_sha = ""
                def git_short_sha = ""
                def git_date = ""
                //def outFilename = "java/"+project.group.replace('.','/')+"/"+project.name.replace('-','/')+"/BuildVersion.java"
                def outFilename = buildVersionFilename
                def outFile = new File(generatedSrcDir, outFilename)
                outFile.getParentFile().mkdirs()


                                try {
                                    def proc = 'git rev-list --count HEAD'.execute()
                                    proc.consumeProcessErrorStream(new StringBuffer())
                                    proc.waitFor()
                                    if( proc.exitValue() != 0 )
                                        throw new IOException();
                                    git_revision = proc.text.trim()
                                } catch (IOException ignore) {}
                                try {
                                    def proc = 'git rev-parse HEAD'.execute()
                                    proc.consumeProcessErrorStream(new StringBuffer())
                                    proc.waitFor()
                                    if( proc.exitValue() != 0 )
                                        throw new IOException()
                                    git_sha = proc.text.trim()
                                } catch (IOException ignore) {}

                                try {
                                    def proc = 'git rev-parse --short HEAD'.execute()
                                    proc.consumeProcessErrorStream(new StringBuffer())
                                    proc.waitFor()
                                    if( proc.exitValue() != 0 )
                                        throw new IOException()
                                    git_short_sha = proc.text.trim()
                                } catch (IOException ignore) {}

                                try {
                                    def proc = 'git show -s --format=%cI HEAD'.execute()
                                    proc.consumeProcessErrorStream(new StringBuffer())
                                    proc.waitFor()
                                    if( proc.exitValue() != 0 )
                                        throw new IOException()
                                    git_date = proc.text.trim()
                                } catch (IOException ignore) {}

                def f = new FileWriter(outFile)
                if (project.group != null && project.group.length() >0) {
                    f.write('package  '+project.group+"."+project.name.replace('-','.')+';\n')
                } else {
                    f.write('package  '+project.name.replace('-','.')+';\n')
                }
                f.write("""
/**
 * Simple class for storing the version derived from the gradle build.gradle file.
 *
 */
public class BuildVersion {

    private static final String version = \""""+getVersionString(project)+"""\";
    private static final String name = \""""+project.name+"""\";
    private static final String group = \""""+project.group+"""\";
    private static final String date = \""""+now+"""\";
    private static final String git_revision = \""""+git_revision+"""\";
    private static final String git_short_sha = \""""+git_short_sha+"""\";
    private static final String git_sha = \""""+git_sha+"""\";
    private static final String git_date = \""""+git_date+"""\";

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
    /** returns the git revision when this file was generated, usually the last git revision when the project was modified. */
    public static String getGitRevision() {
        return git_revision;
    }
    /** returns the short git sha when this file was generated, usually the last git revision when the project was modified. */
    public static String getGitShortSha() {
        return git_short_sha;
    }
    /** returns the git sha when this file was generated, usually the last git revision when the project was modified. */
    public static String getGitSha() {
        return git_sha;
    }
    /** returns the git date when this file was generated, usually the last git revision when the project was modified. */
    public static String getGitDate() {
        return git_date;
    }
    public static String getDetailedVersion() {
        String out = getGroup()+":"+getName()+":"+getVersion()+" "+getDate();
        if (git_revision.length() > 0) {
            out += " ("+getGitRevision()+" "+getGitShortSha()+")";
        }
        return out;
    }
""")

                f.write("}\n")
                f.close()
            }
            project.sourceSets {
                version {
                    java {
                        srcDir project.buildDir.name+'/'+getGenSrc()+'/java'
                    }
                }
            }
          }
            makeVersionClassTask.ext.generatedSrcDir = generatedSrcDir
            makeVersionClassTask.getInputs().files(project.sourceSets.main.getAllSource() )
            makeVersionClassTask.getInputs().property("project version", { project.version })
            makeVersionClassTask.getOutputs().files(new File(generatedSrcDir, buildVersionFilename))
            if (project.getBuildFile() != null && project.getBuildFile().exists()) {
                makeVersionClassTask.getInputs().files(project.getBuildFile())
            }
            addTaskDependency(project)
        }

        def void addTaskDependency(Project project) {
            project.getTasks().getByName('compileJava') {
               dependsOn taskName()
               source += project.fileTree(dir:new File(project.buildDir, getGenSrc()))
            }
        }
}
