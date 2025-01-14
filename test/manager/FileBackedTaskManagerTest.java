package manager;

import exceptions.ManagerSaveException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.*;
import util.Managers;

import java.io.*;

import static manager.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    @BeforeEach
    void beforeEach() {
        try {
            super.file = File.createTempFile(FILE_PREFIX, FILE_POSTFIX);
            super.taskManager = Managers.getFileBackedTaskManager(file);

            super.task = new Task(NEW_TASK_NAME, NEW_TASK_DESC, TaskStatus.NEW, NEW_TASK_START_TIME, NEW_TASK_DURATION);
            super.taskId = taskManager.addTask(task).getTaskId();
            super.epic = new Epic(NEW_EPIC_NAME, NEW_EPIC_DESC, TaskStatus.NEW);
            super.epicId = taskManager.addEpic(epic).getTaskId();
            super.subTask = new SubTask(NEW_SUBTASK_NAME, NEW_SUBTASK_DESC, TaskStatus.NEW, epicId, NEW_TASK_START_TIME.plusHours(1), NEW_TASK_DURATION);
            super.subTaskId = taskManager.addSubTask(subTask).getTaskId();
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка создания файла в тестах");
        }
    }

    @Test
    void addEpicTaskAndSubTaskForHimShouldReturnSameEpicId() {
        assertEquals(subTask.getEpicTaskId(), epicId);
    }

    @Test
    void addEpicTaskAndSubTaskForHimShouldReturnSameSubTaskId() {
        assertEquals(epic.getSubTaskIds().getFirst(), subTaskId);
    }

    @Test
    void loadFromFileTest() {
        try (Writer fileWriter = new FileWriter(file)) {
            fileWriter.append("id,type,name,status,description,startTime,duration,epic")
                      .append(String.format("%n"))
                      .append("1,TASK,Test addNewTask,NEW,Test addNewTask description,2025-01-01T00:00,60")
                      .append(String.format("%n"))
                      .append("2,EPIC,Test addNewEpicTask,NEW,Test addNewEpicTask description,2025-01-01T01:00,60")
                      .append(String.format("%n"))
                      .append("3,SUBTASK,Test addNewSubTask,NEW,Test addNewSubTask description,2025-01-01T01:00,60,2")
                      .append(String.format("%n"));
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при заполнении файла в тесте!");
        }

        taskManager = FileBackedTaskManager.loadFromFile(file);

        assertNotNull(taskManager.getTaskById(1));
        assertNotNull(taskManager.getEpicById(2));
        assertNotNull(taskManager.getSubTaskById(3));
    }

    @Test
    void throwExceptionWhenLoadFromFileTest() {
        assertThrows(ManagerSaveException.class, ()-> {
            file = new File("");
            taskManager = FileBackedTaskManager.loadFromFile(file);
        }, "Попытка загрузить пустой файл должна приводить к исключению");

    }

    @Test
    void saveToFileTest() {
        String fileContent = "";

        try (FileReader reader = new FileReader(file.getName())) {
            BufferedReader br = new BufferedReader(reader);

            while (br.ready()) {
                fileContent = fileContent.concat(br.readLine());
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assertEquals("id,type,name,status,description,startTime,duration,epic" +
                        "1,TASK,Test addNewTask,NEW,Test addNewTask description,2025-01-01T00:00,60" +
                        "2,EPIC,Test addNewEpicTask,NEW,Test addNewEpicTask description,2025-01-01T01:00,60" +
                        "3,SUBTASK,Test addNewSubTask,NEW,Test addNewSubTask description,2025-01-01T01:00,60,2", fileContent);
    }

    @Test
    void loadFromEmptyFileTest() {
        try (Writer fileWriter = new FileWriter(file)) {
            fileWriter.append("id,type,name,status,description,startTime,duration,epic")
                      .append(String.format("%n"));
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при заполнении файла в тесте!");
        }
        taskManager = FileBackedTaskManager.loadFromFile(file);

        assertTrue(taskManager.getTasks().isEmpty());
        assertTrue(taskManager.getEpics().isEmpty());
        assertTrue(taskManager.getSubTasks().isEmpty());
    }

    @Test
    void saveToFileVoidDataTest() {
        taskManager.removeAll();
        String fileContent = "";

        taskManager.save();

        try (FileReader reader = new FileReader(file.getName())) {
            BufferedReader br = new BufferedReader(reader);

            while (br.ready()) {
                fileContent = fileContent.concat(br.readLine());
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assertEquals("id,type,name,status,description,startTime,duration,epic", fileContent);
    }
}