package util;

import java.time.Duration;
import java.time.LocalDateTime;

public class TestConstants {
    public static final String TASK_NOT_FOUND_ERR = "Задача не найдена";
    public static final String TASK_NOT_MATCH_ERR = "Задачи не совпадают";
    public static final String TASK_NOT_RETURN_ERR = "Задачи не возвращаются";
    public static final String TASK_RETURN_ERR = "Задачи возвращаются";
    public static final String SUBTASK_NOT_RETURN_ERR = "Подзадачи не возвращаются";
    public static final String INCORRECT_ID_OF_TASK_ERR = "Некорректный id задачи";
    public static final String INCORRECT_NUM_OF_TASK_ERR = "Неверное количество задач";
    public static final String INCORRECT_NUM_OF_SUBTASK_ERR = "Некорректное количество подзадач";
    public static final String INCORRECT_TASK_NAME_ERR = "Некорректное имя задачи";
    public static final String INCORRECT_SUBTASK_NAME_ERR = "Некорректное имя подзадачи";
    public static final String HISTORY_NOT_SAVED_ERR = "Не сохраняется история просмотров";
    public static final String TASK_NOT_UPDATED = "Обновления задачи не произошло";
    public static final String TASK_NOT_REMOVED = "Задача не удалена";
    public static final String SUBTASK_NOT_REMOVED = "Подадача не удалена";
    public static final String EPIC_NOT_REMOVED = "Эпик не удален";
    public static final String INCORRECT_RESP_WHEN_TASK_NOT_FOUND = "Некорректный ответ, если не нашли таску";
    public static final String INCORRECT_RESP_WHEN_UPD_TASK = "Некорректный ответ, при обновлении таски";
    public static final String INCORRECT_RESP_WHEN_UPD_TASK_WITH_CROSS = "Некорректный ответ, при обновлении подзадачи с пересечением";



    public static final String NEW_TASK_NAME = "Test addNewTask";
    public static final String NEW_TASK_DESC = "Test addNewTask description";
    public static final String NEW_SUBTASK_NAME = "Test addNewSubTask";
    public static final String NEW_SUBTASK_DESC = "Test addNewSubTask description";
    public static final String NEW_EPIC_NAME = "Test addNewEpicTask";
    public static final String NEW_EPIC_DESC = "Test addNewEpicTask description";
    public static final LocalDateTime NEW_TASK_START_TIME = LocalDateTime.of(2025, 1, 1, 0, 0);
    public static final Duration NEW_TASK_DURATION = Duration.ofHours(1);
    
    public static final String FILE_PREFIX = "kanban";
    public static final String FILE_POSTFIX = ".csv";
}

