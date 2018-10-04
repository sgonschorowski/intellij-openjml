package fh.luebeck.openjml.util;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.Executor;
import com.intellij.execution.actions.StopProcessAction;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.actions.CloseAction;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import fh.luebeck.openjml.filter.JmlHyperlinkFilter;
import fh.luebeck.openjml.setting.JmlPersistantConfig;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static com.intellij.openapi.ui.Messages.getErrorIcon;
import static com.intellij.openapi.ui.Messages.showMessageDialog;

/**
 * Util Class
 */
public class JmlRunUtil {
    /**
     * Path to JRE
     */
    private static final String PATH_TO_JRE = System.getProperty("java.home");
    private static final String OPENJML_JAR = "openjml.jar";


    /**
     * Prepares and executes the OpenJML process.
     *
     * @param project       Current project reference.
     * @param canonicalPath Path to the actual java file.
     * @throws ExecutionException Exception if something went wrong while execution.
     */
    public static void runOpenJml(Project project, String canonicalPath) throws ExecutionException {
        // prepare command for execution
        ArrayList<String> commands = prepareCommand(canonicalPath);
        if (commands.isEmpty()) {
            showMessageDialog("Please configure OpenJML first", "Error", getErrorIcon());
            return;
        }
        // configure the GeneralCommandLine
        GeneralCommandLine generalCommandLine = new GeneralCommandLine(commands);
        // initialize the ProcessHandler
        OSProcessHandler osProcessHandler = new OSProcessHandler(generalCommandLine);
        // Build the ConsoleView
        ConsoleViewImpl consoleViewInstance = (ConsoleViewImpl) initProcessOutputConsole(osProcessHandler, project, DefaultRunExecutor.getRunExecutorInstance());
        // Print Start Information
        consoleViewInstance.print("Start OpenJML/ESC with file " + canonicalPath.substring(canonicalPath.lastIndexOf("/") + 1) + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
    }

    /**
     * Prepares the command to execute OpenJML.
     *
     * @param pathToJavaFile Path to the java file.
     * @return Command as ArrayList
     */
    @NotNull
    private static ArrayList<String> prepareCommand(String pathToJavaFile) {
        ArrayList<String> commands = new ArrayList<>();
        if (JmlPersistantConfig.getInstance() != null && JmlPersistantConfig.getInstance().getState() != null) {
            if(JmlPersistantConfig.getInstance().getPathToOJml().isEmpty()) {
                return commands;
            }
            JmlPersistantConfig state = JmlPersistantConfig.getInstance().getState();
            commands.add(PATH_TO_JRE + File.separator + "bin" + File.separator + "java");
            commands.add("-jar");
            commands.add(state.getPathToOJml() + File.separator + OPENJML_JAR);
            // Set -exec argument only if a solver is available.
            if (state.isUseCustomSolver() && !state.getPathToCustomSolver().isEmpty()) {
                commands.add("-exec");
                commands.add(state.getPathToCustomSolver());
            } else if (!state.getSelectedSolver().isEmpty()){
                commands.add("-exec");
                commands.add(state.getSelectedSolver());
            }
            commands.add("-esc");
            commands.add(pathToJavaFile);
            return commands;
        }
        return commands;
    }

    /**
     * Initializes the ConsoleView and add some GUI Elements like stop and close buttons.
     *
     * @param runHandler      ProcessHandler Instance
     * @param project         Current project reference
     * @param defaultExecutor Executor Instance
     * @return ConsoleView Instance of the ConsoleView.
     */
    private static ConsoleView initProcessOutputConsole(OSProcessHandler runHandler, Project project, Executor defaultExecutor) {
        // attach the ProcessTerminatedListener to print the exit code after the process is terminated.
        ProcessTerminatedListener.attach(runHandler, project);
        // create a new ConsoleView to print the process output
        ConsoleView consoleView = new ConsoleViewImpl(project, true);
        consoleView.addMessageFilter(new JmlHyperlinkFilter(project));
        // create a new ActionGroup
        DefaultActionGroup toolbarActions = new DefaultActionGroup();
        // create the GUI Elements
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(consoleView.getComponent(), "Center");
        // Create ActionToolbar
        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("unknown", toolbarActions, false);
        toolbar.setTargetComponent(consoleView.getComponent());
        panel.add(toolbar.getComponent(), "West");
        // create a new RunContent description.
        RunContentDescriptor runDescriptor = new RunContentDescriptor(consoleView,
                runHandler, panel, "OpenJML", AllIcons.RunConfigurations.Application);
        // add actions to the toolbar buttons.
        AnAction[]
                consoleActions = consoleView.createConsoleActions();
        toolbarActions.addAll((AnAction[]) Arrays.copyOf(consoleActions, consoleActions.length));
        toolbarActions.add(new StopProcessAction("Stop process", "Stop process", runHandler));
        toolbarActions.add(new CloseAction(defaultExecutor, runDescriptor, project));
        // show the console in the run toolwindow
        runHandler.startNotify();
        consoleView.attachToProcess(runHandler);
        // Open Tool-Window
        showConsole(project, defaultExecutor, runDescriptor);

        return consoleView;
    }

    /**
     * Adds the ConsoleView to the Run ToolWindow.
     *
     * @param project           Current project reference.
     * @param defaultExecutor   Instance of the Executor
     * @param contentDescriptor Run content.
     */
    private static void showConsole(Project project, Executor defaultExecutor, @NotNull RunContentDescriptor contentDescriptor) {
        // Open Run Tool-Window and show the ConsoleView
        ExecutionManager.getInstance(project).getContentManager().showRunContent(defaultExecutor, contentDescriptor);


    }


}
