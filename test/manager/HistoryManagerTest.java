package manager;

import org.junit.jupiter.api.Test;
import task.Task;
import task.TaskStatus;

import static manager.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;

abstract class HistoryManagerTest<T extends HistoryManager> {
    T historyManager;
    TaskManager taskManager;

    @Test
    void add() {
        Task task = new Task(NEW_TASK_NAME, NEW_TASK_DESC, TaskStatus.NEW, NEW_TASK_START_TIME, NEW_TASK_DURATION);
        final int taskId = taskManager.addTask(task).getTaskId();
        historyManager.add(task);

        assertEquals(historyManager.getHistory().getFirst(), task, "Задача не добавлена в историю");
    }

    @Test
    void remove() {
        Task task = new Task(NEW_TASK_NAME, NEW_TASK_DESC, TaskStatus.NEW, NEW_TASK_START_TIME, NEW_TASK_DURATION);
        final int taskId = taskManager.addTask(task).getTaskId();
        historyManager.add(task);
        historyManager.remove(taskId);

        assertTrue(historyManager.getHistory().isEmpty(), "Истрия не пуста");
    }

    @Test
    void removeFirst() {
        Task task = new Task(NEW_TASK_NAME, NEW_TASK_DESC, TaskStatus.NEW, NEW_TASK_START_TIME, NEW_TASK_DURATION);
        final int taskId = taskManager.addTask(task).getTaskId();
        historyManager.add(task);
        Task otherTask = new Task(NEW_TASK_NAME, NEW_TASK_DESC, TaskStatus.IN_PROGRESS, NEW_TASK_START_TIME.plusHours(1), NEW_TASK_DURATION);
        final int otherTaskId = taskManager.addTask(otherTask).getTaskId();
        historyManager.add(otherTask);
        Task otherTask1 = new Task(NEW_TASK_NAME, NEW_TASK_DESC, TaskStatus.IN_PROGRESS, NEW_TASK_START_TIME.plusHours(2), NEW_TASK_DURATION);
        final int otherTask1Id = taskManager.addTask(otherTask1).getTaskId();
        historyManager.add(otherTask1);

        historyManager.remove(taskId);

        assertTrue(historyManager.getHistory().get(0).equals(otherTask) &&
                    historyManager.getHistory().get(1).equals(otherTask1), "Первая запись в истории удалилась не корректно");
    }

    @Test
    void removeMidle() {
        Task task = new Task(NEW_TASK_NAME, NEW_TASK_DESC, TaskStatus.NEW, NEW_TASK_START_TIME, NEW_TASK_DURATION);
        final int taskId = taskManager.addTask(task).getTaskId();
        historyManager.add(task);
        Task otherTask = new Task(NEW_TASK_NAME, NEW_TASK_DESC, TaskStatus.IN_PROGRESS, NEW_TASK_START_TIME.plusHours(1), NEW_TASK_DURATION);
        final int otherTaskId = taskManager.addTask(otherTask).getTaskId();
        historyManager.add(otherTask);
        Task otherTask1 = new Task(NEW_TASK_NAME, NEW_TASK_DESC, TaskStatus.IN_PROGRESS, NEW_TASK_START_TIME.plusHours(2), NEW_TASK_DURATION);
        final int otherTask1Id = taskManager.addTask(otherTask1).getTaskId();
        historyManager.add(otherTask1);

        historyManager.remove(otherTaskId);

        assertTrue(historyManager.getHistory().get(0).equals(task) &&
                historyManager.getHistory().get(1).equals(otherTask1), "Запись в середине истории удалилась не корректно");
    }

    @Test
    void removeLast() {
        Task task = new Task(NEW_TASK_NAME, NEW_TASK_DESC, TaskStatus.NEW, NEW_TASK_START_TIME, NEW_TASK_DURATION);
        final int taskId = taskManager.addTask(task).getTaskId();
        historyManager.add(task);
        Task otherTask = new Task(NEW_TASK_NAME, NEW_TASK_DESC, TaskStatus.IN_PROGRESS, NEW_TASK_START_TIME.plusHours(1), NEW_TASK_DURATION);
        final int otherTaskId = taskManager.addTask(otherTask).getTaskId();
        historyManager.add(otherTask);
        Task otherTask1 = new Task(NEW_TASK_NAME, NEW_TASK_DESC, TaskStatus.IN_PROGRESS, NEW_TASK_START_TIME.plusHours(2), NEW_TASK_DURATION);
        final int otherTask1Id = taskManager.addTask(otherTask1).getTaskId();
        historyManager.add(otherTask1);

        historyManager.remove(otherTask1Id);

        assertTrue(historyManager.getHistory().get(0).equals(task) &&
                historyManager.getHistory().get(1).equals(otherTask), "Последняя запись в истории удалилась не корректно");
    }

    @Test
    void getHistory() {
        Task task = new Task(NEW_TASK_NAME, NEW_TASK_DESC, TaskStatus.NEW, NEW_TASK_START_TIME, NEW_TASK_DURATION);
        final int taskId = taskManager.addTask(task).getTaskId();
        historyManager.add(task);

        assertFalse(historyManager.getHistory().isEmpty(), "История просмотров пуста");
    }
}