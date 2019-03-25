package com.amazon.ask.heroQuiz.services

import com.amazon.ask.dispatcher.request.handler.HandlerInput
import com.amazon.ask.heroQuiz.Question
import com.amazon.ask.model.Response
import com.amazon.ask.model.Session
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.Table
import com.amazonaws.services.dynamodbv2.model.ScanRequest
import com.amazonaws.services.dynamodbv2.model.ScanResult
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Value

import javax.annotation.PostConstruct
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Slf4j
class QuestionService {
    DynamoDB dynamoDB = new DynamoDB(AmazonDynamoDBClientBuilder.defaultClient())
    int tableRowCount

    @Inject DisplayService displayService
    @Value('${quiz.numberOfQuestions:5}')
    int numberOfQuestions

    @PostConstruct
    void init(){
        dynamoDB = new DynamoDB(AmazonDynamoDBClientBuilder.defaultClient())
        AmazonDynamoDBClient amazonDynamoDBClient
        amazonDynamoDBClient = new AmazonDynamoDBClient()
        ScanRequest req = new ScanRequest()
        req.setTableName("StarWarsQuiz")
        ScanResult result = amazonDynamoDBClient.scan(req)
        List quizItems = result.items
        tableRowCount = quizItems.size()
        log.info("This many rows in the table:  " + tableRowCount)
    }


    Question getQuestion(int questionIndex) {

        Table table = dynamoDB.getTable("StarWarsQuiz")
        Item item = table.getItem("Id", questionIndex)
        def questionText = item.getString("Question")
        def questionAnswer = item.getInt("answer")
        def options = new String[4]
        options[0] = item.getString("option1")
        options[1] = item.getString("option2")
        options[2] = item.getString("option3")
        options[3] = item.getString("option4")
        Question question = new Question()
        question.setQuestion(questionText)
        question.setOptions(options)
        question.setAnswer(questionAnswer)
        question.setIndex(questionIndex)
        log.info("question retrieved index:  " + question.getIndex())
        log.info("question retrieved text:SpeechletResponse  " + question.getQuestion())
        log.info("question retrieved correct:  " + question.getAnswer())
        log.info("question retrieved number of options:  " + question.getOptions().length)
        question
    }

    Question getRandomQuestion(Session session) {
        //int tableRowCount = Integer.parseInt((String) session.getAttribute("tableRowCount"))
        int questionIndex = (new Random().nextInt() % tableRowCount).abs()
        log.info("The question index is:  " + questionIndex)
        Question question = getQuestion(questionIndex)
        question
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

    int decrementQuestionCounter(Session session) {
        log.debug("session attributes=${session.attributes}")

        int questionCounter = numberOfQuestions
        if (session.attributes?.containsKey("questionCounter")) {
            questionCounter = (int) session.attributes.get("questionCounter")
        }

        questionCounter--
        session.attributes.put("questionCounter", questionCounter)
        questionCounter

    }

    Question getRandomUnaskedQuestion(Session session) {
        LinkedHashMap<String, Question> askedQuestions = (LinkedHashMap) session.attributes.get("askedQuestions")
        Question question = getRandomQuestion(session)
        while(askedQuestions.get(question.getQuestion()) != null) {
            question = getRandomQuestion(session)
        }
        askedQuestions.put(question.getQuestion(), question)
        session.attributes.put("askedQuestions", askedQuestions)
        question
    }

    Optional<Response> repeatQuestion(HandlerInput input, final Session session, boolean supportDisplay, boolean invalidAnswer) {
        Question question = (Question) session.getAttribute("lastQuestionAsked")
        String speechText = ""
        if(invalidAnswer) {
            speechText = "I didn't understand that.  Let's try again.\n\n"
        }
        speechText += question.getSpeechText()
        displayService.askResponse(input, speechText, speechText, supportDisplay)

    }
}
