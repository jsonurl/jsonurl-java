package org.jsonurl.example1;

import java.io.IOException;
import java.util.Collections;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonBuilderFactory;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import org.jsonurl.JsonUrlStringBuilder;
import org.jsonurl.jsonp.JsonUrlParser;
import org.jsonurl.jsonp.JsonUrlWriter;

public class Main {
    
    /**
     * Return an example value.
     */
    public static final JsonArray getExampleValue() {
        JsonBuilderFactory jbf = Json.createBuilderFactory(
                Collections.emptyMap());

        return jbf.createArrayBuilder()
                .add(jbf.createObjectBuilder()
                    .add("id", 1)
                    .add("name", jbf.createObjectBuilder()
                            .add("firstName", "Montgomery")
                            .add("lastName", "Burns")
                            .add("evil", true)
                            .add("age", JsonObject.NULL)))
                .add(jbf.createObjectBuilder()
                        .add("id", 2)
                        .add("name", jbf.createObjectBuilder()
                                .add("firstName", "Homer")
                                .add("lastName", "Simpson")
                                .add("evil", false)
                                .add("age", 36)
                                .add("oddKeyName=", false)
                                .add("oddKeyName&", true)
                                .add("101.5", 101.5)
                                .add("1e1", 1e1)
                                .add("15e+1", 15e+1)
                                .add("16e-1", 16e-1)
                                .add("\"text\"", "funny+key")))
                .build();
    }

    /**
     * Serialize the given JsonArray and return a String.
     */
    public static final String getExampleValueText(JsonArray value) throws IOException {
        JsonUrlStringBuilder text = new JsonUrlStringBuilder();
        JsonUrlWriter.write(text, value);
        return text.build();
    }

    /**
     * OS entry point.
     */
    public static final void main(String[] args)
            throws JsonException, IOException {

        //
        // get my example value as a POJO
        //
        JsonArray value = getExampleValue();
        
        //
        // turn that into text
        //
        String jsonUrlText = getExampleValueText(value);

        System.out.println(jsonUrlText);

        //
        // parse the text and build a JsonArray
        //
        JsonUrlParser p = new JsonUrlParser();
        JsonValue parsedValue = p.parse(jsonUrlText);

        JsonWriter jout = Json.createWriter(System.out);
        jout.write(parsedValue);
    }
}
