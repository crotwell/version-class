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

    def String runGitCommand(projectDir, cmd) {
        def serr = new StringBuilder()
        try {
            def proc = cmd.execute([], projectDir)
            proc.consumeProcessErrorStream(serr)
            proc.waitFor()
            if( proc.exitValue() != 0 )
                throw new IOException();
            return proc.text.trim()
        } catch (IOException ignore) {
            throw new RuntimeException("trouble with "+cmd+": "+serr, ignore)
        }
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
            def git_last_tag = ""
            def git_last_tag_date = ""
            def git_commits_since_last_tag = "-1"
            //def outFilename = "java/"+project.group.replace('.','/')+"/"+project.name.replace('-','/')+"/BuildVersion.java"
            def outFilename = getBuildVersionFilename(project)
            def outFile = new File(generatedSrcDir, outFilename)
            outFile.getParentFile().mkdirs()

            if (new File(project.projectDir, '.git').exists()) {
                def cmd
                cmd = 'git rev-list --count HEAD'
                git_revision = runGitCommand(project.projectDir, cmd)
                cmd = 'git rev-parse HEAD'
                git_sha = runGitCommand(project.projectDir, cmd)
                cmd = 'git rev-parse --short HEAD'
                git_short_sha = runGitCommand(project.projectDir, cmd)
                cmd = 'git show -s --format=%cI HEAD'
                git_date = runGitCommand(project.projectDir, cmd)
                cmd = 'git describe --tags --abbrev=0 --always'
                git_last_tag = runGitCommand(project.projectDir, cmd)
                cmd = "git show -s --format=%cI $git_last_tag --"
                git_last_tag_date = runGitCommand(project.projectDir, cmd)
                cmd = "git rev-list $git_last_tag..HEAD --count"
                git_commits_since_last_tag = runGitCommand(project.projectDir, cmd)
            }

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
    private static final String git_last_tag = \""""+git_last_tag+"""\";
    private static final String git_last_tag_date = \""""+git_last_tag_date+"""\";
    private static final int git_commits_since_last_tag = """+git_commits_since_last_tag+""";

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
    
    /** returns the closet parent git tag */
    public static String getGitLastTag() {
        return git_last_tag;
    }
    
    /** returns date of commit of last git tag */
    public static String getGitLastTagDate() {
        return git_last_tag_date;
    }
    
    /** returns number of commits since last git tag */
    public static int getGitCommitsSinceLastTag() {
        return git_commits_since_last_tag;
    }
    
    public static String getDetailedVersion() {
        String out = getGroup()+":"+getName()+":"+getVersion()+" "+getDate();
        if (git_revision.length() > 0) {
            out += " ("+getGitRevision()+" "+getGitShortSha()+")";
        }
        return out;
    }

    public static String getVersionAsJSON() {
        String N = "\\n";
        String out = "{\\n";
        out += "  \\"version\\": \\""+getVersion()+"\\","+N;
        out += "  \\"name\\": \\""+getName()+"\\","+N;
        out += "  \\"group\\": \\""+getGroup()+"\\","+N;
        out += "  \\"date\\": \\""+getDate()+"\\","+N;
        out += "  \\"git\\": {"+N;
        out += "    \\"revision\\": \\""+getGitRevision()+"\\","+N;
        out += "    \\"shortsha\\": \\""+getGitShortSha()+"\\","+N;
        out += "    \\"sha\\": \\""+getGitSha()+"\\","+N;
        out += "    \\"date\\": \\""+getGitDate()+"\\","+N;
        out += "    \\"lastTag\\": \\""+getGitLastTag()+"\\","+N;
        out += "    \\"lastTagDate\\": \\""+getGitLastTagDate()+"\\","+N;
        out += "    \\"commitsSinceLastTag\\": "+getGitCommitsSinceLastTag()+N;
        out += "  }"+N;
        out += "}";
        return out;
    }
""")

                f.write("}\n")
                f.close()
            }
            project.sourceSets {
                main {
                  java {
                    srcDir(getGenSrcDir(project))
                    }
                }
            }
          }
            makeVersionClassTask.ext.generatedSrcDir = generatedSrcDir
            makeVersionClassTask.getInputs().property("project version", { project.version })
            project.afterEvaluate {
              makeVersionClassTask.getOutputs().files(new File(generatedSrcDir, getBuildVersionFilename(project)))
            }
            if (project.getBuildFile() != null && project.getBuildFile().exists()) {
                makeVersionClassTask.getInputs().files(project.getBuildFile())
            }
            addTaskDependency(project)
        }

        def void addTaskDependency(Project project) {
            project.getTasks().getByName('compileJava') {
               dependsOn taskName()
            }
        }
}
