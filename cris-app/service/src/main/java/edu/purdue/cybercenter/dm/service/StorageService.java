/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.service;

import edu.purdue.cybercenter.dm.domain.Storage;
import edu.purdue.cybercenter.dm.domain.StorageAccessMethod;
import edu.purdue.cybercenter.dm.repository.StorageAccessMethodRepository;
import edu.purdue.cybercenter.dm.repository.StorageRepository;
import edu.purdue.cybercenter.dm.storage.AccessMethodType;
import edu.purdue.cybercenter.dm.storage.FileStorageFileManager;
import edu.purdue.cybercenter.dm.storage.StorageFileManager;
import edu.purdue.cybercenter.dm.storage.GlobusStorageFileManager;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author xu222
 */
@Service
public class StorageService {

    @Autowired
    private StorageRepository storageRepository;
    @Autowired
    private StorageAccessMethodRepository storageAccessMethodRepository;

    private final Map<String, StorageFileManager> storageFileManagers;

    public StorageService() {
        storageFileManagers = new HashMap<>();
    }

    public StorageFileManager getStorageFileManager(String type) throws IOException {
        StorageFileManager storageFileManager;
        if (this.storageFileManagers.containsKey(type)) {
            storageFileManager = this.storageFileManagers.get(type);
        } else {
            StorageAccessMethod storageAccessMethod = storageAccessMethodRepository.findByType(type);
            switch (type) {
                case AccessMethodType.FILE:
                    storageFileManager = new FileStorageFileManager(storageAccessMethod);
                    break;
                case AccessMethodType.FTP:
                    storageFileManager = new FileStorageFileManager(storageAccessMethod); // should be FtpStorageManager
                    break;
                case AccessMethodType.HTTP:
                    storageFileManager = new FileStorageFileManager(storageAccessMethod); // should be HttpStorageManager
                    break;
                case AccessMethodType.GLOBUS:
                    storageFileManager = new GlobusStorageFileManager(storageAccessMethod);
                    break;
                default:
                    throw new RuntimeException("Unknown access method: " + type);
            }
            storageFileManagers.put(type, storageFileManager);
        }

        return storageFileManager;
    }

    public String getStorageRoot() {
        Storage defaultStorage;

        List<Storage> storages = (List<Storage>) storageRepository.findAll();
        if (storages.size() == 1) {
            defaultStorage = storages.get(0);
        } else if (storages.isEmpty()) {
            throw new RuntimeException("no storage configured");
        } else {
            throw new RuntimeException("more than one storage configured");
        }

        String root = defaultStorage.getLocation();
        if (!root.endsWith("/")) {
            root += "/";
        }
        return root;
    }

}
