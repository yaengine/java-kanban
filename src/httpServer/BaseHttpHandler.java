package httpServer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class BaseHttpHandler  {
    protected final static Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    protected final static String ENDPOINT_UNKNOWN_ERR = "Такого эндпоинта не существует";
    protected final static String ILLEGAL_ID_ERR = "Некорректный id";
    protected final static String ILLEGAL_JSON_ERR = "Некорректный JSON";
    protected final static String TASK_ADD = "Задача успешно добавлена";
    protected final static String TASK_UPDATED = "Задача успешно обновлена";
    protected final static String TASK_DELETED = "Задача успешно удалена";

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
                if (pathParts.length == 2) {
                    return Endpoint.GET_EPICS;
                } else if (pathParts.length == 3) {
                    return Endpoint.GET_EPIC_BY_ID;
                } else if (pathParts.length == 4 && "subtasks".equals(pathParts[3])) {
                    return Endpoint.GET_EPIC_SUBTASKS;
                }
            } else if ("POST".equals(requestMethod)) {
                return Endpoint.POST_EPICS;
            } else if ("DELETE".equals(requestMethod)) {
                return  Endpoint.DELETE_EPIC;
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

        GsonBuilder gsonBuilder = new GsonBuilder().serializeNulls();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, localDateTimeTypeAdapter.nullSafe());
        gsonBuilder.registerTypeAdapter(Duration.class, localDurationTypeAdapter.nullSafe());
        Gson gson = gsonBuilder.create();

        return gson.toJson(task);
    }

    protected  <T> Optional<T> jsonToTask(String body, Type typeToken) {
        GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.registerTypeAdapter(LocalDateTime.class, localDateTimeTypeAdapter.nullSafe());
        gsonBuilder.registerTypeAdapter(Duration.class, localDurationTypeAdapter.nullSafe());
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


