package manager;

import task.Epic;
import task.SubTask;
import task.Task;

import java.util.List;

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

    Integer getNewId();

    void removeAll();

    void removeAllTasks();

    void removeAllEpics();

    void removeAllSubTasks();

    Task getTaskById(int taskId);

    SubTask getSubTaskById(int taskId);

    Epic getEpicById(int taskId);

    boolean deleteTaskById(int taskId);

    boolean deleteSubTaskById(int taskId);

    boolean deleteEpicById(int taskId);

    void updateEpicStatus(int epicId);

    List<Task> getHistory();
}
