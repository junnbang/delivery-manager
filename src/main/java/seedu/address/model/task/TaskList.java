package seedu.address.model.task;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.person.Driver;
import seedu.address.model.task.exceptions.TaskNotFoundException;

/**
 * Represents a list of delivery tasks. The `TaskList` class supports a minimal set
 * of list operations. List sort and filter operations are provided.
 */
public class TaskList {

    private final ObservableList<Task> tasks = FXCollections.observableArrayList();
    private final ObservableList<Task> tasksUnmodifiable = FXCollections.unmodifiableObservableList(tasks);

    public TaskList() {
    }

    public int getSize() {
        return tasks.size();
    }

    public Task getTask(int taskId) {
        Optional<Task> foundTask = tasks
                                    .stream()
                                    .filter(task -> task.getId() == taskId)
                                    .findFirst();
        if (foundTask.isEmpty()) {
            throw new TaskNotFoundException();
        }

        return foundTask.get();
    }

    /**
     * Checks if the task list has a task with the {@code int taskId}.
     *
     * @param taskId Unique task id.
     */
    public boolean hasTask(int taskId) {
        Optional<Task> foundTask = tasks
                                    .stream()
                                    .filter(currentTask -> currentTask.getId() == taskId)
                                    .findFirst();
        return foundTask.isPresent();
    }

    /**
     * Check if task exists in the task list.
     */
    public boolean hasTask(Task task) {
        requireNonNull(task);
        Optional<Task> foundTask = tasks
                                    .stream()
                                    .filter(currentTask -> currentTask.equals(task))
                                    .findFirst();
        return foundTask.isPresent();
    }

    /**
     * Adds tasks into the task list.
     *
     * @param newTask task to be added.
     */
    public void addTask(Task newTask) {
        requireNonNull(newTask);
        tasks.add(newTask);
    }

    /**
     * Deletes the task from the task list.
     * @param taskToRemove task to be deleted.
     */
    public void deleteTask(Task taskToRemove) {
        requireNonNull(taskToRemove);
        if (!tasks.contains(taskToRemove)) {
            throw new TaskNotFoundException();
        }

        tasks.remove(taskToRemove);
    }

    /**
     * Updates the details of the task.
     * @param taskToEdit task to be edited.
     * @param editedTask task that is to replace the original task.
     */
    public void setTask(Task taskToEdit, Task editedTask) {
        requireAllNonNull(taskToEdit, editedTask);
        if (!tasks.contains(taskToEdit)) {
            throw new TaskNotFoundException();
        }

        for (int i = 0; i < getSize(); i++) {
            Task task = tasks.get(i);
            if (task == taskToEdit) {
                tasks.set(i, editedTask);
                break;
            }
        }
    }

    public ObservableList<Task> getList() {
        return tasksUnmodifiable;
    }

    public static List<Task> getSortedList(List<Task> tasks, Comparator<Task> comparator) {
        tasks.sort(comparator);
        return tasks;
    }

    public static List<Task> getFilteredList(List<Task> tasks, Predicate<Task> predicate) {
        return tasks
                    .stream()
                    .filter(predicate)
                    .collect(Collectors.toList());
    }

    /**
     * Creates a driver list out of task list.
     * NOTE: task list must be filtered by assigned tasks so that all the tasks contains a driver.
     *
     * @param assignedTasks list of task that is assigned to drivers.
     * @return driver list that contains only drivers with assigned tasks.
     */
    public static List<Driver> getDriversFromTasks(List<Task> assignedTasks) {
        HashSet<Driver> driverSet = new HashSet<>();
        for (Task task : assignedTasks) {
            assert task.getDriver().isPresent();

            Driver driver = task.getDriver().get();
            driverSet.add(driver);
        }

        List<Driver> driverList = new ArrayList<>(driverSet);

        return driverList;
    }

    public Iterator<Task> getIterator() {
        return tasks.iterator();
    }

    public void setTaskList(List<Task> savedTasks) {
        tasks.setAll(savedTasks);
    }

    @Override
    public String toString() {
        return getList().size() + " tasks";
    }
}
