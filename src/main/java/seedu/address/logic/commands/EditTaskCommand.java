package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CUSTOMER;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATETIME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DRIVER;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DURATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GOODS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TASK;

import java.time.LocalDate;
import java.util.Optional;

import seedu.address.commons.util.CollectionUtil;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Description;
import seedu.address.model.EventTime;
import seedu.address.model.Model;
import seedu.address.model.person.Customer;
import seedu.address.model.person.Driver;
import seedu.address.model.task.Task;
import seedu.address.model.task.TaskStatus;

/**
 * Edits the details of an existing task in the task list.
 */
public class EditTaskCommand extends Command {

    public static final String COMMAND_WORD = "editT";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": "
            + "Edits the details of a task. "
            + "Existing values will be overwritten by the input values. "
            + "Indicate only the parameters you want to change.\n"
            + "Parameters: "
            + "[TASK ID] "
            + "[" + PREFIX_GOODS + "DESCRIPTION] "
            + "[" + PREFIX_CUSTOMER + "CUSTOMER ID] "
            + "[" + PREFIX_DATETIME + "DATE] "
            + "[" + PREFIX_DRIVER + "DRIVER ID] "
            + "[" + PREFIX_DURATION + "DURATION]\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_TASK + "2 "
            + PREFIX_GOODS + "10 ice boxes of red groupers "
            + PREFIX_DATETIME + "10/12/2019\n";

    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";
    public static final String MESSAGE_NOTHING_TO_EDIT = "At least one field need to be different to edit.";
    public static final String MESSAGE_EDIT_TASK_SUCCESS = "Edited Task: %1$s";
    public static final String MESSAGE_DRIVER_DURATION_FIELD_CONSTRAINTS =
            "Driver AND Duration field are compulsory IF driver was not assigned initially.";

    private final int id;
    private final EditTaskDescriptor editTaskDescriptor;


    public EditTaskCommand(int id, EditTaskDescriptor editTaskDescriptor) {
        this.id = id;
        this.editTaskDescriptor = editTaskDescriptor;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        if (!model.hasTask(id)) {
            throw new CommandException(Task.MESSAGE_INVALID_ID);
        }

        Task taskToEdit = model.getTask(id);
        Task editedTask = createEditedTask(taskToEdit, editTaskDescriptor, model);

        if (taskToEdit.isSameTask(editedTask)) {
            throw new CommandException(MESSAGE_NOTHING_TO_EDIT);
        }

        //if original task doesn't have driver and eventTime
        //throw error if ONLY 1 of the field is edited.
        if ((editedTask.getDriver().isPresent() && editedTask.getEventTime().isEmpty())
                || editedTask.getDriver().isEmpty() && editedTask.getEventTime().isPresent()) {
            throw new CommandException(MESSAGE_DRIVER_DURATION_FIELD_CONSTRAINTS);
        }

        //if driver or duration has been changed, adjust their schedule accordingly.
        if (taskToEdit.getStatus() == TaskStatus.ON_GOING) {
            if (taskToEdit.getDriver() != editedTask.getDriver()
                    || taskToEdit.getEventTime() != editedTask.getEventTime()) {
                //disregard check for optional empty because if a task is ON_GOING, there MUST be a driver and duration.
                Driver previousDriver = taskToEdit.getDriver().get();
                previousDriver.deleteFromSchedule(taskToEdit.getEventTime().get());

                Driver updatedDriver = editedTask.getDriver().get();
                if (!updatedDriver.isScheduleAvailable(editedTask.getEventTime().get())) {
                    previousDriver.addToSchedule(taskToEdit.getEventTime().get());
                    throw new CommandException(String.format(Driver.MESSAGE_NOT_AVAILABLE, updatedDriver.getId()));
                }

                updatedDriver.addToSchedule(editedTask.getEventTime().get());
            }

        }

        //if task was incomplete and was assigned driver and duration through edit command.
        if (taskToEdit.getStatus() == TaskStatus.INCOMPLETE
                && (editedTask.getDriver().isPresent() && editedTask.getEventTime().isPresent())) {
            Driver updatedDriver = editedTask.getDriver().get();
            EventTime durationToAdd = editedTask.getEventTime().get();
            if (updatedDriver.isScheduleAvailable(durationToAdd)) {
                throw new CommandException(String.format(Driver.MESSAGE_NOT_AVAILABLE, updatedDriver.getId()));
            }
            updatedDriver.addToSchedule(durationToAdd);

            editedTask.setStatus(TaskStatus.ON_GOING);
        }

        model.setTask(taskToEdit, editedTask);

        return new CommandResult(String.format(MESSAGE_EDIT_TASK_SUCCESS, editedTask));
    }

    /**
     * Creates and returns a {@code Person} with the details of {@code personToEdit}
     * edited with {@code editPersonDescriptor}.
     */
    private static Task createEditedTask(Task taskToEdit, EditTaskDescriptor editTaskDescriptor,
                                         Model model) throws CommandException {
        assert taskToEdit != null;

        Description updatedDescription = editTaskDescriptor.getDescription().orElse(taskToEdit.getDescription());

        //check if customer id provided is valid
        Optional<Integer> customerId = editTaskDescriptor.getCustomer();
        if (customerId.isPresent() && !model.hasCustomer(customerId.get())) {
            throw new CommandException(Customer.MESSAGE_INVALID_ID);
        }

        Customer updatedCustomer;
        if (customerId.isPresent()) {
            //get the new customer to be assigned from customer list.
            int updatedCustomerId = customerId.get();
            updatedCustomer = model.getCustomer(updatedCustomerId);
        } else {
            //get the original customer that is assigned to the task.
            updatedCustomer = taskToEdit.getCustomer();
        }

        LocalDate updatedDate = editTaskDescriptor.getDate().orElse(taskToEdit.getDate());

        //check if driver id provided is valid
        Optional<Integer> driverId = editTaskDescriptor.getDriver();
        if (driverId.isPresent() && !model.hasDriver(driverId.get())) {
            throw new CommandException(Driver.MESSAGE_INVALID_ID);
        }

        Optional<Driver> updatedDriver;
        if (driverId.isPresent()) {
            //get the new customer to be assigned from customer list.
            int updatedDriverId = driverId.get();
            updatedDriver = model.getDriver(updatedDriverId);
        } else {
            //get the original customer that is assigned to the task.
            updatedDriver = taskToEdit.getDriver();
        }

        Optional<EventTime> updatedEventTime = editTaskDescriptor.getEventTime();
        if (updatedEventTime.isEmpty()) {
            updatedEventTime = taskToEdit.getEventTime();
        }

        Task editedTask = new Task(taskToEdit.getId(), updatedDescription, updatedDate);
        editedTask.setCustomer(updatedCustomer);
        editedTask.setDriver(updatedDriver);
        editedTask.setEventTime(updatedEventTime);

        editedTask.setStatus(taskToEdit.getStatus());

        return editedTask;
    }

    /**
     * Stores the details to edit the task with. Each non-empty field value will replace the
     * corresponding field value of the person.
     */
    public static class EditTaskDescriptor {
        private Description description;
        private LocalDate date;
        private Integer driver;
        private Integer customer;
        private EventTime eventTime;


        public EditTaskDescriptor() {

        }

        public EditTaskDescriptor(EditTaskDescriptor toCopy) {
            setDescription(toCopy.description);
            setDate(toCopy.date);
            setDriver(toCopy.driver);
            setCustomer(toCopy.customer);
            setEventTime(toCopy.eventTime);
        }

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(description, date, driver, customer, eventTime);
        }

        public Optional<Description> getDescription() {
            return Optional.ofNullable(description);
        }

        public void setDescription(Description description) {
            this.description = description;
        }

        public Optional<LocalDate> getDate() {
            return Optional.ofNullable(date);
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public Optional<Integer> getDriver() {
            return Optional.ofNullable(driver);
        }

        public void setDriver(int driver) {
            this.driver = driver;
        }

        public Optional<Integer> getCustomer() {
            return Optional.ofNullable(customer);
        }

        public void setCustomer(int customer) {
            this.customer = customer;
        }

        public Optional<EventTime> getEventTime() {
            return Optional.ofNullable(eventTime);
        }

        public void setEventTime(EventTime eventTime) {
            this.eventTime = eventTime;
        }

    }
}
