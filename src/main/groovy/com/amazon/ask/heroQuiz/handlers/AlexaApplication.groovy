package com.amazon.ask.heroQuiz.handlers

// tag::imports[]
import com.amazon.ask.dispatcher.request.handler.HandlerInput
import com.amazon.ask.heroQuiz.Question
import com.amazon.ask.heroQuiz.services.AnswerService
import com.amazon.ask.heroQuiz.services.DisplayService
import com.amazon.ask.heroQuiz.services.QuestionService
import com.amazon.ask.model.Intent
import com.amazon.ask.model.IntentRequest
import com.amazon.ask.model.Response
import com.amazon.ask.model.Session
import com.amazon.ask.model.Slot
import com.amazon.ask.model.interfaces.display.BodyTemplate1
import com.amazon.ask.model.interfaces.display.Image
import com.amazon.ask.model.interfaces.display.ImageInstance
import com.amazon.ask.model.interfaces.display.RenderTemplateDirective
import com.amazon.ask.model.interfaces.display.RichText
import com.amazon.ask.model.interfaces.display.Template
import com.amazon.ask.model.interfaces.display.TextContent
import com.amazon.ask.model.ui.PlainTextOutputSpeech
import com.amazon.ask.model.ui.Reprompt
import com.amazon.ask.model.ui.SimpleCard
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.function.aws.alexa.AlexaIntents
import io.micronaut.function.aws.alexa.annotation.IntentHandler
import javax.inject.Singleton
// end::imports[]

// tag::class[]
@Singleton // <1>
@CompileStatic
@Slf4j
class AlexaApplication {

    private final DisplayService displayService

    private final QuestionService questionService

    private final AnswerService answerService

    AlexaApplication(DisplayService displayService,
                     QuestionService questionService,
                     AnswerService answerService) { // <2>
        this.displayService = displayService
        this.questionService = questionService
        this.answerService = answerService
    }

// end::class[]

    @IntentHandler(AlexaIntents.HELP)
    Optional<Response> help(HandlerInput input) {
        String speechText = "You can say hello to me!"
        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("HelloWorld", speechText)
                .withReprompt(speechText)
                .build()
    }

    @IntentHandler(AlexaIntents.FALLBACK)
    Optional<Response> fallback(HandlerInput input) {
        String speechText = "Sorry, I don't know that. You can say try saying help!"
        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("HelloWorld", speechText)
                .withReprompt(speechText)
                .build()
    }

    @IntentHandler([AlexaIntents.CANCEL, AlexaIntents.STOP])
    Optional<Response> cancel(HandlerInput input) {
        return input.getResponseBuilder()
                .withSpeech("Goodbye")
                .withSimpleCard("HelloWorld", "Goodbye")
                .build()
    }


    @IntentHandler("AnswerIntent")
    public Optional<Response> answerIntent(HandlerInput input) {
        log.debug("inside answer intent")
        Intent intent = ((IntentRequest) input.getRequestEnvelope().getRequest()).getIntent()
        Session session = input.getRequestEnvelope().getSession()
        Slot query = intent.getSlots().get("Answer")
        log.debug("raw answer ${query.name}:${query.value}")
        try {
            int guessedAnswer = Integer.parseInt(query.getValue())
            log.info("Guessed answer is:  " + query.getValue())

            return answerService.processAnswer(input,session, guessedAnswer, displayService.isSupportDisplay(session))
        } catch (NumberFormatException n) {
            return questionService.repeatQuestion(input, session, displayService.isSupportDisplay(session), true)
        }
    }

    @IntentHandler("RepeatIntent")
    public Optional<Response> repeatIntent(HandlerInput input) {
        Intent intent = ((IntentRequest) input.getRequestEnvelope().getRequest()).getIntent()
        Session session = input.getRequestEnvelope().getSession()
        Question question = (Question) session.attributes.get("lastQuestionAsked")
        String speechText = ""

        speechText += question.getSpeechText()
        askResponse(input,speechText, speechText, isSupportDisplay(session))


    }

    @IntentHandler("DontKnowIntent")
    public Optional<Response> dontKnowIntent(HandlerInput input) {

    }

    boolean isSupportDisplay(Session session) {
        boolean supportDisplay = (Boolean) session.attributes.get("supportDisplay")
        supportDisplay
    }

    Optional<Response> askResponse(HandlerInput input, String cardText, String speechText, boolean supportDisplay) {


        if (supportDisplay) {
            input.getResponseBuilder()
                    .withSpeech(speechText)
                    .withReprompt(speechText)
                    .addRenderTemplateDirective(displayService.buildBodyTemplate1(cardText))
                    .withSimpleCard(speechText, speechText)
                    .build()
        } else {
            input.getResponseBuilder()
                    .withSpeech(speechText)
                    .withReprompt(speechText)
                    .withSimpleCard(speechText, speechText)
                    .build()
        }

    }



}
