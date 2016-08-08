/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.storage;

import edu.purdue.cybercenter.dm.domain.StorageFile;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface StorageFileManager {

    public List<StorageFile> putFile(String source, StorageFile targetStorageFile, boolean sync) throws IOException;
    public List<StorageFile> putFile(String sourcePath, InputStream source, StorageFile targetStorageFile, boolean sync) throws IOException;

    public String getFile(StorageFile sourceStorageFile, String target, boolean sync) throws IOException;

    public void deleteFile(StorageFile storageFile) throws IOException;

    public StorageFile transferFile(StorageFile sourceStorageFile, StorageFile targetStorageFile, boolean sync)  throws IOException;

    public StorageFile moveFile(StorageFile sourceStorageFile, StorageFile targetStorageFile, boolean sync) throws IOException;

    public List<StorageFile> listFile(String path, FileFilter filter) throws IOException;
}
