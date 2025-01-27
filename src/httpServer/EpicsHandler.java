package httpServer;

import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.NotFoundException;
import manager.TaskManager;
import task.Epic;
import task.SubTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class EpicsHandler extends BaseHttpHandler implements HttpHandler {
    TaskManager taskManager;

    public EpicsHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        Endpoint endpoint = getEndpoint(httpExchange.getRequestURI().getPath(), httpExchange.getRequestMethod());

        switch (endpoint) {
            case GET_EPICS: {
                handleGetEpics(httpExchange);
                break;
            }
            case GET_EPIC_BY_ID: {
                handleGetEpicById(httpExchange);
                break;
            }
            case GET_EPIC_SUBTASKS: {
                handleGetEpicSubtasks(httpExchange);
                break;
            }
            case POST_EPICS: {
                handlePostEpics(httpExchange);
            }
            case DELETE_EPIC:
                handlerDeleteEpics(httpExchange);
            default:
                sendNotFound(httpExchange, errToJson(ENDPOINT_UNKNOWN_ERR));
        }

    }

    private void handleGetEpics(HttpExchange httpExchange) throws IOException {
        try {
            ArrayList<Epic> epics = (ArrayList<Epic>) taskManager.getEpics();
            sendText(httpExchange, taskToJson(epics), 200);
        } catch (Exception e) {
            sendText(httpExchange, errToJson(e.getMessage()), 500);
        }
    }

    private void handleGetEpicById(HttpExchange httpExchange) throws IOException {
        try {
            Optional<Integer> taskIdOpt = getId(httpExchange);
            if (taskIdOpt.isEmpty()) {
                sendText(httpExchange, errToJson(ILLEGAL_ID_ERR), 400);
                return;
            }
            Epic epic = taskManager.getEpicById(taskIdOpt.get());
            sendText(httpExchange, taskToJson(epic), 200);
        } catch (NotFoundException e) {
            sendNotFound(httpExchange, errToJson(e.getMessage()));
        } catch (Exception e) {
            sendText(httpExchange, errToJson(e.getMessage()), 500);
        }
    }

    private void handleGetEpicSubtasks(HttpExchange httpExchange) throws IOException {
        try {
            Optional<Integer> taskIdOpt = getId(httpExchange);
            if (taskIdOpt.isEmpty()) {
                sendText(httpExchange, errToJson(ILLEGAL_ID_ERR), 400);
                return;
            }
            Epic epic = taskManager.getEpicById(taskIdOpt.get());
            List<SubTask> subTasks = taskManager.getSubTasksByEpic(epic);
            sendText(httpExchange, taskToJson(subTasks), 200);
        } catch (NotFoundException e) {
            sendNotFound(httpExchange, errToJson(e.getMessage()));
        } catch (Exception e) {
            sendText(httpExchange, errToJson(e.getMessage()), 500);
        }
    }

    private void handlePostEpics(HttpExchange httpExchange) throws IOException {
        try {
            Optional<Epic> taskOpt = jsonToTask(
                    new String(httpExchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET),
                    new EpicTypeToken().getType());
            if (taskOpt.isEmpty()) {
                sendText(httpExchange, errToJson(ILLEGAL_JSON_ERR), 400);
                return;
            }
            Epic epic = taskOpt.get();

            try {
                if (epic.getTaskId() == null) {
                    taskManager.addEpic(epic);
                    sendText(httpExchange, successToJson(TASK_ADD), 201);
                } else {
                    taskManager.updateEpic(epic);
                    sendText(httpExchange, successToJson(TASK_UPDATED), 201);
                }
            } catch (IllegalArgumentException e) {
                sendHasInteractions(httpExchange, errToJson(e.getMessage()));
            }
        } catch (Exception e) {
            sendText(httpExchange, errToJson(e.getMessage()), 500);
        }
    }

    private void handlerDeleteEpics(HttpExchange httpExchange) throws IOException {
        try {
            Optional<Integer> taskIdOpt = getId(httpExchange);
            if (taskIdOpt.isEmpty()) {
                sendText(httpExchange, errToJson(ILLEGAL_ID_ERR), 400);
                return;
            }
            taskManager.deleteEpicById(taskIdOpt.get());
            sendText(httpExchange, successToJson(TASK_DELETED), 200);
        } catch (NotFoundException e) {
            sendNotFound(httpExchange, errToJson(e.getMessage()));
        } catch (Exception e) {
            sendText(httpExchange, errToJson(e.getMessage()), 500);
        }
    }
}

class EpicTypeToken extends TypeToken<Epic> {
}
