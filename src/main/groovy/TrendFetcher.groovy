#!/usr/bin/env groovy
import com.cloudbees.groovy.cps.NonCPS
import groovy.json.JsonSlurperClassic

import java.text.SimpleDateFormat

library 'jenkins-shared-library'

node('trnds') {

    apiData = [:]
    fetchedData = ""
    processedData = [:]

    try {

        stage('Initialization') {
            echo 'Initialize...'
            def apiDataFile = readJSON file:'api_data.json'
            echo 'File has been read'
            // apiData = parseJSON(apiDataFile)
        }

        stage('Fetch Data') {
            def providersMap = apiData["providers"]
            def urlList = []

            providersMap.each {
                def baseUrl = it["baseUrl"]
                def query = "?part=snippet,contentDetails&chart=mostPopular&regionCode=US&maxResults=25&"
                def apiKey = "key=" + it["apiKey"]
                urlList.add(baseUrl + query + apiKey)
            }

            echo "GET Following URLs:"
            urlList.each {
                println it.toString()

                def connection = new URL(it as String).openConnection() as HttpURLConnection

                // set some headers
                connection.setRequestProperty('User-Agent', 'groovy-2.4.15')
                connection.setRequestProperty('Accept', 'application/json')

                // get the response code - automatically sends the request
                echo "Response code: " + connection.responseCode
                fetchedData = connection.inputStream.text
            }
        }
        stage('Process fetched Data') {
            echo "Process fetched data..."
            def date = new Date()
            def sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
            processedData << ["date": sdf.format(date)]
            if (fetchedData != null) {
                apiData.providers.each {
                    switch (it.name) {
                        case "YouTube":
                            processYouTubeData()
                    }
                }
            } else {
                echo "No data was fetched."
            }
        }



        stage('Persist to MongoDB') {
            echo("Save data...")
            MongoDBService.save(processedData)
        }

    } catch (Exception e) {
        echo e.toString()
        e.stackTrace.each {
            echo it.toString()
        }
        currentBuild.result = "FAILED"
    } finally {
        echo currentBuild.result
    }
}



def processYouTubeData() {
    def youtubeVideos = []
    def jsonSlurper = new JsonSlurperClassic()
    fetchedData = jsonSlurper.parseText(fetchedData as String)
    fetchedData.items.snippet.eachWithIndex { it, count ->
        def video = [
                "publishedAt": new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSX",it.publishedAt as String),
                "title"      : it.title,
                "description": it.description,
                "url"        : "https://www.youtube.com/watch?v=" + fetchedData.items[count].id as String
        ]
        youtubeVideos.add(video)
        println("Built Video:" +
                "\nTitle: " + video.title +
                "\nPublished at: " + video.publishedAt.toString() +
                "\nUrl: " + video.url)
    }
    processedData << ["youtube": youtubeVideos]
}

@NonCPS
static parseJSON(jsonFile) {
    def map = new groovy.json.JsonSlurperClassic().parseText(jsonFile)
    return map
}
