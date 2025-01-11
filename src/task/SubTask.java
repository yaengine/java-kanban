package task;

import java.time.Duration;
import java.time.LocalDateTime;

public class SubTask extends Task {
    private Integer epicTaskId;

    public SubTask(String name, String description, TaskStatus status, int epicTaskId, LocalDateTime startTime, Duration duration) {
        super(name, description, status, startTime, duration);
        this.epicTaskId = epicTaskId;
    }

    public SubTask(String name, String description, TaskStatus status, int epicTaskId, int subTaskId, LocalDateTime startTime, Duration duration) {
        super(name, description, status, subTaskId, startTime, duration);
        this.epicTaskId = epicTaskId;
    }

    public Integer getEpicTaskId() {
        return epicTaskId;
    }

    public void clearIds() {
        this.epicTaskId = null;
        this.taskId = null;
    }

    @Override
    public String toString() {
        return String.format("%d,%s,%s,%s,%s,%s,%s,%d", taskId, TaskType.SUBTASK, name, status, description, startTime, duration.toMinutes(), epicTaskId);
    }

}
