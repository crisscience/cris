DELETE FROM "public"."tenant";
INSERT INTO "public"."tenant"("id", "uuid", "url_identifier", "name", "enabled") VALUES(1, 'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'brouder_sylvie', 'Sylvie Brouder''s Workspace', true);
ALTER SEQUENCE "public"."tenant_id_seq" RESTART WITH 2;

DELETE FROM "public"."acl_sid";
INSERT INTO "public"."acl_sid"("id", "principal", "sid", "tenant_id") VALUES('1', true, 'george.washington', 1);
INSERT INTO "public"."acl_sid"("id", "principal", "sid", "tenant_id") VALUES('2', true, 'john.adams', 1);
INSERT INTO "public"."acl_sid"("id", "principal", "sid", "tenant_id") VALUES('3', true, '3', 1);
INSERT INTO "public"."acl_sid"("id", "principal", "sid", "tenant_id") VALUES('4', false, '1001', 1);
INSERT INTO "public"."acl_sid"("id", "principal", "sid", "tenant_id") VALUES('5', true, '2', 1);
INSERT INTO "public"."acl_sid"("id", "principal", "sid", "tenant_id") VALUES('6', true, '4', 1);
INSERT INTO "public"."acl_sid"("id", "principal", "sid", "tenant_id") VALUES('7', false, '1003', 1);
ALTER SEQUENCE "public"."acl_sid_id_seq" RESTART WITH 8;

DELETE FROM "public"."acl_class";
INSERT INTO "public"."acl_class"("id", "class", "tenant_id") VALUES('1', 'edu.purdue.cybercenter.dm.domain.Project', 1);
INSERT INTO "public"."acl_class"("id", "class", "tenant_id") VALUES('2', 'edu.purdue.cybercenter.dm.domain.Experiment', 1);
INSERT INTO "public"."acl_class"("id", "class", "tenant_id") VALUES('3', 'edu.purdue.cybercenter.dm.domain.Job', 1);
ALTER SEQUENCE "public"."acl_class_id_seq" RESTART WITH 4;

DELETE FROM "public"."acl_object_identity";
INSERT INTO "public"."acl_object_identity"("id", "object_id_class", "object_id_identity", "parent_object", "owner_sid", "entries_inheriting", "tenant_id") VALUES('1', '2', '7002', null, '2', true, 1);
INSERT INTO "public"."acl_object_identity"("id", "object_id_class", "object_id_identity", "parent_object", "owner_sid", "entries_inheriting", "tenant_id") VALUES('2', '1', '5001', null, '1', true, 1);
INSERT INTO "public"."acl_object_identity"("id", "object_id_class", "object_id_identity", "parent_object", "owner_sid", "entries_inheriting", "tenant_id") VALUES('3', '1', '5002', null, '1', true, 1);
INSERT INTO "public"."acl_object_identity"("id", "object_id_class", "object_id_identity", "parent_object", "owner_sid", "entries_inheriting", "tenant_id") VALUES('4', '1', '5003', null, '1', true, 1);
INSERT INTO "public"."acl_object_identity"("id", "object_id_class", "object_id_identity", "parent_object", "owner_sid", "entries_inheriting", "tenant_id") VALUES('5', '1', '0', null, '1', true, 1);
INSERT INTO "public"."acl_object_identity"("id", "object_id_class", "object_id_identity", "parent_object", "owner_sid", "entries_inheriting", "tenant_id") VALUES('6', '2', '7004', null, '1', true, 1);
INSERT INTO "public"."acl_object_identity"("id", "object_id_class", "object_id_identity", "parent_object", "owner_sid", "entries_inheriting", "tenant_id") VALUES('7', '2', '7005', null, '2', true, 1);
INSERT INTO "public"."acl_object_identity"("id", "object_id_class", "object_id_identity", "parent_object", "owner_sid", "entries_inheriting", "tenant_id") VALUES('8', '2', '7006', null, '1', true, 1);
INSERT INTO "public"."acl_object_identity"("id", "object_id_class", "object_id_identity", "parent_object", "owner_sid", "entries_inheriting", "tenant_id") VALUES('9', '3', '752', null, '1', true, 1);
INSERT INTO "public"."acl_object_identity"("id", "object_id_class", "object_id_identity", "parent_object", "owner_sid", "entries_inheriting", "tenant_id") VALUES('10', '3', '753', null, '1', true, 1);
INSERT INTO "public"."acl_object_identity"("id", "object_id_class", "object_id_identity", "parent_object", "owner_sid", "entries_inheriting", "tenant_id") VALUES('11', '3', '754', null, '2', true, 1);
INSERT INTO "public"."acl_object_identity"("id", "object_id_class", "object_id_identity", "parent_object", "owner_sid", "entries_inheriting", "tenant_id") VALUES('12', '2', '7001', 2, '6', true, 1);
ALTER SEQUENCE "public"."acl_object_identity_id_seq" RESTART WITH 13;

DELETE FROM "public"."acl_entry";
INSERT INTO "public"."acl_entry"("id", "acl_object_identity", "ace_order", "sid", "mask", "granting", "audit_success", "audit_failure", "tenant_id") VALUES ('1', '1', 0, '3', 11, true, false, false, 1);
INSERT INTO "public"."acl_entry"("id", "acl_object_identity", "ace_order", "sid", "mask", "granting", "audit_success", "audit_failure", "tenant_id") VALUES ('2', '1', 1, '4', 11, true, false, false, 1);
INSERT INTO "public"."acl_entry"("id", "acl_object_identity", "ace_order", "sid", "mask", "granting", "audit_success", "audit_failure", "tenant_id") VALUES ('3', '2', 0, '5', 1, true, false, false, 1);
INSERT INTO "public"."acl_entry"("id", "acl_object_identity", "ace_order", "sid", "mask", "granting", "audit_success", "audit_failure", "tenant_id") VALUES ('4', '3', 0, '5', 2, true, false, false, 1);
INSERT INTO "public"."acl_entry"("id", "acl_object_identity", "ace_order", "sid", "mask", "granting", "audit_success", "audit_failure", "tenant_id") VALUES ('5', '4', 0, '5', 9, true, false, false, 1);
INSERT INTO "public"."acl_entry"("id", "acl_object_identity", "ace_order", "sid", "mask", "granting", "audit_success", "audit_failure", "tenant_id") VALUES ('6', '8', 0, '5', 9, true, false, false, 1);
INSERT INTO "public"."acl_entry"("id", "acl_object_identity", "ace_order", "sid", "mask", "granting", "audit_success", "audit_failure", "tenant_id") VALUES ('7', '8', 1, '6', 3, true, false, false, 1);
INSERT INTO "public"."acl_entry"("id", "acl_object_identity", "ace_order", "sid", "mask", "granting", "audit_success", "audit_failure", "tenant_id") VALUES ('8', '4', 1, '7', 1, true, false, false, 1);
INSERT INTO "public"."acl_entry"("id", "acl_object_identity", "ace_order", "sid", "mask", "granting", "audit_success", "audit_failure", "tenant_id") VALUES ('9', '9', 0, '3', 1, true, false, false, 1);
INSERT INTO "public"."acl_entry"("id", "acl_object_identity", "ace_order", "sid", "mask", "granting", "audit_success", "audit_failure", "tenant_id") VALUES ('10', '9', 1, '5', 3, true, false, false, 1);
INSERT INTO "public"."acl_entry"("id", "acl_object_identity", "ace_order", "sid", "mask", "granting", "audit_success", "audit_failure", "tenant_id") VALUES ('11', '9', 2, '6', 9, true, false, false, 1);
INSERT INTO "public"."acl_entry"("id", "acl_object_identity", "ace_order", "sid", "mask", "granting", "audit_success", "audit_failure", "tenant_id") VALUES ('12', '10', 0, '7', 1, true, false, false, 1);
INSERT INTO "public"."acl_entry"("id", "acl_object_identity", "ace_order", "sid", "mask", "granting", "audit_success", "audit_failure", "tenant_id") VALUES ('13', '12', 0, '6', 15, true, false, false, 1);
INSERT INTO "public"."acl_entry"("id", "acl_object_identity", "ace_order", "sid", "mask", "granting", "audit_success", "audit_failure", "tenant_id") VALUES ('14', '2', 1, '6', 4, true, false, false, 1);
ALTER SEQUENCE "public"."acl_entry_id_seq" RESTART WITH 15;

DELETE FROM "public"."configuration";
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('externalSource', 'text', 'Purdue University', null);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('CopyrightYear', 'text', '2012', null);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsFavicon', 'text', 'static/images/favicon.ico', null);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsBannerImage', 'text', 'static/images/header.jpg', null);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsAuthBackgroundImage', 'text', '', null);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsName', 'text', 'CRIS Application', null);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsDescription', 'text', 'Please add the workspace name to the end of the URL to access your workspace', null);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsSigninInstruction', 'text', 'If you are a Purdue user, use your Purdue Career Account.', null);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsSignupInstruction', 'text', 'If you have a Purdue Career Account, please use that instead of creating a new account.', null);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsAuthProblem', 'text', 'Please provide the email that you used as username. Password reset instruction will be sent to this email', null);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsAuthReset', 'text', 'Please copy/paste the token in the email that has been sent to your, then enter a new password', null);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsEmailGeneral', 'text', 'cyber@purdue.edu', null);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsEmailAccountProblem', 'text', 'cyber@purdue.edu', null);

INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('externalSource', 'text', 'Purdue University', 1);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('CopyrightYear', 'text', '2012', 1);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsFavicon', 'text', 'static/images/favicon.ico', 1);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsBannerImage', 'text', 'static/images/header.jpg', 1);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsAuthBackgroundImage', 'text', '', 1);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsName', 'text', 'Sylvie Brouder''s Workspace', 1);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsDescription', 'text', '<b>Extension:</b><br>
<ul>
<li>Provide technical information on plant nutrition, nutrient management and cropping systems to farmers, county educators, and industry.</li>
<li>Develop/deliver workshops on nutrient management and produce educational materials (hard copy and electronic) for the Certified Crop Advisor Program and other educational multipliers including State and Federal agency personnel.</li>
<li>Facilitate educational programming focused on the commercial agriculture/environment interface to promote awareness and knowledge among stakeholders of the complexities of simultaneous management of agricultural lands for bioenergy, food security and stewardship objectives.</li></ul>
<br>
<b>Research Areas:</b>
<ul>
<li>Carbon and nitrogen cycling, soil sequestration, greenhouse gas emission and water quality impacts of intensively and extensively managed agriculture including maize-based and emerging, dedicated bioenergy cropping systems and native prairies.</li>
<li>Tensions and trade-off among non-provisioning ecosystem services as influenced by management and technology.</li>
<li>Root/soil interrelationships and nutrient accumulation patterns for Indiana cropping systems.</li>
<li>Indigenous soil potassium, fertilizer-potassium use efficiency, and potassium balance in Indiana maize and soybean production.</li>
<li>Development of the scientific foundations for improved fertilizer recommendation protocols for Indiana cropping systems.</li></ul>', 1);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsSigninInstruction', 'text', 'If you are a Purdue user, use your Purdue Career Account.', 1);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsSignupInstruction', 'text', 'If you have a Purdue Career Account, please use that instead of creating a new account.', 1);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsAuthProblem', 'text', 'Please provide the email that you used as username. Password reset instruction will be sent to this email', 1);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsAuthReset', 'text', 'Please copy/paste the token in the email that has been sent to your, then enter a new password', 1);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsEmailGeneral', 'text', 'cyber@purdue.edu', 1);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsEmailAccountProblem', 'text', 'cyber@purdue.edu', 1);

DELETE FROM "public"."small_object";
INSERT INTO "public"."small_object"("name", "content", "tenant_id") VALUES('icon', 'static/images/favicon.ico', 1);

DELETE FROM "public"."classification";
INSERT INTO "public"."classification"("id", "code", "name", "description", "tenant_id") VALUES(1, 1, 'Member', 'PUCCR Member', 1);
INSERT INTO "public"."classification"("id", "code", "name", "description", "tenant_id") VALUES(2, 2, 'Non-Member', 'PUCCR Non-Member', 1);
ALTER SEQUENCE "public"."classification_id_seq" RESTART WITH 3;

DELETE FROM "public"."session";

DELETE FROM "public"."user";
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (1, 'george.washington', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'george', '', 'washington', 'george.washington@whitehouse.gov', true, true, true, true, 1);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (2, 'john.adams', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'john', '', 'adams', 'john.adams@whitehouse.gov', true, true, true, true, 1);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (3, 'abraham.lincoln', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'abraham', '', 'lincoln', 'abraham.lincoln@whitehouse.gov', true, true, true, true, 1);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (4, 'george.w.bush', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'george', 'w', 'bush', 'george.w.bush@whitehouse.gov', true, true, true, true, 1);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (5, 'bill.clinton', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'bill', '', 'clinton', 'bill.clinton@whitehouse.gov', true, true, true, true, 1);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (6, 'ronald.reagen', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'ronald', '', 'reagen', 'ronald.reagen@whitehouse.gov', true, true, true, true, 1);
ALTER SEQUENCE "public"."user_id_seq" RESTART WITH 7;

DELETE FROM "public"."group";
INSERT INTO "public"."group"("id", "name", "description", "owner_id", "classification_id", "tenant_id") VALUES (1000, 'Admin Group', '', 1, 1, 1);
INSERT INTO "public"."group"("id", "name", "description", "owner_id", "classification_id", "tenant_id") VALUES (1001, 'Mass Spectrometry Technician Group ', '', 1, 1, 1);
INSERT INTO "public"."group"("id", "name", "description", "owner_id", "classification_id", "tenant_id") VALUES (1002, 'Business Group', '', 1, 1, 1);
INSERT INTO "public"."group"("id", "name", "description", "owner_id", "classification_id", "tenant_id") VALUES (1003, 'PI: Mark S Cushman', '', 1, 1, 1);
INSERT INTO "public"."group"("id", "name", "description", "owner_id", "classification_id", "tenant_id") VALUES (1004, 'Acme', '', 1, 2, 1);
INSERT INTO "public"."group"("id", "name", "description", "owner_id", "classification_id", "tenant_id") VALUES (1005, 'Test Group', '', 1, 1, 1);
ALTER SEQUENCE "public"."group_id_seq" RESTART WITH 2000;

DELETE FROM "public"."group_user";
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (1000, 1, 1);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (1000, 6, 1);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (1001, 1, 1);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (1001, 3, 1);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (1001, 4, 1);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (1002, 2, 1);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (1003, 3, 1);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (1003, 4, 1);

DELETE FROM "public"."project";
INSERT INTO "public"."project"("id", "asset_type_id", "status_id", "name", "description", "tenant_id") VALUES(5001, 7, 1, 'NSF Fund 12345678', 'Purdue Center for Cancer Research', 1);
INSERT INTO "public"."project"("id", "asset_type_id", "status_id", "name", "description", "tenant_id") VALUES(5002, 7, 1, 'NIH Fund ABCDEFGH', 'Purdue Center for Cancer Research', 1);
INSERT INTO "public"."project"("id", "asset_type_id", "status_id", "name", "description", "tenant_id") VALUES(5003, 7, 1, 'Google Fund ABCD1234', 'Purdue Center for Agronomy Research', 1);
INSERT INTO "public"."project"("id", "asset_type_id", "status_id", "name", "description", "tenant_id") VALUES(5004, 7, 1, 'Intel Fund MNOP6789', 'Purdue Center for Semiconductor Research', 1);
INSERT INTO "public"."project"("id", "asset_type_id", "status_id", "name", "description", "tenant_id") VALUES(5005, 7, 0, 'ProjectControllerDeprecatedTest Project', 'Cyber Center at Purdue', 1);
ALTER SEQUENCE "public"."project_id_seq" RESTART WITH 5006;

DELETE FROM "public"."experiment";
INSERT INTO "public"."experiment"("id", "asset_type_id", "project_id", "status_id", "name", "description", "tenant_id") VALUES(7001, 5, 5001, 1, 'NSF Fund ABCD1234 Experiment 1', 'Purdue Center for Cancer Research', 1);
INSERT INTO "public"."experiment"("id", "asset_type_id", "project_id", "status_id", "name", "description", "tenant_id") VALUES(7002, 5, 5001, 1, 'NSF Fund ABCD1234 Experiment 2', 'Purdue Center for Cancer Research', 1);
INSERT INTO "public"."experiment"("id", "asset_type_id", "project_id", "status_id", "name", "description", "tenant_id") VALUES(7003, 5, 5002, 1, 'NSF Fund ABCD1234 Experiment 3', 'Purdue Center for Cancer Research', 1);
INSERT INTO "public"."experiment"("id", "asset_type_id", "project_id", "status_id", "name", "description", "tenant_id") VALUES(7004, 5, 5003, 1, 'Google Fund ABCD1234 Experiment 1', 'Purdue Center for Agronomy Research', 1);
INSERT INTO "public"."experiment"("id", "asset_type_id", "project_id", "status_id", "name", "description", "tenant_id") VALUES(7005, 5, 5004, 1, 'Intel Fund MNOP6789 Experiment 1', 'Purdue Center for Semiconductor Research', 1);
INSERT INTO "public"."experiment"("id", "asset_type_id", "project_id", "status_id", "name", "description", "tenant_id") VALUES(7006, 5, 5004, 1, 'Intel Fund MNOP6789 Experiment 2', 'Purdue Center for Semiconductor Research', 1);
INSERT INTO "public"."experiment"("id", "asset_type_id", "project_id", "status_id", "name", "description", "tenant_id") VALUES(7007, 5, 5001, 0, 'Deprecated Project', 'Cyber Center at Purdue', 1);
ALTER SEQUENCE "public"."experiment_id_seq" RESTART WITH 7008;

DELETE FROM "public"."resource";
INSERT INTO "public"."resource"("id", "asset_type_id", "status_id", "name", "description", "owner_id", "tenant_id") VALUES(6001, 6, 1, 'HPLC Resource', 'HPLC related', 1000, 1);
INSERT INTO "public"."resource"("id", "asset_type_id", "status_id", "name", "description", "owner_id", "tenant_id") VALUES(6002, 6, 1, 'MASS Spec', 'MASS spec related', 1000, 1);
ALTER SEQUENCE "public"."resource_id_seq" RESTART WITH 6003;

DELETE FROM "public"."vocabulary";
DELETE FROM "public"."term";
INSERT INTO "public"."vocabulary"("id", "asset_type_id", "uuid", "version_number", "name", "description", "domain", "content", "image_id", "owner_id", "status_id", "creator_id", "updater_id", "time_created", "time_updated", "tenant_id") VALUES(1, null, '51c5ddd0-09a2-11e2-892e-0800200c9a66', 'a7ccaf90-c8a3-11e2-8b8b-0800200c9a66', 'HPLC Instrument Collection', 'HPLC Instrument Collection', 'test domain',
'<?xml version="1.0" encoding="UTF-8"?>
<vocabulary
    xmlns="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0 vocabulary-1.0.0.xsd"
    uuid="51c5ddd0-09a2-11e2-892e-0800200c9a66" version="a7ccaf90-c8a3-11e2-8b8b-0800200c9a66">

    <domain>HPLC</domain>
    <name>HPLC Vocabulary</name>
    <description>Vocabulary for HPLC</description>
    <contributors>
        <contributor>John Doe</contributor>
        <contributor>Jane Doe</contributor>
    </contributors>
    <copyright>(C)2011 Purdue University</copyright>

    <terms>
    </terms>
</vocabulary>
', null, null, 1, null, null, '2013-03-21 11:22:11.802', '2013-03-21 11:22:11.802', 1);
INSERT INTO "public"."vocabulary"("id", "asset_type_id", "uuid", "version_number", "name", "description", "domain", "content", "image_id", "owner_id", "status_id", "creator_id", "updater_id", "time_created", "time_updated", "tenant_id") VALUES(2, null, '7193dbbd-2b79-4d82-833d-36465e2bfb90', '7c99184d-2594-4720-86c5-67c024111e64', 'Globus Test Collection', 'Globus Test Collection', 'test domain',
'<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<vocabulary xmlns="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" uuid="7193dbbd-2b79-4d82-833d-36465e2bfb90" version="7c99184d-2594-4720-86c5-67c024111e64" xsi:schemaLocation="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0 vocabulary-1.0.0.xsd">
    <domain>Globus Test</domain>
    <name>Globus Test</name>
    <description></description>
    <contributors/>
    <copyright></copyright>
    <terms>
        <term uuid="8b59449a-6103-4e04-87a0-6657e17015b1" version="476627c7-5e25-40eb-aaac-c8a30364bb20" ui-display-order="0">
            <name>Name</name>
            <type>simple</type>
            <description>Name</description>
            <validation>
                <validator type="text">
                    <property name="type"></property>
                    <property name="length"></property>
                    <property name="ui-vertical-lines">1</property>
                </validator>
            </validation>
        </term>
        <term uuid="c5fbc688-bc61-4fe9-8e25-268f25b95bae" version="fb67901e-3aa2-4005-82c6-d359e535da97" ui-display-order="1">
            <name>browse file</name>
            <type>simple</type>
            <description>browse file</description>
            <validation>
                <validator type="file">
                    <property name="multiple">false</property>
                    <property name="globus">true</property>
                </validator>
            </validation>
        </term>
    </terms>
</vocabulary>
', null, null, 1, null, null, '2015-03-10 10:51:11.802', '2015-03-10 10:51:11.802', 1);
ALTER SEQUENCE "public"."vocabulary_id_seq" RESTART WITH 1000;

--------------------------------------------------------
-- Terms
--------------------------------------------------------
INSERT INTO "public"."term"("asset_type_id", "uuid", "version_number", "name", "description", "key", "content", "image_id", "owner_id", "status_id", "creator_id", "updater_id", "time_created", "time_updated", "tenant_id", "vocabulary_uuid", "vocabulary_id", "is_template") VALUES(null, 'c5fbc688-bc61-4fe9-8e25-268f25b95bae', 'fb67901e-3aa2-4005-82c6-d359e535da97', 'browse file', 'browse file', null,
'<?xml version="1.0" encoding="UTF-8"?>
<term uuid="c5fbc688-bc61-4fe9-8e25-268f25b95bae" version="fb67901e-3aa2-4005-82c6-d359e535da97"
	  xmlns="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0 vocabulary-1.0.0.xsd">
            <name>browse file</name>
            <type>simple</type>
            <description>browse file</description>
            <validation>
                <validator type="file">
                    <property name="multiple">false</property>
                    <property name="globus">true</property>
                </validator>
            </validation>
        </term>
', null, null, 1, null, null, '2013-03-21 11:22:11.802', '2013-03-21 11:22:11.802', 1, '7193dbbd-2b79-4d82-833d-36465e2bfb90', 1, false);
INSERT INTO "public"."term"("asset_type_id", "uuid", "version_number", "name", "description", "key", "content", "image_id", "owner_id", "status_id", "creator_id", "updater_id", "time_created", "time_updated", "tenant_id", "vocabulary_uuid", "vocabulary_id", "is_template") VALUES(null, '8b59449a-6103-4e04-87a0-6657e17015b1', '476627c7-5e25-40eb-aaac-c8a30364bb20', 'browse file', 'browse file', null,
'<?xml version="1.0" encoding="UTF-8"?>
<term uuid="8b59449a-6103-4e04-87a0-6657e17015b1" version="476627c7-5e25-40eb-aaac-c8a30364bb20"
	  xmlns="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0 vocabulary-1.0.0.xsd">
            <name>Name</name>
            <type>simple</type>
            <description>Name</description>
            <validation>
                <validator type="text">
                    <property name="type"></property>
                    <property name="length"></property>
                    <property name="ui-vertical-lines">1</property>
                </validator>
            </validation>
        </term>
', null, null, 1, null, null, '2013-03-21 11:22:11.802', '2013-03-21 11:22:11.802', 1, '7193dbbd-2b79-4d82-833d-36465e2bfb90', 1, false);
INSERT INTO "public"."term"("asset_type_id", "uuid", "version_number", "name", "description", "key", "content", "image_id", "owner_id", "status_id", "creator_id", "updater_id", "time_created", "time_updated", "tenant_id", "vocabulary_uuid", "vocabulary_id", "is_template") VALUES(null, '516186c0-cdef-11e2-8b8b-0800200c9a66', '58d7ed40-cdef-11e2-8b8b-0800200c9a66', 'Boolean', 'A Boolean', null,
'<?xml version="1.0" encoding="UTF-8"?>
<term uuid="516186c0-cdef-11e2-8b8b-0800200c9a66" version="58d7ed40-cdef-11e2-8b8b-0800200c9a66"
      xmlns="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0 vocabulary-1.0.0.xsd">

    <name>Boolean</name>
    <description>A Boolean</description>
    <validation>
        <validator type="boolean">
        </validator>
    </validation>
</term>
', null, null, 1, null, null, '2013-03-21 11:22:11.802', '2013-03-21 11:22:11.802', 1, '51c5ddd0-09a2-11e2-892e-0800200c9a66', 1, false);

INSERT INTO "public"."term"("asset_type_id", "uuid", "version_number", "name", "description", "key", "content", "image_id", "owner_id", "status_id", "creator_id", "updater_id", "time_created", "time_updated", "tenant_id", "vocabulary_uuid", "vocabulary_id", "is_template") VALUES(null, '885ab520-cdef-11e2-8b8b-0800200c9a66', '8ff9d950-cdef-11e2-8b8b-0800200c9a66', 'Number', 'A Number', null,
'<?xml version="1.0" encoding="UTF-8"?>
<term uuid="885ab520-cdef-11e2-8b8b-0800200c9a66" version="8ff9d950-cdef-11e2-8b8b-0800200c9a66"
      xmlns="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0 vocabulary-1.0.0.xsd">

    <name>Number</name>
    <description>A Number</description>
    <validation>
        <validator type="numeric">
        </validator>
    </validation>
</term>
', null, null, 1, null, null, '2013-03-21 11:22:11.802', '2013-03-21 11:22:11.802', 1, '51c5ddd0-09a2-11e2-892e-0800200c9a66', 1, false);

INSERT INTO "public"."term"("asset_type_id", "uuid", "version_number", "name", "description", "key", "content", "image_id", "owner_id", "status_id", "creator_id", "updater_id", "time_created", "time_updated", "tenant_id", "vocabulary_uuid", "vocabulary_id", "is_template") VALUES(null, 'c3f83e40-cdef-11e2-8b8b-0800200c9a66', 'cb5a3260-cdef-11e2-8b8b-0800200c9a66', 'String', 'A String', null,
'<?xml version="1.0" encoding="UTF-8"?>
<term uuid="c3f83e40-cdef-11e2-8b8b-0800200c9a66" version="cb5a3260-cdef-11e2-8b8b-0800200c9a66"
      xmlns="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0 vocabulary-1.0.0.xsd">

    <name>String</name>
    <description>A String</description>
    <validation>
        <validator type="text">
        </validator>
    </validation>
</term>
', null, null, 1, null, null, '2013-03-21 11:22:11.802', '2013-03-21 11:22:11.802', 1, '51c5ddd0-09a2-11e2-892e-0800200c9a66', 1, false);

INSERT INTO "public"."term"("asset_type_id", "uuid", "version_number", "name", "description", "key", "content", "image_id", "owner_id", "status_id", "creator_id", "updater_id", "time_created", "time_updated", "tenant_id", "vocabulary_uuid", "vocabulary_id", "is_template") VALUES(null, 'ef4737c0-ce00-11e2-8b8b-0800200c9a66', 'f9294710-ce00-11e2-8b8b-0800200c9a66', 'Date', 'A Date', null,
'<?xml version="1.0" encoding="UTF-8"?>
<term uuid="ef4737c0-ce00-11e2-8b8b-0800200c9a66" version="f9294710-ce00-11e2-8b8b-0800200c9a66"
      xmlns="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0 vocabulary-1.0.0.xsd">

    <name>Date</name>
    <description>A Date</description>
    <validation>
        <validator type="date">
        </validator>
    </validation>
</term>
', null, null, 1, null, null, '2013-03-21 11:22:11.802', '2013-03-21 11:22:11.802', 1, '51c5ddd0-09a2-11e2-892e-0800200c9a66', 1, false);

INSERT INTO "public"."term"("asset_type_id", "uuid", "version_number", "name", "description", "key", "content", "image_id", "owner_id", "status_id", "creator_id", "updater_id", "time_created", "time_updated", "tenant_id", "vocabulary_uuid", "vocabulary_id", "is_template") VALUES(null, '01059b50-ce01-11e2-8b8b-0800200c9a66', '081e0350-ce01-11e2-8b8b-0800200c9a66', 'Time', 'A Time', null,
'<?xml version="1.0" encoding="UTF-8"?>
<term uuid="01059b50-ce01-11e2-8b8b-0800200c9a66" version="081e0350-ce01-11e2-8b8b-0800200c9a66"
      xmlns="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0 vocabulary-1.0.0.xsd">

    <name>Time</name>
    <description>A Time</description>
    <validation>
        <validator type="time">
        </validator>
    </validation>
</term>
', null, null, 1, null, null, '2013-03-21 11:22:11.802', '2013-03-21 11:22:11.802', 1, '51c5ddd0-09a2-11e2-892e-0800200c9a66', 1, false);

INSERT INTO "public"."term"("asset_type_id", "uuid", "version_number", "name", "description", "key", "content", "image_id", "owner_id", "status_id", "creator_id", "updater_id", "time_created", "time_updated", "tenant_id", "vocabulary_uuid", "vocabulary_id", "is_template") VALUES(null, '0ecfba40-ce01-11e2-8b8b-0800200c9a66', '1a9b4a10-ce01-11e2-8b8b-0800200c9a66', 'DateTime', 'A DateTime', null,
'<?xml version="1.0" encoding="UTF-8"?>
<term uuid="0ecfba40-ce01-11e2-8b8b-0800200c9a66" version="1a9b4a10-ce01-11e2-8b8b-0800200c9a66"
      xmlns="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0 vocabulary-1.0.0.xsd">

    <name>DateTime</name>
    <description>A DateTime</description>
    <validation>
        <validator type="date-time">
        </validator>
    </validation>
</term>
', null, null, 1, null, null, '2013-03-21 11:22:11.802', '2013-03-21 11:22:11.802', 1, '51c5ddd0-09a2-11e2-892e-0800200c9a66', 1, false);

INSERT INTO "public"."term"("asset_type_id", "uuid", "version_number", "name", "description", "key", "content", "image_id", "owner_id", "status_id", "creator_id", "updater_id", "time_created", "time_updated", "tenant_id", "vocabulary_uuid", "vocabulary_id", "is_template") VALUES(null, 'f3b15fc0-cdec-11e2-8b8b-0800200c9a66', '0c07fed0-cded-11e2-8b8b-0800200c9a66', 'File', 'A File', null,
'<?xml version="1.0" encoding="UTF-8"?>
<term uuid="f3b15fc0-cdec-11e2-8b8b-0800200c9a66" version="0c07fed0-cded-11e2-8b8b-0800200c9a66"
      xmlns="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0 vocabulary-1.0.0.xsd">

    <name>File</name>
    <description>A File</description>
    <validation>
        <validator type="file">
        </validator>
    </validation>
</term>
', null, null, 1, null, null, '2013-03-21 11:22:11.802', '2013-03-21 11:22:11.802', 1, '51c5ddd0-09a2-11e2-892e-0800200c9a66', 1, false);

INSERT INTO "public"."term"("asset_type_id", "uuid", "version_number", "name", "description", "key", "content", "image_id", "owner_id", "status_id", "creator_id", "updater_id", "time_created", "time_updated", "tenant_id", "vocabulary_uuid", "vocabulary_id", "is_template") VALUES(null, '137ac8a0-cfaa-11e2-8b8b-0800200c9a66', '1b1fe040-cfaa-11e2-8b8b-0800200c9a66', 'Fruits', 'A Fruit', null,
'<?xml version="1.0" encoding="UTF-8"?>
<term uuid="137ac8a0-cfaa-11e2-8b8b-0800200c9a66" version="1b1fe040-cfaa-11e2-8b8b-0800200c9a66"
      xmlns="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0 vocabulary-1.0.0.xsd">

    <name>Fruits</name>
    <description>A List</description>
    <validation>
        <validator type="list">
            <property name="item">Apple</property>
            <property name="item">Banana</property>
            <property name="item">Cantaloupe</property>
            <property name="item">Grape</property>
            <property name="item">Honeydew</property>
            <property name="item">Orange</property>
            <property name="item">Pear</property>
            <property name="item">Watermelon</property>
            <property name="isMultiSelect">false</property>
        </validator>
    </validation>
</term>
', null, null, 1, null, null, '2013-03-21 11:22:11.802', '2013-03-21 11:22:11.802', 1, '51c5ddd0-09a2-11e2-892e-0800200c9a66', 1, false);

INSERT INTO "public"."term"("asset_type_id", "uuid", "version_number", "name", "description", "key", "content", "image_id", "owner_id", "status_id", "creator_id", "updater_id", "time_created", "time_updated", "tenant_id", "vocabulary_uuid", "vocabulary_id", "is_template") VALUES(null, 'ef8955b0-723e-11e2-bcfd-0800200c9a66', '5b879f30-c8a5-11e2-8b8b-0800200c9a66', 'hplc_id', 'HPLC ID', null,
'<?xml version="1.0" encoding="UTF-8"?>
<term uuid="ef8955b0-723e-11e2-bcfd-0800200c9a66" version="5b879f30-c8a5-11e2-8b8b-0800200c9a66"
      xmlns="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0 vocabulary-1.0.0.xsd">

    <name>hplc_id</name>
    <description>HPLC ID</description>
    <validation>
        <validator type="text" >
        </validator>
    </validation>
</term>
', null, null, 1, null, null, '2013-03-21 11:22:11.802', '2013-03-21 11:22:11.802', 1, '51c5ddd0-09a2-11e2-892e-0800200c9a66', 1, false);

INSERT INTO "public"."term"("asset_type_id", "uuid", "version_number", "name", "description", "key", "content", "image_id", "owner_id", "status_id", "creator_id", "updater_id", "time_created", "time_updated", "tenant_id", "vocabulary_uuid", "vocabulary_id", "is_template") VALUES(null, 'f3585f10-723e-11e2-bcfd-0800200c9a66', '635d63c0-c8a5-11e2-8b8b-0800200c9a66', 'hplc_name', 'HPLC Name', null,
'<?xml version="1.0" encoding="UTF-8"?>
<term uuid="f3585f10-723e-11e2-bcfd-0800200c9a66" version="635d63c0-c8a5-11e2-8b8b-0800200c9a66"
      xmlns="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0 vocabulary-1.0.0.xsd">

    <name>hplc_name</name>
    <description>HPLC Name</description>
    <validation>
        <validator type="text" >
        </validator>
    </validation>
</term>
', null, null, 1, null, null, '2013-03-21 11:22:11.802', '2013-03-21 11:22:11.802', 1, '51c5ddd0-09a2-11e2-892e-0800200c9a66', 1, false);

INSERT INTO "public"."term"("asset_type_id", "uuid", "version_number", "name", "description", "key", "content", "image_id", "owner_id", "status_id", "creator_id", "updater_id", "time_created", "time_updated", "tenant_id", "vocabulary_uuid", "vocabulary_id", "is_template") VALUES(null, 'f8171950-6f01-11e2-bcfd-0800200c9a66', 'b9f805c0-c8a3-11e2-8b8b-0800200c9a66', 'hplc_owner', 'HPLC Owner', null,
'<?xml version="1.0" encoding="UTF-8"?>
<term uuid="f8171950-6f01-11e2-bcfd-0800200c9a66" version="b9f805c0-c8a3-11e2-8b8b-0800200c9a66"
      xmlns="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0 vocabulary-1.0.0.xsd">

    <name>hplc_owner</name>
    <description>HPLC Owner</description>
    <validation>
        <validator type="text" >
        </validator>
    </validation>
</term>
', null, null, 1, null, null, '2013-03-21 11:22:11.802', '2013-03-21 11:22:11.802', 1, '51c5ddd0-09a2-11e2-892e-0800200c9a66', 1, false);

INSERT INTO "public"."term"("asset_type_id", "uuid", "version_number", "name", "description", "key", "content", "image_id", "owner_id", "status_id", "creator_id", "updater_id", "time_created", "time_updated", "tenant_id", "vocabulary_uuid", "vocabulary_id", "is_template") VALUES(null, 'ff9c73f0-6f01-11e2-bcfd-0800200c9a66', 'c29e39b0-c8a3-11e2-8b8b-0800200c9a66', 'make', 'Make', null,
'<?xml version="1.0" encoding="UTF-8"?>
<term uuid="ff9c73f0-6f01-11e2-bcfd-0800200c9a66" version="c29e39b0-c8a3-11e2-8b8b-0800200c9a66"
      xmlns="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0 vocabulary-1.0.0.xsd">

    <name>make</name>
    <description>Make</description>
    <validation>
        <validator type="text">
        </validator>
    </validation>
</term>
', null, null, 1, null, null, '2013-03-21 11:22:11.802', '2013-03-21 11:22:11.802', 1, '51c5ddd0-09a2-11e2-892e-0800200c9a66', 1, false);

INSERT INTO "public"."term"("asset_type_id", "uuid", "version_number", "name", "description", "key", "content", "image_id", "owner_id", "status_id", "creator_id", "updater_id", "time_created", "time_updated", "tenant_id", "vocabulary_uuid", "vocabulary_id", "is_template") VALUES(null, '614c46b0-6f03-11e2-bcfd-0800200c9a66', 'cb1917e0-c8a3-11e2-8b8b-0800200c9a66', 'hplc_desc', 'HPLC Desc', null,
'<?xml version="1.0" encoding="UTF-8"?>
<term uuid="614c46b0-6f03-11e2-bcfd-0800200c9a66" version="cb1917e0-c8a3-11e2-8b8b-0800200c9a66"
      xmlns="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0 vocabulary-1.0.0.xsd">

    <name>hplc_desc</name>
    <description>Description </description>
    <validation>
        <validator type="text">
        </validator>
    </validation>
</term>
', null, null, 1, null, null, '2013-03-21 11:22:11.802', '2013-03-21 11:22:11.802', 1, '51c5ddd0-09a2-11e2-892e-0800200c9a66', 1, false);

INSERT INTO "public"."term"("asset_type_id", "uuid", "version_number", "name", "description", "key", "content", "image_id", "owner_id", "status_id", "creator_id", "updater_id", "time_created", "time_updated", "tenant_id", "vocabulary_uuid", "vocabulary_id", "is_template") VALUES(null, 'ea68fbf0-6f03-11e2-bcfd-0800200c9a66', 'd52173e0-c8a3-11e2-8b8b-0800200c9a66', 'model', 'Model', null,
'<?xml version="1.0" encoding="UTF-8"?>
<term uuid="ea68fbf0-6f03-11e2-bcfd-0800200c9a66" version="d52173e0-c8a3-11e2-8b8b-0800200c9a66"
      xmlns="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0 vocabulary-1.0.0.xsd">

    <name>model</name>
    <description>Model</description>
    <validation>
        <validator type="text">
        </validator>
    </validation>
</term>
', null, null, 1, null, null, '2013-03-21 11:22:11.802', '2013-03-21 11:22:11.802', 1, '51c5ddd0-09a2-11e2-892e-0800200c9a66', 1, false);

INSERT INTO "public"."term"("asset_type_id", "uuid", "version_number", "name", "description", "key", "content", "image_id", "owner_id", "status_id", "creator_id", "updater_id", "time_created", "time_updated", "tenant_id", "vocabulary_uuid", "vocabulary_id", "is_template") VALUES(null, 'ae0aa4a2-8d87-11e1-b0c4-0800200c9a66', 'dfd0e550-c8a3-11e2-8b8b-0800200c9a66', 'serial_number', 'Serial Number', null,
'<?xml version="1.0" encoding="UTF-8"?>
<term uuid="ae0aa4a2-8d87-11e1-b0c4-0800200c9a66" version="dfd0e550-c8a3-11e2-8b8b-0800200c9a66"
      xmlns="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0 vocabulary-1.0.0.xsd">

    <name>serial_number</name>
    <description>Serial Number</description>
    <validation>
        <validator type="text">
        </validator>
    </validation>
</term>
', null, null, 1, null, null, '2013-03-21 11:22:11.802', '2013-03-21 11:22:11.802', 1, '51c5ddd0-09a2-11e2-892e-0800200c9a66', 1, false);

INSERT INTO "public"."term"("asset_type_id", "uuid", "version_number", "name", "description", "key", "content", "image_id", "owner_id", "status_id", "creator_id", "updater_id", "time_created", "time_updated", "tenant_id", "vocabulary_uuid", "vocabulary_id", "is_template") VALUES(null, '1dbd2910-6f02-11e2-bcfd-0800200c9a66', 'e8fb3d60-c8a3-11e2-8b8b-0800200c9a66', 'Diameter', 'Seperation column diameter', null,
'<?xml version="1.0" encoding="UTF-8"?>
<term uuid="1dbd2910-6f02-11e2-bcfd-0800200c9a66" version="e8fb3d60-c8a3-11e2-8b8b-0800200c9a66"
      xmlns="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0 vocabulary-1.0.0.xsd">

    <name>Diameter</name>
    <description>Seperation column diameter</description>
    <validation>
        <validator type="numeric">
            <property name="range">[0, +infinity]</property>
            <property name="minPrecision">2</property>
            <property name="maxPrecision">2</property>
        </validator>
    </validation>
</term>
', null, null, 1, null, null, '2013-03-21 11:22:11.802', '2013-03-21 11:22:11.802', 1, '51c5ddd0-09a2-11e2-892e-0800200c9a66', 1, false);

INSERT INTO "public"."term"("asset_type_id", "uuid", "version_number", "name", "description", "key", "content", "image_id", "owner_id", "status_id", "creator_id", "updater_id", "time_created", "time_updated", "tenant_id", "vocabulary_uuid", "vocabulary_id", "is_template") VALUES(null, '25398300-6f02-11e2-bcfd-0800200c9a66', 'f1cb8e90-c8a3-11e2-8b8b-0800200c9a66', 'length', 'Seperation column length', null,
'<?xml version="1.0" encoding="UTF-8"?>
<term uuid="25398300-6f02-11e2-bcfd-0800200c9a66" version="f1cb8e90-c8a3-11e2-8b8b-0800200c9a66"
      xmlns="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0 vocabulary-1.0.0.xsd">

    <name>length</name>
    <description>Seperation column length</description>
    <validation>
        <validator type="numeric">
            <property name="range">[0, +infinity]</property>
            <property name="minPrecision">2</property>
            <property name="maxPrecision">2</property>
        </validator>
    </validation>
</term>
', null, null, 1, null, null, '2013-03-21 11:22:11.802', '2013-03-21 11:22:11.802', 1, '51c5ddd0-09a2-11e2-892e-0800200c9a66', 1, false);

INSERT INTO "public"."term"("asset_type_id", "uuid", "version_number", "name", "description", "key", "content", "image_id", "owner_id", "status_id", "creator_id", "updater_id", "time_created", "time_updated", "tenant_id", "vocabulary_uuid", "vocabulary_id", "is_template") VALUES(null, '2bd5b620-6f02-11e2-bcfd-0800200c9a66', 'fbb517f0-c8a3-11e2-8b8b-0800200c9a66', 'seperation_column_enabled', 'Whether the separation column is available or not', null,
'<?xml version="1.0" encoding="UTF-8"?>
<term uuid="2bd5b620-6f02-11e2-bcfd-0800200c9a66" version="fbb517f0-c8a3-11e2-8b8b-0800200c9a66"
      xmlns="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0 vocabulary-1.0.0.xsd">

    <name>seperation_column_enabled</name>
    <description>Whether the separation column is available or not</description>
    <validation>
        <validator type="boolean">
        </validator>
    </validation>
</term>
', null, null, 1, null, null, '2013-03-21 11:22:11.802', '2013-03-21 11:22:11.802', 1, '51c5ddd0-09a2-11e2-892e-0800200c9a66', 1, false);

--------------------------------------------------------
-- Templates
--------------------------------------------------------
INSERT INTO "public"."term"("asset_type_id", "uuid", "version_number", "name", "description", "key", "content", "image_id", "owner_id", "status_id", "creator_id", "updater_id", "time_created", "time_updated", "tenant_id", "vocabulary_uuid", "vocabulary_id", "is_template") VALUES(null, '85833b40-73d3-11e2-bcfd-0800200c9a66', '167822c0-c85a-11e2-8b8b-0800200c9a66', 'HPLC Instrument Collection', 'HPLC Instrument Collection', null,
'<?xml version="1.0" encoding="UTF-8"?>
<term uuid="85833b40-73d3-11e2-bcfd-0800200c9a66" version="167822c0-c85a-11e2-8b8b-0800200c9a66"
      xmlns="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0 vocabulary-1.0.0.xsd">

    <name>HPLC Instrument Collection</name>
    <description>HPLC Instrument Collection</description>

    <term uuid="ef8955b0-723e-11e2-bcfd-0800200c9a66" version="5b879f30-c8a5-11e2-8b8b-0800200c9a66" alias="hplc_id"/>
    <term uuid="f3585f10-723e-11e2-bcfd-0800200c9a66" version="635d63c0-c8a5-11e2-8b8b-0800200c9a66" alias="hplc_name"/>
    <term uuid="f8171950-6f01-11e2-bcfd-0800200c9a66" version="b9f805c0-c8a3-11e2-8b8b-0800200c9a66" alias="hplc_owner"/>
    <term uuid="ff9c73f0-6f01-11e2-bcfd-0800200c9a66" version="c29e39b0-c8a3-11e2-8b8b-0800200c9a66" alias="hplc_make"/>
    <term uuid="ea68fbf0-6f03-11e2-bcfd-0800200c9a66" version="d52173e0-c8a3-11e2-8b8b-0800200c9a66" alias="hplc_model"/>
    <term uuid="ae0aa4a2-8d87-11e1-b0c4-0800200c9a66" version="dfd0e550-c8a3-11e2-8b8b-0800200c9a66" alias="hplc_serialnum"/>
    <term uuid="614c46b0-6f03-11e2-bcfd-0800200c9a66" version="cb1917e0-c8a3-11e2-8b8b-0800200c9a66" alias="hplc_description"/>
</term>
', null, null, 1, null, null, '2013-03-21 11:22:11.802', '2013-03-21 11:22:11.802', 1, null, null, true);

INSERT INTO "public"."term"("asset_type_id", "uuid", "version_number", "name", "description", "key", "content", "image_id", "owner_id", "status_id", "creator_id", "updater_id", "time_created", "time_updated", "tenant_id", "vocabulary_uuid", "vocabulary_id", "is_template") VALUES(null, '85833b40-73d3-11e2-bcfd-0800200c9a66', 'bf7b1cd0-cfab-11e2-8b8b-0800200c9a66', 'HPLC Instrument Collection 2', 'HPLC Instrument Collection 2', null,
'<?xml version="1.0" encoding="UTF-8"?>
<!--  the following UUID should be in HTML as well -->
<term uuid="85833b40-73d3-11e2-bcfd-0800200c9a66" version="bf7b1cd0-cfab-11e2-8b8b-0800200c9a66"
      xmlns="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0 vocabulary-1.0.0.xsd">

    <name>HPLC Instrument Collection V2</name>
    <description>HPLC Instrument Collection V2</description>

    <!--  the following UUId should be in Vocabulary as well -->
    <term uuid="ef8955b0-723e-11e2-bcfd-0800200c9a66" version="5b879f30-c8a5-11e2-8b8b-0800200c9a66" alias="hplc_id"/>
    <term uuid="f3585f10-723e-11e2-bcfd-0800200c9a66" version="635d63c0-c8a5-11e2-8b8b-0800200c9a66" alias="hplc_name"/>
    <term uuid="f8171950-6f01-11e2-bcfd-0800200c9a66" version="b9f805c0-c8a3-11e2-8b8b-0800200c9a66" alias="hplc_owner"/>
    <term uuid="ff9c73f0-6f01-11e2-bcfd-0800200c9a66" version="c29e39b0-c8a3-11e2-8b8b-0800200c9a66" alias="hplc_make"/>
    <term uuid="ea68fbf0-6f03-11e2-bcfd-0800200c9a66" version="d52173e0-c8a3-11e2-8b8b-0800200c9a66" alias="hplc_model"/>
    <term uuid="ae0aa4a2-8d87-11e1-b0c4-0800200c9a66" version="dfd0e550-c8a3-11e2-8b8b-0800200c9a66" alias="hplc_serialnum"/>
    <term uuid="614c46b0-6f03-11e2-bcfd-0800200c9a66" version="cb1917e0-c8a3-11e2-8b8b-0800200c9a66" alias="hplc_description"/>

    <!-- extra fields -->
    <term uuid="516186c0-cdef-11e2-8b8b-0800200c9a66" version="58d7ed40-cdef-11e2-8b8b-0800200c9a66" alias="a_boolean"/>
    <term uuid="885ab520-cdef-11e2-8b8b-0800200c9a66" version="8ff9d950-cdef-11e2-8b8b-0800200c9a66" alias="a_number"/>
    <term uuid="ef4737c0-ce00-11e2-8b8b-0800200c9a66" version="f9294710-ce00-11e2-8b8b-0800200c9a66" alias="a_date"/>
    <term uuid="01059b50-ce01-11e2-8b8b-0800200c9a66" version="081e0350-ce01-11e2-8b8b-0800200c9a66" alias="a_time"/>
    <term uuid="0ecfba40-ce01-11e2-8b8b-0800200c9a66" version="1a9b4a10-ce01-11e2-8b8b-0800200c9a66" alias="a_date_time"/>
    <term uuid="137ac8a0-cfaa-11e2-8b8b-0800200c9a66" version="1b1fe040-cfaa-11e2-8b8b-0800200c9a66" alias="a_list"/>
</term>
', null, null, 1, null, null, '2013-03-22 11:22:11.802', '2013-03-22 11:22:11.802', 1, null, null, true);

INSERT INTO "public"."term"("asset_type_id", "uuid", "version_number", "name", "description", "key", "content", "image_id", "owner_id", "status_id", "creator_id", "updater_id", "time_created", "time_updated", "tenant_id", "vocabulary_uuid", "vocabulary_id", "is_template") VALUES(null, '305b0f27-e829-424e-84eb-7a8a9ed93e28', 'db719406-f665-45cb-a8fb-985b6082b654', 'GLB', 'Globus Test', 'GLB',
'<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<term xmlns="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" uuid="305b0f27-e829-424e-84eb-7a8a9ed93e28" version="db719406-f665-45cb-a8fb-985b6082b654" xsi:schemaLocation="http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0 vocabulary-1.0.0.xsd">
    <name><![CDATA[GLB]]></name>
    <description><![CDATA[GLB]]></description>
    <term alias="Name" uuid="8b59449a-6103-4e04-87a0-6657e17015b1" version="476627c7-5e25-40eb-aaac-c8a30364bb20" ui-display-order="0"/>
    <term alias="browsefile1" uuid="c5fbc688-bc61-4fe9-8e25-268f25b95bae" version="fb67901e-3aa2-4005-82c6-d359e535da97" read-only="false" ui-display-order="1">
        <description><![CDATA[browsefile1]]></description>
    </term>
    <term alias="browsefile2" uuid="c5fbc688-bc61-4fe9-8e25-268f25b95bae" version="fb67901e-3aa2-4005-82c6-d359e535da97" read-only="false" ui-display-order="2">
        <description><![CDATA[browsefile2]]></description>
        <validation>
            <validator type="file">
                <property name="multiple"><![CDATA[true]]></property>
                <property name="globus"><![CDATA[true]]></property>
            </validator>
        </validation>
    </term>
</term>
', null, null, 1, null, null, '2015-03-10 10:56:11.802', '2015-03-10 10:56:11.802', 1, null, null, true);

DELETE FROM "public"."tool";

DELETE FROM "public"."workflow";

DELETE FROM "public"."storage";
INSERT INTO "public"."storage"("id", "name", "description", "type", "location",  "tenant_id") VALUES(1, 'local storage', 'the one and only storage', 'file', '/data/cris/storage_test/', null);
ALTER SEQUENCE "public"."storage_id_seq" RESTART WITH 2;

DELETE FROM "public"."storage_access_method";
INSERT INTO "public"."storage_access_method"("id", "storage_id", "type", "name", "description", "uri", "root", "tenant_id") VALUES(1, 1, 'file', 'file', 'file access method', '/data/cris/storage_test/', '/', null);
INSERT INTO "public"."storage_access_method"("id", "storage_id", "type", "name", "description", "uri", "root", "tenant_id") VALUES(2, 1, 'globus', 'globus', 'globus access method', 'https://transfer.api.globusonline.org/v0.10', 'pnbaker#crisnitin', null);
ALTER SEQUENCE "public"."storage_access_method_id_seq" RESTART WITH 3;

DELETE FROM "public"."storage_file";
INSERT INTO "public"."storage_file"("id", "asset_type_id", "storage_id", "location", "creator_id", "updater_id", "time_created", "time_updated", "tenant_id", "file_name") VALUES(1, 3, 1, 'f47ac10b-58cc-4372-a567-0e02b2c3d479/0000/0000/0000/0000/0001_barney1.txt', 1, 1, '2015-03-26 16:20:53.298', '2015-03-26 16:20:53.384', 1, 'barney1.txt');
ALTER SEQUENCE "public"."storage_file_id_seq" RESTART WITH 2;

DELETE FROM "public"."computational_node";

DELETE FROM "public"."role";

DELETE FROM "public"."role_operation";

DELETE FROM "public"."permission";
INSERT INTO "public"."permission"("group_id", "role_type_id", "tenant_id") VALUES(1000, 1, 1);

DELETE FROM "public"."job";
INSERT INTO "public"."job"("id", "parent_id", "group_id", "user_id", "resource_id", "workflow_id", "project_id", "experiment_id", "name", "description", "parameters", "status_id", "creator_id", "updater_id", "time_created", "time_updated", "tenant_id") VALUES (752, NULL, 1000, 1, NULL, NULL, 5001, 7001, 'test 1', '', '{"experimentId":"7001","workflowId":"983","name":"test 1","projectId":"5001"}', 4, 1, 1, '2013-03-20 13:08:49.983', '2013-03-20 13:09:13.78', 1);
INSERT INTO "public"."job"("id", "parent_id", "group_id", "user_id", "resource_id", "workflow_id", "project_id", "experiment_id", "name", "description", "parameters", "status_id", "creator_id", "updater_id", "time_created", "time_updated", "tenant_id") VALUES (753, NULL, 1000, 1, NULL, NULL, 5001, 7001, 'test 1', '', '{"experimentId":"7001","workflowId":"983","name":"test 1","projectId":"5001"}', 4, 1, 1, '2013-03-20 13:08:49.983', '2013-03-20 13:09:13.78', 1);
INSERT INTO "public"."job"("id", "parent_id", "group_id", "user_id", "resource_id", "workflow_id", "project_id", "experiment_id", "name", "description", "parameters", "status_id", "creator_id", "updater_id", "time_created", "time_updated", "tenant_id") VALUES (754, NULL, 1000, 1, NULL, NULL, 5002, 7003, 'test 1', '', '{"experimentId":"7003","workflowId":"983","name":"test 1","projectId":"5002"}', 4, 1, 1, '2013-03-20 13:08:49.983', '2013-03-20 13:09:13.78', 1);
ALTER SEQUENCE "public"."job_id_seq" RESTART WITH 755;

DELETE FROM "public"."job_context";
