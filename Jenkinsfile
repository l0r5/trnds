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
          script {
            echo 'Starting stage: trnds...'
            def rootDir = pwd()
            echo "rootdir: ${rootDir}"
            echo dir()
            def trnds = load '${rootDir}@script/TrendFetcher.groovy'
            trnds.node()
            echo 'Completed stage: trnds'
          }
        }
       }
   }
}