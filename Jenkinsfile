pipeline {
  agent {
    docker {
      image 'maven:3.3.3'
    }

  }
  stages {
    stage('build') {
      steps {
        sh 'mvn --version'
      }
    }
    stage('trnds') {
      steps {
        load 'src/main/groovy/TrendFetcher.groovy'
       }
    }
  }
}