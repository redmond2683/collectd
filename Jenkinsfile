pipeline {
    agent any
    environment {
        COLLECTD_DIR="collectd"
        COLLECTD_REPO="git@github.com:redmond2683/collectd.git"
        XML_DEFINITION_DIR="xml_definition"
        XML_DEFINITION_REPO="git@github.com:ScaleWX/xml_definition.git"
        CREDENTIALS_ID="redmond2683"
    }
    stages {
        stage('Build') {
            steps {
                dir(COLLECTD_DIR) {
                    checkout([$class: 'GitSCM', branches: [[name: 'refs/tags/*']], extensions: [], userRemoteConfigs: [[credentialsId: CREDENTIALS_ID, url: COLLECTD_REPO]]])
                    sh './build.sh && ./configure && make rpms'
                }
                dir(XML_DEFINITION_DIR) {
                    checkout([$class: 'GitSCM', branches: [[name: 'refs/tags/*']], extensions: [], userRemoteConfigs: [[credentialsId: CREDENTIALS_ID, url: XML_DEFINITION_REPO]]])
                    sh './bootstrap.sh && ./configure && make rpm'
                }
            }
        }
        stage('Deploy') {
            environment {
                COLLECTD="""${sh(
                            returnStdout: true,
                            script: 'basename `find . -type f -regextype posix-egrep -regex ".+/collectd-[0-9].+rpm" -print`'
                         )}"""
                FILEDATA="""${sh(
                            returnStdout: true,
                            script: 'basename `find . -type f -regextype posix-egrep -regex ".+/collectd-filedata.+rpm" -print`'
                         )}"""
                SSH="""${sh(
                            returnStdout: true,
                            script: 'basename `find . -type f -regextype posix-egrep -regex ".+/collectd-ssh.+rpm" -print`'
                         )}"""
                RRDTOOL="""${sh(
                            returnStdout: true,
                            script: 'basename `find . -type f -regextype posix-egrep -regex ".+/collectd-rrdtool.+rpm" -print`'
                         )}"""
            }
            steps {
                dir(COLLECTD_DIR) {
                    sh 'sudo rpm -Uvh $COLLECTD $FILEDATA $SSH $RRDTOOL'
                }
            }
        }
        stage('Test') {
            steps {
                sh 'echo "testing"'
            }
        }
        stage('Release') {
            steps {
                sh 'echo "releasing"'
            }
        }
    }
}