package sdrs.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class FileDataStore implements DataStore {

    private final File file;

    public FileDataStore(File file) {
        this.file = file;
    }

    @Override
    public SystemData load() {
        if (!file.exists()) {
            return null;
        }
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(new FileInputStream(file));
            Object object = in.readObject();
            if (object instanceof SystemData) {
                return (SystemData) object;
            }
            return null;
        } catch (Exception ex) {
            return null;
        } finally {
            closeQuietly(in);
        }
    }

    @Override
    public void save(SystemData data) {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        File backup = new File(file.getAbsolutePath() + ".bak");
        if (file.exists()) {
            copyFile(file, backup);
        }
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(new FileOutputStream(file));
            out.writeObject(data);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to save data: " + ex.getMessage(), ex);
        } finally {
            closeQuietly(out);
        }
    }

    public SystemData loadBackup() {
        File backup = new File(file.getAbsolutePath() + ".bak");
        if (!backup.exists()) {
            return null;
        }
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(new FileInputStream(backup));
            Object object = in.readObject();
            if (object instanceof SystemData) {
                return (SystemData) object;
            }
        } catch (Exception ex) {
            return null;
        } finally {
            closeQuietly(in);
        }
        return null;
    }

    private void copyFile(File source, File target) {
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(source);
            out = new FileOutputStream(target);
            byte[] buffer = new byte[4096];
            int read = in.read(buffer);
            while (read != -1) {
                out.write(buffer, 0, read);
                read = in.read(buffer);
            }
        } catch (IOException ex) {
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
            }
        }
    }

    private void closeQuietly(ObjectInputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ex) {
            }
        }
    }

    private void closeQuietly(ObjectOutputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ex) {
            }
        }
    }
}
