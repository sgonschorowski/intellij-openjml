package fh.luebeck.openjml.github;


import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.SystemInfo;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

public class JmlDownloadUtil {

    private static final String targeturl = "https://api.github.com/repos/OpenJML/OpenJML/releases/latest";
    private String fileName;
    private String downloadLink;

    public JmlDownloadUtil() {
    }

    public boolean init() {
        return extractDownloadLinkAndFileName(getJsonResponseFromGitHub());
    }

    public static void setExecutable(File selectedFile) {
        if(SystemInfo.isMac) {
            File directory = new File(selectedFile.getParent() + File.separator + "Solvers-macos");
            Arrays.stream(directory.listFiles()).forEach(f -> f.setExecutable(true));

        } else if(SystemInfo.isLinux) {
            File directory = new File(selectedFile.getParent() + File.separator + "Solvers-linux");
            Arrays.stream(directory.listFiles()).forEach(f -> f.setExecutable(true));
        }
    }

    private boolean extractDownloadLinkAndFileName(String response) {
        if (response.isEmpty()) {
            return false;
        }
        JSONObject jsonObject = new JSONObject(response);
        JSONArray assets = jsonObject.getJSONArray("assets");
        if (!assets.isNull(0)) {
            JSONObject obj = (JSONObject) assets.get(0);
            this.downloadLink = obj.get("browser_download_url").toString();
            this.fileName = obj.get("name").toString();
            return true;
        }
        return false;
    }

    private String getJsonResponseFromGitHub() {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(targeturl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } catch (Exception e) {
            Messages.showErrorDialog("The Github API is not reachable.","Error");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return "";
    }

    public String getFileName() {
        return fileName;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

}

