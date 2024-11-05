package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.SubTask;
import task.Task;
import task.TaskStatus;
import util.Managers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    TaskManager taskManager;

    @BeforeEach
    void beforeEach() {
        taskManager = Managers.getDefault();
    }

    @Test
    void addTask() {
        Task task = new Task("Test addNewTask", "Test addNewTask description", TaskStatus.NEW);
        final int taskId = taskManager.addTask(task).getTaskId();

        final Task savedTask = taskManager.getTaskById(taskId);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        final List<Task> tasks = taskManager.getTasks();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.getFirst(), "Задачи не совпадают.");
    }

    @Test
    void addEpicTask() {
        Epic epic = new Epic("Test addNewEpicTask", "Test addNewEpicTask description", TaskStatus.NEW);
        final int epicId = taskManager.addEpic(epic).getTaskId();

        final Epic savedEpic = taskManager.getEpicById(epicId);

        assertNotNull(savedEpic, "Задача не найдена.");
        assertEquals(epic, savedEpic, "Задачи не совпадают.");

        final List<Epic> epics = taskManager.getEpics();

        assertNotNull(epics, "Задачи не возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество задач.");
        assertEquals(epic, epics.getFirst(), "Задачи не совпадают.");
    }

    @Test
    void addSubTask() {
        Epic epic = new Epic("Test addNewEpicTask", "Test addNewEpicTask description", TaskStatus.NEW);
        final int epicId = taskManager.addEpic(epic).getTaskId();

        SubTask subTask = new SubTask("Test addNewSubTask", "Test addNewSubTask description", TaskStatus.NEW, epicId);
        final int SubTaskId = taskManager.addSubTask(subTask).getTaskId();

        final SubTask savedSubTask = taskManager.getSubTaskById(SubTaskId);

        assertNotNull(savedSubTask, "Задача не найдена.");
        assertEquals(subTask, savedSubTask, "Задачи не совпадают.");

        final List<SubTask> subTasks = taskManager.getSubTasks();

        assertNotNull(subTasks, "Задачи не возвращаются.");
        assertEquals(1, subTasks.size(), "Неверное количество задач.");
        assertEquals(subTask, subTasks.getFirst(), "Задачи не совпадают.");
    }

    @Test
    void setIdAndGenerateIdNotConflictCheck() {
        Task task = new Task("Test addNewTask", "Test addNewTask description", TaskStatus.NEW);
        final int taskId = taskManager.addTask(task).getTaskId();

        taskManager.updateTask(new Task(task.getName(), task.getDescription(), task.getStatus(), taskId + 1));
        assertNull(taskManager.getTaskById(taskId + 1), "Удалось добавить в taskManager задачу с " +
                                                                "опережающим счетчик номером, что приведет к конфликту");
    }

    @Test
    void immutabilityTaskCheck() {
        Task task = new Task("Test addNewTask", "Test addNewTask description", TaskStatus.NEW);
        final int taskId = taskManager.addTask(task).getTaskId();
        Task taskRef = new Task(task.getName(), task.getDescription(), task.getStatus(), taskId);

        assertEquals(task, taskRef, "Задача поменялась при добавлении в taskManager");
    }

    @Test
    void historyVersionCheck() {
        Task task = new Task("Test addNewTask", "Test addNewTask description", TaskStatus.NEW);
        final int taskId = taskManager.addTask(task).getTaskId();
        taskManager.getTaskById(taskId);
        assertEquals(1, taskManager.getHistory().size(), "Не сохраняется история просмотров");

        taskManager.updateTask(new Task(task.getName(), task.getDescription(), TaskStatus.IN_PROGRESS, task.getTaskId()));
        taskManager.getTaskById(taskId);
        assertEquals(2, taskManager.getHistory().size(), "Не сохраняется история просмотров");

        assertNotEquals(taskManager.getHistory().getFirst(), taskManager.getHistory().getLast(), "История не сохраняет предыдущую версию задачи");
    }

}