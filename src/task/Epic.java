package task;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    List<Integer> subTaskIds;

    public Epic(String name, String description, TaskStatus status) {
        super(name, description, status);
        this.subTaskIds = new ArrayList<>();
    }

    public Epic(String name, String description, TaskStatus status, int epicTaskId, List<Integer> subTaskIds) {
        super(name, description, status, epicTaskId);
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
}
