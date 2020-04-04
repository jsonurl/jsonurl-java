package org.jsonurl.example1;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonValue;
import org.jsonurl.JsonUrlStringBuilder;
import org.jsonurl.example1.Main;
import org.jsonurl.jsonp.JsonUrlParser;
import org.jsonurl.jsonp.JsonUrlWriter;
import org.junit.jupiter.api.Test;

class Example1Test {

    @Test
    void test() throws JsonException, IOException {
        //
        // get my example value as a POJO
        //
        JsonArray value = Main.getExampleValue();

        //
        // turn that into JSON->URL text
        //
        JsonUrlStringBuilder text = new JsonUrlStringBuilder();
        JsonUrlWriter.write(text, value);
        String jsonUrlText = text.build();

        //
        // parse the JSON->URL text and build a JsonArray
        //
        JsonUrlParser p = new JsonUrlParser();
        JsonValue parsedValue = p.parse(jsonUrlText);
        
        //
        // it should be the same as the original
        //
        assertEquals(value, parsedValue);
    }
}
