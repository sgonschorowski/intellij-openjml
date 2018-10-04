package fh.luebeck.openjml.setting;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import fh.luebeck.openjml.github.JmlDownloadWorker;
import fh.luebeck.openjml.github.JmlDownloadUtil;
import fh.luebeck.openjml.github.JmlUnzipWorker;
import org.fest.util.Files;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class JmlConfigurable implements Configurable {
    private JmlSettingGUI mGUI;
    private JmlPersistantConfig persistantState;
    private JmlDownloadWorker downloadWorker;

    public JmlConfigurable() {
        persistantState = JmlPersistantConfig.getInstance();
        mGUI = new JmlSettingGUI();
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "OpenJml";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        loadSavedSettings();
        JPanel rootPanel = mGUI.getRootPanel();
        initCancelButton();
        initDownloadListener();
        initBrowseListener();
        initLoadButton();
        initCustomSolverCheckBox();
        return rootPanel;
    }

    private void loadSavedSettings() {
        mGUI.getPathToJMLTextBoxWithButton().setText(persistantState.getPathToOJml());
        mGUI.getComboBoxSolvers().addItem(persistantState.getSelectedSolver());
        mGUI.getComboBoxSolvers().setSelectedItem(persistantState.getSelectedSolver());
        mGUI.getUseCustomSolverCheckBox().setSelected(persistantState.isUseCustomSolver());
        if (mGUI.getUseCustomSolverCheckBox().isSelected()) {
            mGUI.getPathToSolverTextBoxWithButton().setEnabled(true);
            mGUI.getComboBoxSolvers().setEnabled(false);
        }
        mGUI.getPathToSolverTextBoxWithButton().setText(persistantState.getPathToCustomSolver());
    }

    @Override
    public boolean isModified() {
        if (isModified(mGUI.getPathToJMLTextBoxWithButton().getTextField(), persistantState.getPathToOJml())) {
            return true;
        }
        if (mGUI.getUseCustomSolverCheckBox().isSelected() != persistantState.isUseCustomSolver()) {
            return true;
        }
        if (isModified(mGUI.getPathToSolverTextBoxWithButton().getTextField(), persistantState.getPathToCustomSolver())) {
            return true;
        }
        return mGUI.getComboBoxSolvers().getSelectedItem() != null && !mGUI.getComboBoxSolvers().getSelectedItem().equals(persistantState.getSelectedSolver());
    }

    @Override
    public void apply() {
        if (persistantState != null) {
            persistantState.setPathToOJml(mGUI.getPathToJMLTextBoxWithButton().getText());
            persistantState.setUseCustomSolver(mGUI.getUseCustomSolverCheckBox().isSelected());
            persistantState.setPathToCustomSolver(mGUI.getPathToSolverTextBoxWithButton().getText());
            String selectedItem = (String) mGUI.getComboBoxSolvers().getSelectedItem();
            if (selectedItem != null) {
                persistantState.setSelectedSolver(selectedItem);
            } else {
                persistantState.setSelectedSolver("");
            }
        }
    }

    @Override
    public void reset() {
        loadSavedSettings();
    }

    private void initDownloadListener() {
        mGUI.getDownloadButton().addActionListener(e -> {
            JmlDownloadUtil downloadUtil = new JmlDownloadUtil();
            if(downloadUtil.init()) {
                VirtualFile selectedPath = FileChooser.chooseFile(FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                        null, null);
                if (selectedPath != null) {
                    mGUI.getProgressBar().setEnabled(true);
                    mGUI.getCancelButton().setEnabled(true);
                    downloadWorker = new JmlDownloadWorker(downloadUtil.getDownloadLink(), selectedPath, downloadUtil.getFileName());
                    downloadWorker.addPropertyChangeListener(pcEvt -> {
                    if (downloadWorker.isDone() && !downloadWorker.isCancelled()) {
                        mGUI.getCancelButton().setEnabled(false);
                        mGUI.getDownloadButton().setEnabled(true);
                        startUnzipWorker(downloadWorker.getFile());
                    }
                        if (downloadWorker.isCancelled()) {
                            resetGUIAfterCancelDownload();
                            Files.delete(downloadWorker.getFile());
                        }
                        if ("progress".equals(pcEvt.getPropertyName())) {
                            mGUI.getProgressBar().setValue((Integer) pcEvt.getNewValue());
                        } else if (pcEvt.getNewValue() == SwingWorker.StateValue.DONE) {
                            try {
                                if (!downloadWorker.isCancelled()) {
                                    downloadWorker.get();
                                }
                            } catch (InterruptedException | java.util.concurrent.ExecutionException exception) {
                                exception.printStackTrace();
                            }
                        }
                    });
                    downloadWorker.execute();
                    mGUI.getStatusLable().setText("Downloading..");
                    mGUI.getDownloadButton().setEnabled(false);
                    initCancelButton();
                }
            }
        });
    }


    private void startUnzipWorker(File selectedFile) {
        try {
            JmlUnzipWorker unzipWorker = new JmlUnzipWorker(selectedFile);
            unzipWorker.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {

                    if ("progress".equals(evt.getPropertyName())) {
                        mGUI.getProgressBar().setValue((Integer) evt.getNewValue());
                    }
                    if (unzipWorker.isDone()) {
                        mGUI.getProgressBar().setValue(0);
                        mGUI.getProgressBar().updateUI();
                        mGUI.getStatusLable().setText("");
                        mGUI.getPathToJMLTextBoxWithButton().setText(selectedFile.getParent());
                        JmlDownloadUtil.setExecutable(selectedFile);
                        selectedFile.delete();

                    }
                }
            });
            unzipWorker.execute();
            mGUI.getStatusLable().setText("Unzipping..");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initCancelButton() {
        mGUI.getCancelButton().addActionListener(f -> {
            downloadWorker.cancel(true);
        });
    }


    private void initCustomSolverCheckBox() {
        mGUI.getUseCustomSolverCheckBox().addActionListener(e -> {
            if (mGUI.getUseCustomSolverCheckBox().isSelected()) {
                mGUI.getPathToSolverTextBoxWithButton().setEnabled(true);
                mGUI.getComboBoxSolvers().setEnabled(false);
            } else {
                mGUI.getPathToSolverTextBoxWithButton().setEnabled(false);
                mGUI.getComboBoxSolvers().setEnabled(true);
            }
        });
    }

    private void initLoadButton() {
        mGUI.getLoadSolverButton().addActionListener(e -> {
            File path = new File(mGUI.getPathToJMLTextBoxWithButton().getText());
            if (path.isDirectory()) {
                Set<File> files = searchForSolvers(path);
                if (files != null) {
                    initComboBox(files);
                }
            } else {
                Messages.showErrorDialog("Please set path to OpenJML first.", "Error");
            }
        });
    }

    private void initComboBox(Set<File> files) {
        JComboBox<String> comboBox1 = mGUI.getComboBoxSolvers();
        comboBox1.removeAllItems();
        files.forEach(file -> {
            comboBox1.addItem(file.getAbsolutePath());
        });
    }

    private Set<File> searchForSolvers(File path) {
        if (SystemInfo.isMac) {
            File temp = new File(path.getPath() + File.separator + "Solvers-macos");
            return getOSSpecificSolvers(temp);
        } else if (SystemInfo.isWindows) {
            File temp = new File(path.getPath() + File.separator + "Solvers-windows");
            return getOSSpecificSolvers(temp);
        } else if (SystemInfo.isLinux) {
            File temp = new File(path.getPath() + File.separator + "Solvers-linux");
            return getOSSpecificSolvers(temp);
        }
        return null;
    }

    private Set<File> getOSSpecificSolvers(File temp) {
        HashSet<File> solvers = new HashSet<>();
        if (temp.exists()) {
            File[] listFiles = temp.listFiles();
            if (listFiles != null && listFiles.length > 0) {
                solvers.addAll(Arrays.asList(listFiles));
            } else {
                Messages.showErrorDialog("No solvers available at path " + temp.getPath(), "Error");
            }
        } else {
            Messages.showErrorDialog("Please select a valid path to OpenJML", "Error");
        }
        return solvers;
    }

    private void initBrowseListener() {
        mGUI.getPathToJMLTextBoxWithButton().addBrowseFolderListener("", "", null, FileChooserDescriptorFactory.createSingleFolderDescriptor());
//        mGUI.getPathToSolverTextBoxWithButton().addBrowseFolderListener("", "", null, FileChooserDescriptorFactory.createSingleLocalFileDescriptor());
        mGUI.getPathToSolverTextBoxWithButton().addActionListener(e -> {
            VirtualFile file = FileChooser.chooseFile(FileChooserDescriptorFactory.createSingleFileDescriptor(), null, null);
            if (file != null && !file.isDirectory()) {
                mGUI.getPathToSolverTextBoxWithButton().setText(file.getPath());
            } else if (!file.isDirectory()){
                Messages.showMessageDialog("Please select a file.", "Error", Messages.getErrorIcon());
            }
        });
    }

    public void resetGUIAfterCancelDownload() {
        mGUI.getStatusLable().setText("");
        mGUI.getCancelButton().setEnabled(false);
        mGUI.getDownloadButton().setEnabled(true);
        mGUI.getProgressBar().setValue(0);
        mGUI.getProgressBar().updateUI();
    }
}
