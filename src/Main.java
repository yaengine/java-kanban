import java.util.Scanner;

public class Main {
    static TaskManager taskManager = new TaskManager();

    public static void main(String[] args) {
        tests();
        while (true) {
            Scanner scanner = new Scanner(System.in);
            printMenu();
            String command = scanner.nextLine();
            switch (command) {
                case "1":
                    printAllTasks();
                    break;
                case "2":
                    taskManager.deleteAllTasks();
                    System.out.println("Все задачи успешно удалены");
                    break;
                case "3":
                    System.out.println("Введите номер задачи для поиска:");
                    int findTaskId = scanner.nextInt();
                    scanner.nextLine();
                    Task task = taskManager.getTaskById(findTaskId, null);
                    if (task != null){
                        System.out.println(task);
                    } else {
                        System.out.println("Такой задачи не существует");
                    }
                    break;
                case "4":
                    createTask();
                    break;
                case "5":
                    updateTask();
                    break;
                case "6":
                    System.out.println("Введите номер задачи для удаления:");
                    int delTaskId = scanner.nextInt();
                    scanner.nextLine();
                    if (taskManager.deleteTaskById(delTaskId)){
                        System.out.println("Задача успешно удалена");
                    } else {
                        System.out.println("Такой задачи не существует");
                    }
                    break;
                case "7":
                    findSubTasks();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Такой команды не существует. Повторите ввод.");
                    break;
            }
            System.out.println("-".repeat(50));
            System.out.println();
        }
    }

    public static void printMenu() {
        System.out.println("1 - Получение списка всех задач");
        System.out.println("2 - Удаление всех задач");
        System.out.println("3 - Получение задачи по идентификатору");
        System.out.println("4 - Создать задачу");
        System.out.println("5 - Обновить задачу");
        System.out.println("6 - Удалить по индентификатору");
        System.out.println("7 - Получить список подзадач эпика");
        System.out.println("0 - Выход");
    }

    public static void createTask() {
        Scanner scanner = new Scanner(System.in);
        String epicIds = "";
        System.out.println("Какой тип задачи создаем?");
        System.out.println("1 - простая задача");
        System.out.println("2 - подзадача");
        System.out.println("3 - эпик");
        int taskType = scanner.nextInt();
        scanner.nextLine();
        if (taskType == 2) {
            for (Epic epic: taskManager.getEpics().values()){
                epicIds = epicIds + epic.getTaskId() + ":" + epic.getName() + "; ";
            }
            if (epicIds.isEmpty()){
                System.out.println("Не найдено эпиков, вначале заведите эпик");
                return;
            }
        }
        System.out.println("Введите имя задачи");
        String taskName = scanner.nextLine();
        System.out.println("Введите описание задачи");
        String taskDesc = scanner.nextLine();

        switch (taskType) {
            case 1:
                taskManager.setTask(new Task(taskName, taskDesc, taskManager.getNewId(), TaskStatuses.NEW));
                System.out.println("Задача успешно создана");
                break;
            case 2:
                System.out.println("Введите номер эпика");
                System.out.println("Вот какие эпики у Вас есть (формат EpicNum:EpicName;): " + epicIds);
                taskManager.setSubTask(new SubTask(taskName, taskDesc, taskManager.getNewId(), TaskStatuses.NEW, scanner.nextInt()));
                scanner.nextLine();
                System.out.println("Подзадача успешно создана");
                break;
            case 3:
                taskManager.setEpic(new Epic(taskName, taskDesc, taskManager.getNewId(), TaskStatuses.NEW));
                System.out.println("Эпик успешно создан");
                break;
            default:
                System.out.println("Такой команды не существует. Задача не создана.");
                break;
        }
    }

    public static void printAllTasks() {
        for (Epic epic: taskManager.getEpics().values()) {
            System.out.println(epic.toString());
        }
        for (Task task: taskManager.getTasks().values()) {
            System.out.println(task.toString());
        }
        for (SubTask subTask: taskManager.getSubTasks().values()) {
            System.out.println(subTask.toString());
        }
    }

    public static void updateTask() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите номер задачи для редактирования:");
        int updTaskId = scanner.nextInt();
        scanner.nextLine();
        Task task = taskManager.getTaskById(updTaskId, null);
        TaskStatuses newStatus;
        if (task != null){
            String epicIds = "";
            System.out.println("Введите имя задачи");
            String taskName = scanner.nextLine();
            System.out.println("Введите описание задачи");
            String taskDesc = scanner.nextLine();

            switch (task.getTaskType()) {
                case TaskTypes.TASK:
                    System.out.println("Введите новый статус");
                    newStatus = TaskStatuses.valueOf(scanner.next());
                    scanner.nextLine();
                    taskManager.setTask(new Task(taskName, taskDesc, task.getTaskId(), newStatus));
                    System.out.println("Задача успешно изменена");
                    break;
                case TaskTypes.SUBTASK:
                    System.out.println("Введите новый статус");
                    newStatus = TaskStatuses.valueOf(scanner.next());
                    scanner.nextLine();
                    for (Epic epic: taskManager.getEpics().values()){
                        epicIds = epicIds + epic.getTaskId() + ":" + epic.getName() + "; ";
                    }
                    if (epicIds.isEmpty()){
                        System.out.println("Не найдено эпиков, вначале заведите эпик");
                        return;
                    }
                    System.out.println("Введите номер эпика");
                    System.out.println("Вот какие эпики у Вас есть (формат EpicNum:EpicName;): " + epicIds);
                    taskManager.setSubTask(new SubTask(taskName, taskDesc, task.getTaskId(), newStatus, scanner.nextInt()));
                    scanner.nextLine();
                    System.out.println("Подзадача успешно изменена");
                    break;
                case TaskTypes.EPIC:
                    taskManager.setEpic(new Epic(taskName, taskDesc, task.getTaskId(), task.getStatus()));
                    System.out.println("Эпик успешно изменен");
                    break;
                default:
                    System.out.println("Такого типа задач не найдено. Задача не обновлена.");
                    break;
            }
        } else {
            System.out.println("Такой задачи не существует");
        }
    }

    public static void findSubTasks() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите номер эпика");
        int findTaskId = scanner.nextInt();
        scanner.nextLine();
        Task task = taskManager.getTaskById(findTaskId, TaskTypes.EPIC);
        if (task != null){
            for (SubTask subTask: taskManager.findSubTasks(findTaskId).values()){
                System.out.println(subTask);
            }
        } else {
            System.out.println("Такого эпика не существует");
        }
    }

    public static void tests() {
        String taskName;
        String taskDesc;
        int epicTaskId;
        taskName = "taskName test 1";
        taskDesc = "taskDesc test 1";
        taskManager.setTask(new Task(taskName, taskDesc, taskManager.getNewId(), TaskStatuses.NEW));

        taskName = "taskName test 2";
        taskDesc = "taskDesc test 2";
        taskManager.setTask(new Task(taskName, taskDesc, taskManager.getNewId(), TaskStatuses.NEW));

        taskName = "EpicName test 1";
        taskDesc = "EpicDesc test 1";
        epicTaskId = taskManager.getNewId();
        taskManager.setEpic(new Epic(taskName, taskDesc, epicTaskId, TaskStatuses.NEW));

        taskName = "SubTaskName for epic " + epicTaskId + " test 1";
        taskDesc = "SubTaskDesc for epic " + epicTaskId + " test 1";
        taskManager.setSubTask(new SubTask(taskName, taskDesc, taskManager.getNewId(), TaskStatuses.NEW, epicTaskId));
        taskName = "SubTaskName for epic " + epicTaskId + " test 2";
        taskDesc = "SubTaskDesc for epic " + epicTaskId + " test 2";
        taskManager.setSubTask(new SubTask(taskName, taskDesc, taskManager.getNewId(), TaskStatuses.IN_PROGRESS, epicTaskId));

        taskName = "EpicName test 2";
        taskDesc = "EpicDesc test 2";
        epicTaskId = taskManager.getNewId();
        taskManager.setEpic(new Epic(taskName, taskDesc, epicTaskId, TaskStatuses.NEW));

        taskName = "SubTaskName for epic " + epicTaskId + " test 3";
        taskDesc = "SubTaskDesc for epic " + epicTaskId + " test 3";
        taskManager.setSubTask(new SubTask(taskName, taskDesc, taskManager.getNewId(), TaskStatuses.DONE, epicTaskId));
    }
}
