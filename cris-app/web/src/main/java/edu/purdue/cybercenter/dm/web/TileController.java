package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.domain.Shortcut;
import edu.purdue.cybercenter.dm.domain.Tile;
import edu.purdue.cybercenter.dm.service.DomainObjectService;
import edu.purdue.cybercenter.dm.util.DomainObjectHelper;
import edu.purdue.cybercenter.dm.util.DomainObjectUtils;
import edu.purdue.cybercenter.dm.util.Helper;
import edu.purdue.cybercenter.dm.web.util.WebJsonHelper;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/tiles")
@Controller
public class TileController {

    @Autowired
    private DomainObjectService domainObjectService;
    @Autowired
    private WebJsonHelper WebJsonHelper;

    @RequestMapping(value = "/index", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String index(Model model) {
        return "tiles/index";
    }

    @RequestMapping(value = "/layout", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object layoutTile(@RequestBody String json, HttpServletRequest request, HttpServletResponse response) {
        // layout is a array of map of {id, location}
        List<Map<String, Object>> layout = Helper.deserialize(json, List.class);
        layout.stream().forEach((item) -> {
            Integer id = (Integer) item.get("id");
            String location = (String) item.get("location");
            Tile tile = domainObjectService.findById(id, Tile.class);
            tile.setLocation(location);
            domainObjectService.persist(tile, Tile.class);
        });
        return WebJsonHelper.list(request, response, Tile.class);
    }

    @RequestMapping(value = "/click/{id}", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String run(@PathVariable("id") Integer id, Model model, HttpServletRequest request, HttpServletResponse response) {
        TypedQuery<Tile> query = DomainObjectHelper.createNamedQuery("Tile.findById", Tile.class);
        query.setParameter("id", id);
        Tile mTile = domainObjectService.executeTypedQueryWithSingleResult(query);
        Shortcut shortCut = mTile.getShortcutId();
        if (shortCut != null) {
            UUID uuid = shortCut.getUuid();
            String forwardingUrl = "/shortcuts/run/" + uuid.toString();
            return "forward:" + forwardingUrl;
        }

        return "home";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object showJson(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        return WebJsonHelper.show(id, request, response, Tile.class);
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String listJson(HttpServletRequest request, HttpServletResponse response) {
        return WebJsonHelper.list(request, response, Tile.class);
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object createFromJson(@RequestBody String json, HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
        return save(json, request, response);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object updateFromJson(@RequestBody String json, HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
        return save(json, request, response);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> deleteFromJson(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        Object deletingTile = this.showJson(id, request, response);

        TypedQuery<Tile> query = DomainObjectHelper.createNamedQuery("Tile.findById", Tile.class);
        query.setParameter("id", id);
        Tile mTile = domainObjectService.executeTypedQueryWithSingleResult(query);
        Shortcut shortCut = mTile.getShortcutId();
        if (shortCut != null) {
            Integer shortcutId = shortCut.getId();
            WebJsonHelper.delete(shortcutId, request, response, Shortcut.class);
        }

        return WebJsonHelper.delete(id, request, response, Tile.class);

    }

    private Object save(String json, HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
        Map<String, Object> map = Helper.deserialize(URLDecoder.decode(json, "UTF-8"), Map.class);
        Integer tileId = (Integer) map.get("tileId");
        Integer shortcutId = (Integer) map.get("shortcutId");
        String url = (String) map.get("url");
        String name = (String) map.get("name");
        String description = (String) map.get("description");
        String size = (String) map.get("size");
        String color = (String) map.get("color");

        if (url == null || url.isEmpty()) {
            throw new RuntimeException("Missing URL for the tile");
        } else if (url.length() > 8) {
            int index = url.indexOf("/", 8) + request.getContextPath().length();
            if (index == -1) {
                throw new RuntimeException("Invalid URL for the tile: " + url);
            }
            url = url.substring(index);

        } else {
            throw new RuntimeException("Invalid URL for the tile: " + url);
        }

        if (name == null || name.isEmpty()) {
            throw new RuntimeException("Missing name for the tile");
        }

        // create a shortcut
        Shortcut shortcut;
        if (shortcutId == null) {
            shortcut = new Shortcut();
        } else {
            shortcut = domainObjectService.findById(shortcutId, Shortcut.class);
            if (shortcut == null) {
                throw new RuntimeException("Shortcut does not exist: " + shortcutId);
            }
        }
        shortcut.setUrl(url);
        shortcut.setName(name);
        shortcut.setDescription(description);
        domainObjectService.persist(shortcut, Shortcut.class);

        // create a tile
        String height = "120px";
        String width;
        if ("rectangle".equalsIgnoreCase(size)) {
            width = "180px";
        } else {
            width = "120px";
        }
        Tile tile;
        if (tileId == null) {
            tile = new Tile();
        } else {
            tile = domainObjectService.findById(tileId, Tile.class);
            if (tile == null) {
                throw new RuntimeException("Tile does not exist: " + tileId);
            }
        }
        tile.setName(name);
        tile.setDescription(description);
        tile.setShortcutId(shortcut);
        tile.setHtml("<div id=tileId class=\"dojoDndItem\" style=\"resStyle\"><div class=\"tileImage\" style=\"background-image: url(dummyUrl);\"></div><div class=\"tileTitle\">tileName</div></div>");
        tile.setStyle("{\"height\":\"" + height + "\",\"width\":\"" + width + "\",\"float\":\"left\",\"margin\":\"5px 4px 5px 4px\",\"background-color\":\"" + color + "\"}");
        domainObjectService.persist(tile, Tile.class);

        return DomainObjectUtils.toJson(tile, "");
    }

}
