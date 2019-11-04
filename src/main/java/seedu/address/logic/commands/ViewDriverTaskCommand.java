package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DRIVER;

import java.util.List;

import seedu.address.commons.core.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Driver;
import seedu.address.model.task.Task;

/**
 * List delivered task for specified driver
 */
public class ViewDriverTaskCommand extends Command {

    public static final String COMMAND_WORD = "viewD";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Views the delivered tasks of the specified driver "
            + "and displays them as a list with index numbers."
            + "\n"
            + "Parameters: " + "INDEX (must be a positive integer)"
            + "[" + PREFIX_DRIVER + "DRIVER_ID] "
            + "\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_DRIVER + "1";

    public static final String MESSAGE_SUCCESS = "listed delivered tasks for the specified Driver";

    private int driverId;

    public ViewDriverTaskCommand(int driverId) {
        this.driverId = driverId;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Driver> currentDriverList = model.getFilteredDriverList();
        Driver driver = model.getDriver(driverId);


        if (driverId > currentDriverList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_DRIVER_DISPLAYED_INDEX);
        }

        model.viewDriverTask(driver);
        return new CommandResult(String.format(MESSAGE_SUCCESS, driver));
    }

}