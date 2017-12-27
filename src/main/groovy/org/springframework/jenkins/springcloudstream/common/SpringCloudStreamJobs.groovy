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
                        cd ci-docker-compose
						echo "Running script"
						bash ${script}
					"""
    }

    String cleanAndDeploy(boolean docsBuild, boolean isRelease, String releaseType) {

        if (isRelease && releaseType != null && !releaseType.equals("milestone")) {
            return """
                        lines=\$(find . -type f -name pom.xml | xargs egrep "SNAPSHOT|M[0-9]|RC[0-9]" | grep -v regex | wc -l)
                        if [ \$lines -eq 0 ]; then
                            set +x
                            ./mvnw clean deploy -Dgpg.secretKeyring="\$${gpgSecRing()}" -Dgpg.publicKeyring="\$${
                gpgPubRing()}" -Dgpg.passphrase="\$${gpgPassphrase()}" -DSONATYPE_USER="\$${sonatypeUser()}" -DSONATYPE_PASSWORD="\$${sonatypePassword()}" -Pcentral -U
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
        }
        else if (!docsBuild) {
            return """
                ./mvnw clean deploy -U
            """
        }
        else if(docsBuild) {
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

}