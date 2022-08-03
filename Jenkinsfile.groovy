def collectd=''
def filedata=''
def ssh=''
def rrdtool=''
def xml_definition=''
pipeline {
    agent any
    environment {
        COLLECTD_DIR="collectd"
        COLLECTD_REPO="git@github.com:redmond2683/collectd.git"
        XML_DEFINITION_DIR="xml_definition"
        XML_DEFINITION_REPO="git@github.com:ScaleWX/xml_definition.git"
        CREDENTIALS_ID="redmond2683"
        TEST_DIR="/var/lib/jenkins/work"
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
            steps {
                script {
                    collectd=sh(
                        script: 'basename `find . -type f -regextype posix-egrep -regex ".+/collectd-[0-9].+rpm" -print`',
                        returnStdout: true
                    ).trim()
                    filedata=sh(
                        script: 'basename `find . -type f -regextype posix-egrep -regex ".+/collectd-filedata.+rpm" -print`',
                        returnStdout: true
                    ).trim()
                    ssh=sh(
                        script: 'basename `find . -type f -regextype posix-egrep -regex ".+/collectd-ssh.+rpm" -print`',
                        returnStdout: true
                    ).trim()
                    rrdtool=sh(
                        script: 'basename `find . -type f -regextype posix-egrep -regex ".+/collectd-rrdtool.+rpm" -print`',
                        returnStdout: true
                    ).trim()
                }
                dir(COLLECTD_DIR) {
                    sh "sudo rpm -Uvh $collectd $filedata $ssh $rrdtool"
                }
                script {
                    xml_definition=sh(
                        script: 'basename `find . -type f -regextype posix-egrep -regex ".+/xml_definition.+noarch.+rpm" -print`',
                        returnStdout: true
                    ).trim()
                }
                dir(XML_DEFINITION_DIR) {
                    sh "sudo rpm -Uvh RPMS/noarch/$xml_definition"
                }
            }
        }
        stage('Test') {
            steps {
                dir(TEST_DIR) {
                    sh 'python verify_metrics.py -d /var/lib/jenkins/work -f /etc/lustre-b_es5_2.xml -t tests.xml -c ./collectd.conf'
                }
            }
        }
        stage('Cleanup') {
            steps {
                sh 'sudo rpm -e collectd collectd-ssh collectd-rrdtool collectd-filedata xml_definition'
            }
        }
        stage('Release') {
            environment {
                GITHUB_TOKEN='<token>'
            }
            steps {
                dir(COLLECTD_DIR) {
                    sh './publish.sh $GITHUB_TOKEN feature/jenkinscd redmond2683/collectd.git'
                }
            }
        }
    }
}
