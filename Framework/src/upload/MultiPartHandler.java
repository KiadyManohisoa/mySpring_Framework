package upload;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import jakarta.servlet.http.Part;

public class MultiPartHandler {

    String fileName;
    byte[] byteContents;
    InputStream dataFeeds;
    String appPath;
    // Part filePart;

    public void setSelf(Part part) throws Exception {
        try {
            this.setFileName(part.getSubmittedFileName());
            this.setDataFeeds(part.getInputStream());
            this.setByteContents(part);
        } catch (Exception e) {
            throw e;
        }
    }

    public void saveTo(String uploadDirectory) throws Exception {
        String savePath = this.getAppPath() + uploadDirectory;
        File fileSaveDirectory = new File(savePath);
        if (!fileSaveDirectory.exists()) {
            fileSaveDirectory.mkdirs();
        }
        // System.out.println("savePath " + savePath);
        // System.out.println("fileName " + this.getFileName());
        Path filePath = Paths.get(savePath, this.getFileName());
        try {
            Files.copy(this.getDataFeeds(), filePath);
        } catch (Exception e) {
            throw e;
        }
    }

    public MultiPartHandler() {
    }

    public String getAppPath() {
        return appPath;
    }

    public void setAppPath(String appPath) {
        this.appPath = appPath;
    }

    // public Part getFilePart() {
    // return filePart;
    // }

    // public void setFilePart(Part filePart) {
    // this.filePart = filePart;
    // }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getByteContents() {
        return byteContents;
    }

    public void setByteContents(Part filePart) throws Exception {
        try (InputStream inputStream = filePart.getInputStream();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

            byte[] data = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, bytesRead);
            }

            this.setByteContents(buffer.toByteArray());
        }
    }

    public void setByteContents(byte[] byteContents) {
        this.byteContents = byteContents;
    }

    public InputStream getDataFeeds() {
        return dataFeeds;
    }

    public void setDataFeeds(InputStream dataFeeds) {
        this.dataFeeds = dataFeeds;
    }

}
