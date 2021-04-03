package org.jsonurl.example2;

import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsonurl.jsonorg.JsonUrlStringBuilder;
import org.jsonurl.jsonorg.JsonUrlParser;
import org.jsonurl.jsonorg.JsonUrlWriter;

public class Main {
    
    /**
     * Return an example value.
     */
    public static final JSONArray getExampleValue() {
        return new JSONArray()
                .put(new JSONObject()
                    .put("id", 1)
                    .put("name", new JSONObject()
                            .put("firstName", "Montgomery")
                            .put("lastName", "Burns")
                            .put("evil", true)
                            .put("age", JSONObject.NULL)))
                .put(new JSONObject()
                        .put("id", 2)
                        .put("name", new JSONObject()
                                .put("firstName", "Homer")
                                .put("lastName", "Simpson")
                                .put("evil", false)
                                .put("age", 36)
                                .put("oddKeyName=", false)
                                .put("oddKeyName&", true)
                                .put("101.5", 101.5)
                                .put("1e1", 1e1)
                                .put("15e+1", 15e+1)
                                .put("16e-1", 16e-1)
                                .put("\"text\"", "funny+key")));
    }

    /**
     * Serialize the given JsonArray and return a String.
     */
    public static final String getExampleValueText(JSONArray value) throws IOException {
        JsonUrlStringBuilder text = new JsonUrlStringBuilder();
        JsonUrlWriter.write(text, value);
        return text.build();
    }

    /**
     * OS entry point.
     */
    public static final void main(String[] args) throws IOException {

        //
        // get my example value as a POJO
        //
        JSONArray value = getExampleValue();
        
        //
        // turn that into text
        //
        String jsonUrlText = getExampleValueText(value);

        System.out.println(jsonUrlText);

        //
        // parse the text and build a JsonArray
        //
        JsonUrlParser p = new JsonUrlParser();
        JSONArray parsedValue = p.parseArray(jsonUrlText);
        //parsedValue.write(new PrintWriter(System.out));
        System.out.println(parsedValue.toString());
    }
}
