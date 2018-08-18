package org.springframework.jenkins.springcloudstream.common

import org.springframework.jenkins.common.job.BuildAndDeploy

/**
 * @author Marcin Grzejszczak
 */
trait SpringCloudStreamJobs extends BuildAndDeploy {

    @Override
    String projectSuffix() {
        return 'spring-cloud-stream'
    }

    String scriptToExecute(String scriptDir, String script) {
        return """
                        echo "cd to ${scriptDir}"
                        cd ${scriptDir}
						echo "Running script"
						bash ${script}
					"""
    }

    String startCFAcceptanceTests() {
        return """
                        echo "cd to custom-stream-apps/uppercase-transformer-kafka"
                        cd custom-stream-apps/uppercase-transformer-kafka
                        ${customStreamAppBuildForTests()}
                        
                        echo "cd to ../uppercase-transformer-rabbit"
                        cd ../uppercase-transformer-rabbit
                        ${customStreamAppBuildForTests()}

                        echo "cd to ../partitioning-producer-sample-kafka"
                        cd ../partitioning-producer-sample-kafka
                        ${customStreamAppBuildForTests()}

                        echo "cd to ../partitioning-producer-sample-rabbit"
                        cd ../partitioning-producer-sample-rabbit
                       ${customStreamAppBuildForTests()}

                        echo "cd to ../partitioning-consumer-sample-kafka"
                        cd ../partitioning-consumer-sample-kafka
                        "${customStreamAppBuildForTests()}"

                        echo "cd to ../partitioning-consumer-sample-rabbit"
                        cd ../partitioning-consumer-sample-rabbit
                       ${customStreamAppBuildForTests()}

                        echo "cd to spring-cloud-stream-cf-acceptance-tests"
                        cd ../../spring-cloud-stream-cf-acceptance-tests
						echo "Running script"
						bash "runAcceptanceTests.sh" "\$${cfAcceptanceTestUrl()}" "\$${cfAcceptanceTestUser()}" "\$${cfAcceptanceTestPassword()}" "\$${cfAcceptanceTestOrg()}" "\$${cfAcceptanceTestSpace()}" "\$${cfAcceptanceTestSkipSsl()}" 
					"""
    }

    String scriptToExecuteForCFAcceptanceTest(String scriptDir, String script) {
        return """
                        echo "cd to ${scriptDir}"
                        cd ${scriptDir}
						echo "Running script"
						bash ${script} "\$${cfAcceptanceTestUrl()}" "\$${cfAcceptanceTestUser()}" "\$${
            cfAcceptanceTestPassword()
        }" "\$${cfAcceptanceTestOrg()}" "\$${cfAcceptanceTestSpace()}" "\$${cfAcceptanceTestSkipSsl()}" 
					"""
    }

    String cleanAndPackage() {
        //just build
        return """
                ./mvnw clean package
            """
    }

    String cleanAndDeploy(boolean docsBuild, boolean isRelease, String releaseType) {

        if (isRelease && releaseType != null && !releaseType.equals("milestone")) {
            return """
                        lines=\$(find . -type f -name pom.xml | xargs egrep "SNAPSHOT|M[0-9]|RC[0-9]" | grep -v regex | wc -l)
                        if [ \$lines -eq 0 ]; then
                            set +x
                            ./mvnw clean deploy -Dgpg.secretKeyring="\$${gpgSecRing()}" -Dgpg.publicKeyring="\$${
                gpgPubRing()
            }" -Dgpg.passphrase="\$${gpgPassphrase()}" -DSONATYPE_USER="\$${sonatypeUser()}" -DSONATYPE_PASSWORD="\$${
                sonatypePassword()
            }" -Pcentral -U
                            set -x
                        else
                            echo "Non release versions found. Aborting build"
                        fi
                    """
        }
        if (isRelease && releaseType != null && releaseType.equals("milestone")) {
            return """
                        lines=\$(find . -type f -name pom.xml | xargs egrep "SNAPSHOT" | grep -v regex | wc -l)
                        if [ \$lines -eq 0 ]; then
                            set +x
                            ./mvnw clean deploy -Pspring -U
                            set -x
                        else
                            echo "snapshots found. Aborting build"
                        fi
                    """
        } else if (!docsBuild) {
            return """
                ./mvnw clean deploy -U
            """
        } else if (docsBuild) {
            //just build
            return """
                ./mvnw clean package
            """
        }
    }

    String gpgSecRing() {
        return 'FOO_SEC'
    }

    String gpgPubRing() {
        return 'FOO_PUB'
    }

    String gpgPassphrase() {
        return 'FOO_PASSPHRASE'
    }

    String sonatypeUser() {
        return 'SONATYPE_USER'
    }

    String sonatypePassword() {
        return 'SONATYPE_PASSWORD'
    }

    String cfAcceptanceTestUrl() {
        return 'CF_E2E_TEST_SPRING_CLOUD_STREAM_URL'
    }

    String cfAcceptanceTestSkipSsl() {
        return 'CF_E2E_TEST_SPRING_CLOUD_STREAM_SKIP_SSL'
    }

    String cfAcceptanceTestOrg() {
        return 'CF_E2E_TEST_SPRING_CLOUD_STREAM_ORG'
    }

    String cfAcceptanceTestSpace() {
        return 'CF_E2E_TEST_SPRING_CLOUD_STREAM_SPACE'
    }

    String cfAcceptanceTestUser() {
        return 'CF_E2E_TEST_SPRING_CLOUD_STREAM_USER'
    }

    String cfAcceptanceTestPassword() {
        return 'CF_E2E_TEST_SPRING_CLOUD_STREAM_PASSWORD'
    }

    String dockerHubUserNameEnvVar() {
        return 'DOCKER_HUB_USERNAME'
    }

    String dockerHubPasswordEnvVar() {
        return 'DOCKER_HUB_PASSWORD'
    }

    String customStreamAppBuildForTests() {
        return """
            ./mvnw -U clean deploy -DskipTests
            ./ mvnw docker:build docker:push -DskipTests -Ddocker.username="\$${dockerHubUserNameEnvVar()}" -Ddocker.password="\$${dockerHubPasswordEnvVar()}"
        """
    }
}