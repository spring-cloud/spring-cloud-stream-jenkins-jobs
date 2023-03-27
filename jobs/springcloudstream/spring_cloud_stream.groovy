package springcloudstream

import org.springframework.jenkins.springcloudstream.ci.SpringCloudStreamBuildMarker
import org.springframework.jenkins.springcloudstream.ci.SpringCloudStreamPhasedBuildMaker
import javaposse.jobdsl.dsl.DslFactory

DslFactory dsl = this

doKinesisMilestoneReleaseBuild(dsl)

//doKinesisGAReleaseBuild(dsl)

doMainSnapshotBuild(dsl)
//kinesis40WIPBuild(dsl)

//do31xSnapshotBuild(dsl)

//do32xSnapshotBuild(dsl)

// AWS Kinesis Binders builds
// Use this as the milestone build parameters into deploy method
// true, false, "", null, null, null, false, true, "milestone"
//new SpringCloudStreamBuildMarker(dsl,
//        "spring-cloud",
//        "spring-cloud-stream-binder-aws-kinesis", "main", [:])
//        .deploy()

//Kinesis binder 1.2.x build
new SpringCloudStreamBuildMarker(dsl, "spring-cloud",  "spring-cloud-stream-binder-aws-kinesis", "1.2.x", true)
        .deploy(true, false,"")

//No GH Trigger for acceptance tests
new SpringCloudStreamBuildMarker(dsl, "spring-cloud", "stream-applications-acceptance-tests", "main", false)
        .deploy(false, false,"spring-cloud-stream-cf-acceptance-tests")

void doKinesisMilestoneReleaseBuild(DslFactory dsl){
    new SpringCloudStreamBuildMarker(dsl, "spring-cloud", "spring-cloud-stream-binder-aws-kinesis", "4.0-WIP", false)
            .deploy(true, false, "",
            null, null, null, false, true, "milestone")
}

void doKinesisGAReleaseBuild(DslFactory dsl){
    new SpringCloudStreamBuildMarker(dsl, "spring-cloud", "spring-cloud-stream-binder-aws-kinesis", "main", false)
            .deploy(true, false, "",
            null, null, null, false, true, "ga")
}

void doMainSnapshotBuild(DslFactory dsl){
    new SpringCloudStreamPhasedBuildMaker(dsl).build(['spring-cloud-stream-binder-aws-kinesis':'main'], false, "")
}

void kinesis40WIPBuild(DslFactory dsl){
    new SpringCloudStreamPhasedBuildMaker(dsl).build(['spring-cloud-stream-binder-aws-kinesis':'4.0-WIP'], false, "")
}

void do31xSnapshotBuild(DslFactory dsl) {
    // Spring Cloud Stream 3.1.x builds
    new SpringCloudStreamPhasedBuildMaker(dsl).build("3.1.x", "3.1.x", "spring-cloud-stream-31x-builds",
            ['spring-cloud-stream-binder-kafka' : '3.1.x',
             'spring-cloud-stream-binder-rabbit': '3.1.x'], false, "", "main")
}

void do32xSnapshotBuild(DslFactory dsl) {
    // Spring Cloud Stream 3.2.x builds
    new SpringCloudStreamPhasedBuildMaker(dsl).build("3.2.x", "3.2.x", "spring-cloud-stream-32x-builds",
            [:], false, "", "main")
}
