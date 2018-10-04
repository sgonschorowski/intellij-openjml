package fh.luebeck.openjml.github;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.progress.ProgressMonitor;

import javax.swing.*;
import java.io.File;

public class JmlUnzipWorker extends SwingWorker {

    private File myFileToUnzip;

    public JmlUnzipWorker(File file) throws Exception {
        this.myFileToUnzip = file;
    }

    @Override
    protected Object doInBackground() throws Exception {

        ZipFile zipFile = new ZipFile(myFileToUnzip);
        zipFile.extractAll(myFileToUnzip.getParent());
        ProgressMonitor progressMonitor = zipFile.getProgressMonitor();
        setProgress(progressMonitor.getPercentDone());
        return null;

    }

}
