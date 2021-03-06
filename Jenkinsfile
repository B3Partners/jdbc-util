node {
    properties([[
        $class: 'jenkins.model.BuildDiscarderProperty',
        strategy: [
            $class: 'LogRotator',
            artifactDaysToKeepStr: '8',
            artifactNumToKeepStr: '3',
            daysToKeepStr: '15',
            numToKeepStr: '5'
            ]
        ]]);

    withEnv([
        "JAVA_HOME=${ tool 'OpenJDK11' }",
        "PATH+MAVEN=${tool 'Maven CURRENT'}/bin:${ tool 'OpenJDK11' }/bin"
        ]) {

        stage('Prepare') {
            checkout scm
            sh "cp .jenkins/local.oracle.properties src/test/resources/"
        }

        stage('Build') {
            echo "Building branch: ${env.BRANCH_NAME}"
            sh "mvn clean install --global-toolchains .jenkins/toolchains.xml -Dmaven.test.skip=true -B -V -e -fae -q -Poracle"
        }

        stage('Test') {
            echo "Running unit tests"
            sh "mvn -e test --global-toolchains .jenkins/toolchains.xml -B -Poracle"
        }

        lock('brmo-oracle') {
            // sh ".jenkins/start-oracle-brmo.sh"
            timeout(30) {
                stage('Prepare Oracle Databases') {
                    sh "sqlplus -l -S jenkins_staging/jenkins_staging@192.168.1.26:15210/XE < ./.jenkins/clear-schema.sql"
                    sh ".jenkins/db-prepare-staging.sh"
                }

                stage('Integration Test') {
                    echo "Running integration tests"
                    sh "mvn -e verify --global-toolchains .jenkins/toolchains.xml -B -Poracle -T1 -Dtest.onlyITs=true"
                }

                stage('Cleanup Oracle Database') {
                    sh "sqlplus -l -S jenkins_staging/jenkins_staging@192.168.1.26:15210/XE < ./.jenkins/clear-schema.sql"
                }
            }
        }

        stage('Publish Test Results') {
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/TEST-*.xml, **/target/failsafe-reports/TEST-*.xml'
            jacoco classPattern: '**/target/classes', execPattern: '**/target/**.exec'
            sh "curl -s https://codecov.io/bash | bash"
        }

        stage('OWASP Dependency Check') {
            echo "Uitvoeren OWASP dependency check"
            sh "mvn org.owasp:dependency-check-maven:check --global-toolchains .jenkins/toolchains.xml"
            dependencyCheckPublisher pattern: '**/dependency-check-report.xml', failedNewCritical: 1, failedNewHigh: 1, failedTotalCritical: 1, failedTotalHigh: 3, unstableTotalHigh: 2
        }
    }
}
