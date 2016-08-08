package edu.purdue.cybercenter.dm.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Configurable
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext.xml")
@Transactional
public class ViewSidIntegrationTest {

    @Autowired
    private ViewSidDataOnDemand dod;

    @Autowired
    private UserDataOnDemand dodUser;

    @Autowired
    private GroupDataOnDemand dodGroup;

    @Test
    public void testSid() {
        ViewSid viewSid = dod.getRandomViewSid();
        String sid = viewSid.getSid();

        if (!viewSid.isUser() && !viewSid.isGroup()) {
            Assert.isTrue(false, "viewSid is neither a user nor a group");
        }
        Assert.notNull(viewSid);
        Assert.isTrue(sid.startsWith("U") || sid.startsWith("G"), "sid should starts with either \"U\" or \"G\"");

        if (viewSid.isUser()) {
            User userFromSid = viewSid.toUser();
            User user = User.findUser(userFromSid.getId());
            Assert.isTrue(userFromSid == user, "User from ViewSid is not the same as from User");
        }

        if (viewSid.isGroup()) {
            Group groupFromSid = viewSid.toGroup();
            Group group = Group.findGroup(groupFromSid.getId());
            Assert.isTrue(groupFromSid == group, "group from ViewSid is not the same as from Group");
        }
    }

    @Test
    public void testUser() {
        User user = dodUser.getRandomUser();
        ViewSid viewSid = ViewSid.toViewSid(user);
        User userFromViewSid = viewSid.toUser();
        Assert.isTrue(viewSid.isUser(), "viewSid should be a user");
        Assert.isTrue(userFromViewSid == user, "User from ViewSid is not the same as from User");
    }

    @Test
    public void testGroup() {
        Group group = dodGroup.getRandomGroup();
        ViewSid viewSid = ViewSid.toViewSid(group);
        Group groupFromViewSid = viewSid.toGroup();
        Assert.isTrue(viewSid.isGroup(), "viewSid should be a group");
        Assert.isTrue(groupFromViewSid == group, "Group from ViewSid is not the same as from Group");
    }
}
