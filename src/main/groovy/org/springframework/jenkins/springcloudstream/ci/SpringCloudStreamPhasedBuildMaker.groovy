package org.springframework.jenkins.springcloudstream.ci

import javaposse.jobdsl.dsl.DslFactory
import org.springframework.jenkins.springcloudstream.common.SpringCloudStreamJobs

/**
 * @author Soby Chacko
 */
class SpringCloudStreamPhasedBuildMaker implements SpringCloudStreamJobs {

    private final DslFactory dsl

    SpringCloudStreamPhasedBuildMaker(DslFactory dsl) {
        this.dsl = dsl
    }

    void build(String coreBranch = 'main', String releaseTrainBranch = 'main',
               String groupName = 'spring-cloud-stream-builds', Map<String, String> binders,
               boolean isRelease, String releaseType,
               String sampleRepoVersion = 'main') {
        def bindersCopy = [:]
        bindersCopy << binders
        buildAllRelatedJobs(coreBranch, bindersCopy, releaseTrainBranch, isRelease, releaseType, sampleRepoVersion)
        dsl.multiJob(groupName) {
            steps {
                phase('spring-cloud-stream-phase', 'COMPLETED') {
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
                    binders.keySet().each { String project ->
                        def branch = binders.find { it.key == project }?.value
                        if (branch) {
                            String prefixedProjectName = prefixJob(project)
                            phaseJob("${prefixedProjectName}-${branch}-ci".toString()) {
                                currentJobParameters()
                            }
                        }
                    }
                }
                if (coreBranch.equals("3.1.x")) {
                    phase('spring-cloud-stream-starters-phase') {
                        String prefixedProjectName = prefixJob("spring-cloud-stream-starters")
                        phaseJob("${prefixedProjectName}-${releaseTrainBranch}-ci".toString()) {
                            currentJobParameters()
                        }
                    }
                }
                if (!isRelease) {
                    //samples are enabled for Ditmars and above
                    if (sampleRepoVersion.equals("main") || sampleRepoVersion.equals("3.2.x")) {
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
    }

    void buildAllRelatedJobs(String coreBranch, Map<String, String> binders, String releaseTrainBranch,
                             boolean isRelease, String releaseType,
                             String sampleRepoVersion) {

        //release is only supported on the main branch

        if (isRelease) {
            //core build
            new SpringCloudStreamBuildMarker(dsl, "spring-cloud", "spring-cloud-stream", coreBranch, [:])
                    .deploy(true, false, "",
                    null, null, null, false, true, releaseType)
        }
        else {

            //core build
            if (coreBranch.equals("main") || coreBranch.equals("3.2.x")) {
                new SpringCloudStreamBuildMarker(dsl, "spring-cloud", "spring-cloud-stream", coreBranch, [:])
                        .deploy(true, false, "",
                                "binders/rabbit-binder/ci-docker-compose", "docker-compose-RABBITMQ.sh",
                                "docker-compose-RABBITMQ-stop.sh")
            }
            else {
                new SpringCloudStreamBuildMarker(dsl, "spring-cloud", "spring-cloud-stream", coreBranch, [:])
                        .deploy()
            }
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
                        .deploy(true, false, "",
                        "ci-docker-compose", "docker-compose-RABBITMQ.sh",
                        "docker-compose-RABBITMQ-stop.sh", false, true, releaseType)
            }
            else {
                new SpringCloudStreamBuildMarker(dsl, "spring-cloud", "spring-cloud-stream-binder-rabbit", rabbitBinderBranch, [:])
                        .deploy(true, false, "",
                        "ci-docker-compose", "docker-compose-RABBITMQ.sh",
                        "docker-compose-RABBITMQ-stop.sh")
            }
            binders.remove('spring-cloud-stream-binder-rabbit')
        }
        def kinesisBinderBranch = binders.find { it.key == "spring-cloud-stream-binder-aws-kinesis" }?.value
        if (kinesisBinderBranch) {
            if (isRelease) {
                new SpringCloudStreamBuildMarker(dsl, "spring-cloud", "spring-cloud-stream-binder-aws-kinesis", kinesisBinderBranch)
                        .deploy(true, false, "",
                        null, null, null, false, true, releaseType)
            }
            else {
                new SpringCloudStreamBuildMarker(dsl, "spring-cloud", "spring-cloud-stream-binder-aws-kinesis", kinesisBinderBranch)
                        .deploy()
            }

            binders.remove('spring-cloud-stream-binder-aws-kinesis')
        }
        binders.each { k, v -> new SpringCloudStreamBuildMarker(dsl, "spring-cloud", k, v).deploy() }
        //starter builds
        if (coreBranch.equals("3.1.x")) {
            if (isRelease) {
                new SpringCloudStreamBuildMarker(dsl, "spring-cloud", "spring-cloud-stream-starters", releaseTrainBranch)
                        .deploy(false, false, "", null, null, null, true,
                true, releaseType)
            }
            else {
                new SpringCloudStreamBuildMarker(dsl, "spring-cloud", "spring-cloud-stream-starters", releaseTrainBranch)
                        .deploy(false, false, "", null, null, null, true)
            }
        }

        if (!isRelease) {
            new SpringCloudStreamBuildMarker(dsl, "spring-cloud", "spring-cloud-stream-samples", sampleRepoVersion)
                    .deploy(false, false, "")
        }
    }
}
