package httpserver;

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

import static util.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HttpTaskManagerPrioritizedTest {

    // создаём экземпляр InMemoryTaskManager
    TaskManager manager = new InMemoryTaskManager();
    // передаём его в качестве аргумента в конструктор HttpTaskServer
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    BaseHttpHandler baseHttpHandler= new BaseHttpHandler();

    public HttpTaskManagerPrioritizedTest() {
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
    public void testGetPrioritizedTask() throws IOException, InterruptedException {
        Task task = new Task(NEW_TASK_NAME, NEW_TASK_DESC,
                TaskStatus.NEW, LocalDateTime.now(),Duration.ofMinutes(5));
        int taskId = manager.addTask(task).getTaskId();
        Epic epic = new Epic(NEW_EPIC_NAME, NEW_EPIC_DESC, TaskStatus.NEW);
        int epicId = manager.addEpic(epic).getTaskId();
        SubTask subTask = new SubTask(NEW_SUBTASK_NAME, NEW_SUBTASK_DESC,
                TaskStatus.NEW, epicId, LocalDateTime.now().plusHours(1),Duration.ofMinutes(5));
        int subTaskId = manager.addSubTask(subTask).getTaskId();

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверяем, что вернулась одна задача с корректным именем
        List<Task> tasksListFromHttpServer = (List<Task>) baseHttpHandler.jsonToTask(response.body(),
                new ListTaskTypeToken().getType()).get();

        System.out.println(tasksListFromHttpServer);
        assertNotNull(tasksListFromHttpServer, TASK_NOT_RETURN_ERR);
        assertEquals(2, tasksListFromHttpServer.size(), INCORRECT_NUM_OF_TASK_ERR);
        assertEquals(NEW_TASK_NAME, tasksListFromHttpServer.get(0).getName(), INCORRECT_TASK_NAME_ERR);
        assertEquals(NEW_SUBTASK_NAME, tasksListFromHttpServer.get(1).getName(), INCORRECT_SUBTASK_NAME_ERR);
    }
}