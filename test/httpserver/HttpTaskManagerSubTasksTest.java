package httpserver;

import com.google.gson.reflect.TypeToken;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.SubTask;
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
import static manager.InMemoryTaskManager.*;
import static org.junit.jupiter.api.Assertions.*;
import static util.TestConstants.*;

public class HttpTaskManagerSubTasksTest {

    // создаём экземпляр InMemoryTaskManager
    TaskManager manager = new InMemoryTaskManager();
    // передаём его в качестве аргумента в конструктор HttpTaskServer
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    BaseHttpHandler baseHttpHandler= new BaseHttpHandler();

    public HttpTaskManagerSubTasksTest()  {
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
    public void testGetSubTasks() throws IOException, InterruptedException {
        // создаём задачу
        Epic epic = new Epic(NEW_EPIC_NAME, NEW_EPIC_DESC, TaskStatus.NEW);
        int epicId = manager.addEpic(epic).getTaskId();
        SubTask subTask = new SubTask(NEW_TASK_NAME, NEW_SUBTASK_DESC,
                TaskStatus.NEW, epicId, LocalDateTime.now(),Duration.ofMinutes(5));
        manager.addSubTask(subTask);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверяем, что вернулась одна задача с корректным именем
        List<SubTask> subTasksListFromHttpServer = (List<SubTask>) baseHttpHandler.jsonToTask(response.body(),
                new ListSubTaskTypeToken().getType()).get();

        assertNotNull(subTasksListFromHttpServer, SUBTASK_NOT_RETURN_ERR);
        assertEquals(1, subTasksListFromHttpServer.size(), INCORRECT_NUM_OF_SUBTASK_ERR);
        assertEquals(NEW_TASK_NAME, subTasksListFromHttpServer.get(0).getName(), INCORRECT_SUBTASK_NAME_ERR);
    }

    @Test
    public void testGetSubTaskById() throws IOException, InterruptedException {
        // создаём задачу
        Epic epic = new Epic(NEW_EPIC_NAME, NEW_EPIC_DESC, TaskStatus.NEW);
        int epicId = manager.addEpic(epic).getTaskId();
        SubTask subTask = new SubTask(NEW_TASK_NAME, NEW_SUBTASK_DESC,
                TaskStatus.NEW, epicId, LocalDateTime.now(),Duration.ofMinutes(5));
        int subTaskId = manager.addSubTask(subTask).getTaskId();;

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + subTaskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверяем, что вернулась одна задача с корректным именем
        Task subTaskFromHttpServer = (SubTask) baseHttpHandler.jsonToTask(response.body(),
               new SubTaskTypeToken().getType()).get();

        assertNotNull(subTaskFromHttpServer, TASK_NOT_RETURN_ERR);
        assertEquals(subTaskId, subTaskFromHttpServer.getTaskId(), INCORRECT_ID_OF_TASK_ERR);
        assertEquals(NEW_TASK_NAME, subTaskFromHttpServer.getName(), INCORRECT_TASK_NAME_ERR);
    }

    @Test
    public void testGetSubTaskByIdWhenTaskIsNotExists() throws IOException, InterruptedException {
        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(404, response.statusCode());
        assertEquals(baseHttpHandler.errToJson(SUBTASK_NOT_FOUND_ERR), response.body(), INCORRECT_RESP_WHEN_TASK_NOT_FOUND);
    }

    @Test
    public void testAddSubTask() throws IOException, InterruptedException {
        // создаём задачу
        Epic epic = new Epic(NEW_EPIC_NAME, NEW_EPIC_DESC, TaskStatus.NEW);
        int epicId = manager.addEpic(epic).getTaskId();
        SubTask subTask = new SubTask(NEW_SUBTASK_NAME, NEW_SUBTASK_DESC,
                TaskStatus.NEW, epicId, LocalDateTime.now(),Duration.ofMinutes(5));
        // конвертируем её в JSON
        String taskJson = baseHttpHandler.taskToJson(subTask);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        List<SubTask> subTasksFromManager = manager.getSubTasks();

        assertNotNull(subTasksFromManager, SUBTASK_NOT_RETURN_ERR);
        assertEquals(1, subTasksFromManager.size(), INCORRECT_NUM_OF_SUBTASK_ERR);
        assertEquals(NEW_SUBTASK_NAME, subTasksFromManager.get(0).getName(), INCORRECT_SUBTASK_NAME_ERR);
    }

    @Test
    public void testAddSubTaskWithId() throws IOException, InterruptedException {
        // создаём задачу
        Epic epic = new Epic(NEW_EPIC_NAME, NEW_EPIC_DESC, TaskStatus.NEW);
        int epicId = manager.addEpic(epic).getTaskId();
        SubTask subTask = new SubTask(NEW_TASK_NAME, NEW_SUBTASK_DESC,
                TaskStatus.NEW, epicId, LocalDateTime.now(),Duration.ofMinutes(5));
        int subTaskId = manager.addSubTask(subTask).getTaskId();
        SubTask updSubTask = new SubTask(NEW_TASK_NAME, NEW_SUBTASK_DESC,
                TaskStatus.NEW, epicId, subTaskId, LocalDateTime.now(),Duration.ofMinutes(5));
        // конвертируем её в JSON
        String taskJson = baseHttpHandler.taskToJson(updSubTask);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        // вызываем рест, отвечающий за апдейт задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        List<SubTask> subTasksFromManager = manager.getSubTasks();

        assertNotNull(subTasksFromManager, TASK_NOT_RETURN_ERR);
        assertEquals(1, subTasksFromManager.size(), INCORRECT_NUM_OF_TASK_ERR);
        assertEquals(baseHttpHandler.successToJson(TASK_UPDATED), response.body(),
                INCORRECT_RESP_WHEN_UPD_TASK);
        assertEquals(NEW_TASK_NAME, subTasksFromManager.get(0).getName(), INCORRECT_TASK_NAME_ERR);
    }

    @Test
    public void testAddSubTaskWithHasInteractions() throws IOException, InterruptedException {
        Epic epic = new Epic(NEW_EPIC_NAME, NEW_EPIC_DESC, TaskStatus.NEW);
        int epicId = manager.addEpic(epic).getTaskId();
        SubTask subTask = new SubTask(NEW_TASK_NAME, NEW_SUBTASK_DESC,
                TaskStatus.NEW, epicId, LocalDateTime.now(),Duration.ofMinutes(5));
        manager.addSubTask(subTask);
        SubTask updSubTask = new SubTask(NEW_TASK_NAME, NEW_SUBTASK_DESC,
                TaskStatus.NEW, epicId, LocalDateTime.now().plusMinutes(1), Duration.ofMinutes(5));

        // конвертируем её в JSON
        String taskJson = baseHttpHandler.taskToJson(updSubTask);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(406, response.statusCode());

        assertEquals(baseHttpHandler.errToJson(TASK_CROSS_ERROR), response.body(),
                INCORRECT_RESP_WHEN_UPD_TASK_WITH_CROSS);
    }

    @Test
    public void testDeleteSubTask() throws IOException, InterruptedException {
        Epic epic = new Epic(NEW_EPIC_NAME, NEW_EPIC_DESC, TaskStatus.NEW);
        int epicId = manager.addEpic(epic).getTaskId();
        SubTask subTask = new SubTask(NEW_TASK_NAME, NEW_SUBTASK_DESC,
                TaskStatus.NEW, epicId, LocalDateTime.now(),Duration.ofMinutes(5));
        int subTaskId = manager.addSubTask(subTask).getTaskId();

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + subTaskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());

        List<SubTask> subTasksFromManager = manager.getSubTasks();
        assertTrue(subTasksFromManager.isEmpty(), TASK_RETURN_ERR);
    }
}

class ListSubTaskTypeToken extends TypeToken<List<SubTask>> {
}
class SubTaskTypeToken extends TypeToken<SubTask> {
}