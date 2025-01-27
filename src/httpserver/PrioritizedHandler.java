package httpserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import task.Task;

import java.io.IOException;
import java.util.TreeSet;


public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {
    TaskManager taskManager;

    public PrioritizedHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        Endpoint endpoint = getEndpoint(httpExchange.getRequestURI().getPath(), httpExchange.getRequestMethod());

        switch (endpoint) {
            case GET_PRIOR: {
                handleGetPrior(httpExchange);
                break;
            }
            default:
                sendNotFound(httpExchange, errToJson(ENDPOINT_UNKNOWN_ERR));
        }
    }

    private void handleGetPrior(HttpExchange httpExchange) throws IOException {
        try {
            TreeSet<Task> histTasks = taskManager.getPrioritizedTasks();
            sendText(httpExchange, taskToJson(histTasks), 200);
        } catch (Exception e) {
            sendText(httpExchange, errToJson(e.getMessage()), 500);
        }
    }
}