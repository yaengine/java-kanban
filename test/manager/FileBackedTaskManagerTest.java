package manager;

import exceptions.ManagerSaveException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.*;
import util.Managers;

import java.io.*;

import static manager.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    File file;
    FileBackedTaskManager taskManager;

    @BeforeEach
    void beforeEach() {
        try {
            file = File.createTempFile(FILE_PREFIX, FILE_POSTFIX);
            taskManager = Managers.getFileBackedTaskManager(file);
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка создания файла в тестах");
        }
    }
    @Test
    void shouldAddTaskInAndGet1BackFromFileManager() {
        Task task = new Task(NEW_TASK_NAME, NEW_TASK_DESC, TaskStatus.NEW);
        final int taskId = taskManager.addTask(task).getTaskId();

        final Task savedTask = taskManager.getTaskById(taskId);

        assertNotNull(savedTask, TASK_NOT_FOUND_ERR);
        assertEquals(task, savedTask, TASK_NOT_MATCH_ERR);
    }

    @Test
    void addEpicTaskAndSubTaskForHimShouldReturnSameEpicId() {
        Epic epic = new Epic(NEW_EPIC_NAME, NEW_EPIC_DESC, TaskStatus.NEW);
        final int epicId = taskManager.addEpic(epic).getTaskId();

        SubTask subTask = new SubTask(NEW_SUBTASK_NAME, NEW_SUBTASK_DESC, TaskStatus.NEW, epicId);
        final int SubTaskId = taskManager.addSubTask(subTask).getTaskId();

        assertEquals(subTask.getEpicTaskId(), epicId);
    }

    @Test
    void addEpicTaskAndSubTaskForHimShouldReturnSameSubTaskId() {
        Epic epic = new Epic(NEW_EPIC_NAME, NEW_EPIC_DESC, TaskStatus.NEW);
        final int epicId = taskManager.addEpic(epic).getTaskId();

        SubTask subTask = new SubTask(NEW_SUBTASK_NAME, NEW_SUBTASK_DESC, TaskStatus.NEW, epicId);
        final int SubTaskId = taskManager.addSubTask(subTask).getTaskId();

        assertEquals(epic.getSubTaskIds().getFirst(), SubTaskId);
    }

    @Test
    void loadFromFileTest() {
        try (Writer fileWriter = new FileWriter(file)) {
            fileWriter.append("id,type,name,status,description,epic")
                      .append(String.format("%n"))
                      .append("1,TASK,Test addNewTask,NEW,Test addNewTask description,")
                      .append(String.format("%n"))
                      .append("2,EPIC,Test addNewEpicTask,NEW,Test addNewEpicTask description,")
                      .append(String.format("%n"))
                      .append("3,SUBTASK,Test addNewSubTask,NEW,Test addNewSubTask description,2")
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
    void saveToFileTest() {
        Task task = new Task(NEW_TASK_NAME, NEW_TASK_DESC, TaskStatus.NEW);
        taskManager.addTask(task).getTaskId();
        Epic epic = new Epic(NEW_EPIC_NAME, NEW_EPIC_DESC, TaskStatus.NEW);
        final int epicId = taskManager.addEpic(epic).getTaskId();
        SubTask subTask = new SubTask(NEW_SUBTASK_NAME, NEW_SUBTASK_DESC, TaskStatus.NEW, epicId);
        taskManager.addSubTask(subTask).getTaskId();
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

        assertEquals("id,type,name,status,description,epic" +
                        "1,TASK,Test addNewTask,NEW,Test addNewTask description," +
                        "2,EPIC,Test addNewEpicTask,NEW,Test addNewEpicTask description," +
                        "3,SUBTASK,Test addNewSubTask,NEW,Test addNewSubTask description,2", fileContent);
    }

    @Test
    void loadFromEmptyFileTest() {
        try (Writer fileWriter = new FileWriter(file)) {
            fileWriter.append("id,type,name,status,description,epic")
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

        assertEquals("id,type,name,status,description,epic", fileContent);
    }
}