package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.domain.GroupUser;
import edu.purdue.cybercenter.dm.service.GroupUserService;
import edu.purdue.cybercenter.dm.util.DomainObjectUtils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/groupusers")
@Controller
public class GroupUserController {

    @Autowired
    private GroupUserService groupUserService;

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getGroupUser(@RequestParam(value = "groupId", required = true) Integer groupId, @RequestParam(value = "userId", required = true) Integer userId, HttpServletRequest request, HttpServletResponse response) {
        GroupUser groupUser = groupUserService.getGroupUser(groupId, userId);

        String responseBody = DomainObjectUtils.toJson(groupUser, request.getContextPath());
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        return responseEntity;
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createGroupUser(@RequestBody String json, HttpServletRequest request, HttpServletResponse response) {
        GroupUser groupUser = DomainObjectUtils.fromJson(json, request.getContextPath(), GroupUser.class);
        groupUser = groupUserService.createGroupUser(groupUser);

        String responseBody = DomainObjectUtils.toJson(groupUser, request.getContextPath());
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.CREATED);

        return responseEntity;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> deleteGroupUser(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        groupUserService.deleteGroupUser(id);

        ResponseEntity<String> responseEntity =  new ResponseEntity<>(HttpStatus.NO_CONTENT);

        return responseEntity;
    }

}
