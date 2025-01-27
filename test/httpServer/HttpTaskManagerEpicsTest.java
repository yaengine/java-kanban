package httpServer;

import com.google.gson.reflect.TypeToken;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Task;
import task.TaskStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static httpServer.BaseHttpHandler.TASK_UPDATED;
import static manager.InMemoryTaskManager.*;
import static org.junit.jupiter.api.Assertions.*;
import static util.TestConstants.*;

public class HttpTaskManagerEpicsTest {

    // создаём экземпляр InMemoryTaskManager
    TaskManager manager = new InMemoryTaskManager();
    // передаём его в качестве аргумента в конструктор HttpTaskServer
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    BaseHttpHandler baseHttpHandler= new BaseHttpHandler();

    public HttpTaskManagerEpicsTest() {
    }

    @BeforeEach
    public void setUp() throws IOException {
        manager.removeAll();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testGetEpics() throws IOException, InterruptedException {
        // создаём задачу
        Epic epic = new Epic(NEW_EPIC_NAME, NEW_EPIC_DESC, TaskStatus.NEW);
        manager.addEpic(epic);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверяем, что вернулась одна задача с корректным именем
        List<Epic> epicsListFromHttpServer = (List<Epic>) baseHttpHandler.jsonToTask(response.body(),
                new ListEpicsTypeToken().getType()).get();

        assertNotNull(epicsListFromHttpServer, TASK_NOT_RETURN_ERR);
        assertEquals(1, epicsListFromHttpServer.size(), INCORRECT_NUM_OF_TASK_ERR);
        assertEquals(NEW_EPIC_NAME, epicsListFromHttpServer.get(0).getName(), INCORRECT_TASK_NAME_ERR);
    }

    @Test
    public void testGetEpicById() throws IOException, InterruptedException {
        // создаём задачу
        Epic epic = new Epic(NEW_EPIC_NAME, NEW_EPIC_DESC, TaskStatus.NEW);
        int taskId = manager.addEpic(epic).getTaskId();

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + taskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверяем, что вернулась одна задача с корректным именем
        Task epicFromHttpServer = (Epic) baseHttpHandler.jsonToTask(response.body(),
               new EpicTypeToken().getType()).get();

        assertNotNull(epicFromHttpServer, TASK_NOT_RETURN_ERR);
        assertEquals(1, epicFromHttpServer.getTaskId(), INCORRECT_ID_OF_TASK_ERR);
        assertEquals(NEW_EPIC_NAME, epicFromHttpServer.getName(), INCORRECT_TASK_NAME_ERR);
    }

    @Test
    public void testGetEpicByIdWhenTaskIsNotExists() throws IOException, InterruptedException {

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(404, response.statusCode());
        assertEquals(baseHttpHandler.errToJson(EPIC_NOT_FOUND_ERR), response.body(), INCORRECT_RESP_WHEN_TASK_NOT_FOUND);
    }

    @Test
    public void testAddEpic() throws IOException, InterruptedException {
        // создаём задачу
        Epic epic = new Epic(NEW_EPIC_NAME, NEW_EPIC_DESC, TaskStatus.NEW);
        // конвертируем её в JSON
        String taskJson = baseHttpHandler.taskToJson(epic);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создалась одна задача с корректным именем
        List<Epic> epicsFromManager = manager.getEpics();

        assertNotNull(epicsFromManager, TASK_NOT_RETURN_ERR);
        assertEquals(1, epicsFromManager.size(), INCORRECT_NUM_OF_TASK_ERR);
        assertEquals(NEW_EPIC_NAME, epicsFromManager.get(0).getName(), INCORRECT_TASK_NAME_ERR);
    }

    @Test
    public void testDeleteEpic() throws IOException, InterruptedException {
        // создаём задачу
        Epic epic = new Epic(NEW_EPIC_NAME, NEW_EPIC_DESC, TaskStatus.NEW);
        int taskId = manager.addEpic(epic).getTaskId();

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + taskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());

        List<Epic> epicsFromManager = manager.getEpics();
        assertTrue(epicsFromManager.isEmpty(), TASK_RETURN_ERR);
    }
}

class ListEpicsTypeToken extends TypeToken<List<Epic>> {
}
class EpicTypeToken extends TypeToken<Epic> {
}