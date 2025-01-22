import com.sun.net.httpserver.HttpServer;
import httpServer.SubTasksHandler;
import httpServer.TasksHandler;
import manager.TaskManager;
import task.Epic;
import task.SubTask;
import task.Task;
import task.TaskStatus;
import util.Managers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        TaskManager taskManager = Managers.getDefault();
        //Удалить!
        Task task = new Task("NEW_TASK_NAME", "NEW_TASK_DESC", TaskStatus.NEW, LocalDateTime.of(2025, 1, 1, 0, 0),
                Duration.ofMinutes(60));
        int taskId = taskManager.addTask(task).getTaskId();
        Epic epic = new Epic("NEW_EPIC_NAME", "NEW_EPIC_DESC", TaskStatus.NEW);
        int epicId = taskManager.addEpic(epic).getTaskId();
        SubTask subTask = new SubTask("NEW_SUBTASK_NAME", "NEW_SUBTASK_DESC", TaskStatus.NEW, epicId, LocalDateTime.of(2025, 1, 1, 1, 0),
                Duration.ofMinutes(60));
        int subTaskId = taskManager.addSubTask(subTask).getTaskId();

        HttpServer httpServer = HttpServer.create();
        httpServer.bind(new InetSocketAddress(PORT), 0);
        httpServer.createContext("/tasks", new TasksHandler(taskManager));
        httpServer.createContext("/subtasks", new SubTasksHandler(taskManager));

        httpServer.start();
    }
}
