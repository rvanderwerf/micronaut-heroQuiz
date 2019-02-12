package com.amazon.ask.heroQuiz.services

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.Table
import groovy.util.logging.Slf4j

import javax.annotation.PostConstruct
import javax.inject.Singleton

@Singleton
@Slf4j
class MetricsService {

    DynamoDB dynamoDB

    @PostConstruct init() {
        dynamoDB = new DynamoDB(new AmazonDynamoDBClient())
    }

    void questionMetricsCorrect(int questionIndex) {
        questionMetrics(questionIndex, true)
    }

    void questionMetricsWrong(int questionIndex) {
        questionMetrics(questionIndex, false)
    }

    void questionMetrics(int questionIndex, boolean correct) {

        Table table = dynamoDB.getTable("StarWarzQuizMetrics")
        log.debug("getting question id from table ${questionIndex}")
        Item item = table.getItem("id", questionIndex)
        int askedCount = 0
        int correctCount = 0
        if (item != null) {
            askedCount = item.getInt("asked")
            correctCount = item.getInt("correct")
        }
        askedCount++
        if (correct) {
            correctCount++
        }
        Item newItem = new Item()
        newItem.withInt("id", questionIndex)
        newItem.withInt("asked", askedCount)
        newItem.withInt("correct", correctCount)
        table.putItem(newItem)
    }

    void userMetrics(String userId, int score) {

        Table table = dynamoDB.getTable("StarWarsQuizUserMetrics")
        Item item = table.getItem("id", userId)
        int timesPlayed = 0
        int correctCount = 0
        if (item != null) {
            timesPlayed = item.getInt("timesPlayed")
            correctCount = item.getInt("lifeTimeCorrect")
        }
        timesPlayed++
        correctCount += score
        Item newItem = new Item()
        newItem.withString("id", userId)
        newItem.withInt("timesPlayed", timesPlayed)
        newItem.withInt("lifeTimeCorrect", correctCount)
        table.putItem(newItem)
    }
}
