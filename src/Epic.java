public class Epic extends Task{
    private TaskTypes taskType = TaskTypes.EPIC;

    Epic(String name, String description, int taskId, TaskStatuses status) {
        super(name, description, taskId, status);
    }

    @Override
    public TaskTypes getTaskType() {
        return taskType;
    }
}
