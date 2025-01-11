package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.SubTask;
import task.Task;
import task.TaskStatus;
import util.Managers;

import static manager.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @BeforeEach
    void beforeEach() {
        super.taskManager = (InMemoryTaskManager) Managers.getDefault();

        super.task = new Task(NEW_TASK_NAME, NEW_TASK_DESC, TaskStatus.NEW, NEW_TASK_START_TIME, NEW_TASK_DURATION);
        super.taskId = taskManager.addTask(task).getTaskId();
        super.epic = new Epic(NEW_EPIC_NAME, NEW_EPIC_DESC, TaskStatus.NEW);
        super.epicId = taskManager.addEpic(epic).getTaskId();
        super.subTask = new SubTask(NEW_SUBTASK_NAME, NEW_SUBTASK_DESC, TaskStatus.NEW, epicId, NEW_TASK_START_TIME.plusHours(1), NEW_TASK_DURATION);
        super.subTaskId = taskManager.addSubTask(subTask).getTaskId();
    }

    @Test
    void canNotAddEpicToHimself() {
        epic.addSubTaskId(epicId);
        assertFalse(epic.getSubTaskIds().contains(epicId), "Эпик добавился сам в свои подзадачи");
    }

    @Test
    void setIdAndGenerateIdNotConflictCheck() {
        taskManager.removeAll();
        Task task = new Task(NEW_TASK_NAME, NEW_TASK_DESC, TaskStatus.NEW, NEW_TASK_START_TIME, NEW_TASK_DURATION);
        int taskId = taskManager.addTask(task).getTaskId();
        taskManager.updateTask(new Task(task.getName(), task.getDescription(), task.getStatus(), taskId + 1, NEW_TASK_START_TIME.plusHours(1), NEW_TASK_DURATION));
        assertNull(taskManager.getTaskById(taskId + 1), "Удалось добавить в taskManager задачу с " +
                                                                "опережающим счетчик номером, что приведет к конфликту");
    }

    @Test
    void immutabilityTaskCheck() {
        Task taskRef = new Task(task.getName(), task.getDescription(), task.getStatus(), taskId, NEW_TASK_START_TIME, NEW_TASK_DURATION);

        assertEquals(task, taskRef, "Задача поменялась при добавлении в taskManager");
    }

    @Test
    void subTaskShouldAddsToEpic() {
        assertTrue(epic.getSubTaskIds().contains(subTaskId), "Подзадача не добавилась в эпик");
    }

    @Test
    void canNotAddSubTaskToHimselfEpic() {
        taskManager.updateSubTask(new SubTask(subTask.getName(), subTask.getDescription(), subTask.getStatus(), subTaskId,
                subTaskId, subTask.getStartTime(), subTask.getDuration()));
        assertNotEquals(taskManager.getSubTaskById(subTaskId).getEpicTaskId(), subTaskId, "Подзадача добавилась в свой эпик");
    }

    @Test
    void deleteSubTasksShouldNotSaveOldIds() {
        taskManager.deleteSubTaskById(subTaskId);
        assertNull(subTask.getEpicTaskId(), "Удаляемые подзадачи не должны хранить внутри себя старые ID");
    }

    @Test
    void deleteSubTasksShouldNotStayInEpic() {
        taskManager.deleteSubTaskById(subTaskId);
        assertFalse(epic.getSubTaskIds().contains(subTaskId), "ID подзадачи остался в эпике после удаления");
    }

    @Test
    void checkEpicStatusForAllSubTasksWithStatusNEW() {
        SubTask subTask1 = new SubTask(NEW_SUBTASK_NAME, NEW_SUBTASK_DESC, TaskStatus.NEW, epicId, NEW_TASK_START_TIME.plusHours(1), NEW_TASK_DURATION);
        taskManager.addSubTask(subTask1).getTaskId();

        assertEquals(epic.getStatus(),TaskStatus.NEW, "Статус эпика не NEW");
    }

    @Test
    void checkEpicStatusForAllSubTasksWithStatusDone() {
        taskManager.updateSubTask(new SubTask(NEW_SUBTASK_NAME, NEW_SUBTASK_DESC, TaskStatus.DONE, epicId, subTaskId, subTask.getStartTime(), subTask.getDuration()));
        SubTask subTask1 = new SubTask(NEW_SUBTASK_NAME, NEW_SUBTASK_DESC, TaskStatus.DONE, epicId, NEW_TASK_START_TIME.plusHours(2), NEW_TASK_DURATION);
        taskManager.addSubTask(subTask1).getTaskId();

        assertEquals(epic.getStatus(),TaskStatus.DONE, "Статус эпика не DONE");
    }

    @Test
    void checkEpicStatusForSubTasksWithStatusNewAndDone() {
        SubTask subTask1 = new SubTask(NEW_SUBTASK_NAME, NEW_SUBTASK_DESC, TaskStatus.DONE, epicId, NEW_TASK_START_TIME.plusHours(1), NEW_TASK_DURATION);
        taskManager.addSubTask(subTask1).getTaskId();

        assertEquals(epic.getStatus(),TaskStatus.IN_PROGRESS, "Статус эпика не IN_PROGRESS");
    }

    @Test
    void checkEpicStatusForSubTasksWithStatusInProgress() {
        SubTask subTask1 = new SubTask(NEW_SUBTASK_NAME, NEW_SUBTASK_DESC, TaskStatus.IN_PROGRESS, epicId, NEW_TASK_START_TIME.plusHours(1), NEW_TASK_DURATION);
        taskManager.addSubTask(subTask1).getTaskId();

        assertEquals(epic.getStatus(),TaskStatus.IN_PROGRESS, "Статус эпика не IN_PROGRESS");
    }
}