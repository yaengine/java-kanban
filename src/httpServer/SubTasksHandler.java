package httpServer;

import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
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
                sendNotFound(httpExchange, errToJson(ENDPOINT_UNKNOWN));
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
                sendText(httpExchange, errToJson("Некорректный номер задачи"), 400);
                return;
            }
            SubTask subTask = taskManager.getSubTaskById(taskIdOpt.get());
            if (subTask == null) {
                sendNotFound(httpExchange, errToJson("Такой задачи нет"));
                return;
            }
            sendText(httpExchange, taskToJson(subTask), 200);
        } catch (Exception e) {
            sendText(httpExchange, errToJson(e.getMessage()), 500);
        }
    }

    private void handlePostSubTasks(HttpExchange httpExchange) throws IOException {
        try {
            Optional<SubTask> taskOpt = JsonToTask(httpExchange.getRequestBody(), new SubTaskTypeToken().getType());
            if(taskOpt.isEmpty()) {
                sendText(httpExchange, errToJson("Некорректный JSON задачи"), 400);
                return;
            }
            SubTask subTask = taskOpt.get();

            try {
                if (subTask.getTaskId() == null) {
                    taskManager.addSubTask(subTask);
                    sendText(httpExchange, successToJson("Задача успешно добавлена"), 200);
                } else {
                    taskManager.updateSubTask(subTask);
                    sendText(httpExchange, successToJson("Задача успешно обновлена"), 200);
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
                sendText(httpExchange, errToJson("Некорректный номер задачи"), 400);
                return;
            }
            boolean isDelete = taskManager.deleteSubTaskById(taskIdOpt.get());
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

class SubTaskTypeToken extends TypeToken<SubTask> {
}
