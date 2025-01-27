package manager;

import task.Epic;
import task.SubTask;
import task.Task;

import java.util.List;
import java.util.TreeSet;

public interface TaskManager {
    Task addTask(Task task);

    SubTask addSubTask(SubTask subTask);

    Epic addEpic(Epic epic);

    void updateTask(Task task);

    void updateSubTask(SubTask subTask);

    void updateEpic(Epic epic);

    List<Task> getTasks();

    List<SubTask> getSubTasks();

    List<Epic> getEpics();

    List<SubTask> getSubTasksByEpic(Epic epic);

    void removeAll();

    void removeAllTasks();

    void removeAllEpics();

    void removeAllSubTasks();

    Task getTaskById(int taskId);

    SubTask getSubTaskById(int taskId);

    Epic getEpicById(int taskId);

    void deleteTaskById(int taskId);

    void deleteSubTaskById(int taskId);

    void deleteEpicById(int taskId);

    List<Task> getHistory();

    TreeSet<Task> getPrioritizedTasks();
}
