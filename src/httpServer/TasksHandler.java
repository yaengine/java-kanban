package httpServer;

import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import task.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;


public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    TaskManager taskManager;

    public TasksHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        Endpoint endpoint = getEndpoint(httpExchange.getRequestURI().getPath(), httpExchange.getRequestMethod());

        switch (endpoint) {
            case GET_TASKS: {
                handleGetTasks(httpExchange);
                break;
            }
            case GET_TASK_BY_ID: {
                handleGetTaskById(httpExchange);
                break;
            }
            case POST_TASKS: {
                handlePostTasks(httpExchange);
            }
            case DELETE_TASK:
                handlerDeleteTasks(httpExchange);
            default:
                sendNotFound(httpExchange, errToJson(ENDPOINT_UNKNOWN));
        }

    }

    private void handleGetTasks(HttpExchange httpExchange) throws IOException {
        try {
            ArrayList<Task> tasks = (ArrayList<Task>) taskManager.getTasks();
            sendText(httpExchange, taskToJson(tasks), 200);
        } catch (Exception e) {
            sendText(httpExchange, errToJson(e.getMessage()), 500);
        }
    }

    private void handleGetTaskById(HttpExchange httpExchange) throws IOException {
        try {
            Optional<Integer> taskIdOpt = getId(httpExchange);
            if (taskIdOpt.isEmpty()) {
                sendText(httpExchange, errToJson("Некорректный номер задачи"), 400);
                return;
            }
            Task task = taskManager.getTaskById(taskIdOpt.get());
            if (task == null) {
                sendNotFound(httpExchange, errToJson("Такой задачи нет"));
                return;
            }
            sendText(httpExchange, taskToJson(task), 200);
        } catch (Exception e) {
            sendText(httpExchange, errToJson(e.getMessage()), 500);
        }
    }

    private void handlePostTasks(HttpExchange httpExchange) throws IOException {
        try {
            Optional<Task> taskOpt = JsonToTask(httpExchange.getRequestBody(), new TaskTypeToken().getType());
            if (taskOpt.isEmpty()) {
                sendText(httpExchange, errToJson("Некорректный JSON задачи"), 400);
                return;
            }
            Task task = taskOpt.get();

            try {
                if (task.getTaskId() == null) {
                    taskManager.addTask(task);
                    sendText(httpExchange, successToJson("Задача успешно добавлена"), 200);
                } else {
                    taskManager.updateTask(task);
                    sendText(httpExchange, successToJson("Задача успешно обновлена"), 200);
                }
            } catch (IllegalArgumentException e) {
                sendHasInteractions(httpExchange, errToJson(e.getMessage()));
            }
        } catch (Exception e) {
            sendText(httpExchange, errToJson(e.getMessage()), 500);
        }
    }

    private void handlerDeleteTasks(HttpExchange httpExchange) throws IOException {
        try {
            Optional<Integer> taskIdOpt = getId(httpExchange);
            if (taskIdOpt.isEmpty()) {
                sendText(httpExchange, errToJson("Некорректный номер задачи"), 400);
                return;
            }
            boolean isDelete = taskManager.deleteTaskById(taskIdOpt.get());
            if (!isDelete) {
                sendNotFound(httpExchange, errToJson("Такой задачи нет"));
                return;
            }
            sendText(httpExchange, successToJson("Задача успешно удалена"), 200);
        } catch (Exception e) {
            sendText(httpExchange, errToJson(e.getMessage()), 500);
        }
    }
}

class TaskTypeToken extends TypeToken<Task> {
}
