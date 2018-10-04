package fh.luebeck.openjml.github;

import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.io.BufferedOutputStream;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;

public class JmlDownloadWorker extends SwingWorker<Void, Void> {
    private File myfile;
    private String mydownloadLink;

    public JmlDownloadWorker(String downloadLink, VirtualFile file, String zipName) {
        this.mydownloadLink = downloadLink;
        this.myfile = new File(file.getCanonicalPath() + File.separator + zipName);
    }

    @Override
    protected Void doInBackground() throws Exception {
        if (mydownloadLink != null) {
            URL url = new URL(mydownloadLink);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            long filesize = connection.getContentLength();
            long totalDataRead = 0;
            try (java.io.BufferedInputStream in = new java.io.BufferedInputStream(
                    connection.getInputStream())) {
                java.io.FileOutputStream fos = new java.io.FileOutputStream(myfile);
                try (java.io.BufferedOutputStream bout = new BufferedOutputStream(
                        fos, 1024)) {
                    byte[] data = new byte[1024];
                    int i;
                    while (((i = in.read(data, 0, 1024)) >= 0) && !isCancelled()) {
                        totalDataRead = totalDataRead + i;
                        bout.write(data, 0, i);
                        int percent = (int) (((totalDataRead * 100) / filesize));
                        setProgress(percent);
                    }

                }
            }
        }
        return null;
    }

    public File getFile() {
        return myfile;
    }
}
