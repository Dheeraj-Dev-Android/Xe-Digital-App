package app.xedigital.ai.utills;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

import app.xedigital.ai.model.profile.Crossmanager;

public class CrossmanagerTypeAdapter extends TypeAdapter<Crossmanager> {
    @Override
    public Crossmanager read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.STRING) {
            return new Crossmanager(reader.nextString(), null, null, null);
        } else if (reader.peek() == JsonToken.BEGIN_OBJECT) {
            reader.beginObject();
            String id = null;
            String firstName = null;
            String lastName = null;
            String email = null;
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("_id")) {
                    id = reader.nextString();
                } else if (name.equals("firstname")) {
                    firstName = reader.nextString();
                } else if (name.equals("lastname")) {
                    lastName = reader.nextString();
                } else if (name.equals("email")) {
                    email = reader.nextString();
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
            return new Crossmanager(id, firstName, lastName, email);
        } else {
            reader.skipValue();
            return null;
        }
    }

    @Override
    public void write(JsonWriter writer, Crossmanager value) throws IOException {
        if (value == null) {
            writer.nullValue();
            return;
        }
        writer.beginObject();
        writer.name("_id").value(value.getId());
        writer.name("firstname").value(value.getFirstname());
        writer.name("lastname").value(value.getLastname());
        writer.name("email").value(value.getEmail());
        writer.endObject();
    }

}