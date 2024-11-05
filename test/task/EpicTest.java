package task;

import manager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.Managers;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    TaskManager taskManager;

    @BeforeEach
    void beforeEach() {
        taskManager = Managers.getDefault();
    }

    @Test
    void canNotAddEpicToHimself() {
        Epic epic = new Epic("Test addNewEpicTask", "Test addNewEpicTask description", TaskStatus.NEW);
        final int epicId = taskManager.addEpic(epic).getTaskId();

        epic.addSubTaskId(epicId);
        assertTrue(epic.subTaskIds.isEmpty(), "Эпик добавился сам в свои подзадачи");
    }
}