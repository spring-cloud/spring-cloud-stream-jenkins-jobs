package org.springframework.jenkins.springcloudstream.ci

import javaposse.jobdsl.dsl.DslFactory
import org.springframework.jenkins.springcloudstream.common.SpringCloudStreamJobs

/**
 * @author Soby Chacko
 */
class SpringCloudStreamPhasedBuildMaker implements SpringCloudStreamJobs {

//    public static final List<String> BINDER_PHASE_JOBS = ['spring-cloud-stream-binder-kafka', 'spring-cloud-stream-binder-rabbit',
//                                            'spring-cloud-stream-binder-aws-kinesis']

    public static final List<String> BINDER_PHASE_JOBS = ['spring-cloud-stream-binder-kafka', 'spring-cloud-stream-binder-rabbit']

    private final DslFactory dsl

    SpringCloudStreamPhasedBuildMaker(DslFactory dsl) {
        this.dsl = dsl
    }

    void build(String coreBranch = 'master', String releaseTrainBranch = 'master',
               String groupName = 'spring-cloud-stream-builds', Map<String, String> binders,
               boolean isRelease, String releaseType,
               String sampleRepoVersion = 'master') {
        def bindersCopy = [:]
        bindersCopy << binders
        buildAllRelatedJobs(coreBranch, bindersCopy, releaseTrainBranch, isRelease, releaseType, sampleRepoVersion)
        dsl.multiJob(groupName) {
            steps {
                phase('spring-cloud-stream-core-phase', 'COMPLETED') {
                    if (!isRelease) {
                        triggers {
                            githubPush()
                        }
                    }

                    scm {
                        git {
                            remote {
                                url "https://github.com/spring-cloud/spring-cloud-stream"
                                branch coreBranch
                            }
                        }
                    }
                    String prefixedProjectName = prefixJob("spring-cloud-stream")
                    phaseJob("${prefixedProjectName}-${coreBranch}-ci".toString()) {
                        currentJobParameters()
                    }
                }
                phase("spring-cloud-stream-binders-phase", 'COMPLETED') {
                    BINDER_PHASE_JOBS.each { String project ->
                        def branch = binders.find { it.key == project }?.value
                        if (branch) {
                            String prefixedProjectName = prefixJob(project)
                            phaseJob("${prefixedProjectName}-${branch}-ci".toString()) {
                                currentJobParameters()
                            }
                        }
                    }
                }
                phase('spring-cloud-stream-starters-phase') {
                    String prefixedProjectName = prefixJob("spring-cloud-stream-starters")
                    phaseJob("${prefixedProjectName}-${releaseTrainBranch}-ci".toString()) {
                        currentJobParameters()
                    }
                }
                if (!isRelease) {
                    phase('spring-cloud-stream-samples-phase') {
                        String prefixedProjectName = prefixJob("spring-cloud-stream-samples")
                        phaseJob("${prefixedProjectName}-${sampleRepoVersion}-ci".toString()) {
                            currentJobParameters()
                        }
                    }
                }
            }
        }
    }

    void buildAllRelatedJobs(String coreBranch, Map<String, String> binders, String releaseTrainBranch,
                             boolean isRelease, String releaseType,
                             String sampleRepoVersion) {

        //release is only supported on the master branch

        if (isRelease) {
            //core build
            new SpringCloudStreamBuildMarker(dsl, "spring-cloud", "spring-cloud-stream", coreBranch, [:])
                    .deploy(true, false, "",
                    null, null, null, false, true, releaseType)
        }
        else {

            //core build
            new SpringCloudStreamBuildMarker(dsl, "spring-cloud", "spring-cloud-stream", coreBranch, [:])
                    .deploy()
        }

        //binder builds
        def kafkaBinderBranch = binders.find { it.key == "spring-cloud-stream-binder-kafka" }?.value
        if (kafkaBinderBranch) {
            if (isRelease) {
                new SpringCloudStreamBuildMarker(dsl, "spring-cloud", "spring-cloud-stream-binder-kafka", kafkaBinderBranch, [KAFKA_TIMEOUT_MULTIPLIER: '60'])
                        .deploy(true, false, "",
                        null, null, null, false, true, releaseType)
            }
            else {

                new SpringCloudStreamBuildMarker(dsl, "spring-cloud", "spring-cloud-stream-binder-kafka", kafkaBinderBranch, [KAFKA_TIMEOUT_MULTIPLIER: '60'])
                        .deploy()
            }

            binders.remove('spring-cloud-stream-binder-kafka')
        }
        def rabbitBinderBranch = binders.find { it.key == "spring-cloud-stream-binder-rabbit" }?.value
        if (rabbitBinderBranch) {
            if (isRelease) {
                new SpringCloudStreamBuildMarker(dsl, "spring-cloud", "spring-cloud-stream-binder-rabbit", rabbitBinderBranch, [:])
                        .deploy(true, false,
                        "clean deploy -U -Pspring", "ci-docker-compose", "docker-compose-RABBITMQ.sh",
                        "docker-compose-RABBITMQ-stop.sh", false, true, releaseType)
            }
            else {
                new SpringCloudStreamBuildMarker(dsl, "spring-cloud", "spring-cloud-stream-binder-rabbit", rabbitBinderBranch, [:])
                        .deploy(true, false,
                        "clean deploy -U -Pspring", "ci-docker-compose", "docker-compose-RABBITMQ.sh",
                        "docker-compose-RABBITMQ-stop.sh")
            }
            binders.remove('spring-cloud-stream-binder-rabbit')
        }
        binders.each { k, v -> new SpringCloudStreamBuildMarker(dsl, "spring-cloud", k, v).deploy() }
        //starter builds
        if (isRelease) {
            new SpringCloudStreamBuildMarker(dsl, "spring-cloud", "spring-cloud-stream-starters", releaseTrainBranch)
                    .deploy(false, true, "clean package -Pspring", null, null, null, true,
            true, releaseType)
        }
        else {
            new SpringCloudStreamBuildMarker(dsl, "spring-cloud", "spring-cloud-stream-starters", releaseTrainBranch)
                    .deploy(false, true, "clean package -Pspring", null, null, null, true)
        }

        if (!isRelease) {
            new SpringCloudStreamBuildMarker(dsl, "spring-cloud", "spring-cloud-stream-samples", sampleRepoVersion)
                    .deploy(false, false, "clean package")
        }
    }
}
