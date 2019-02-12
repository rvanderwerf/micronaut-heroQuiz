# Micronaut Star Wars Quiz

This is a demo app ported from the ASK v1 Hero Quiz/Star Wars Quiz that worked with Groovy/Lazybones/Lambda and Grails.
Make sure your ~/.aws/credentials is configured, and appropriate lambda service roles are created as well.

This app requires a database table set up in DynamoDB with the quiz questions. See the old app at [https://github.com/rvanderwerf/heroQuiz/tree/starwarsquiz](https://github.com/rvanderwerf/heroQuiz/tree/starwarsquiz)
to configure the tables.

The application can be build with either Gradle (which builds the JAR to the `build/libs` directory):

```bash
./gradlew shadowJar
```

Or Maven which builds the JAR to the `target` directory:

```bash
./mvnw package
```

Follow the instructions in [the tutorial](https://alexa-skills-kit-sdk-for-java.readthedocs.io/en/latest/Developing-Your-First-Skill.html) for how to deploy the Alexa Skill.