package org.jsonurl.example2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import org.json.JSONArray;
import org.jsonurl.example2.Main;
import org.jsonurl.jsonorg.JsonUrlStringBuilder;
import org.jsonurl.jsonorg.JsonUrlParser;
import org.jsonurl.jsonorg.JsonUrlWriter;
import org.junit.jupiter.api.Test;

class Example2Test {

    @Test
    void test() throws IOException {
        //
        // get my example value as a POJO
        //
        JSONArray value = Main.getExampleValue();

        //
        // turn that into JSON-&gt;URL text
        //
        JsonUrlStringBuilder text = new JsonUrlStringBuilder();
        JsonUrlWriter.write(text, value);
        String jsonUrlText = text.build();

        //
        // parse the JSON-&gt;URL text and build a JsonArray
        //
        JsonUrlParser p = new JsonUrlParser();
        JSONArray parsedValue = p.parseArray(jsonUrlText);
        
        //
        // I'm testing the string values because JSONArray does not override
        // the equals() method so the test fails even though the values are
        // semantically equivalent.
        //
        assertEquals(value.toString(), parsedValue.toString());
    }
}
