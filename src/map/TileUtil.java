import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.TileFactory;
import org.jxmapviewer.viewer.TileFactoryInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class TileUtil {
    Boolean isOnline;

    public TileUtil(Boolean isOnline) {
        this.isOnline = isOnline;
    }

    public TileFactory getTileFactory() {
        TileFactoryInfo info;
        if (isOnline) {
            info = new OSMTileFactoryInfo();
        } else {
            info = new OSMTileFactoryInfo("Zip archive", "jar:file:" +
                    System.getProperty("user.home") + File.separator + "Archive.zip!");
        }
        return new DefaultTileFactory(info);
    }

    public void createZip(String sourceDirPath, String zipFilePath) throws IOException {
        File sourceDir = new File(sourceDirPath);
        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            zipDirectory(sourceDir, "", zos);
        }
    }

    private void zipDirectory(File folder, String parentFolder, ZipOutputStream zos) throws IOException {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                // Skip the "tile.openstreetmap.org" folder and only include its contents
                if (parentFolder.isEmpty() && file.getName().equals("tile.openstreetmap.org")) {
                    zipDirectory(file, "", zos);
                } else {
                    zipDirectory(file, parentFolder + (parentFolder.isEmpty() ? "" : "/") + file.getName(), zos);
                }
                continue;
            }
            try (FileInputStream fis = new FileInputStream(file)) {
                String zipEntryName = parentFolder + "/" + file.getName();
                zos.putNextEntry(new ZipEntry(zipEntryName));
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
                zos.closeEntry();
            }
        }
    }
}