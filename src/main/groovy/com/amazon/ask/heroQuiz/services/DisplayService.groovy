package com.amazon.ask.heroQuiz.services

import com.amazon.ask.dispatcher.request.handler.HandlerInput
import com.amazon.ask.model.Response
import com.amazon.ask.model.Session
import com.amazon.ask.model.interfaces.display.BodyTemplate1
import com.amazon.ask.model.interfaces.display.Image
import com.amazon.ask.model.interfaces.display.ImageInstance
import com.amazon.ask.model.interfaces.display.RichText
import com.amazon.ask.model.interfaces.display.Template
import com.amazon.ask.model.interfaces.display.TextContent
import com.amazon.ask.model.interfaces.system.SystemState

import javax.inject.Singleton

@Singleton
class DisplayService {

    boolean isSupportDisplay(Session session) {
        boolean supportDisplay = false

        if (session?.attributes?.containsKey("supportDisplay")) {
            supportDisplay = (Boolean) session.attributes.get("supportDisplay")
        }
        supportDisplay
    }

    Optional<Response> askResponse(HandlerInput input, String cardText, String speechText, boolean supportDisplay) {


        if (supportDisplay) {
            input.getResponseBuilder()
                    .withSpeech(speechText)
                    .withReprompt(speechText)
                    .addRenderTemplateDirective(buildBodyTemplate1(cardText))
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

    Optional<Response> tellResponse(HandlerInput input, String cardText, String speechText, boolean supportDisplay) {


        if (supportDisplay) {
            input.getResponseBuilder()
                    .withSpeech(speechText)
                    .addRenderTemplateDirective(buildBodyTemplate1(cardText))
                    .withSimpleCard(speechText, speechText)
                    .build()
        } else {
            input.getResponseBuilder()
                    .withSpeech(speechText)
                    .withSimpleCard(speechText, speechText)
                    .build()
        }

    }

    Template buildBodyTemplate1(String cardText) {

        return BodyTemplate1.builder()
                .withBackgroundImage(getImageInstance("https://media.giphy.com/media/YJNOIvcwG1NcY/giphy.gif"))
                .withTitle("Unofficial Star Wars Quiz")
                .withTextContent(getTextContent(cardText, cardText))
                .build();

    }

    Image getImageInstance(String imageUrl) {
        List<ImageInstance> instances = new ArrayList<>();
        ImageInstance instance = ImageInstance.builder()
                .withUrl(imageUrl)
                .build();
        instances.add(instance);
        instances
        Image.builder()
                .withSources(instances).build()
    }

    TextContent getTextContent(String primaryText, String secondaryText) {
        return TextContent.builder()
                .withPrimaryText(makeRichText(primaryText))
                .withSecondaryText(makeRichText(secondaryText))
                .build();
    }

    RichText makeRichText(String text) {
        return RichText.builder()
                .withText(text)
                .build();
    }
}
