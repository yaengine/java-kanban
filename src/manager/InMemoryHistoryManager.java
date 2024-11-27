package manager;

import task.Task;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    private Map<Integer, Node> histNodes;
    private Node prevNode;
    private Integer headTaskId;

    public InMemoryHistoryManager() {
        this.histNodes = new HashMap<>();
    }

    @Override
    public void add(Task task) {
        if (task != null) {
            Node currNode = new Node<>(prevNode, task, null);
            Integer currTaskId = task.getTaskId();

            if (histNodes.containsKey(currTaskId)) {
                remove(currTaskId);
            }
            linkLast(currTaskId, currNode);
            prevNode = currNode;
        }
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    @Override
    public void remove(int taskId) {
        if (histNodes.containsKey(taskId)) {
            removeNode(histNodes.get(taskId), taskId);
        }
    }

    public void linkLast(Integer currTaskId, Node currNode) {
        histNodes.put(currTaskId, currNode);
        if (headTaskId != null) {
            histNodes.put(currTaskId, histNodes.get(headTaskId).next = currNode);
        }
        headTaskId = currTaskId;
    }

    public List getTasks() {
        List<Task> history = new ArrayList<>();
        for (Node node : histNodes.values()) {
            history.add((Task) node.data);
        }
        return history;
    }

    private void removeNode(Node taskNode, Integer taskId) {
        Node prNode = taskNode.prev;
        Node nxNode = taskNode.next;

        histNodes.remove(taskId);

        if (prNode != null) {
            prNode.next = nxNode;
        }
        if (nxNode != null) {
            nxNode.prev = prNode;
        }

        prevNode = prNode;
        if (headTaskId.equals(taskId)) {
            if (prNode != null) {
                Task prevData = (Task) prNode.data;
                headTaskId = prevData.getTaskId();
            } else {
                headTaskId = null;
            }
        }
    }
}
