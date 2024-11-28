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
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryTaskManagerTest {

    TaskManager taskManager;
    private final String TASK_NOT_FOUND_ERR = "Задача не найдена.";
    private final String TASK_NOT_MATCH_ERR = "Задачи не совпадают.";
    private final String TASK_NOT_RETURN_ERR = "Задачи не возвращаются.";
    private final String INCORRECT_NUM_OF_TASK_ERR = "Неверное количество задач.";
    private final String HISTORY_NOT_SAVED_ERR = "Не сохраняется история просмотров";

    private final String NEW_TASK_NAME = "Test addNewTask";
    private final String NEW_TASK_DESC = "Test addNewTask description";
    private final String NEW_SUBTASK_NAME = "Test addNewSubTask";
    private final String NEW_SUBTASK_DESC = "Test addNewSubTask description";
    private final String NEW_EPIC_NAME = "Test addNewEpicTask";
    private final String NEW_EPIC_DESC = "Test addNewEpicTask description";


    @BeforeEach
    void beforeEach() {
        taskManager = Managers.getDefault();
    }

    @Test
    void shouldAddTaskInAndGet1BackFromManager() {
        Task task = new Task(NEW_TASK_NAME, NEW_TASK_DESC, TaskStatus.NEW);
        final int taskId = taskManager.addTask(task).getTaskId();

        final Task savedTask = taskManager.getTaskById(taskId);

        assertNotNull(savedTask, TASK_NOT_FOUND_ERR);
        assertEquals(task, savedTask, TASK_NOT_MATCH_ERR);

        final List<Task> tasks = taskManager.getTasks();

        assertNotNull(tasks, TASK_NOT_RETURN_ERR);
        assertEquals(1, tasks.size(), INCORRECT_NUM_OF_TASK_ERR);
        assertEquals(task, tasks.getFirst(), TASK_NOT_MATCH_ERR);
    }

    @Test
    void shouldAddEpicTaskInAndGet1BackFromManager() {
        Epic epic = new Epic(NEW_EPIC_NAME, NEW_EPIC_DESC, TaskStatus.NEW);
        final int epicId = taskManager.addEpic(epic).getTaskId();

        final Epic savedEpic = taskManager.getEpicById(epicId);

        assertNotNull(savedEpic, TASK_NOT_FOUND_ERR);
        assertEquals(epic, savedEpic, TASK_NOT_MATCH_ERR);

        final List<Epic> epics = taskManager.getEpics();

        assertNotNull(epics, TASK_NOT_RETURN_ERR);
        assertEquals(1, epics.size(), INCORRECT_NUM_OF_TASK_ERR);
        assertEquals(epic, epics.getFirst(), TASK_NOT_MATCH_ERR);
    }

    @Test
    void shouldAddSubTaskInAndGet1BackFromManager() {
        Epic epic = new Epic(NEW_EPIC_NAME, NEW_EPIC_DESC, TaskStatus.NEW);
        final int epicId = taskManager.addEpic(epic).getTaskId();

        SubTask subTask = new SubTask(NEW_SUBTASK_NAME, NEW_SUBTASK_DESC, TaskStatus.NEW, epicId);
        final int SubTaskId = taskManager.addSubTask(subTask).getTaskId();

        final SubTask savedSubTask = taskManager.getSubTaskById(SubTaskId);

        assertNotNull(savedSubTask, TASK_NOT_FOUND_ERR);
        assertEquals(subTask, savedSubTask, TASK_NOT_MATCH_ERR);

        final List<SubTask> subTasks = taskManager.getSubTasks();

        assertNotNull(subTasks, TASK_NOT_RETURN_ERR);
        assertEquals(1, subTasks.size(), INCORRECT_NUM_OF_TASK_ERR);
        assertEquals(subTask, subTasks.getFirst(), TASK_NOT_MATCH_ERR);
    }

    @Test
    void canNotAddEpicToHimself() {
        Epic epic = new Epic(NEW_EPIC_NAME, NEW_EPIC_DESC, TaskStatus.NEW);
        final int epicId = taskManager.addEpic(epic).getTaskId();

        epic.addSubTaskId(epicId);
        assertTrue(epic.getSubTaskIds().isEmpty(), "Эпик добавился сам в свои подзадачи");
    }

    @Test
    void setIdAndGenerateIdNotConflictCheck() {
        Task task = new Task(NEW_TASK_NAME, NEW_TASK_DESC, TaskStatus.NEW);
        final int taskId = taskManager.addTask(task).getTaskId();

        taskManager.updateTask(new Task(task.getName(), task.getDescription(), task.getStatus(), taskId + 1));
        assertNull(taskManager.getTaskById(taskId + 1), "Удалось добавить в taskManager задачу с " +
                                                                "опережающим счетчик номером, что приведет к конфликту");
    }

    @Test
    void immutabilityTaskCheck() {
        Task task = new Task(NEW_TASK_NAME, NEW_TASK_DESC, TaskStatus.NEW);
        final int taskId = taskManager.addTask(task).getTaskId();
        Task taskRef = new Task(task.getName(), task.getDescription(), task.getStatus(), taskId);

        assertEquals(task, taskRef, "Задача поменялась при добавлении в taskManager");
    }

    @Test
    void historyVersionCheck() {
        Task task = new Task(NEW_TASK_NAME, NEW_TASK_DESC, TaskStatus.NEW);
        final int taskId = taskManager.addTask(task).getTaskId();
        taskManager.getTaskById(taskId);
        assertEquals(1, taskManager.getHistory().size(), HISTORY_NOT_SAVED_ERR);

        taskManager.getTaskById(taskId);
        assertEquals(1, taskManager.getHistory().size(), "Повторный просмотр не должен сохраняться в истории");

        Task otherTask = new Task(NEW_TASK_NAME, NEW_TASK_DESC, TaskStatus.IN_PROGRESS);
        taskManager.getTaskById(taskManager.addTask(otherTask).getTaskId());
        assertEquals(2, taskManager.getHistory().size(), HISTORY_NOT_SAVED_ERR);
    }

    @Test
    void checkTaskInHistory() {
        Task task = new Task(NEW_TASK_NAME, NEW_TASK_DESC, TaskStatus.NEW);
        final int taskId = taskManager.addTask(task).getTaskId();
        taskManager.getTaskById(taskId);
        assertTrue(taskManager.getHistory().contains(task), "Просмотренная таска не попала в историю просмотра");
    }

    @Test
    void checkDeleteTasksFromHistory() {
        Task task = new Task(NEW_TASK_NAME, NEW_TASK_DESC, TaskStatus.NEW);
        final int taskId = taskManager.addTask(task).getTaskId();
        taskManager.getTaskById(taskId);

        taskManager.deleteTaskById(task.getTaskId());
        assertFalse(taskManager.getHistory().contains(task), "Удаленная таска осталась в истории");
    }

    @Test
    void checkDeleteEpicWithSubTaskFromHistory() {
        Epic epic = new Epic(NEW_EPIC_NAME, NEW_EPIC_DESC, TaskStatus.NEW);
        final int epicId = taskManager.addEpic(epic).getTaskId();
        taskManager.getEpicById(epicId);
        assertTrue(taskManager.getHistory().contains(epic), "Просмотренный эпик не попал в историю просмотра");

        SubTask subTask = new SubTask(NEW_SUBTASK_NAME, NEW_SUBTASK_DESC, TaskStatus.NEW, epicId);
        final int subTaskId = taskManager.addSubTask(subTask).getTaskId();
        taskManager.getSubTaskById(subTaskId);
        assertTrue(taskManager.getHistory().contains(subTask), "Просмотренная подзадача не попала в историю просмотра");

        taskManager.deleteEpicById(epicId);
        assertFalse(taskManager.getHistory().contains(epic), "Удаленный эпик остался в истории");
        assertFalse(taskManager.getHistory().contains(subTask), "Удаленная подзадача осталась в истории");
    }

    @Test
    void subTaskShouldAddsToEpic() {
        Epic epic = new Epic(NEW_EPIC_NAME, NEW_EPIC_DESC, TaskStatus.NEW);
        final int epicId = taskManager.addEpic(epic).getTaskId();

        SubTask subTask = new SubTask(NEW_SUBTASK_NAME, NEW_SUBTASK_DESC, TaskStatus.NEW, epicId);
        final int subTaskId = taskManager.addSubTask(subTask).getTaskId();
        assertTrue(epic.getSubTaskIds().contains(subTaskId), "Подзадача не добавилась в эпик");
    }

    @Test
    void deleteSubTasksShouldNotSaveOldIds() {
        Epic epic = new Epic(NEW_EPIC_NAME, NEW_EPIC_DESC, TaskStatus.NEW);
        final int epicId = taskManager.addEpic(epic).getTaskId();

        SubTask subTask = new SubTask(NEW_SUBTASK_NAME, NEW_SUBTASK_DESC, TaskStatus.NEW, epicId);
        final int subTaskId = taskManager.addSubTask(subTask).getTaskId();

        taskManager.deleteSubTaskById(subTaskId);
        assertNull(subTask.getEpicTaskId(), "Удаляемые подзадачи не должны хранить внутри себя старые ID");
    }

    @Test
    void deleteSubTasksShouldNotStayInEpic() {
        Epic epic = new Epic(NEW_EPIC_NAME, NEW_EPIC_DESC, TaskStatus.NEW);
        final int epicId = taskManager.addEpic(epic).getTaskId();

        SubTask subTask = new SubTask(NEW_SUBTASK_NAME, NEW_SUBTASK_DESC, TaskStatus.NEW, epicId);
        final int subTaskId = taskManager.addSubTask(subTask).getTaskId();

        taskManager.deleteSubTaskById(subTaskId);
        assertFalse(epic.getSubTaskIds().contains(subTaskId), "ID подзадачи остался в эпике после удаления");
    }

    @Test
    void checkRemoveAllTasksFromHistory() {
        Task task = new Task(NEW_TASK_NAME, NEW_TASK_DESC, TaskStatus.NEW);
        final int taskId = taskManager.addTask(task).getTaskId();
        taskManager.getTaskById(taskId);

        Task otherTask = new Task(NEW_TASK_NAME, NEW_TASK_DESC, TaskStatus.IN_PROGRESS);
        taskManager.getTaskById(taskManager.addTask(otherTask).getTaskId());

        taskManager.removeAllTasks();
        assertTrue(taskManager.getHistory().isEmpty(), "История просмотров не пуста");
    }
}