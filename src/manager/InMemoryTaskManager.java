package manager;

import task.*;
import util.Managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    protected int idsCounter;
    protected HashMap<Integer, Task> tasks;
    protected HashMap<Integer, SubTask> subTasks;
    protected HashMap<Integer, Epic> epics;
    private HistoryManager historyManager;

    public InMemoryTaskManager() {
        this.tasks = new HashMap<>();
        this.subTasks = new HashMap<>();
        this.epics = new HashMap<>();
        this.historyManager = Managers.getDefaultHistory();
        idsCounter = 1;
    }

    @Override
    public Task addTask(Task task) {
        task.setTaskId(getNewId());
        tasks.put(task.getTaskId(), task);
        return task;
    }

    @Override
    public SubTask addSubTask(SubTask subTask) {
        subTask.setTaskId(getNewId());
        subTasks.put(subTask.getTaskId(), subTask);
        getEpicById(subTask.getEpicTaskId()).addSubTaskId(subTask.getTaskId()); //Добавляем сабтаску эпику
        updateEpicStatus(subTask.getEpicTaskId());
        return subTask;
    }

    @Override
    public Epic addEpic(Epic epic) {
        epic.setTaskId(getNewId());
        epics.put(epic.getTaskId(), epic);
        return epic;
    }

    @Override
    public void updateTask(Task task) {
        if (isCorrectTaskId(task.getTaskId())) {
            tasks.put(task.getTaskId(), task);
        }
    }

    @Override
    public void updateSubTask(SubTask subTask) {
        if (subTask.getTaskId() != subTask.getEpicTaskId() && isCorrectTaskId(subTask.getTaskId())) {
            subTasks.put(subTask.getTaskId(), subTask);
            updateEpicStatus(subTask.getEpicTaskId());
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (isCorrectTaskId(epic.getTaskId())) {
            epics.put(epic.getTaskId(), epic);
        }
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<SubTask> getSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<SubTask> getSubTasksByEpic(Epic epic) {
        List<SubTask> epicSubTasks = new ArrayList<>();
        for (int subTaskId: epic.getSubTaskIds()) {
            epicSubTasks.add(getSubTaskById(subTaskId));
        }
        return epicSubTasks;
    }

    private Integer getNewId() {
        return idsCounter++;
    }

    @Override
    public void removeAll() {
        epics.clear();
        tasks.clear();
        subTasks.clear();
        historyManager = Managers.getDefaultHistory();
    }

    @Override
    public void removeAllTasks() {
        for (Integer taskId: tasks.keySet()) {
            historyManager.remove(taskId);
        }
        tasks.clear();
    }

    @Override
    public void removeAllEpics() {
        for (Integer taskId: subTasks.keySet()) {
            historyManager.remove(taskId);
        }
        for (Integer taskId: epics.keySet()) {
            historyManager.remove(taskId);
        }
        subTasks.clear();
        epics.clear();
    }

    @Override
    public void removeAllSubTasks() {
        //Т.к. удаляем все сабтаски, то можем просто очистить их списки у эпиков.
        for (Epic epic: epics.values()) {
            epic.getSubTaskIds().clear();
            updateEpicStatus(epic.getTaskId());
        }
        for (Integer taskId: subTasks.keySet()) {
            historyManager.remove(taskId);
        }
        subTasks.clear();
    }

    @Override
    public Task getTaskById(int taskId) {
        Task retVal = tasks.get(taskId);
        historyManager.add(retVal);
        return retVal;
    }

    @Override
    public SubTask getSubTaskById(int taskId) {
        SubTask retVal = subTasks.get(taskId);
        historyManager.add(retVal);
        return retVal;
    }

    @Override
    public Epic getEpicById(int taskId) {
        Epic retVal = epics.get(taskId);
        historyManager.add(retVal);
        return retVal;
    }

    @Override
    public boolean deleteTaskById(int taskId) {
        if (tasks.containsKey(taskId)) {
            tasks.remove(taskId);
            historyManager.remove(taskId);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteSubTaskById(int taskId) {
        if (subTasks.containsKey(taskId)) {
            SubTask subTask = subTasks.get(taskId);
            int epicId = subTask.getEpicTaskId();
            getEpicById(epicId).removeSubTaskId(taskId); //Удаляем сабтаску из списка эпика.
            subTasks.remove(taskId);
            updateEpicStatus(epicId);
            subTask.clearIds();
            historyManager.remove(taskId);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteEpicById(int taskId) {
        if (epics.containsKey(taskId)) {
            for (int subTaskId: getEpicById(taskId).getSubTaskIds()) {
                subTasks.remove(subTaskId);
                historyManager.remove(subTaskId);
            }
            epics.remove(taskId);
            historyManager.remove(taskId);
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

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private boolean isCorrectTaskId(int taskId) {
        return (taskId >= 1 && taskId < idsCounter);
    }

}
