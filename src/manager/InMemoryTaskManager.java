package manager;

import task.*;
import util.Managers;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected int idsCounter;
    protected Map<Integer, Task> tasks;
    protected Map<Integer, SubTask> subTasks;
    protected Map<Integer, Epic> epics;
    private HistoryManager historyManager;
    private Set<Task> sortedTasks;
    private Set<SubTask> sortedSubTasks;
    private static final String TASK_CROSS_ERROR = "Ошибка! Задача имеет пересечение по времени выполнения!";

    public InMemoryTaskManager() {
        this.tasks = new HashMap<>();
        this.subTasks = new HashMap<>();
        this.epics = new HashMap<>();
        this.historyManager = Managers.getDefaultHistory();
        this.sortedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));
        this.sortedSubTasks = new TreeSet<>(Comparator.comparing(SubTask::getStartTime));
        idsCounter = 1;
    }

    @Override
    public Task addTask(Task task) {
        if (isCrossTask(task)) {
            throw new IllegalArgumentException(TASK_CROSS_ERROR);
        }
        task.setTaskId(getNewId());
        tasks.put(task.getTaskId(), task);

        if (task.getStartTime() != null) {
                sortedTasks.add(task);
        }
        return task;
    }

    @Override
    public SubTask addSubTask(SubTask subTask) {
        if (isCrossTask(subTask)) {
            throw new IllegalArgumentException(TASK_CROSS_ERROR);
        }
        subTask.setTaskId(getNewId());
        subTasks.put(subTask.getTaskId(), subTask);
        getEpicById(subTask.getEpicTaskId()).addSubTaskId(subTask.getTaskId()); //Добавляем сабтаску эпику
        updateEpicStatus(subTask.getEpicTaskId());

        if (subTask.getStartTime() != null) {
            sortedSubTasks.add(subTask);
            updateEpicStartTime(subTask.getEpicTaskId());
            updateEpicDuration(subTask.getEpicTaskId());
        }
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
        if (isCrossTask(task)) {
            throw new IllegalArgumentException(TASK_CROSS_ERROR);
        }
        if (isCorrectTaskId(task.getTaskId())) {
            tasks.put(task.getTaskId(), task);

            if (task.getStartTime() != null) {
                Optional<Task> taskToRem = sortedTasks.stream()
                        .filter(t -> t.getTaskId() == task.getTaskId())
                        .findFirst();
                taskToRem.ifPresent(t -> sortedTasks.remove(t));
                sortedTasks.add(task);
            }
        }
    }

    @Override
    public void updateSubTask(SubTask subTask) {
        if (isCrossTask(subTask)) {
            throw new IllegalArgumentException(TASK_CROSS_ERROR);
        }
        if (subTask.getTaskId() != subTask.getEpicTaskId() && isCorrectTaskId(subTask.getTaskId())) {
            subTasks.put(subTask.getTaskId(), subTask);
            updateEpicStatus(subTask.getEpicTaskId());

            if (subTask.getStartTime() != null) {
                Optional<SubTask> taskToRem = sortedSubTasks.stream()
                        .filter(t -> t.getTaskId() == subTask.getTaskId())
                        .findFirst();
                taskToRem.ifPresent(t -> sortedSubTasks.remove(t));
                sortedSubTasks.add(subTask);
                updateEpicStartTime(subTask.getEpicTaskId());
                updateEpicDuration(subTask.getEpicTaskId());
            }
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
        return epic.getSubTaskIds().stream()
                .map(this::getSubTaskById)
                .collect(Collectors.toList());
    }

    private Integer getNewId() {
        return idsCounter++;
    }

    @Override
    public void removeAll() {
        epics.clear();
        tasks.clear();
        subTasks.clear();
        sortedTasks.clear();
        sortedSubTasks.clear();
        historyManager = Managers.getDefaultHistory();
    }

    @Override
    public void removeAllTasks() {
        for (Integer taskId: tasks.keySet()) {
            historyManager.remove(taskId);
        }
        tasks.clear();
        sortedTasks.clear();
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
        sortedSubTasks.clear();
        epics.clear();
    }

    @Override
    public void removeAllSubTasks() {
        //Т.к. удаляем все сабтаски, то можем просто очистить их списки у эпиков.
        for (Epic epic: epics.values()) {
            epic.getSubTaskIds().clear();
            updateEpicStatus(epic.getTaskId());
            epic.setDuration(Duration.ZERO);
            epic.setStartTime(null);
        }
        for (Integer taskId: subTasks.keySet()) {
            historyManager.remove(taskId);
        }
        subTasks.clear();
        sortedSubTasks.clear();
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
            sortedTasks.remove(tasks.get(taskId));
            tasks.remove(taskId);
            historyManager.remove(taskId);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteSubTaskById(int taskId) {
        if (subTasks.containsKey(taskId)) {
            sortedSubTasks.remove(subTasks.get(taskId));
            SubTask subTask = subTasks.get(taskId);
            int epicId = subTask.getEpicTaskId();
            getEpicById(epicId).removeSubTaskId(taskId); //Удаляем сабтаску из списка эпика.
            subTasks.remove(taskId);
            updateEpicStatus(epicId);
            updateEpicDuration(epicId);
            updateEpicStartTime(epicId);
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

    private void updateEpicStartTime(int epicId) {
        Epic epic = getEpicById(epicId);
        sortedSubTasks.stream()
                .findFirst()
                .ifPresentOrElse(t -> epic.setStartTime(t.getStartTime()),
                                () -> epic.setStartTime(null));
    }

    private void updateEpicDuration(int epicId) {
        Epic epic = getEpicById(epicId);
        epic.setDuration(sortedSubTasks.stream()
                        .map(SubTask::getDuration)
                        .reduce(Duration.ZERO, Duration::plus));
    }

    public TreeSet<Task> getPrioritizedTasks() {
        TreeSet<Task> priorTasks = (TreeSet<Task>) sortedTasks;
        priorTasks.addAll(sortedSubTasks);
        return priorTasks;
    }

    public boolean isCrossTask(Task task) {
        TreeSet<Task> srtTasks = getPrioritizedTasks();
        return srtTasks
                .stream()
                .filter(t -> !(t instanceof Epic) && !Objects.equals(t.getTaskId(), task.getTaskId()))
                .anyMatch(t -> ((task.getStartTime().isAfter(t.getStartTime()) &&
                                      task.getStartTime().isBefore(t.getEndTime())) ||
                                 (t.getEndTime().isAfter(task.getStartTime()) &&
                                      t.getEndTime().isBefore(task.getEndTime())) ||
                                 task.getStartTime() == t.getStartTime() ||
                                      t.getEndTime() == task.getEndTime())
                         );
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private boolean isCorrectTaskId(int taskId) {
        return (taskId >= 1 && taskId < idsCounter);
    }
}
