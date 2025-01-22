package manager;

import org.junit.jupiter.api.Test;
import task.Epic;
import task.SubTask;
import task.Task;
import task.TaskStatus;

import java.io.File;
import java.util.List;

import static manager.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {

    T taskManager;
    File file;

    Task task;
    int taskId;
    Epic epic;
    int epicId;
    SubTask subTask;
    int subTaskId;

    @Test
    void addTask() {
        final Task savedTask = taskManager.getTaskById(taskId);

        assertNotNull(savedTask, TASK_NOT_FOUND_ERR);
        assertEquals(task, savedTask, TASK_NOT_MATCH_ERR);
    }

    @Test
    void addSubTask() {
        final List<SubTask> subTasks = taskManager.getSubTasks();
        assertNotNull(subTasks, TASK_NOT_RETURN_ERR);
        assertEquals(1, subTasks.size(), INCORRECT_NUM_OF_TASK_ERR);
        assertEquals(subTask, subTasks.getFirst(), TASK_NOT_MATCH_ERR);
    }

    @Test
    void addEpic() {
        final List<Epic> epics = taskManager.getEpics();

        assertNotNull(epics, TASK_NOT_RETURN_ERR);
        assertEquals(1, epics.size(), INCORRECT_NUM_OF_TASK_ERR);
        assertEquals(epic, epics.getFirst(), TASK_NOT_MATCH_ERR);
    }

    @Test
    void updateTask() {
        taskManager.updateTask(new Task(task.getName(), task.getDescription(), TaskStatus.IN_PROGRESS, task.getTaskId(),
                NEW_TASK_START_TIME, NEW_TASK_DURATION));
        Task savedTask = taskManager.getTaskById(taskId);

        assertEquals(savedTask.getStatus(), TaskStatus.IN_PROGRESS, TASK_NOT_UPDATED);
    }

    @Test
    void updateSubTask() {
        taskManager.updateSubTask(new SubTask(subTask.getName(), subTask.getDescription(), TaskStatus.IN_PROGRESS, epicId,
                subTask.getTaskId(), subTask.getStartTime(), subTask.getDuration()));
        SubTask savedSubTask = taskManager.getSubTaskById(subTaskId);

        assertEquals(savedSubTask.getStatus(), TaskStatus.IN_PROGRESS, TASK_NOT_UPDATED);
    }

    @Test
    void updateEpic() {
        Epic savedEpic = taskManager.addEpic(new Epic(NEW_EPIC_NAME, NEW_EPIC_DESC+"1", TaskStatus.NEW));

        assertEquals(savedEpic.getDescription(), NEW_EPIC_DESC+"1", TASK_NOT_UPDATED);
    }

    @Test
    void getTasks() {
        assertTrue(taskManager.getTasks().contains(task), TASK_NOT_FOUND_ERR);
    }

    @Test
    void getSubTasks() {
        assertTrue(taskManager.getSubTasks().contains(subTask), TASK_NOT_FOUND_ERR);
    }

    @Test
    void getEpics() {
        assertTrue(taskManager.getEpics().contains(epic), TASK_NOT_FOUND_ERR);
    }

    @Test
    void getSubTasksByEpic() {
        assertTrue(taskManager.getSubTasksByEpic(epic).contains(subTask), TASK_NOT_FOUND_ERR);
    }

    @Test
    void removeAll() {
        taskManager.removeAll();

        assertTrue(taskManager.getTasks().isEmpty(), TASK_NOT_REMOVED);
        assertTrue(taskManager.getSubTasks().isEmpty(), SUBTASK_NOT_REMOVED);
        assertTrue(taskManager.getEpics().isEmpty(), EPIC_NOT_REMOVED);
    }

    @Test
    void removeAllTasks() {
        Task task1 = new Task(NEW_TASK_NAME, NEW_TASK_DESC, TaskStatus.NEW, NEW_TASK_START_TIME.plusHours(2), NEW_TASK_DURATION);
        final int taskId1 = taskManager.addTask(task1).getTaskId();

        taskManager.removeAllTasks();

        assertTrue(taskManager.getTasks().isEmpty(), TASK_NOT_REMOVED);
    }

    @Test
    void removeAllEpics() {
        Epic epic1 = new Epic(NEW_EPIC_NAME, NEW_EPIC_DESC, TaskStatus.NEW);
        final int epicId1 = taskManager.addEpic(epic1).getTaskId();

        taskManager.removeAllEpics();

        assertTrue(taskManager.getEpics().isEmpty(), EPIC_NOT_REMOVED);
    }

    @Test
    void removeAllSubTasks() {
        SubTask subTask1 = new SubTask(NEW_SUBTASK_NAME, NEW_SUBTASK_DESC, TaskStatus.NEW, epicId, NEW_TASK_START_TIME.plusHours(2), NEW_TASK_DURATION);
        final int subTaskId1 = taskManager.addSubTask(subTask1).getTaskId();

        taskManager.removeAllSubTasks();

        assertTrue(taskManager.getSubTasks().isEmpty(), SUBTASK_NOT_REMOVED);
    }

    @Test
    void getTaskById() {
        assertEquals(taskManager.getTaskById(taskId), task, TASK_NOT_FOUND_ERR);
    }

    @Test
    void getSubTaskById() {
        assertEquals(taskManager.getSubTaskById(subTaskId), subTask, TASK_NOT_FOUND_ERR);
    }

    @Test
    void getEpicById() {
        assertEquals(taskManager.getEpicById(epicId), epic, TASK_NOT_FOUND_ERR);
    }

    @Test
    void deleteTaskById() {
        taskManager.deleteTaskById(taskId);
        assertNull(taskManager.getTaskById(taskId), TASK_NOT_REMOVED);
    }

    @Test
    void deleteSubTaskById() {
        taskManager.deleteSubTaskById(subTaskId);
        assertNull(taskManager.getSubTaskById(subTaskId), TASK_NOT_REMOVED);
    }

    @Test
    void deleteEpicById() {
        taskManager.deleteEpicById(epicId);
        assertNull(taskManager.getEpicById(epicId), TASK_NOT_REMOVED);
    }

    @Test
    void getHistory() {
        assertEquals(2, taskManager.getHistory().size(), HISTORY_NOT_SAVED_ERR);
    }

    @Test
    void checkCrossTasksWithSameStartTime() {
        Task task1 = new Task(NEW_TASK_NAME, NEW_TASK_DESC, TaskStatus.NEW, NEW_TASK_START_TIME, NEW_TASK_DURATION);

        assertThrows(IllegalArgumentException.class, () -> taskManager.addTask(task1), "Добавление таски с тем " +
                "же временем начала должно приводить к исключению");
    }

    @Test
    void checkCrossTasksWithStartTimeInDurationOther() {
        Task task1 = new Task(NEW_TASK_NAME, NEW_TASK_DESC, TaskStatus.NEW, NEW_TASK_START_TIME.plusMinutes(30), NEW_TASK_DURATION);

        assertThrows(IllegalArgumentException.class, () -> taskManager.addTask(task1), "Добавление таски с " +
                "временем начала в интервал другой таски (пересечение) должно приводить к исключению");
    }
}