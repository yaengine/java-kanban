import manager.TaskManager;
import task.*;

public class Main {
    static TaskManager taskManager = new TaskManager();

    public static void main(String[] args) {
        tests();
    }

    public static void tests() {
        String taskName;
        String taskDesc;
        int epicTaskId;
        Epic epic;

        taskName = "taskName test 1";
        taskDesc = "taskDesc test 1";
        taskManager.addTask(new Task(taskName, taskDesc, TaskStatus.NEW));

        taskName = "taskName test 2";
        taskDesc = "taskDesc test 2";
        taskManager.addTask(new Task(taskName, taskDesc, TaskStatus.NEW));

        taskName = "EpicName test 1";
        taskDesc = "EpicDesc test 1";
        epic = taskManager.addEpic(new Epic(taskName, taskDesc, TaskStatus.NEW));
        epicTaskId = epic.getTaskId();

        taskName = "SubTaskName for epic " + epicTaskId + " test 1";
        taskDesc = "SubTaskDesc for epic " + epicTaskId + " test 1";
        taskManager.addSubTask(new SubTask(taskName, taskDesc, TaskStatus.NEW, epicTaskId));
        taskName = "SubTaskName for epic " + epicTaskId + " test 2";
        taskDesc = "SubTaskDesc for epic " + epicTaskId + " test 2";
        taskManager.addSubTask(new SubTask(taskName, taskDesc, TaskStatus.IN_PROGRESS, epicTaskId));

        taskName = "EpicName test 2";
        taskDesc = "EpicDesc test 2";
        epic = taskManager.addEpic(new Epic(taskName, taskDesc, TaskStatus.NEW));
        epicTaskId = epic.getTaskId();

        taskName = "SubTaskName for epic " + epicTaskId + " test 3";
        taskDesc = "SubTaskDesc for epic " + epicTaskId + " test 3";
        taskManager.addSubTask(new SubTask(taskName, taskDesc, TaskStatus.DONE, epicTaskId));

        System.out.println("Создали задачи:");
        printAllTasks();

        for (Task task: taskManager.getTasks()) {
            TaskStatus newStatus;
            newStatus = switch (task.getStatus()) {
                case NEW -> TaskStatus.IN_PROGRESS;
                case IN_PROGRESS -> TaskStatus.DONE;
                case DONE -> TaskStatus.NEW;
            };
            taskManager.updateTask(new Task(task.getName(), task.getDescription(), newStatus, task.getTaskId()));
        }

        for (SubTask subTask: taskManager.getSubTasks()) {
            TaskStatus newStatus;
            newStatus = switch (subTask.getStatus()) {
                case NEW -> TaskStatus.IN_PROGRESS;
                case IN_PROGRESS -> TaskStatus.DONE;
                case DONE -> TaskStatus.NEW;
            };
            taskManager.updateSubTask(new SubTask(subTask.getName(), subTask.getDescription(), newStatus, subTask.getEpicTaskId(), subTask.getTaskId()));
        }

        System.out.println("Обновили статусы задач:");
        printAllTasks();

        int taskToDel = taskManager.getTasks().getLast().getTaskId();
        int epicToDel = taskManager.getEpics().getLast().getTaskId();
        taskManager.deleteTaskById(taskToDel);
        taskManager.deleteEpicById(epicToDel);

        System.out.printf("Удалили задачу:%d и эпик:%d", taskToDel, epicToDel);
        System.out.println();
        printAllTasks();

        System.out.println("Удалили подзадачи':");
        taskManager.removeAllSubTasks();
        printAllTasks();

        System.out.println("Удалили эпики':");
        taskManager.removeAllEpics();
        printAllTasks();

        System.out.println("Удалили задачи:");
        taskManager.removeAllTasks();
        printAllTasks();
    }

    public static void printAllTasks() {
        for (Epic epic: taskManager.getEpics()) {
            System.out.println(epic.toString());
        }
        for (Task task: taskManager.getTasks()) {
            System.out.println(task.toString());
        }
        for (SubTask subTask: taskManager.getSubTasks()) {
            System.out.println(subTask.toString());
        }
        System.out.println("-".repeat(70));
    }
}
