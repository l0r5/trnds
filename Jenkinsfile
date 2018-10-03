pipeline {
  agent any
  stages {
    stage('trnds') {
      steps {
        script {
          load 'src/main/groovy/TrendFetcher.groovy'
        }
      }
    }
  }
}