public class SubTask extends Task{
    private Integer epicTaskId;
    private TaskTypes taskType = TaskTypes.SUBTASK;

    SubTask(String name, String description, int taskId, TaskStatuses status, Integer epicTaskId) {
        super(name, description, taskId, status);
        this.epicTaskId = epicTaskId;
    }

    public Integer getEpicTaskId() {
        return epicTaskId;
    }

    @Override
    public TaskTypes getTaskType() {
        return taskType;
    }
}
