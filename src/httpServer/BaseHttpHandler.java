package httpServer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.sun.net.httpserver.HttpExchange;
import task.Task;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class BaseHttpHandler  {
    protected final static Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    protected final static String ENDPOINT_UNKNOWN = "Такого эндпоинта не существует";

    protected LocalDateTimeTypeAdapter localDateTimeTypeAdapter = new LocalDateTimeTypeAdapter();
    protected LocalDurationTypeAdapter localDurationTypeAdapter = new LocalDurationTypeAdapter();

    protected void sendText(HttpExchange h, String text, int respCode) throws IOException {
        byte[] resp = text.getBytes(DEFAULT_CHARSET);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(respCode, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendNotFound(HttpExchange h, String text) throws IOException {
        sendText(h, text, 404);
    }

    protected void sendHasInteractions(HttpExchange h, String text) throws IOException {
        sendText(h, text, 406);
    }

    protected Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if ("tasks".equals(pathParts[1])) {
            if ("GET".equals(requestMethod)) {
                if (pathParts.length == 2) {
                    return Endpoint.GET_TASKS;
                } else if (pathParts.length == 3) {
                    return Endpoint.GET_TASK_BY_ID;
                }
            } else if ("POST".equals(requestMethod)) {
                return Endpoint.POST_TASKS;
            } else if ("DELETE".equals(requestMethod)) {
                return  Endpoint.DELETE_TASK;
            }
        } else if ("subtasks".equals(pathParts[1])) {
            if ("GET".equals(requestMethod)) {
                if (pathParts.length == 2) {
                    return Endpoint.GET_SUBTASKS;
                } else if (pathParts.length == 3) {
                    return Endpoint.GET_SUBTASK_BY_ID;
                }
            } else if ("POST".equals(requestMethod)) {
                return Endpoint.POST_SUBTASKS;
            } else if ("DELETE".equals(requestMethod)) {
                return  Endpoint.DELETE_SUBTASK;
            }
        } else if ("epics".equals(pathParts[1])) {
            if ("GET".equals(requestMethod)) {
                return Endpoint.GET_EPICS;
            } else if ("POST".equals(requestMethod)) {
                return Endpoint.POST_EPICS;
            } else if ("DELETE".equals(requestMethod)) {
                return  Endpoint.DELETE_EPICS;
            }
        } else if ("history".equals(pathParts[1])) {
            if ("GET".equals(requestMethod)) {
                return Endpoint.GET_HISTORY;
            }
        } else if ("prioritized".equals(pathParts[1])) {
            if ("GET".equals(requestMethod)) {
                return Endpoint.GET_PRIOR;
            }
        }

        return Endpoint.UNKNOWN;
    }

    protected String errToJson (String text) {
        return "{\"error\": \"" + text + "\"}";
    }

    protected String successToJson (String text) {
        return "{\"success\": \"" + text + "\"}";
    }

    protected Optional<Integer> getId (HttpExchange httpExchange) {
        String[] pathParts = httpExchange.getRequestURI().getPath().split("/");
        try {
            return Optional.of(Integer.parseInt(pathParts[2]));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    protected <T> String taskToJson (T task) {

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, localDateTimeTypeAdapter);
        gsonBuilder.registerTypeAdapter(Duration.class, localDurationTypeAdapter);
        Gson gson = gsonBuilder.create();

        return gson.toJson(task);
    }

    protected  <T> Optional<T> JsonToTask (InputStream bodyInputStream, Type typeToken) throws IOException {
        String body = new String(bodyInputStream.readAllBytes(), DEFAULT_CHARSET);
        GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.registerTypeAdapter(LocalDateTime.class, localDateTimeTypeAdapter);
        gsonBuilder.registerTypeAdapter(Duration.class, localDurationTypeAdapter);
        Gson gson = gsonBuilder.create();
        return Optional.of(gson.fromJson(body, typeToken));
    }
}

class LocalDateTimeTypeAdapter extends TypeAdapter<LocalDateTime> {
    private final DateTimeFormatter timeFormatter =  DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    @Override
    public void write(final JsonWriter jsonWriter, final LocalDateTime localDateTime) throws IOException {
        jsonWriter.value(localDateTime.format(timeFormatter));
    }

    @Override
    public LocalDateTime read(final JsonReader jsonReader) throws IOException {
        return LocalDateTime.parse(jsonReader.nextString(), timeFormatter);
    }
}

class LocalDurationTypeAdapter extends TypeAdapter<Duration> {

    @Override
    public void write(JsonWriter jsonWriter, Duration duration) throws IOException {
        jsonWriter.value(duration.toMinutes());
    }

    @Override
    public Duration read(JsonReader jsonReader) throws IOException {
        return Duration.ofMinutes(jsonReader.nextLong());
    }
}


