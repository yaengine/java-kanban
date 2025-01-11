package task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    List<Integer> subTaskIds;

    public Epic(String name, String description, TaskStatus status) {
        super(name, description, status, null, Duration.ZERO);
        this.subTaskIds = new ArrayList<>();
    }

    public Epic(String name, String description, TaskStatus status, int epicTaskId, List<Integer> subTaskIds, LocalDateTime startTime, Duration duration) {
        super(name, description, status, epicTaskId, startTime, duration);
        this.subTaskIds = subTaskIds;
    }

    public void addSubTaskId(int subTaskId) {
        if (subTaskId != this.taskId) {
            this.subTaskIds.add(subTaskId);
        }
    }

    public void removeSubTaskId(int subTaskId) {
        this.subTaskIds.remove(this.subTaskIds.indexOf(subTaskId));
    }

    public List<Integer> getSubTaskIds() {
        return subTaskIds;
    }

    @Override
    public String toString() {
        return String.format("%d,%s,%s,%s,%s,%s,%d", taskId, TaskType.EPIC, name, status, description, startTime,
                startTime == null ? Duration.ZERO.toMinutes() : Duration.between(startTime, getEndTime()).toMinutes());
    }
}
