job('spring-cloud-stream-meta-seed') {
    triggers {
        githubPush()
    }
    scm {
        git {
            remote {
                github('spring-cloud/spring-cloud-stream-jenkins-jobs')
            }
            branch('master')
        }
    }
    steps {
        gradle("clean build")
        dsl {
            external('projects/*.groovy')
            removeAction('DISABLE')
            removeViewAction('DELETE')
            ignoreExisting(false)
            additionalClasspath([
                    'src/main/groovy', 'src/main/resources', 'build/lib/*.jar'
            ].join("\n"))
        }
    }
}
