/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.storage;

import edu.purdue.cybercenter.dm.domain.Storage;
import edu.purdue.cybercenter.dm.domain.StorageFile;
import edu.purdue.cybercenter.dm.domain.Tenant;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;

/**
 *
 * @author xu222
 */
public class HdfsStorageFileManager extends AbstractStorageFileManager {

    private Configuration configuration;
    private Storage storage;

    public HdfsStorageFileManager(Storage storage) throws IOException {
        if (!AccessMethodType.FILE.equals(storage.getType())) {
            throw new RuntimeException("FileStorageFileManager cannot handle storge type: " + storage.getType());
        }

        this.configuration = new Configuration();
        this.storage = storage;
    }

    @Override
    public List<StorageFile> putFile(String sourcePath, StorageFile targetStorageFile, boolean sync) throws IOException {
        if (targetStorageFile != null && targetStorageFile.getStorageId() != this.storage) {
            throw new RuntimeException(String.format("Storage type: %s, target storage type: %s", this.storage.getType(), targetStorageFile.getStorageId().getType()));
        }

        String fileSystemType = this.getStorageTypeFromFilePath(sourcePath);
        StorageFile target = this.initiateTransferOneFile(fileSystemType, this.getAbsoultuePath(fileSystemType, sourcePath), storage.getType(), storage.getId());
        List<StorageFile> storageFiles = new ArrayList<StorageFile>();
        storageFiles.add(target);
        return storageFiles;
    }

    @Override
    public String getFile(StorageFile sourceStorageFile, String targetPath, boolean sync) throws IOException {
        String fileSystemType = this.getStorageTypeFromFilePath(targetPath);
        return this.getFileFromStorage(sourceStorageFile, this.getAbsoultuePath(fileSystemType, targetPath));
    }

    @Override
    public void deleteFile(StorageFile file) throws IOException {
        String sourcePath = file.getStorageId().getLocation() + file.getLocation();
        FileSystem sourceFs = getFileSystemType(file.getStorageId().getType());
        FileUtil.fullyDelete(sourceFs, new Path(sourcePath));
        removeStorageFileEntry(file.getId());
    }

    @Override
    public StorageFile transferFile(StorageFile source, StorageFile destination, boolean sync) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public StorageFile moveFile(StorageFile sourceStorageFile, StorageFile targetStorageFile, boolean sync) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<StorageFile> listFile(String path, FileFilter filter) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private FileSystem getFileSystemType(String type) throws IOException {
        FileSystem fs = null;
        if (type.equals(AccessMethodType.FILE)) {
            fs = FileSystem.getLocal(configuration).getRawFileSystem();
        }
        return fs;
    }

    @Override
    public void removeStorageFileEntry(Integer storageFileId) {
        if (storageFileId != null) {
            StorageFile storageFile = StorageFile.findStorageFile(storageFileId);
            storageFile.remove();
        }
    }

    private static String getStorageDesitiantion(FileSystem sourceFS, String source, String sourceType, FileSystem destFS, Storage dest, FileId fileId) {
        String fileName = "";
        StorageFile fileStorage = new StorageFile();
        if (source.length() > 255) {
            String extenstion = source.substring(source.lastIndexOf("."));
            source = source.substring(0, 255 - (extenstion.length())) + extenstion;
        }
        fileStorage.setSource(source);
        fileStorage.setStorageId(dest);
        fileStorage.setLocation(source);
        fileStorage.persist();
        fileId.setFileId(fileStorage.getId());
        String destPath = "0000000000000000";
        destPath = destPath.substring(0, 16 - fileStorage.getId().toString().length()) + fileStorage.getId().toString();
        String s0 = "";
        String s1 = "";
        String s2 = "";
        String s3 = "";
        String s4 = "";
        try {
            List<Tenant> tenants = Tenant.findAllTenants();
            if (tenants.size() == 1) {
                s0 = tenants.get(0).getUuid().toString();
            } else {
                s0 = "unknownUUID";
            }
        } catch (Exception e) {
            s0 = "unknownUUID";
            e.printStackTrace();
        }
        s1 = destPath.substring(0, 4);
        s2 = destPath.substring(4, 8);
        s3 = destPath.substring(8, 12);
        s4 = destPath.substring(12, 15);
        destPath = dest.getLocation() + s0 + "/" + s1 + "/" + s2 + "/" + s3 + "/" + s4 + "/";
        int destPathLength = destPath.length();
        if (sourceType.equals(AccessMethodType.STRING)) {
            destPath = destPath + fileStorage.getId();
        } else if (sourceType.equals(AccessMethodType.FILE)) {
            int fileNameLoc = source.lastIndexOf("\\");
            if (fileNameLoc == -1) {
                fileNameLoc = source.lastIndexOf("/");
            }
            fileName = source.substring(fileNameLoc + 1);
            if (fileName.length() + destPathLength + fileStorage.getId().toString().length() + 1 > 255) {
                String extenstion = source.substring(fileName.lastIndexOf("."));
                fileName = fileName.substring(0, 250 - (extenstion.length() + destPathLength + fileStorage.getId().toString().length() + dest.getType().length() + 2)) + extenstion;
            }
            destPath = destPath + fileStorage.getId() + "_" + fileName;
        }
        fileStorage.setFileName(fileName);
        fileStorage.setLocation(s0 + "/" + s1 + "/" + s2 + "/" + s3 + "/" + s4 + "/" + fileStorage.getId() + "_" + fileName);
        fileStorage.persist();
        return destPath;
    }

    // starting from a source root directory and destination repository
    // will need to traverse the subdirectories if any from the source
    private void initiateTrasfer(String sourceType, String source, String destType, Integer storageDestination) throws Exception {
        FileSystem sourceFS = getFileSystemType(sourceType);
        FileSystem destFS = getFileSystemType(destType);
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
            FileId fileId = new FileId();
            String dest = getStorageDesitiantion(sourceFS, dir.getAbsolutePath(), AccessMethodType.FILE, destFS, destStorage, fileId);
            transferFile(sourceFS, dir.getAbsolutePath(), destFS, dest, fileId, true);
        }
    }

    private void visitAllFiles2(FileSystem sourceFS, String dir, FileSystem destFS, Storage destStorage) throws Exception {
        FileStatus[] status = sourceFS.listStatus(new Path(dir));

        for (int i = 0; i < status.length; i++) {
            System.out.println(status[i].getPath());
            if (status[i].isDir()) {
                visitAllFiles2(sourceFS, status[i].getPath().toString(), destFS, destStorage);
            } else {
                FileId fileId = new FileId();
                String dest = getStorageDesitiantion(sourceFS, status[i].getPath().toString(), AccessMethodType.FILE, destFS, destStorage, fileId);
                transferFile(sourceFS, status[i].getPath().toString(), destFS, dest, fileId, true);
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

        transferFile(getFileSystemType(storageFile.getStorageId().getType()), source, getFileSystemType(AccessMethodType.FILE), destination, null, false);

        return destination;
    }

    private StorageFile initiateTransferOneFile(String sourceType, String source, String destType, Integer storageDestination) throws IOException {
        FileSystem sourceFS = getFileSystemType(sourceType);
        FileSystem destFS = getFileSystemType(destType);
        Storage destStorage = Storage.findStorage(storageDestination);
        destFS.setVerifyChecksum(false);
        FileId fileId = new FileId();
        String dest = getStorageDesitiantion(sourceFS, source, AccessMethodType.FILE, destFS, destStorage, fileId);
        transferFile(sourceFS, source, destFS, dest, fileId, true);
        return StorageFile.findStorageFile(fileId.getFileId());
    }

    private void transferFile(FileSystem sourceType, String source, FileSystem destFS, String dest, FileId fileId, boolean makeDir) throws IOException {
        if (makeDir) {
            destFS.mkdirs(new Path(dest.substring(0, dest.lastIndexOf("/"))));
        }
        // FileUtil.copy(sourceType,new
        // Path("c://TEMP/springsource.exe"),destType,new
        // Path("Z://springsource.exe"),false,conf);
        boolean success = false;
        Exception e1 = null;
        try {
            FileUtil.copy(sourceType, new Path(source), destFS, new Path(dest), false, configuration);
            success = true;
            System.out.println("file: " + source + " transfered");
        } catch (Exception e) {
            success = false;
            e.printStackTrace();
            e1 = e;
        } finally {
            if (!success) {
                if (fileId != null) {
                    removeStorageFileEntry(fileId.getFileId());
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
        FileSystem destFS = getFileSystemType(storage.getType());
        String destPath = storage.getLocation() + file.getLocation();
        String sourcePath = file.getStorageId().getLocation() + file.getLocation();
        FileSystem sourceFs = getFileSystemType(file.getStorageId().getType());
        destFS.mkdirs(new Path(destPath.substring(0, destPath.lastIndexOf("/"))));
        FileUtil.copy(getFileSystemType(file.getStorageId().getType()), new Path(sourcePath), destFS, new Path(destPath), false, configuration);
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
            type = AccessMethodType.HDFS;
        } else if (filePath.startsWith(AccessMethodType.FTP)) {
            type = AccessMethodType.FTP;
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
        FileUtil.fullyDelete(getFileSystemType(fileSysType), new Path(target.getStorageId().getLocation() + target.getLocation()));
        FileUtil.copy(getFileSystemType(target.getStorageId().getType()), new Path(source), getFileSystemType(target.getStorageId().getType()), new Path(target.getStorageId().getLocation() + target.getLocation()), false, configuration);
        return target;
    }

}
