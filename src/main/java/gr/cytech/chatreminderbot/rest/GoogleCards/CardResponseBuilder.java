package gr.cytech.chatreminderbot.rest.GoogleCards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CardResponseBuilder {

    private interface Builder {
        Object get();
    }

    private ObjectBuilder createObjectBuilder() {
        return new ObjectBuilder();
    }

    private static class ObjectBuilder implements Builder {
        Map<String, Object> map = new HashMap<>(5);

        ObjectBuilder add(String key, Object value) {
            map.put(key, value);
            return this;
        }

        ObjectBuilder add(String key, Builder builder) {
            return add(key, builder.get());
        }

        @Override
        public Map<String, Object> get() {
            return map;
        }
    }

    private ArrayBuilder createArrayBuilder() {
        return new ArrayBuilder();
    }

    private static class ArrayBuilder implements Builder {
        List<Object> list = new ArrayList<>(4);

        ArrayBuilder add(Builder builder) {
            list.add(builder.get());
            return this;
        }

        @Override
        public List<Object> get() {
            return list;
        }
    }

    private ObjectBuilder thread;
    private ObjectBuilder headerNode;
    private ObjectBuilder responseNode;
    private ArrayBuilder widgetsArray;
    private ArrayBuilder cardsArray;

    /**
     * Default public constructor.
     */
    public CardResponseBuilder() {
        this.thread = createObjectBuilder();
        this.responseNode = createObjectBuilder();
        this.cardsArray = createArrayBuilder();
        this.widgetsArray = createArrayBuilder();
    }

    /**
     * Adds a TextParagraph widget to the card response.
     *
     * @param message the message in the text paragraph
     * @return this CardResponseBuilder
     */
    public CardResponseBuilder textParagraph(String message) {
        this.widgetsArray.add(
                createObjectBuilder()
                        .add("textParagraph",
                                createObjectBuilder().add("text", message)));
        return this;
    }

    public CardResponseBuilder textButton(String text, String redirectUrl) {
        this.widgetsArray.add(createObjectBuilder()
                .add("buttons", createArrayBuilder()
                        .add(createObjectBuilder()
                                .add("textButton", createObjectBuilder()
                                        .add("text", text)
                                        .add("onClick", createObjectBuilder()
                                                .add("openLink", createObjectBuilder()
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
    public Object build() {

        // If you want your header to appear before all other cards,
        // you must add it to the `cards` array as the first / 0th item.
        if (this.headerNode != null) {
            this.cardsArray.add(this.headerNode);
        }

        return responseNode.add("cards", this.cardsArray
                .add(createObjectBuilder()
                        .add("sections", createArrayBuilder()
                                .add(createObjectBuilder()
                                        .add("widgets", this.widgetsArray))))).add("thread",this.thread)
                .get();
    }

}

