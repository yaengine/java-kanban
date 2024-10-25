import java.util.HashMap;

public class TaskManager {
    public static int idsCounter;
    HashMap<Integer, Task> tasks;
    HashMap<Integer, SubTask> subTasks;
    HashMap<Integer, Epic> epics;

    public TaskManager() {
        this.tasks = new HashMap<>();
        this.subTasks = new HashMap<>();
        this.epics = new HashMap<>();
        idsCounter = 1;
    }

    public void setTask(Task task) {
        tasks.put(task.getTaskId(), task);
    }

    public void setSubTask(SubTask subTask) {
        subTasks.put(subTask.getTaskId(), subTask);
        updateEpicStatus(subTask.getEpicTaskId());
    }

    public void setEpic(Epic epic) {
        epics.put(epic.getTaskId(), epic);
        updateEpicStatus(epic.getTaskId());
    }

    public HashMap<Integer, Task> getTasks() {
        return tasks;
    }

    public HashMap<Integer, SubTask> getSubTasks() {
        return subTasks;
    }

    public HashMap<Integer, Epic> getEpics() {
        return epics;
    }

    public Integer getNewId() {
        return idsCounter++;
    }

    public void deleteAllTasks() {
        epics.clear();
        tasks.clear();
        subTasks.clear();
    }

    public Task getTaskById(int taskId, TaskTypes taskType) {
        if (taskType == null) {
            if (tasks.containsKey(taskId)) {
                return tasks.get(taskId);
            } else if (epics.containsKey(taskId)) {
                return epics.get(taskId);
            } else if (subTasks.containsKey(taskId)) {
                return subTasks.get(taskId);
            }
            return null;
        } else {
            switch (taskType) {
                case TaskTypes.TASK:
                    return tasks.get(taskId);
                case TaskTypes.SUBTASK:
                    return subTasks.get(taskId);
                case TaskTypes.EPIC:
                    return epics.get(taskId);
                default:
                    return null;
            }
        }
    }

    public boolean deleteTaskById(int taskId) {
        if (tasks.containsKey(taskId)) {
            tasks.remove(taskId);
            return true;
        } else if (epics.containsKey(taskId)) {
            epics.remove(taskId);
            return true;
        } else if (subTasks.containsKey(taskId)) {
            int epicId = subTasks.get(taskId).getEpicTaskId();
            subTasks.remove(taskId);
            updateEpicStatus(epicId);
            return true;
        }
        return false;
    }

    public HashMap<Integer,SubTask> findSubTasks(int epicId) {
        HashMap<Integer, SubTask> retSubTasks = new HashMap<>();
        for (SubTask subTask: subTasks.values()){
            if (epicId == subTask.getEpicTaskId()){
                retSubTasks.put(subTask.getTaskId(), subTask);
            }
        }
        return retSubTasks;
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = (Epic) getTaskById(epicId, TaskTypes.EPIC);
        HashMap<Integer, SubTask> epicSubTasks = findSubTasks(epicId);
        int newCounter = 0;
        int doneCounter = 0;
        int inPrgCounter = 0;
        if (!epicSubTasks.isEmpty()) {
          for (SubTask subTask: epicSubTasks.values()) {
              if (TaskStatuses.NEW.equals(subTask.getStatus())){
                  newCounter++;
              } else if (TaskStatuses.IN_PROGRESS.equals(subTask.getStatus())){
                  inPrgCounter++;
              } else if (TaskStatuses.DONE.equals(subTask.getStatus())){
                  doneCounter++;
              }
          }
          if (newCounter > 0 && doneCounter == 0 && inPrgCounter == 0) {
              epic.setStatus(TaskStatuses.NEW);
          } else if (doneCounter > 0 && newCounter == 0 && inPrgCounter == 0) {
              epic.setStatus(TaskStatuses.DONE);
          } else {
              epic.setStatus(TaskStatuses.IN_PROGRESS);
          }
            epics.put(epicId, epic);
        } else {
            epic.setStatus(TaskStatuses.NEW);
        }
    }

}
