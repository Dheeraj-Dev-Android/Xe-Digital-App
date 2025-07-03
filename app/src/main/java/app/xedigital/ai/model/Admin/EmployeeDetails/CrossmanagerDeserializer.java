package app.xedigital.ai.model.Admin.EmployeeDetails;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class CrossmanagerDeserializer implements JsonDeserializer<Crossmanager> {
    @Override
    public Crossmanager deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonObject()) {
            return new Gson().fromJson(json, Crossmanager.class);
        } else if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
            String id = json.getAsString();
            Crossmanager cm = new Crossmanager();
            cm.setId(id);
            return cm;
        }
        return null;
    }
}