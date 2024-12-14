package manager;

import exceptions.ManagerSaveException;
import task.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public void save() {
        try (Writer fileWriter = new FileWriter(file.getName())) {
            fileWriter.append("id,type,name,status,description,epic");
            fileWriter.append(String.format("%n"));
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении в файл!");
        }
        try (Writer fileWriter = new FileWriter(file.getName(),true)) {
            for (Task task:tasks.values()) {
                fileWriter.append(task.toString());
                fileWriter.append(String.format("%n"));
            }
            for (Task task:epics.values()) {
                fileWriter.append(task.toString());
                fileWriter.append(String.format("%n"));
            }
            for (Task task:subTasks.values()) {
                fileWriter.append(task.toString());
                fileWriter.append(String.format("%n"));
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении в файл!");
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager taskManager = new FileBackedTaskManager(file);
        String fileContent;
        boolean isFirstString = true;
        try {
            fileContent = Files.readString(file.toPath());
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении в файл!");
        }
        String[] fileContents = fileContent.split(String.format("%n"));
        for (String fContent: fileContents) {
            if (!isFirstString) {
                Task task = taskManager.fromString(fContent);
                if (task instanceof Epic) {
                    taskManager.epics.put(task.getTaskId(),(Epic) task);
                } else if (task instanceof SubTask) {
                    taskManager.subTasks.put(task.getTaskId(), (SubTask) task);
                    /*Добавим эпику подзадачу*/
                    taskManager.getEpicById(((SubTask) task).getEpicTaskId()).getSubTaskIds().add(task.getTaskId());
                } else {
                    taskManager.tasks.put(task.getTaskId(), task);
                }
                if (task.getTaskId() >= taskManager.idsCounter) {
                    taskManager.idsCounter = task.getTaskId() + 1;
                }
            }
            isFirstString = false;
        }
        return taskManager;
    }

    private Task fromString(String value) {
        Task retTask = null;
        String[] values = value.split(",");
        Integer taskId = Integer.parseInt(values[0]);
        String taskType = values[1];
        String name = values[2];
        TaskStatus status = TaskStatus.valueOf(values[3]);
        String description = values[4];
        Integer epicTaskId;

        if (taskType.equals(TaskType.TASK.name())) {
            retTask = new Task(name, description, status, taskId);
        } else if (taskType.equals(TaskType.EPIC.name())) {
            retTask = new Epic(name, description, status, taskId, new ArrayList<>());
        } else if (taskType.equals(TaskType.SUBTASK.name())) {
            epicTaskId = Integer.parseInt(values[5]);
            retTask = new SubTask(name, description, status, epicTaskId, taskId);
        }
        return retTask;
    }

    @Override
    public Task addTask(Task task) {
        super.addTask(task);
        save();
        return task;
    }

    @Override
    public SubTask addSubTask(SubTask subTask) {
        super.addSubTask(subTask);
        save();
        return subTask;
    }

    @Override
    public Epic addEpic(Epic epic) {
        super.addEpic(epic);
        save();
        return epic;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateSubTask(SubTask subTask) {
        super.updateSubTask(subTask);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void removeAll() {
        super.removeAll();
        save();
    }

    @Override
    public void removeAllTasks() {
        super.removeAllTasks();
        save();
    }

    @Override
    public void removeAllEpics() {
        super.removeAllEpics();
        save();
    }

    @Override
    public void removeAllSubTasks() {
        super.removeAllSubTasks();
        save();
    }

    @Override
    public boolean deleteTaskById(int taskId) {
        boolean isDel = super.deleteTaskById(taskId);
        save();
        return isDel;
    }

    @Override
    public boolean deleteSubTaskById(int taskId) {
        boolean isDel = super.deleteSubTaskById(taskId);
        save();
        return isDel;
    }

    @Override
    public boolean deleteEpicById(int taskId) {
        boolean isDel = super.deleteEpicById(taskId);
        save();
        return isDel;
    }
}
