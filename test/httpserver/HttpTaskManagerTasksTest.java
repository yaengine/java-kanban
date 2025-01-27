package httpserver;

import com.google.gson.reflect.TypeToken;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
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

import static httpserver.BaseHttpHandler.TASK_UPDATED;
import static manager.InMemoryTaskManager.TASK_CROSS_ERROR;
import static manager.InMemoryTaskManager.TASK_NOT_FOUND_ERR;
import static org.junit.jupiter.api.Assertions.*;
import static util.TestConstants.*;

public class HttpTaskManagerTasksTest {
    
    // создаём экземпляр InMemoryTaskManager
    TaskManager manager = new InMemoryTaskManager();
    // передаём его в качестве аргумента в конструктор HttpTaskServer
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    BaseHttpHandler baseHttpHandler= new BaseHttpHandler();

    public HttpTaskManagerTasksTest() {
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
    public void testGetTask() throws IOException, InterruptedException {
        // создаём задачу
        Task task = new Task(NEW_TASK_NAME, NEW_TASK_DESC,
                TaskStatus.NEW, LocalDateTime.now(),Duration.ofMinutes(5));
        manager.addTask(task);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверяем, что вернулась одна задача с корректным именем
        List<Task> tasksListFromHttpServer = (List<Task>) baseHttpHandler.jsonToTask(response.body(),
                new ListTaskTypeToken().getType()).get();

        assertNotNull(tasksListFromHttpServer, TASK_NOT_RETURN_ERR);
        assertEquals(1, tasksListFromHttpServer.size(), INCORRECT_NUM_OF_TASK_ERR);
        assertEquals(NEW_TASK_NAME, tasksListFromHttpServer.get(0).getName(), INCORRECT_TASK_NAME_ERR);
    }

    @Test
    public void testGetTaskById() throws IOException, InterruptedException {
        // создаём задачу
        Task task = new Task(NEW_TASK_NAME, NEW_TASK_DESC,
                TaskStatus.NEW, LocalDateTime.now(),Duration.ofMinutes(5));
        int taskId = manager.addTask(task).getTaskId();

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + taskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверяем, что вернулась одна задача с корректным именем
        Task taskFromHttpServer = (Task) baseHttpHandler.jsonToTask(response.body(),
               new TaskTypeToken().getType()).get();

        assertNotNull(taskFromHttpServer, TASK_NOT_RETURN_ERR);
        assertEquals(1, taskFromHttpServer.getTaskId(), INCORRECT_ID_OF_TASK_ERR);
        assertEquals(NEW_TASK_NAME, taskFromHttpServer.getName(), INCORRECT_TASK_NAME_ERR);
    }

    @Test
    public void testGetTaskByIdWhenTaskIsNotExists() throws IOException, InterruptedException {

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(404, response.statusCode());
        assertEquals(baseHttpHandler.errToJson(TASK_NOT_FOUND_ERR), response.body(), INCORRECT_RESP_WHEN_TASK_NOT_FOUND);
    }

    @Test
    public void testAddTask() throws IOException, InterruptedException {
        // создаём задачу
        Task task = new Task(NEW_TASK_NAME, NEW_TASK_DESC,
                TaskStatus.NEW, LocalDateTime.now(),Duration.ofMinutes(5));
        // конвертируем её в JSON
        String taskJson = baseHttpHandler.taskToJson(task);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создалась одна задача с корректным именем
        List<Task> tasksFromManager = manager.getTasks();

        assertNotNull(tasksFromManager, TASK_NOT_RETURN_ERR);
        assertEquals(1, tasksFromManager.size(), INCORRECT_NUM_OF_TASK_ERR);
        assertEquals(NEW_TASK_NAME, tasksFromManager.get(0).getName(), INCORRECT_TASK_NAME_ERR);
    }

    @Test
    public void testAddTaskWithId() throws IOException, InterruptedException {
        Task task = new Task(NEW_TASK_NAME, NEW_TASK_DESC,
                TaskStatus.NEW, LocalDateTime.now(),Duration.ofMinutes(5));
        manager.addTask(task);
        Task updTask = new Task(NEW_TASK_NAME, NEW_TASK_DESC,
                TaskStatus.NEW, 1, LocalDateTime.now(),Duration.ofMinutes(5));
        // конвертируем её в JSON
        String taskJson = baseHttpHandler.taskToJson(updTask);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        // вызываем рест, отвечающий за апдейт задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создалась одна задача с корректным именем
        List<Task> tasksFromManager = manager.getTasks();

        assertNotNull(tasksFromManager, TASK_NOT_RETURN_ERR);
        assertEquals(1, tasksFromManager.size(), INCORRECT_NUM_OF_TASK_ERR);
        assertEquals(baseHttpHandler.successToJson(TASK_UPDATED), response.body(),
                INCORRECT_RESP_WHEN_UPD_TASK);
        assertEquals(NEW_TASK_NAME, tasksFromManager.get(0).getName(), INCORRECT_TASK_NAME_ERR);
    }

    @Test
    public void testAddTaskWithHasInteractions() throws IOException, InterruptedException {
        // создаём задачу
        Task task = new Task(NEW_TASK_NAME, NEW_TASK_DESC,
                TaskStatus.NEW, LocalDateTime.now(),Duration.ofMinutes(5));
        manager.addTask(task);
        Task updTask = new Task(NEW_TASK_NAME, NEW_TASK_DESC,
                TaskStatus.NEW, LocalDateTime.now().plusMinutes(1),Duration.ofMinutes(5));
        // конвертируем её в JSON
        String taskJson = baseHttpHandler.taskToJson(updTask);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(406, response.statusCode());

        assertEquals(baseHttpHandler.errToJson(TASK_CROSS_ERROR), response.body(),
                "Некорректный ответ, при обновлении таски с пересечением");
    }

    @Test
    public void testDeleteTask() throws IOException, InterruptedException {
        // создаём задачу
        Task task = new Task(NEW_TASK_NAME, NEW_TASK_DESC,
                TaskStatus.NEW, LocalDateTime.now(),Duration.ofMinutes(5));
        int taskId = manager.addTask(task).getTaskId();

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + taskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());

        List<Task> tasksFromManager = manager.getTasks();
        assertTrue(tasksFromManager.isEmpty(), TASK_RETURN_ERR);
    }
}

class ListTaskTypeToken extends TypeToken<List<Task>> {
}
class TaskTypeToken extends TypeToken<Task> {
}