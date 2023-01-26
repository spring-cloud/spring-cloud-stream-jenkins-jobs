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

    String prepareCFAcceptanceTests() {
        return """
                        #echo "cd to custom-stream-apps"
                        #cd custom-stream-apps
                        #${customStreamAppBuildForCFTests()}

                        cd cloudfoundry
						echo "Running script"
                        set +x
						bash "runCFAcceptanceTests.sh" "\$${cfAcceptanceTestUrl()}" "\$${cfAcceptanceTestUser()}" "\$${cfAcceptanceTestPassword()}" "\$${cfAcceptanceTestOrg()}" "\$${cfAcceptanceTestSpace()}" "\$${cfAcceptanceTestSkipSsl()}"
                        set -x
					"""
    }

    String prepareK8SAcceptanceTests() {
        return """
                        cd kubernetes
						echo "Running script"
                        set +x
                        gcloud auth activate-service-account --key-file "\$${googleApplicationCredentials()}"
						bash "runK8SAcceptanceTests.sh" "\$${k8sAcceptanceTestProject()}" "\$${k8sAcceptanceTestCluster()}" "\$${k8sAcceptanceTestZone()}" "\$${k8sAcceptanceTestNamespace()}" "\$${k8sAcceptanceTestBaseDomain()}"
                        set -x
					"""
    }

    String cleanAndPackage() {
        //just build
        return """
                ./mvnw clean package -U
            """
    }

    String cleanAndDeploy(boolean docsBuild, boolean isRelease, String releaseType) {

        if (isRelease && releaseType != null && !releaseType.equals("milestone")) {
            return """
                        lines=\$(find . -type f -name pom.xml | xargs egrep "SNAPSHOT|M[0-9]|RC[0-9]" | grep -v regex | wc -l)
                        if [ \$lines -eq 0 ]; then
                            set +x
                            export temporaryDir

                            echo -e '#!/bin/bash \\n echo $FOO_PASSPHRASE | /usr/bin/gpg --batch --no-tty --passphrase-fd 0 "\$@"' > "${temporaryDir}/our_gpg.sh"
                            chmod +x "${temporaryDir}/our_gpg.sh"

                            ./mvnw clean deploy -Dgpg.executable="$temporaryDir"/our_gpg.sh -Dgpg.secretKeyring="\$${gpgSecRing()}" -Dgpg.publicKeyring="\$${
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
        } else {
            return """
                ./mvnw clean deploy -Pdocs,spring -U
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

    String k8sAcceptanceTestCluster() {
        return 'SPRING_CLOUD_STREAM_K8S_TESTS_CLUSTER'
    }

    String k8sAcceptanceTestClusterVersion() {
        return 'SPRING_CLOUD_STREAM_K8S_TESTS_CLUSTER_VERSION'
    }

    String k8sAcceptanceTestProject() {
        return 'SPRING_CLOUD_STREAM_K8S_TESTS_PROJECT'
    }

    String k8sAcceptanceTestZone() {
        return 'SPRING_CLOUD_STREAM_K8S_TESTS_ZONE'
    }

    String k8sAcceptanceTestNamespace() {
        return 'SPRING_CLOUD_STREAM_K8S_NAMESPACE'
    }

    String k8sAcceptanceTestBaseDomain() {
        return 'SPRING_CLOUD_STREAM_K8S_BASE_DOMAIN'
    }

    String googleApplicationCredentials(){
        return 'GOOGLE_APPLICATION_CREDENTIALS'
    }

    String customStreamAppBuildForCFTests() {
        return """
            #./mvnw -U clean deploy -DskipTests
        """
    }

    String customStreamAppBuildForK8STests() {
        return """
            ./mvnw -U clean package -DskipTests docker:build docker:push -DskipTests -Ddocker.username="\$${dockerHubUserNameEnvVar()}" -Ddocker.password="\$${dockerHubPasswordEnvVar()}"
        """
    }

    String mavenSettingsId() {
        return 'maven-settings-spring-cloud'
    }

    String buildUserNameEnvVar() {
        return 'ARTIFACTORY_USERNAME'
    }

    String buildPasswordEnvVar() {
        return 'ARTIFACTORY_PASSWORD'
    }

    String buildCredentialId() {
        return '02bd1690-b54f-4c9f-819d-a77cb7a9822c'
    }
}
