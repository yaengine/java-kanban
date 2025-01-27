package httpServer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import task.Task;

import java.io.IOException;
import java.util.List;


public class HistoryHandler extends BaseHttpHandler implements HttpHandler {
    TaskManager taskManager;

    public HistoryHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        Endpoint endpoint = getEndpoint(httpExchange.getRequestURI().getPath(), httpExchange.getRequestMethod());

        switch (endpoint) {
            case GET_HISTORY: {
                handleGetHistory(httpExchange);
                break;
            }
            default:
                sendNotFound(httpExchange, errToJson(ENDPOINT_UNKNOWN_ERR));
        }
    }

    private void handleGetHistory(HttpExchange httpExchange) throws IOException {
        try {
            List<Task> histTasks = taskManager.getHistory();
            sendText(httpExchange, taskToJson(histTasks), 200);
        } catch (Exception e) {
            sendText(httpExchange, errToJson(e.getMessage()), 500);
        }
    }
}