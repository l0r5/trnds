#!/usr/bin/env groovy
import org.codehaus.groovy.runtime.DateGroovyMethods

import java.text.SimpleDateFormat

library 'jenkins-shared-library'

apiData = [:]
fetchedData = ""
processedData = [:]

node('trnds') {



    try {

        stage('Initialization') {
            echo 'Initialize...'
            apiData = readJSON(text: libraryResource('api_data.json'))
            echo "File has been read: \n${apiData}"
        }

        stage('Fetch Data') {
            echo 'Fetch Data'
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
                echo "${it}"
                def response = httpRequest it
                echo "Status: ${response.status}"
                fetchedData = response.content
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
    def fetchedYouTubeData = readJSON(text: "${fetchedData}")

    fetchedYouTubeData.items.snippet.eachWithIndex { it, count ->

        def sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")

        def video = [
                "publishedAt": sdf.parse(it.publishedAt.toString()),
                "title"      : it.title,
                "description": it.description,
                "url"        : "https://www.youtube.com/watch?v=" + fetchedYouTubeData.items[count].id.toString()
        ]
        youtubeVideos.add(video)
        println("Built Video:" +
                "\nTitle: " + video.title +
                "\nPublished at: " + video.publishedAt.toString() +
                "\nUrl: " + video.url)
    }
    processedData << ["youtube": youtubeVideos]
}