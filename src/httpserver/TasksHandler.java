package httpserver;

import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.NotFoundException;
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
                sendNotFound(httpExchange, errToJson(ENDPOINT_UNKNOWN_ERR));
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
                sendText(httpExchange, errToJson(ILLEGAL_ID_ERR), 400);
                return;
            }
            Task task = taskManager.getTaskById(taskIdOpt.get());
            sendText(httpExchange, taskToJson(task), 200);
        } catch (NotFoundException e) {
            sendNotFound(httpExchange, errToJson(e.getMessage()));
        } catch (Exception e) {
            sendText(httpExchange, errToJson(e.getMessage()), 500);
        }
    }

    private void handlePostTasks(HttpExchange httpExchange) throws IOException {
        try {
            Optional<Task> taskOpt = jsonToTask(
                    new String(httpExchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET),
                    new TaskTypeToken().getType());
            if (taskOpt.isEmpty()) {
                sendText(httpExchange, errToJson(ILLEGAL_JSON_ERR), 400);
                return;
            }
            Task task = taskOpt.get();

            if (task.getTaskId() == null) {
                taskManager.addTask(task);
                sendText(httpExchange, successToJson(TASK_ADD), 201);
            } else {
                taskManager.updateTask(task);
                sendText(httpExchange, successToJson(TASK_UPDATED), 201);
            }
        } catch (IllegalArgumentException e) {
            sendHasInteractions(httpExchange, errToJson(e.getMessage()));
        } catch (Exception e) {
            sendText(httpExchange, errToJson(e.getMessage()), 500);
        }
    }

    private void handlerDeleteTasks(HttpExchange httpExchange) throws IOException {
        try {
            Optional<Integer> taskIdOpt = getId(httpExchange);
            if (taskIdOpt.isEmpty()) {
                sendText(httpExchange, errToJson(ILLEGAL_ID_ERR), 400);
                return;
            }
            taskManager.deleteTaskById(taskIdOpt.get());
            sendText(httpExchange, successToJson(TASK_DELETED), 200);
        } catch (NotFoundException e) {
            sendNotFound(httpExchange, errToJson(e.getMessage()));
        } catch (Exception e) {
            sendText(httpExchange, errToJson(e.getMessage()), 500);
        }
    }

    class TaskTypeToken extends TypeToken<Task> {
    }
}
