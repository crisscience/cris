package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.domain.Storage;
import edu.purdue.cybercenter.dm.domain.StorageFile;
import edu.purdue.cybercenter.dm.service.StorageService;
import edu.purdue.cybercenter.dm.storage.AccessMethodType;
import edu.purdue.cybercenter.dm.storage.StorageFileManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/transfer")
@Controller
public class TransferController {

    private StorageFileManager storageFileManager;
    @Autowired
    public void setStorageService(StorageService storageService) throws IOException {
        storageFileManager = storageService.getStorageFileManager(AccessMethodType.FILE);
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String list(Model model) {
        populateModel(model);

        return "storagemanagment/transfer";
    }

    /*********************************************************
     * Populate methods
     *********************************************************/
    private void populateModel(Model model) {
        List<Storage> storages = Storage.findAllStorages();
        model.addAttribute("storages", storages);
    }

    @ModelAttribute("storages")
    public java.util.Collection<Storage> populateStorages() {
        return null;
    }

    @RequestMapping(value = "/getDirList", method = RequestMethod.GET)
    public @ResponseBody
    List<String> getDirList(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // get parameters
        Map<String, String[]> params = request.getParameterMap();
        String loc = params.get("loc")[0];
        String sourceId = params.get("sourceId")[0];
        String path = consurcutPath(sourceId, loc);
        List<StorageFile> storageFiles = storageFileManager.listFile(path, null);
        List<String> files = new ArrayList<>();
        for (StorageFile storageFile : storageFiles) {
            files.add(storageFile.getName());
        }

        return files;
    }

    private String consurcutPath(String sourceId, String loc) {
        String path = "";
        Storage sourceStorage = Storage.findStorage(Integer.parseInt(sourceId));
        path =  loc;
        return path;
    }

    @RequestMapping(value = "/initiateTransfer", method = RequestMethod.GET)
    public @ResponseBody
    Map<String, Object> initiateTransfer(HttpServletRequest request,
            HttpServletResponse response) {
        // get parameters
        Map<String, String[]> params = request.getParameterMap();
        String fileId = params.get("fileId")[0];
        String destStorageId = params.get("destId")[0];
        System.out.println("Starting Transfer");

        String message = null;
        try {
            // storageManager.storeString("this is the teststring",
            // FileSystemType.LFS, "z://test3");
            // storageManager.initiateTrasfer(FileSystemType.LFS, "d://Tmp",
            // FileSystemType.LFS, "1");
          StorageFile f =   storageFileManager.moveFile(StorageFile.findStorageFile(Integer.parseInt(fileId)),
                    new StorageFile(), true);
//          StorageService.update("d:/testupdate.txt", f);
//            Storage storage = Storage.findStorage(1);
//            StorageFile file = StorageService.put(storage,
//                    "d:/Tmp/4910_appcompat.txt");
//            String result = StorageService.get(file, "d:/temp");
//            System.out.println(result);
//            message = "success";
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            message = e.getMessage();

        } finally {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("succeeded", message == null);
            map.put("message", message == null ? "" : message);
            return map;
        }

    }

}
