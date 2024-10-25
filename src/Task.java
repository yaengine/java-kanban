public class Task {
    private String name;
    private String description;
    private int taskId;
    private TaskStatuses status;
    private TaskTypes taskType = TaskTypes.TASK;

    Task(String name, String description, int taskId, TaskStatuses status) {
        this.name = name;
        this.description = description;
        this.taskId = taskId;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public int getTaskId() {
        return taskId;
    }

    public TaskStatuses getStatus() {
        return status;
    }

    public void setStatus(TaskStatuses status) {
        this.status = status;
    }

    public TaskTypes getTaskType() {
        return taskType;
    }

    @Override
    public String toString() {
        return getTaskType() + ": {" +
                "Номер: " + taskId +
                ", Имя: '" + name + '\'' +
                ", Описание: '" + description + '\'' +
                ", Статус: " + status +
                '}';
    }
}
