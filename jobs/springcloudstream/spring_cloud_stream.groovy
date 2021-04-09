package springcloudstream

import org.springframework.jenkins.springcloudstream.ci.SpringCloudStreamBuildMarker
import org.springframework.jenkins.springcloudstream.ci.SpringCloudStreamPhasedBuildMaker
import javaposse.jobdsl.dsl.DslFactory

DslFactory dsl = this

//doMasterGAReleaseBuild(dsl)
//doMasterMilestoneReleaseBuild(dsl)
//doDitmarsGAReleaseBuild(dsl)
//doElmhurstGAReleaseBuild(dsl)

//doKinesisMilestoneReleaseBuild(dsl)

//doKinesisGAReleaseBuild(dsl)

doMasterSnapshotBuild(dsl)

doHorshamSnapshotBuild(dsl)

doGermantownSnapshotBuild(dsl)

doFishtownSnapshotBuild(dsl)

doElmhurstSnapshotBuild(dsl)

doDitmarsSnapshotBuild(dsl)

doChelseaSnapshotBuild(dsl)

doBrooklynSnapshotBuild(dsl)

// 1.0.x builds
//new SpringCloudStreamBuildMarker(dsl, "spring-cloud", "spring-cloud-stream",
//        "1.0.x", [KAFKA_TIMEOUT_MULTIPLIER: '60']).deploy(true, false, "clean deploy -Pfull,spring")

// Google PubSub Binders builds
new SpringCloudStreamBuildMarker(dsl, 
                                 "spring-cloud", 
                                 "spring-cloud-stream-binder-google-pubsub", "master", [:])
                      .deploy()

// JMS Binders builds
new SpringCloudStreamBuildMarker(dsl,
        "spring-cloud",
        "spring-cloud-stream-binder-jms", "master", [:])
        .deploy()

// AWS Kinesis Binders builds
// Use this as the milestone build parameters into deploy method
// true, false, "", null, null, null, false, true, "milestone"
//new SpringCloudStreamBuildMarker(dsl,
//        "spring-cloud",
//        "spring-cloud-stream-binder-aws-kinesis", "master", [:])
//        .deploy()

//Schema registry CI master
new SpringCloudStreamBuildMarker(dsl, "spring-cloud", "spring-cloud-schema-registry", "master", true)
        .deploy(true, false,"spring-cloud-schema-registry")

//Schema registry CI 1.0.x
new SpringCloudStreamBuildMarker(dsl, "spring-cloud", "spring-cloud-schema-registry", "1.0.x", true)
        .deploy(true, false,"spring-cloud-schema-registry")

//Kinesis binder 1.2.x build
new SpringCloudStreamBuildMarker(dsl, "spring-cloud",  "spring-cloud-stream-binder-aws-kinesis", "1.2.x", true)
        .deploy(true, false,"")

//No GH Trigger for acceptance tests
new SpringCloudStreamBuildMarker(dsl, "spring-cloud", "stream-applications-acceptance-tests", "master", false)
        .deploy(false, false,"spring-cloud-stream-cf-acceptance-tests")

//new SpringCloudStreamBuildMarker(dsl, "spring-cloud", "spring-cloud-stream-acceptance-tests", "master", false)
//        .deploy(false, false,"spring-cloud-stream-k8s-acceptance-tests")

// 0.11 Kafka build
new SpringCloudStreamBuildMarker(dsl, "spring-cloud", "spring-cloud-stream-binder-kafka",
        "0.11", [KAFKA_TIMEOUT_MULTIPLIER: '60']).deploy()

//new SpringCloudStreamBuildMarker(dsl, "spring-cloud", "spring-cloud-stream-binder-kafka",
//        "0.11", [KAFKA_TIMEOUT_MULTIPLIER: '60']).deploy(true, false, "",
//        null, null, null, false, true, "ga")


void doMasterGAReleaseBuild(DslFactory dsl){
    new SpringCloudStreamPhasedBuildMaker(dsl).build(['spring-cloud-stream-binder-kafka':'master',
                                                  'spring-cloud-stream-binder-rabbit':'master'], true, "ga")

}

void doMasterMilestoneReleaseBuild(DslFactory dsl){
    new SpringCloudStreamPhasedBuildMaker(dsl).build(['spring-cloud-stream-binder-kafka':'master',
                                                      'spring-cloud-stream-binder-rabbit':'master'], true, "milestone")

}

void doKinesisMilestoneReleaseBuild(DslFactory dsl){
    new SpringCloudStreamBuildMarker(dsl, "spring-cloud", "spring-cloud-stream-binder-aws-kinesis", "master", false)
            .deploy(true, false, "",
            null, null, null, false, true, "milestone")
}

void doKinesisGAReleaseBuild(DslFactory dsl){
    new SpringCloudStreamBuildMarker(dsl, "spring-cloud", "spring-cloud-stream-binder-aws-kinesis", "master", false)
            .deploy(true, false, "",
            null, null, null, false, true, "ga")
}

void doMasterSnapshotBuild(DslFactory dsl){
    new SpringCloudStreamPhasedBuildMaker(dsl).build(['spring-cloud-stream-binder-kafka':'master',
                                                      'spring-cloud-stream-binder-rabbit':'master',
                                                      'spring-cloud-stream-binder-aws-kinesis':'master'], false, "")
}

void doHorshamSnapshotBuild(DslFactory dsl) {
    // Spring Cloud Stream Ditmars builds (2.0.x)
    new SpringCloudStreamPhasedBuildMaker(dsl).build("3.0.x", "Horsham", "spring-cloud-stream-Horsham-builds",
            ['spring-cloud-stream-binder-kafka' : '3.0.x',
             'spring-cloud-stream-binder-rabbit': '3.0.x'], false, "", "Horsham")
}

void doGermantownSnapshotBuild(DslFactory dsl) {
    // Spring Cloud Stream Ditmars builds (2.0.x)
    new SpringCloudStreamPhasedBuildMaker(dsl).build("2.2.x", "Germantown.x", "spring-cloud-stream-Germantown-x-builds",
            ['spring-cloud-stream-binder-kafka' : '2.2.x',
             'spring-cloud-stream-binder-rabbit': '2.2.x'], false, "", "Germantown")
}

void doFishtownSnapshotBuild(DslFactory dsl) {
    // Spring Cloud Stream Ditmars builds (2.0.x)
    new SpringCloudStreamPhasedBuildMaker(dsl).build("2.1.x", "Fishtown.x", "spring-cloud-stream-Fishtown-x-builds",
            ['spring-cloud-stream-binder-kafka' : '2.1.x',
             'spring-cloud-stream-binder-rabbit': '2.1.x'], false, "", "Fishtown")
}

void doElmhurstSnapshotBuild(DslFactory dsl) {
    // Spring Cloud Stream Ditmars builds (2.0.x)
    new SpringCloudStreamPhasedBuildMaker(dsl).build("2.0.x", "Elmhurst.x", "spring-cloud-stream-Elmhurst-x-builds",
            ['spring-cloud-stream-binder-kafka' : '2.0.x',
             'spring-cloud-stream-binder-rabbit': '2.0.x'], false, "", "Elmhurst")
}

void doElmhurstGAReleaseBuild(DslFactory dsl){
    // Spring Cloud Stream Elmhurst builds (2.0.x)
    new SpringCloudStreamPhasedBuildMaker(dsl).build("2.0.x", "Elmhurst.x", "spring-cloud-stream-Elmhurst-x-builds",
            ['spring-cloud-stream-binder-kafka':'2.0.x',
             'spring-cloud-stream-binder-rabbit':'2.0.x'], true, "ga", "Elmhurst")
}

void doDitmarsSnapshotBuild(DslFactory dsl){
    // Spring Cloud Stream Ditmars builds (1.3.x)
    new SpringCloudStreamPhasedBuildMaker(dsl).build("1.3.x", "Ditmars.x", "spring-cloud-stream-Ditmars-x-builds",
            ['spring-cloud-stream-binder-kafka':'1.3.x',
             'spring-cloud-stream-binder-rabbit':'1.3.x'], false, "", "Ditmars")
}

void doDitmarsGAReleaseBuild(DslFactory dsl){
    // Spring Cloud Stream Ditmars builds (1.3.x)
    new SpringCloudStreamPhasedBuildMaker(dsl).build("1.3.x", "Ditmars.x", "spring-cloud-stream-Ditmars-x-builds",
            ['spring-cloud-stream-binder-kafka':'1.3.x',
             'spring-cloud-stream-binder-rabbit':'1.3.x'], true, "ga", "Ditmars")
}

void doChelseaSnapshotBuild(DslFactory dsl) {
// Spring Cloud Stream Chelsea builds (1.2.x)
    new SpringCloudStreamPhasedBuildMaker(dsl).build("1.2.x", "Chelsea.x", "spring-cloud-stream-Chelsea-x-builds",
            ['spring-cloud-stream-binder-kafka' : '1.2.x',
             'spring-cloud-stream-binder-rabbit': '1.2.x'], false, "", "Chelsea")
}

void doBrooklynSnapshotBuild(DslFactory dsl) {
// Spring Cloud Stream Brooklyn builds (1.1.x)
    new SpringCloudStreamPhasedBuildMaker(dsl).build("1.1.x", "Brooklyn.x", "spring-cloud-stream-Brooklyn-x-builds",
            ['spring-cloud-stream-binder-kafka' : '1.1.x',
             'spring-cloud-stream-binder-rabbit': '1.1.x'], false, "", "Brooklyn")
}
