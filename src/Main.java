import manager.TaskManager;
import task.*;

public class Main {
    static TaskManager taskManager = new TaskManager();

    public static void main(String[] args) {
        tests();

        for (Epic epic: taskManager.getEpics()) {
            System.out.println(epic.toString());
        }
        for (Task task: taskManager.getTasks()) {
            System.out.println(task.toString());
        }
        for (SubTask subTask: taskManager.getSubTasks()) {
            System.out.println(subTask.toString());
        }
    }

    public static void tests() {
        String taskName;
        String taskDesc;
        int epicTaskId;
        taskName = "taskName test 1";
        taskDesc = "taskDesc test 1";
        taskManager.addTask(new Task(taskName, taskDesc, TaskStatus.NEW));

        taskName = "taskName test 2";
        taskDesc = "taskDesc test 2";
        taskManager.addTask(new Task(taskName, taskDesc, TaskStatus.NEW));

        taskName = "EpicName test 1";
        taskDesc = "EpicDesc test 1";
        epicTaskId = 3;
        taskManager.addEpic(new Epic(taskName, taskDesc, TaskStatus.NEW));

        taskName = "SubTaskName for epic " + epicTaskId + " test 1";
        taskDesc = "SubTaskDesc for epic " + epicTaskId + " test 1";
        taskManager.addSubTask(new SubTask(taskName, taskDesc, TaskStatus.NEW, epicTaskId));
        taskName = "SubTaskName for epic " + epicTaskId + " test 2";
        taskDesc = "SubTaskDesc for epic " + epicTaskId + " test 2";
        taskManager.addSubTask(new SubTask(taskName, taskDesc, TaskStatus.IN_PROGRESS, epicTaskId));

        taskName = "EpicName test 2";
        taskDesc = "EpicDesc test 2";
        epicTaskId = 6;
        taskManager.addEpic(new Epic(taskName, taskDesc, TaskStatus.NEW));

        taskName = "SubTaskName for epic " + epicTaskId + " test 3";
        taskDesc = "SubTaskDesc for epic " + epicTaskId + " test 3";
        taskManager.addSubTask(new SubTask(taskName, taskDesc, TaskStatus.DONE, epicTaskId));
    }
}
