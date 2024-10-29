package task;

public class Task {
    protected String name;
    protected String description;
    protected int taskId;
    protected TaskStatus status;

    public Task(String name, String description, TaskStatus status) {
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public Task(String name, String description, TaskStatus status, int taskId) {
        this.taskId = taskId;
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": {" +
                "Номер: " + taskId +
                ", Имя: '" + name + '\'' +
                ", Описание: '" + description + '\'' +
                ", Статус: " + status +
                '}';
    }
}
