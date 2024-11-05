package task;

import manager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.Managers;

import static org.junit.jupiter.api.Assertions.*;

class SubTaskTest {
    TaskManager taskManager;

    @BeforeEach
    void beforeEach() {
        taskManager = Managers.getDefault();
    }

    @Test
    void canNotAddSubTaskToHimselfEpic() {
        Epic epic = new Epic("Test addNewEpicTask", "Test addNewEpicTask description", TaskStatus.NEW);
        final int epicId = taskManager.addEpic(epic).getTaskId();

        SubTask subTask = new SubTask("Test addNewSubTask", "Test addNewSubTask description", TaskStatus.NEW, epicId);
        final int subTaskId = taskManager.addSubTask(subTask).getTaskId();

        taskManager.updateSubTask(new SubTask(subTask.getName(), subTask.getDescription(), subTask.getStatus(), subTaskId, subTaskId));
        assertNotEquals(taskManager.getSubTaskById(subTaskId).getEpicTaskId(), subTaskId, "Подзадача добавилась в свой эпик");
    }

}