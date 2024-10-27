package task;

public class SubTask extends Task {
    private int epicTaskId;

    public SubTask(String name, String description, TaskStatus status, int epicTaskId) {
        super(name, description, status);
        this.epicTaskId = epicTaskId;
    }

    public SubTask(String name, String description, TaskStatus status, int epicTaskId, int subTaskId) {
        super(name, description, status, subTaskId);
        this.epicTaskId = epicTaskId;
    }

    public Integer getEpicTaskId() {
        return epicTaskId;
    }

}
