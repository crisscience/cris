/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.storage;

import edu.purdue.cybercenter.dm.domain.Storage;
import edu.purdue.cybercenter.dm.domain.StorageFile;
import edu.purdue.cybercenter.dm.domain.Tenant;
import edu.purdue.cybercenter.dm.util.ServiceUtils;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;

/**
 *
 * @author xu222
 */
public abstract class AbstractStorageFileManager implements StorageFileManager{

    private static final String WORKAREA = "workarea/";

    //protected final Configuration configuration;
    protected final FileSystem localFileSystem;
    protected final Storage localStorage;

    public AbstractStorageFileManager() throws IOException {
        Configuration configuration = new Configuration();
        this.localFileSystem = FileSystem.getLocal(configuration).getRawFileSystem();

        // the one and only default storage
        List<Storage> storages = (List<Storage>) Storage.findAllStorages();
        if (storages.size() == 1) {
            localStorage = storages.get(0);
        } else if (storages.isEmpty()) {
            throw new RuntimeException("no storage configured");
        } else {
            throw new RuntimeException("more than one storage configured");
        }
    }

    @Override
    public List<StorageFile> putFile(String sourceName, InputStream sourceStream, StorageFile targetStorageFile, boolean sync) throws IOException {
        throw new RuntimeException("The method is not implemented for this storage type");
    }

    @Override
    public void deleteFile(StorageFile file) throws IOException {
        String sourcePath = file.getStorageId().getLocation() + file.getLocation();
        FileUtil.fullyDelete(localFileSystem, new Path(sourcePath));
        removeStorageFileEntry(file.getId());
    }

    @Override
    public StorageFile transferFile(StorageFile sourceStorageFile, StorageFile targetStorageFile, boolean sync) throws IOException {
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

    protected String getWorkareaDirectory() {
        return getRootDirectory() + WORKAREA;
    }

    protected String getRootDirectory() {
        String path = localStorage.getLocation();
        if (!path.endsWith("/")) {
            path += "/";
        }
        return path;
    }

    protected List<StorageFile> makeStorageFiles(List<String> files) {
        List<StorageFile> storageFiles = new ArrayList<>();
        for (String file: files) {
            StorageFile storageFile = makeStorageFile(file);
            storageFiles.add(storageFile);
        }
        return storageFiles;
    }

    protected StorageFile makeStorageFile(String source) {
        StorageFile storageFile = new StorageFile();
        storageFile.setName(source);
        storageFile.setStorageId(localStorage);
        storageFile.setLocation("");
        storageFile.persist();

        String s0;
        try {
            List<Tenant> tenants = Tenant.findAllTenants();
            if (tenants.size() == 1) {
                s0 = tenants.get(0).getUuid().toString();
            } else {
                s0 = "unknownUUID";
            }
        } catch (Exception e) {
            s0 = "unknownUUID";
        }

        int fileNameIndex = source.lastIndexOf("/");
        if (fileNameIndex == -1) {
            fileNameIndex = source.lastIndexOf("\\");
        }
        String sourceName;
        if (fileNameIndex != -1) {
            sourceName = source.substring(fileNameIndex + 1);
        } else {
            sourceName = source;
        }

        Integer storageFileId = storageFile.getId();
        String dest = s0 + "/" + ServiceUtils.makeFilePath(storageFileId, sourceName);

        storageFile.setFileName(sourceName);
        storageFile.setLocation(dest);
        storageFile.merge();

        return storageFile;
    }

    protected String getAbsolutePath(StorageFile storageFile) throws IOException {
        return localStorage.getLocation() + storageFile.getLocation();
    }

    protected FileSystem getLocalFileSystem() throws IOException {
        return this.localFileSystem;
    }

    protected void removeStorageFileEntry(Integer storageFileId) {
        if (storageFileId != null) {
            StorageFile storageFile = StorageFile.findStorageFile(storageFileId);
            storageFile.remove();
        }
    }
}
