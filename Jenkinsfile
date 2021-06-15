#!groovy

import java.text.SimpleDateFormat

import groovy.json.JsonOutput

def getShortCommitHash() {
  return sh(script: "git rev-parse --short=8 HEAD", returnStdout: true).trim()
}

def getScalaVersionFromBuildSbt() {
  return sh(script: "grep -E '^[[:space:]]*scalaVersion[[:space:]]*:=[[:space:]]*' build.sbt | cut -d'\"' -f2 | cut -d'.' -f1-2", returnStdout: true).trim()
}

def getJarVersionFromBuildSbt() {
  return sh(script: "grep -E '^[[:space:]]*version[[:space:]]*:=[[:space:]]*' build.sbt | cut -d'\"' -f2", returnStdout: true).trim()
}

properties([pipelineTriggers([githubPush()])])

// VC global variables
vcMaxSandboxes = 25

pipeline {
  options {
    timestamps()
    disableConcurrentBuilds()
    buildDiscarder(logRotator(daysToKeepStr: '30', numToKeepStr: '30'))
  }

  agent {
    kubernetes {
      yaml """
apiVersion: v1
kind: Pod
metadata:
  name: test-scala-proj
labels:
  component: ci
spec:
  containers:
    - name: jnlp
      image: jenkins/inbound-agent:4.3-4
      imagePullPolicy: IfNotPresent
    - name: builder
      image: devopsziel/scala-sbt-builder:1.4.1
      tty: true
      command:
      - cat
      resources:
        requests:
          cpu: "300m"
          memory: "500Mi"
"""
    }
  }

  environment {
    isRelease = (env.BRANCH_NAME =~ /^v[0-9]+\.[0-9]+\.[0-9]+$/).matches()
    isMaster = (env.BRANCH_NAME =~ /master/).matches()
    useSandbox = "${isRelease == false && isMaster == false }"
    git_short_commit = getShortCommitHash()
    GITHUB_CD_REPO = "sky-newCRM-datateam-airflow-cd"
    JFROG_FOLDER = "newcrm-datateam"
    APP_BASENAME = "bulk-preprocess"
    CUSTOM_VERSION = "${env.BRANCH_NAME.replaceAll('[^a-zA-Z0-9-]+','-').take(45).toLowerCase()}-${git_short_commit}"
    APP_VERSION = "${isRelease == true ? env.BRANCH_NAME : CUSTOM_VERSION}-${BUILD_NUMBER}"
    SCALA_VERSION = getScalaVersionFromBuildSbt()
    JAR_VERSION = getJarVersionFromBuildSbt()
    JAR_PATH = "target/scala-${SCALA_VERSION}/${APP_BASENAME}-assembly-${JAR_VERSION}.jar"
    PROJECT_NAME = "sky-newCRM-datateam-bulk-preprocess"
    VC_APP_NAME = "${PROJECT_NAME}-SB"
    VC_APP_VERSION = "${env.BRANCH_NAME}-${git_short_commit}-${BUILD_NUMBER}"
    VERACODE_VERSION = "20.12.7.3"
  }

  stages {
    stage('Coverage') {
      steps {
        container('builder') {
          sh 'debug'
          sh 'master value is :${isMaster}'
          sh "sbt jacoco"
          //sh "cp target/scala-${SCALA_VERSION}/jacoco/report/jacoco.xml jacoco.xml"
        }
      }
    }
  }
}
