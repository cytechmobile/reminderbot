package gr.cytech.chatreminderbot.rest.GoogleCards;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Creates a card response to a Hangouts Chat message, in JSON format.
 *
 * See the documentation for more details:
 * https://developers.google.com/hangouts/chat/reference/message-formats/cards
 */
public class CardResponseBuilder {

    private JsonObject headerNode;
    private JsonObjectBuilder thread;
    private JsonObjectBuilder responseNode;
    private JsonArrayBuilder widgetsArray;
    private JsonArrayBuilder cardsArray;

    /**
     * Default public constructor.
     */
    public CardResponseBuilder() {
        this.thread = Json.createObjectBuilder();
        this.responseNode = Json.createObjectBuilder();
        this.cardsArray = Json.createArrayBuilder();
        this.widgetsArray = Json.createArrayBuilder();
    }

    /**
     * Adds a TextParagraph widget to the card response.
     *
     * @param message the message in the text paragraph
     * @return this CardResponseBuilder
     */
    public CardResponseBuilder textParagraph(String message) {
        this.widgetsArray.add(Json.createObjectBuilder()
                .add("textParagraph", Json.createObjectBuilder()
                        .add("text", message)));
        return this;
    }

    /**
     * Adds a Text Button widget to the card response.
     *
     * When clicked, the button opens a link in the user's browser.
     *
     * @param text the text on the button
     * @param redirectUrl the link to open
     * @return this CardResponseBuilder
     */
    public CardResponseBuilder textButton(String text, String redirectUrl) {
        this.widgetsArray.add(Json.createObjectBuilder()
                .add("buttons", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("textButton", Json.createObjectBuilder()
                                        .add("text", text)
                                        .add("onClick", Json.createObjectBuilder()
                                                .add("openLink", Json.createObjectBuilder()
                                                        .add("url", redirectUrl)))))));
        return this;
    }

    public CardResponseBuilder thread(String name) {
        this.thread.add("name", name);
        return this;
    }

    /**
     * Builds the card response and returns a JSON object node.
     *
     * @return card response as JSON-formatted string
     */

    public String build() {

        // If you want your header to appear before all other cards,
        // you must add it to the `cards` array as the first / 0th item.
        if (this.headerNode != null) {
            this.cardsArray.add(this.headerNode);
        }

        JsonObject cardsNode =
                responseNode.add("cards", this.cardsArray
                        .add(Json.createObjectBuilder()
                                .add("sections", Json.createArrayBuilder()
                                        .add(Json.createObjectBuilder()
                                                .add("widgets", this.widgetsArray)))))
                        .add("thread",this.thread)
                        .build();
        return cardsNode.toString();
    }

}