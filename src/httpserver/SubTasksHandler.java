package httpserver;

import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.NotFoundException;
import manager.TaskManager;
import task.SubTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;


public class SubTasksHandler extends BaseHttpHandler implements HttpHandler {
    TaskManager taskManager;

    public SubTasksHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        Endpoint endpoint = getEndpoint(httpExchange.getRequestURI().getPath(), httpExchange.getRequestMethod());

        switch (endpoint) {
            case GET_SUBTASKS: {
                handleGetSubTasks(httpExchange);
                break;
            }
            case GET_SUBTASK_BY_ID: {
                handleGetSubTaskById(httpExchange);
                break;
            }
            case POST_SUBTASKS: {
                handlePostSubTasks(httpExchange);
            }
            case DELETE_SUBTASK:
                handlerDeleteSubTasks(httpExchange);
            default:
                sendNotFound(httpExchange, errToJson(ENDPOINT_UNKNOWN_ERR));
        }

    }

    private void handleGetSubTasks(HttpExchange httpExchange) throws IOException {
        try {
            ArrayList<SubTask> subTasks = (ArrayList<SubTask>) taskManager.getSubTasks();
            sendText(httpExchange, taskToJson(subTasks), 200);
        } catch (Exception e) {
            sendText(httpExchange, errToJson(e.getMessage()), 500);
        }
    }

    private void handleGetSubTaskById(HttpExchange httpExchange) throws IOException {
        try {
            Optional<Integer> taskIdOpt = getId(httpExchange);
            if (taskIdOpt.isEmpty()) {
                sendText(httpExchange, errToJson(ILLEGAL_ID_ERR), 400);
                return;
            }
            SubTask subTask = taskManager.getSubTaskById(taskIdOpt.get());
            sendText(httpExchange, taskToJson(subTask), 200);
        } catch (NotFoundException e) {
            sendNotFound(httpExchange, errToJson(e.getMessage()));
        } catch (Exception e) {
            sendText(httpExchange, errToJson(e.getMessage()), 500);
        }
    }

    private void handlePostSubTasks(HttpExchange httpExchange) throws IOException {
        try {
            Optional<SubTask> taskOpt = jsonToTask(
                    new String(httpExchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET),
                    new SubTaskTypeToken().getType());
            if(taskOpt.isEmpty()) {
                sendText(httpExchange, errToJson(ILLEGAL_JSON_ERR), 400);
                return;
            }
            SubTask subTask = taskOpt.get();

            try {
                if (subTask.getTaskId() == null) {
                    taskManager.addSubTask(subTask);
                    sendText(httpExchange, successToJson(TASK_ADD), 201);
                } else {
                    taskManager.updateSubTask(subTask);
                    sendText(httpExchange, successToJson(TASK_UPDATED), 201);
                }
            } catch (IllegalArgumentException e)  {
                sendHasInteractions(httpExchange, errToJson(e.getMessage()));
            }
        } catch (Exception e) {
            sendText(httpExchange, errToJson(e.getMessage()), 500);
        }
    }

    private void handlerDeleteSubTasks(HttpExchange httpExchange) throws IOException {
        try {
            Optional<Integer> taskIdOpt = getId(httpExchange);
            if(taskIdOpt.isEmpty()) {
                sendText(httpExchange, errToJson(ILLEGAL_ID_ERR), 400);
                return;
            }
            taskManager.deleteSubTaskById(taskIdOpt.get());
            sendText(httpExchange, successToJson(TASK_DELETED), 200);
        } catch (NotFoundException e) {
            sendNotFound(httpExchange, errToJson(e.getMessage()));
        } catch (Exception e) {
            sendText(httpExchange, errToJson(e.getMessage()), 500);
        }
    }

    class SubTaskTypeToken extends TypeToken<SubTask> {
    }
}
