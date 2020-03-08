package org.jsonurl.example1;

import java.io.IOException;
import java.util.Collections;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonBuilderFactory;
import javax.json.JsonException;
import javax.json.JsonWriter;
import org.jsonurl.JsonUrlStringBuilder;
import org.jsonurl.jsonp.JsonUrlParser;
import org.jsonurl.jsonp.JsonUrlWriter;

public class Main {

    /**
     * OS entry point.
     */
    public static final void main(String[] args)
            throws JsonException, IOException {

        JsonBuilderFactory jbf = Json.createBuilderFactory(
                Collections.emptyMap());

        JsonArray jo = jbf.createArrayBuilder()
                .add(jbf.createObjectBuilder()
                    .add("id", 1)
                    .add("name", jbf.createObjectBuilder()
                            .add("firstName", "Montgomery")
                            .add("lastName", "Burns")))
                .add(jbf.createObjectBuilder()
                        .add("id", 2)
                        .add("name", jbf.createObjectBuilder()
                                .add("firstName", "Homer")
                                .add("lastName", "Simpson")))
                .build();

        JsonUrlStringBuilder text = new JsonUrlStringBuilder();

        JsonUrlWriter.write(text, jo);
        String jsonUrl = text.build();

        System.out.println(jsonUrl);
        
        JsonUrlParser p = new JsonUrlParser();
        JsonWriter jout = Json.createWriter(System.out);
        jout.write(p.parse(jsonUrl));
    }
}
