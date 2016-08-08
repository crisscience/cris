package edu.purdue.cybercenter.dm.storage;

import edu.purdue.cybercenter.dm.domain.Storage;
import edu.purdue.cybercenter.dm.domain.StorageAccessMethod;
import edu.purdue.cybercenter.dm.domain.StorageFile;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;

public class FileStorageFileManager extends AbstractStorageFileManager {

    private StorageAccessMethod storageAccessMethod;

    public FileStorageFileManager(StorageAccessMethod sam) throws IOException {
        if (!AccessMethodType.FILE.equals(sam.getType())) {
            throw new RuntimeException("FileStorageFileManager cannot handle storge type: " + sam.getType());
        }

        this.storageAccessMethod = sam;
    }

    @Override
    public List<StorageFile> putFile(String sourcePath, StorageFile targetStorageFile, boolean sync) throws IOException {
        if (targetStorageFile != null && targetStorageFile.getStorageId() != this.storageAccessMethod.getStorageId()) {
            throw new RuntimeException(String.format("Storage type: %s, target storage type: %s", this.storageAccessMethod.getType(), targetStorageFile.getStorageId().getType()));
        }

        String fileSystemType = this.getStorageTypeFromFilePath(sourcePath);
        StorageFile target = this.initiateTransferOneFile(fileSystemType, this.getAbsoultuePath(fileSystemType, sourcePath), storageAccessMethod.getType(), storageAccessMethod.getId());
        List<StorageFile> storageFiles = new ArrayList<>();
        storageFiles.add(target);
        return storageFiles;
    }

    @Override
    public List<StorageFile> putFile(String sourceName, InputStream sourceStream, StorageFile targetStorageFile, boolean sync) throws IOException {
        if (targetStorageFile != null && targetStorageFile.getStorageId() != this.storageAccessMethod.getStorageId()) {
            throw new RuntimeException(String.format("Storage type: %s, target storage type: %s", this.storageAccessMethod.getType(), targetStorageFile.getStorageId().getType()));
        }

        if (targetStorageFile == null) {
            targetStorageFile = makeStorageFile(sourceName);
        }
        String dest = getAbsolutePath(targetStorageFile);
        File file = new File(dest);
        String path = file.getParent();
        (new File(path)).mkdirs();
        try (OutputStream os = new FileOutputStream(dest)) {
            IOUtils.copy(sourceStream, os);
        }

        List<StorageFile> storageFiles = new ArrayList<>();
        storageFiles.add(targetStorageFile);
        return storageFiles;
    }

    @Override
    public String getFile(StorageFile sourceStorageFile, String targetPath, boolean sync) throws IOException {
        String fileSystemType = this.getStorageTypeFromFilePath(targetPath);
        return this.getFileFromStorage(sourceStorageFile, this.getAbsoultuePath(fileSystemType, targetPath));
    }

    // starting from a source root directory and destination repository
    // will need to traverse the subdirectories if any from the source
    private void initiateTrasfer(String sourceType, String source, String destType, Integer storageDestination) throws Exception {
        FileSystem sourceFS = this.getLocalFileSystem();
        FileSystem destFS = this.getLocalFileSystem();
        Storage destStorage = Storage.findStorage(storageDestination); // this
        // method
        // is
        // given
        // the id
        // of the storage you get the
        // storage object of it
        destFS.setVerifyChecksum(false);
        // visitAllFiles2(sourceFS, source, destFS, destStorage);
        visitAllFiles(sourceFS, new File(source), destFS, destStorage);

        System.out.println("files transfered");
    }

    private void visitAllFiles(FileSystem sourceFS, File dir, FileSystem destFS, Storage destStorage) throws Exception {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                visitAllFiles(sourceFS, new File(dir, children[i]), destFS, destStorage);
            }
        } else {
            StorageFile storageFile = makeStorageFile(dir.getAbsolutePath());
            String dest = getAbsolutePath(storageFile);
            transferFile(sourceFS, dir.getAbsolutePath(), destFS, dest, storageFile.getId(), true);
        }
    }

    private void visitAllFiles2(FileSystem sourceFS, String dir, FileSystem destFS, Storage destStorage) throws Exception {
        FileStatus[] status = sourceFS.listStatus(new Path(dir));

        for (int i = 0; i < status.length; i++) {
            System.out.println(status[i].getPath());
            if (status[i].isDir()) {
                visitAllFiles2(sourceFS, status[i].getPath().toString(), destFS, destStorage);
            } else {
                StorageFile storageFile = makeStorageFile(status[i].getPath().toString());
                String dest = getAbsolutePath(storageFile);
                transferFile(sourceFS, status[i].getPath().toString(), destFS, dest, storageFile.getId(), true);
            }
        }
    }

    private String[] getAllFilesInADir(String fSType, String path) {
        File dir = new File(path);
        String[] children = dir.list();
        return children;
    }

    private String getFileFromStorage(StorageFile storageFile, String target) throws IOException {
        String source = storageFile.getStorageId().getLocation() + storageFile.getLocation();

        String destination;
        if (target == null || target.isEmpty() || target.endsWith("/")) {
            // if the target is a directory or empty, use the source file name as the destination file name
            destination = (target == null || target.isEmpty()) ? storageFile.getFileName() : target + storageFile.getFileName();
        } else {
            destination = target;
        }

        transferFile(this.getLocalFileSystem(), source, this.getLocalFileSystem(), destination, null, false);

        return destination;
    }

    private StorageFile initiateTransferOneFile(String sourceType, String source, String destType, Integer storageDestination) throws IOException {
        FileSystem sourceFS = this.getLocalFileSystem();
        FileSystem destFS = this.getLocalFileSystem();
        Storage destStorage = Storage.findStorage(storageDestination);
        destFS.setVerifyChecksum(false);
        StorageFile storageFile = makeStorageFile(source);
        String dest = getAbsolutePath(storageFile);
        transferFile(sourceFS, source, destFS, dest, storageFile.getId(), true);
        return storageFile;
    }

    private void transferFile(FileSystem sourceType, String source, FileSystem destFS, String dest, Integer sourceStorageFileId, boolean makeDir) throws IOException {
        if (makeDir) {
            destFS.mkdirs(new Path(dest.substring(0, dest.lastIndexOf("/"))));
        }
        // FileUtil.copy(sourceType,new
        // Path("c://TEMP/springsource.exe"),destType,new
        // Path("Z://springsource.exe"),false,conf);
        boolean success = false;
        Exception e1 = null;
        try {
            FileUtil.copy(sourceType, new Path(source), destFS, new Path(dest), false, getLocalFileSystem().getConf());
            success = true;
            System.out.println("file: " + source + " transfered");
        } catch (Exception e) {
            success = false;
            e.printStackTrace();
            e1 = e;
        } finally {
            if (!success) {
                if (sourceStorageFileId != null) {
                    removeStorageFileEntry(sourceStorageFileId);
                }
                if (e1 != null) {
                    throw new IOException(e1);
                } else {
                    throw new IOException("File not transfered");
                }
            }
        }

    }

    private void transferString(String toStore, FileSystem destFS, String dest) throws Exception {
        destFS.mkdirs(new Path(dest.substring(0, dest.lastIndexOf("//"))));
        BufferedWriter br = new BufferedWriter(new OutputStreamWriter(destFS.create(new Path(dest), true)));
        // TO append data to a file, use fs.append(Path f)
        br.write(toStore);
        br.close();
        System.out.println("String transfered");
    }

    private StorageFile moveFile(StorageFile file, Storage storage) throws Exception {
        FileSystem destFS = this.getLocalFileSystem();
        String destPath = storage.getLocation() + file.getLocation();
        String sourcePath = file.getStorageId().getLocation() + file.getLocation();
        FileSystem sourceFs = this.getLocalFileSystem();
        destFS.mkdirs(new Path(destPath.substring(0, destPath.lastIndexOf("/"))));
        FileUtil.copy(this.getLocalFileSystem(), new Path(sourcePath), destFS, new Path(destPath), false, getLocalFileSystem().getConf());
        FileUtil.fullyDelete(sourceFs, new Path(sourcePath));
        file.setStorageId(storage);
        file.persist();
        System.out.println("move from ");
        System.out.println(sourcePath);
        System.out.println("To ");
        System.out.println(destPath);
        return file;
    }

    private String getStorageTypeFromFilePath(String filePath) {
        String type;
        if (filePath.startsWith(AccessMethodType.FILE)) {
            type = AccessMethodType.FILE;
        } else if (filePath.startsWith(AccessMethodType.HDFS)) {
            type = AccessMethodType.HDFS;
        } else if (filePath.startsWith(AccessMethodType.HTTP)) {
            type = AccessMethodType.HTTP;
        } else if (filePath.startsWith(AccessMethodType.FTP)) {
            type = AccessMethodType.FTP;
        } else if (filePath.startsWith(AccessMethodType.GLOBUS)) {
            type = AccessMethodType.GLOBUS;
        }
        type = AccessMethodType.FILE;
        return type;
    }

    private String getAbsoultuePath(String fileSystemType, String path) {
        if (path.startsWith(fileSystemType + ":")) {
            return path.substring(path.indexOf(":") + 1);
        }
        return path;

    }

    private StorageFile updateFile(String source, StorageFile target) throws IOException {
        String fileSysType = getStorageTypeFromFilePath(source);
        source = getAbsoultuePath(fileSysType, source);
        FileUtil.fullyDelete(this.getLocalFileSystem(), new Path(target.getStorageId().getLocation() + target.getLocation()));
        FileUtil.copy(this.getLocalFileSystem(), new Path(source), this.getLocalFileSystem(), new Path(target.getStorageId().getLocation() + target.getLocation()), false, getLocalFileSystem().getConf());
        return target;
    }

}
