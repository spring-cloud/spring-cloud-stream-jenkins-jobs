package org.springframework.jenkins.springcloudstream.ci

import javaposse.jobdsl.dsl.DslFactory
import org.springframework.jenkins.common.job.Cron
import org.springframework.jenkins.common.job.JdkConfig
import org.springframework.jenkins.common.job.Maven
import org.springframework.jenkins.common.job.TestPublisher
import org.springframework.jenkins.springcloudstream.common.SpringCloudStreamJobs

import static org.springframework.jenkins.common.job.Artifactory.artifactoryMaven3Configurator
import static org.springframework.jenkins.common.job.Artifactory.artifactoryMavenBuild

/**
 * @author Soby Chacko
 */
class SpringCloudStreamBuildMarker implements JdkConfig, TestPublisher,
        Cron, SpringCloudStreamJobs, Maven {

    private final DslFactory dsl
    final String organization
    final String project

    String branchToBuild

    Map<String, Object> envVariables = new HashMap<>()

    boolean ghPushTrigger = true

    SpringCloudStreamBuildMarker(DslFactory dsl, String organization, String project, String branchToBuild, Map<String, Object> envVariables) {
        this.dsl = dsl
        this.organization = organization
        this.project = project
        this.branchToBuild = branchToBuild
        this.envVariables = envVariables
    }

    SpringCloudStreamBuildMarker(DslFactory dsl, String organization, String project, String branchToBuild = "main", boolean ghPushTrigger = true) {
        this.dsl = dsl
        this.organization = organization
        this.project = project
        this.branchToBuild = branchToBuild
        this.ghPushTrigger = ghPushTrigger
    }

    void deploy(boolean checkTests = true, boolean recurseSubmodules = false, String ciPlanName = "",
                String scriptDir = null, String startScript = null, String stopScript = null, boolean docsBuild = true, boolean isRelease = false,
                String releaseType = "") {
        String ciPlanNameWithBranch = ciPlanName != "" ? ciPlanName + "-${branchToBuild}-ci" : "${prefixJob(project)}-${branchToBuild}-ci"
        dsl.job(ciPlanNameWithBranch) {

            if (ghPushTrigger && !isRelease) {
                triggers {
                    githubPush()
                }
            }

            if (ciPlanName.equals("spring-cloud-stream-cf-acceptance-tests") ||
                    ciPlanName.equals("spring-cloud-stream-k8s-acceptance-tests")) {
                triggers {
                    cron "0 5 * * *"
                }
            }
            if (branchToBuild.equals("main")) {
                jdk jdk17()
            }
            else {
                jdk jdk8()
            }
            wrappers {
                colorizeOutput()
                maskPasswords()
                environmentVariables(envVariables)
                timeout {
                    noActivity(300)
                    failBuild()
                    writeDescription('Build failed due to timeout after {0} minutes of inactivity')
                }
                if (isRelease && releaseType != null && !releaseType.equals("milestone")) {
                    credentialsBinding {
                        file('FOO_SEC', "spring-signing-secring.gpg")
                        file('FOO_PUB', "spring-signing-pubring.gpg")
                        string('FOO_PASSPHRASE', "spring-gpg-passphrase")
                        usernamePassword('SONATYPE_USER', 'SONATYPE_PASSWORD', "oss-token")
                    }
                }
                if (ciPlanName.equals("spring-cloud-stream-cf-acceptance-tests")) {
                    credentialsBinding {
                        usernamePassword('CF_E2E_TEST_SPRING_CLOUD_STREAM_USER', 'CF_E2E_TEST_SPRING_CLOUD_STREAM_PASSWORD', "lahabra-heights-pcf-admin")
                    }
                }
                if (ciPlanName.equals("spring-cloud-stream-k8s-acceptance-tests")) {
                    maskPasswords()
                    credentialsBinding {
                        usernamePassword('DOCKER_HUB_USERNAME', 'DOCKER_HUB_PASSWORD', "hub.docker.com-springbuildmain")
                        file('GOOGLE_APPLICATION_CREDENTIALS', "scdf-acceptance-tests")
                    }
                }
            }
            scm {
                git {
                    remote {
                        url "https://github.com/${organization}/${project}"
                        branch branchToBuild

                    }
//                    extensions {
//                        submoduleOptions {
//                            if (recurseSubmodules) {
//                                recursive()
//                            }
//                        }
//                    }
                }
            }
            steps {
                if (project.equals("spring-cloud-stream-samples")) {
                    shell(cleanAndPackage())
                    //run e2e tests on main and release trains higher than Elmhurst
                    if (branchToBuild.equals("main") || branchToBuild.charAt(0) > 'E') {
                        //shell(scriptToExecute("samples-e2e-tests", "runSamplesE2ETests.sh"))
                    }
                }
                else if (ciPlanName.equals("spring-cloud-stream-cf-acceptance-tests")) {
                    shell(prepareCFAcceptanceTests())
                }
                else if (ciPlanName.equals("spring-cloud-stream-k8s-acceptance-tests")) {
                    shell(prepareK8SAcceptanceTests())
                }
                else {
                    if (scriptDir != null && startScript != null) {
                        shell(scriptToExecute(scriptDir, startScript))
                    }

                    shell(cleanAndDeploy(docsBuild, isRelease, releaseType))

                    if (scriptDir != null && stopScript != null) {
                        shell(scriptToExecute(scriptDir, stopScript))
                    }
                }
            }
            configure {
//                if (branchToBuild.equals("main") && docsBuild && (project.equals("spring-cloud-stream-binder-kafka") || project.equals("spring-cloud-stream-binder-rabbit")
//                                 || project.equals("spring-cloud-stream")) ) {
//                    artifactoryMavenBuild(it as Node) {
//                        mavenVersion(maven35())
//                        if (project.equals("spring-cloud-stream-binder-kafka")) {
//                          goals('clean install -U -Pdocs -Pspring -pl :spring-cloud-stream-binder-kafka-docs')
//                        }
//                        else if (project.equals("spring-cloud-stream-binder-rabbit")) {
//                          goals('clean install -U -Pdocs -Pspring -pl :spring-cloud-stream-binder-rabbit-docs')
//                        }
//                        else if (project.equals("spring-cloud-stream")) {
//                          goals('clean install -U -Pdocs -Pspring -pl :spring-cloud-stream-docs')
//                        }
//                    }
//                    artifactoryMaven3Configurator(it as Node) {
//                        if (isRelease && releaseType != null && releaseType.equals("milestone")) {
//                            deployReleaseRepository("libs-milestone-local")
//                        } else if (isRelease) {
//                            deployReleaseRepository("libs-release-local")
//                        }
//                    }
//                }
            }
            publishers {
                if (checkTests) {
                    archiveJunit mavenJUnitResults()
                }
                if (project.equals("spring-cloud-stream-binder-aws-kinesis")) {
                    mailer('abilan@vmware.com ozhurakousky@vmware.com chackos@vmware.com ', true, true)
                }
                else {
                    mailer('ozhurakousky@vmware.com chackos@vmware.com ', true, true)
                }
            }
        }
    }
}
