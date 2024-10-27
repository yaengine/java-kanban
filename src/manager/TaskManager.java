package manager;

import task.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskManager {
    private int idsCounter;
    private HashMap<Integer, Task> tasks;
    private HashMap<Integer, SubTask> subTasks;
    private HashMap<Integer, Epic> epics;

    public TaskManager() {
        this.tasks = new HashMap<>();
        this.subTasks = new HashMap<>();
        this.epics = new HashMap<>();
        idsCounter = 1;
    }

    public void addTask(Task task) {
        task.setTaskId(getNewId());
        tasks.put(task.getTaskId(), task);
    }

    public void addSubTask(SubTask subTask) {
        subTask.setTaskId(getNewId());
        subTasks.put(subTask.getTaskId(), subTask);
        getEpicById(subTask.getEpicTaskId()).addSubTaskId(subTask.getTaskId()); //Добавляем сабтаску эпику
        updateEpicStatus(subTask.getEpicTaskId());
    }

    public void addEpic(Epic epic) {
        epic.setTaskId(getNewId());
        epics.put(epic.getTaskId(), epic);
    }

    public void updateTask(Task task) {
        tasks.put(task.getTaskId(), task);
    }

    public void updateSubTask(SubTask subTask) {
        subTasks.put(subTask.getTaskId(), subTask);
        updateEpicStatus(subTask.getEpicTaskId());
    }

    public void updateEpic(Epic epic) {
        epics.put(epic.getTaskId(), epic);
    }

    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    public List<SubTask> getSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    public List<SubTask> getSubTasksByEpic(Epic epic) {
        List<SubTask> epicSubTasks = new ArrayList<>();
        for (int subTaskId: epic.getSubTaskIds()){
            epicSubTasks.add(getSubTaskById(subTaskId));
        }
        return epicSubTasks;
    }

    private Integer getNewId() {
        return idsCounter++;
    }

    public void removeAllTasks() {
        epics.clear();
        tasks.clear();
        subTasks.clear();
    }

    public Task getTaskById(int taskId) {
        return tasks.get(taskId);
    }

    public SubTask getSubTaskById(int taskId) {
        return subTasks.get(taskId);
    }

    public Epic getEpicById(int taskId) {
        return epics.get(taskId);
    }

    public boolean deleteTaskById(int taskId) {
        if (tasks.containsKey(taskId)) {
            tasks.remove(taskId);
            return true;
        }
        return false;
    }

    public boolean deleteSubTaskById(int taskId) {
        if (subTasks.containsKey(taskId)) {
            int epicId = subTasks.get(taskId).getEpicTaskId();
            getEpicById(epicId).removeSubTaskId(taskId); //Удаляем сабтаску из списка эпика.
            subTasks.remove(taskId);
            updateEpicStatus(epicId);
            return true;
        }
        return false;
    }

    public boolean deleteEpicById(int taskId) {
        if (epics.containsKey(taskId)) {
            for (int subTaskId: getEpicById(taskId).getSubTaskIds()) {
                subTasks.remove(subTaskId);
            }
            epics.remove(taskId);
            return true;
        }
        return false;
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = getEpicById(epicId);
        List<SubTask> epicSubTasks = getSubTasksByEpic(epic);
        int newCounter = 0;
        int doneCounter = 0;
        int inPrgCounter = 0;
        if (!epicSubTasks.isEmpty()) {
            for (SubTask subTask : epicSubTasks) {
                if (TaskStatus.NEW.equals(subTask.getStatus())) {
                    newCounter++;
                } else if (TaskStatus.IN_PROGRESS.equals(subTask.getStatus())) {
                    inPrgCounter++;
                } else if (TaskStatus.DONE.equals(subTask.getStatus())) {
                    doneCounter++;
                }
            }
            if (newCounter > 0 && doneCounter == 0 && inPrgCounter == 0) {
                epic.setStatus(TaskStatus.NEW);
            } else if (doneCounter > 0 && newCounter == 0 && inPrgCounter == 0) {
                epic.setStatus(TaskStatus.DONE);
            } else {
                epic.setStatus(TaskStatus.IN_PROGRESS);
            }
            epics.put(epicId, epic);
        } else {
            epic.setStatus(TaskStatus.NEW);
        }
    }

}
