package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.SubTask;
import task.Task;
import task.TaskStatus;
import util.Managers;

import static util.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest extends HistoryManagerTest<InMemoryHistoryManager>{

    InMemoryTaskManager taskManager = (InMemoryTaskManager) Managers.getDefault();

    @BeforeEach
    void beforeEach() {
        super.historyManager = (InMemoryHistoryManager) Managers.getDefaultHistory();
        super.taskManager = this.taskManager;
    }

    @Test
    void emptyHistoryCheck() {
        assertTrue(taskManager.getHistory().isEmpty(), "История просмотров не пуста");
    }

    @Test
    void checkRemoveAllTasksFromHistory() {
        Task task = new Task(NEW_TASK_NAME, NEW_TASK_DESC, TaskStatus.NEW, NEW_TASK_START_TIME, NEW_TASK_DURATION);
        final int taskId = taskManager.addTask(task).getTaskId();
        taskManager.getTaskById(taskId);

        Task otherTask = new Task(NEW_TASK_NAME, NEW_TASK_DESC, TaskStatus.IN_PROGRESS, NEW_TASK_START_TIME.plusHours(1), NEW_TASK_DURATION);
        taskManager.getTaskById(taskManager.addTask(otherTask).getTaskId());

        taskManager.removeAllTasks();
        assertTrue(taskManager.getHistory().isEmpty(), "История просмотров не пуста");
    }

    @Test
    void historyVersionCheck() {
        Task task = new Task(NEW_TASK_NAME, NEW_TASK_DESC, TaskStatus.NEW, NEW_TASK_START_TIME, NEW_TASK_DURATION);
        final int taskId = taskManager.addTask(task).getTaskId();

        taskManager.getTaskById(taskId);
        assertEquals(1, taskManager.getHistory().size(), HISTORY_NOT_SAVED_ERR);

        taskManager.getTaskById(taskId);
        assertEquals(1, taskManager.getHistory().size(), "Повторный просмотр не должен сохраняться в истории");

        Task otherTask = new Task(NEW_TASK_NAME, NEW_TASK_DESC, TaskStatus.IN_PROGRESS, NEW_TASK_START_TIME.plusHours(1), NEW_TASK_DURATION);
        taskManager.getTaskById(taskManager.addTask(otherTask).getTaskId());
        assertEquals(2, taskManager.getHistory().size(), HISTORY_NOT_SAVED_ERR);
    }

    @Test
    void checkTaskInHistory() {
        Task task = new Task(NEW_TASK_NAME, NEW_TASK_DESC, TaskStatus.NEW, NEW_TASK_START_TIME, NEW_TASK_DURATION);
        final int taskId = taskManager.addTask(task).getTaskId();

        taskManager.getTaskById(taskId);
        assertTrue(taskManager.getHistory().contains(task), "Просмотренная таска не попала в историю просмотра");
    }

    @Test
    void checkDeleteTasksFromHistory() {
        Task task = new Task(NEW_TASK_NAME, NEW_TASK_DESC, TaskStatus.NEW, NEW_TASK_START_TIME, NEW_TASK_DURATION);
        int taskId = taskManager.addTask(task).getTaskId();
        Epic epic = new Epic(NEW_EPIC_NAME, NEW_EPIC_DESC, TaskStatus.NEW);
        int epicId = taskManager.addEpic(epic).getTaskId();
        SubTask subTask = new SubTask(NEW_SUBTASK_NAME, NEW_SUBTASK_DESC, TaskStatus.NEW, epicId, NEW_TASK_START_TIME.plusHours(1), NEW_TASK_DURATION);
        int subTaskId = taskManager.addSubTask(subTask).getTaskId();

        taskManager.getTaskById(taskId);

        taskManager.deleteTaskById(task.getTaskId());
        assertFalse(taskManager.getHistory().contains(task), "Удаленная таска осталась в истории");
    }

    @Test
    void checkDeleteEpicWithSubTaskFromHistory() {
        Epic epic = new Epic(NEW_EPIC_NAME, NEW_EPIC_DESC, TaskStatus.NEW);
        int epicId = taskManager.addEpic(epic).getTaskId();
        SubTask subTask = new SubTask(NEW_SUBTASK_NAME, NEW_SUBTASK_DESC, TaskStatus.NEW, epicId, NEW_TASK_START_TIME.plusHours(1), NEW_TASK_DURATION);
        int subTaskId = taskManager.addSubTask(subTask).getTaskId();

        taskManager.getEpicById(epicId);
        assertTrue(taskManager.getHistory().contains(epic), "Просмотренный эпик не попал в историю просмотра");

        taskManager.getSubTaskById(subTaskId);
        assertTrue(taskManager.getHistory().contains(subTask), "Просмотренная подзадача не попала в историю просмотра");

        taskManager.deleteEpicById(epicId);
        assertFalse(taskManager.getHistory().contains(epic), "Удаленный эпик остался в истории");
        assertFalse(taskManager.getHistory().contains(subTask), "Удаленная подзадача осталась в истории");
    }

}
