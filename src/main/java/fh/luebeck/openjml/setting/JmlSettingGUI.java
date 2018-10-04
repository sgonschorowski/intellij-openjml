package fh.luebeck.openjml.setting;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;

import javax.swing.*;

public class JmlSettingGUI {

    private JPanel rootPanel;
    private JButton downloadButton;
    private JProgressBar progressBar;
    private JButton cancelButton;
    private JLabel statusLable;
    private TextFieldWithBrowseButton pathToJMLTextBoxWithButton;
    private JCheckBox useCustomSolverCheckBox;
    private TextFieldWithBrowseButton pathToSolverTextBoxWithButton;
    private JComboBox<String> comboBoxSolvers;
    private JButton loadSolverButton;

    public JPanel getRootPanel() {
        return rootPanel;
    }

    public JButton getCancelButton() {
        return cancelButton;
    }

    public TextFieldWithBrowseButton getPathToJMLTextBoxWithButton() {
        return pathToJMLTextBoxWithButton;
    }

    public JButton getDownloadButton() {
        return downloadButton;
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public JLabel getStatusLable() {
        return statusLable;
    }

    public JCheckBox getUseCustomSolverCheckBox() {
        return useCustomSolverCheckBox;
    }

    public TextFieldWithBrowseButton getPathToSolverTextBoxWithButton() {
        return pathToSolverTextBoxWithButton;
    }

    public JComboBox<String> getComboBoxSolvers() {
        return comboBoxSolvers;
    }

    public JButton getLoadSolverButton() {
        return loadSolverButton;
    }

    private void createUIComponents() {
        progressBar = new JProgressBar(0,100);
        comboBoxSolvers = new ComboBox<>();
    }
}
