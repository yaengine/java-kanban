package httpServer;

import com.sun.net.httpserver.HttpServer;
import manager.TaskManager;
import util.Managers;

import java.io.IOException;
import java.net.InetSocketAddress;


public class HttpTaskServer {
    private static final int PORT = 8080;
    static TaskManager taskManager;
    static HttpServer httpServer;

    public HttpTaskServer (TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    public static void main(String[] args) throws IOException {
        start();
    }

    public static void start() throws IOException {
        if (taskManager == null) {
            taskManager = Managers.getDefault();
        }
        httpServer = HttpServer.create();
        httpServer.bind(new InetSocketAddress(PORT), 0);
        httpServer.createContext("/tasks", new TasksHandler(taskManager));
        httpServer.createContext("/subtasks", new SubTasksHandler(taskManager));
        httpServer.createContext("/epics", new EpicsHandler(taskManager));
        httpServer.createContext("/history", new HistoryHandler(taskManager));
        httpServer.createContext("/prioritized", new PrioritizedHandler(taskManager));

        httpServer.start();
    }

    public void stop() {
        httpServer.stop(0);
    }
}
