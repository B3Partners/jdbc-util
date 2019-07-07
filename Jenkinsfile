timestamps {

    properties([
        [$class: 'jenkins.model.BuildDiscarderProperty', strategy: [$class: 'LogRotator',
            artifactDaysToKeepStr: '8',
            artifactNumToKeepStr: '3',
            daysToKeepStr: '15',
            numToKeepStr: '5']
        ]]);

    node {
        withEnv(["JAVA_HOME=${ tool 'JDK8' }", "PATH+MAVEN=${tool 'Maven 3.6.1'}/bin:${env.JAVA_HOME}/bin"]) {

            stage('Prepare') {
                sh "ulimit -a"
                sh "free -m"
                checkout scm
            }

            stage('Build') {
                echo "Building branch: ${env.BRANCH_NAME}"
                sh "mvn install -Dmaven.test.skip=true -B -V -e -fae -q -Poracle"
            }

            stage('Test') {
                echo "Running unit tests"
                sh "mvn -e test -B -Poracle"
            }

            lock('brmo-oracle') {
              timeout(90) {
                stage('Prepare Oracle Databases') {
                    sh "sqlplus -l -S jenkins_staging/jenkins_staging@192.168.1.11:1521/ORCL < ./.jenkins/clear-schema.sql"
                    sh ".jenkins/db-prepare-staging.sh"
                }

                stage('Integration Test') {
                    echo "Running integration tests"
                    sh "mvn -e verify -B -Poracle -T1 -Dtest.onlyITs=true"
                }

                stage('Cleanup Oracle Database') {
                    sh "sqlplus -l -S jenkins_staging/jenkins_staging@192.168.1.11:1521/ORCL < ./.jenkins/clear-schema.sql"
                }
              }
            }

            stage('Publish Test Results') {
                junit allowEmptyResults: true, testResults: '**/target/surefire-reports/TEST-*.xml, **/target/failsafe-reports/TEST-*.xml'
            }

            stage('OWASP Dependency Check') {
                echo "Uitvoeren OWASP dependency check"
                sh "mvn org.owasp:dependency-check-maven:check"
                //dependencyCheckPublisher canComputeNew: false, defaultEncoding: '', healthy: '85', pattern: '**/dependency-check-report.xml', shouldDetectModules: true, unHealthy: ''
                dependencyCheckPublisher(pattern: '**/target/dependency-check-report.xml')
            }
        }
    }
}
