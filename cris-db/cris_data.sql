DELETE FROM "public"."tenant";
INSERT INTO "public"."tenant"("id", "uuid", "url_identifier", "name", "enabled") VALUES(1, 'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'brouder_sylvie', 'Sylvie Brouder''s Workspace', true);
INSERT INTO "public"."tenant"("id", "uuid", "url_identifier", "name", "enabled") VALUES(2, 'f47ac10b-58cc-4372-a567-0e02b2c3d47a', 'baker_peter', 'Peter Baker''s Workspace', true);
INSERT INTO "public"."tenant"("id", "uuid", "url_identifier", "name", "enabled") VALUES(3, 'f47ac10b-58cc-4372-a567-0e02b2c3d47b', 'chapple_clint', 'Clint Chapple''s Workspace', true);
INSERT INTO "public"."tenant"("id", "uuid", "url_identifier", "name", "enabled") VALUES(4, 'f47ac10b-58cc-4372-a567-0e02b2c3d47c', 'hall_mark', 'Mark Hall''s Workspace', true);
INSERT INTO "public"."tenant"("id", "uuid", "url_identifier", "name", "enabled") VALUES(5, 'f47ac10b-58cc-4372-a567-0e02b2c3d47d', 'norris_meghan', 'Dr. Meghan Norris'' Workspace', true);
ALTER SEQUENCE "public"."tenant_id_seq" RESTART WITH 6;

DELETE FROM "public"."configuration";
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('externalSource', 'text', 'Purdue University', null);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('CopyrightYear', 'text', '2012', null);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('searchEngineUrl', 'text', 'http://localhost:9200/', null);
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

INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('externalSource', 'text', 'Purdue University', 2);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('CopyrightYear', 'text', '2012', 2);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsFavicon', 'text', 'static/images/favicon.ico', 2);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsBannerImage', 'text', 'static/images/header.jpg', 2);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsAuthBackgroundImage', 'text', '', 2);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsName', 'text', 'Pete''s Workspace', 2);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsDescription', 'text', 'The Purdue University Center for Cancer Research brings together the best minds from within Purdue University and beyond to study cancers where they start - inside the cell. Using the combined expertise of scientists from disciplines as varied as engineering and veterinary medicine, biology and chemistry, the Cancer Research Center discovers how cancers develop, progress and respond to treatment. Our work leads to the advancement of new medicines, diagnostic tools and treatment devices.  Our Center is the first NCI funded Cancer Center in Indiana.', 2);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsSigninInstruction', 'text', 'If you are a Purdue user, use your Purdue Career Account.', 2);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsSignupInstruction', 'text', 'If you have a Purdue Career Account, please use that instead of creating a new account.', 2);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsAuthProblem', 'text', 'Please provide the email that you used as username. Password reset instruction will be sent to this email', 2);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsAuthReset', 'text', 'Please copy/paste the token in the email that has been sent to your, then enter a new password', 2);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsEmailGeneral', 'text', 'cyber@purdue.edu', 2);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsEmailAccountProblem', 'text', 'cyber@purdue.edu', 2);

INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('externalSource', 'text', 'Purdue University', 3);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('CopyrightYear', 'text', '2012', 3);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsFavicon', 'text', 'static/images/favicon.ico', 3);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsBannerImage', 'text', 'static/images/header.jpg', 3);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsAuthBackgroundImage', 'text', '', 3);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsName', 'text', 'Clint''s Workspace', 3);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsDescription', 'text', 'The Purdue University Center for Cancer Research brings together the best minds from within Purdue University and beyond to study cancers where they start - inside the cell. Using the combined expertise of scientists from disciplines as varied as engineering and veterinary medicine, biology and chemistry, the Cancer Research Center discovers how cancers develop, progress and respond to treatment. Our work leads to the advancement of new medicines, diagnostic tools and treatment devices.  Our Center is the first NCI funded Cancer Center in Indiana.', 3);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsSigninInstruction', 'text', 'If you are a Purdue user, use your Purdue Career Account.', 3);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsSignupInstruction', 'text', 'If you have a Purdue Career Account, please use that instead of creating a new account.', 3);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsAuthProblem', 'text', 'Please provide the email that you used as username. Password reset instruction will be sent to this email', 3);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsAuthReset', 'text', 'Please copy/paste the token in the email that has been sent to your, then enter a new password', 3);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsEmailGeneral', 'text', 'cyber@purdue.edu', 3);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsEmailAccountProblem', 'text', 'cyber@purdue.edu', 3);

INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('externalSource', 'text', 'Purdue University', 4);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('CopyrightYear', 'text', '2012', 4);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsFavicon', 'text', 'static/images/favicon.ico', 4);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsBannerImage', 'text', 'static/images/header.jpg', 4);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsAuthBackgroundImage', 'text', '', 4);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsName', 'text', 'Mark''s Workspace', 4);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsDescription', 'text', 'The Purdue University Center for Cancer Research brings together the best minds from within Purdue University and beyond to study cancers where they start - inside the cell. Using the combined expertise of scientists from disciplines as varied as engineering and veterinary medicine, biology and chemistry, the Cancer Research Center discovers how cancers develop, progress and respond to treatment. Our work leads to the advancement of new medicines, diagnostic tools and treatment devices.  Our Center is the first NCI funded Cancer Center in Indiana.', 4);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsSigninInstruction', 'text', 'If you are a Purdue user, use your Purdue Career Account.', 4);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsSignupInstruction', 'text', 'If you have a Purdue Career Account, please use that instead of creating a new account.', 4);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsAuthProblem', 'text', 'Please provide the email that you used as username. Password reset instruction will be sent to this email', 4);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsAuthReset', 'text', 'Please copy/paste the token in the email that has been sent to your, then enter a new password', 4);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsEmailGeneral', 'text', 'cyber@purdue.edu', 4);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsEmailAccountProblem', 'text', 'cyber@purdue.edu', 4);

INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('externalSource', 'text', 'Purdue University', 5);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('CopyrightYear', 'text', '2012', 5);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsFavicon', 'text', 'static/images/favicon.ico', 5);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsBannerImage', 'text', 'static/images/header.jpg', 5);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsAuthBackgroundImage', 'text', '', 5);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsName', 'text', 'Dr. Meghan Norris'' Workspace', 5);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsDescription', 'text', '<p>My research falls into the categories of social and consumer psychology. Generally, I am interested in attitude structure, attitude strength, and consequences of attitude strength. I am especially interested in how, when, and why our attitudes cause us to attend to certain attitude-relevant features of our environment.</p>I also conduct research in social influence. I am especially interested in the norm of reciprocity and how it can elicit compliance with requests in consumer contexts.', 5);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsSigninInstruction', 'text', 'If you are a Purdue user, use your Purdue Career Account.', 5);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsSignupInstruction', 'text', 'If you have a Purdue Career Account, please use that instead of creating a new account.', 5);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsAuthProblem', 'text', 'Please provide the email that you used as username. Password reset instruction will be sent to this email', 5);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsAuthReset', 'text', 'Please copy/paste the token in the email that has been sent to your, then enter a new password', 5);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsEmailGeneral', 'text', 'cyber@purdue.edu', 5);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('wsEmailAccountProblem', 'text', 'cyber@purdue.edu', 5);

DELETE FROM "public"."small_object";
INSERT INTO "public"."small_object"("name", "content", "tenant_id") VALUES('icon', 'static/images/favicon.ico', 1);

DELETE FROM "public"."classification";
INSERT INTO "public"."classification"("id", "code", "name", "description", "tenant_id") VALUES(1, 1, 'Member', 'PUCCR Member', 1);
INSERT INTO "public"."classification"("id", "code", "name", "description", "tenant_id") VALUES(2, 2, 'Non-Member', 'PUCCR Non-Member', 1);

INSERT INTO "public"."classification"("id", "code", "name", "description", "tenant_id") VALUES(3, 1, 'Member', 'PUCCR Member', 2);
INSERT INTO "public"."classification"("id", "code", "name", "description", "tenant_id") VALUES(4, 2, 'Non-Member', 'PUCCR Non-Member', 2);

INSERT INTO "public"."classification"("id", "code", "name", "description", "tenant_id") VALUES(5, 1, 'Member', 'PUCCR Member', 3);
INSERT INTO "public"."classification"("id", "code", "name", "description", "tenant_id") VALUES(6, 2, 'Non-Member', 'PUCCR Non-Member', 3);

INSERT INTO "public"."classification"("id", "code", "name", "description", "tenant_id") VALUES(7, 1, 'Member', 'PUCCR Member', 4);
INSERT INTO "public"."classification"("id", "code", "name", "description", "tenant_id") VALUES(8, 2, 'Non-Member', 'PUCCR Non-Member', 4);
ALTER SEQUENCE "public"."classification_id_seq" RESTART WITH 9;

DELETE FROM "public"."session";

DELETE FROM "public"."user";
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (1, 'george.washington', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'george', '', 'washington', 'george.washington@whitehouse.gov', true, true, true, true, 1);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (2, 'john.adams', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'john', '', 'adams', 'john.adams@whitehouse.gov', true, true, true, true, 1);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (3, 'thomas.jefferson', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'thomas', '', 'jefferson', 'thomas.jefferson@whitehouse.gov', true, true, true, true, 1);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (4, 'james.madison', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'james', '', 'madison', 'james.madison@whitehouse.gov', true, true, true, true, 1);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (5, 'james.monroe', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'james', '', 'monroe', 'james.monroe@whitehouse.gov', true, true, true, true, 1);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (6, 'john.quincy.adams', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'john', 'quincy', 'adams', 'john.quincy.adams@whitehouse.gov', true, true, true, true, 1);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (7, 'andrew.jackson', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'andrew', '', 'jackson', 'andrew.jackson@whitehouse.gov', true, true, true, true, 1);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (8, 'martin.buren', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'martin', '', 'buren', 'martin.buren@whitehouse.gov', true, true, true, true, 1);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (9, 'william.harrison', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'william', '', 'harrison', 'william.harrison@whitehouse.gov', true, true, true, true, 1);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (10, 'john.tyler', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'john', '', 'tyler', 'john.tyler@whitehouse.gov', true, true, true, true, 1);

INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (11, 'james.polk', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'james', '', 'polk', 'james.polk@whitehouse.gov', true, true, true, true, 2);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (12, 'zachary.taylor', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'zachary', '', 'taylor', 'zachary.taylor@whitehouse.gov', true, true, true, true, 2);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (13, 'millard.fillmore', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'millard', '', 'fillmore', 'millard.fillmore@whitehouse.gov', true, true, true, true, 2);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (14, 'franklin.pierce', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'franklin', '', 'pierce', 'franklin.pierce@whitehouse.gov', true, true, true, true, 2);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (15, 'james.buchanan', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'james', '', 'buchanan', 'james.buchanan@whitehouse.gov', true, true, true, true, 2);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (16, 'abraham.lincoln', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'abraham', '', 'lincoln', 'abraham.lincoln@whitehouse.gov', true, true, true, true, 2);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (17, 'andrew.johnson', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'andrew', '', 'johnson', 'andrew.johnson@whitehouse.gov', true, true, true, true, 2);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (18, 'ulisses.grant', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'ulisses', '', 'grant', 'ulisses.grant@whitehouse.gov', true, true, true, true, 2);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (19, 'rutherford.hayes', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'rutherford', '', 'hayes', 'rutherford.hayes@whitehouse.gov', true, true, true, true, 2);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (20, 'james.garfield', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'james', '', 'garfield', 'james.garfield@whitehouse.gov', true, true, true, true, 2);

INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (21, 'chester.arthur', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'chester', '', 'arthur', 'chester.arthur@whitehouse.gov', true, true, true, true, 3);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (22, 'grover.a.cleveland', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'grover', '', 'cleveland', 'grover.a.doe@whitehouse.gov', true, true, true, true, 3);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (23, 'benjamin.harrison', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'benjamin', '', 'harrison', 'benjamin.harrison@whitehouse.gov', true, true, true, true, 3);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (24, 'grover.b.cleveland', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'grover', '', 'cleveland', 'grover.b.doe@whitehouse.gov', true, true, true, true, 3);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (25, 'william.mckinley', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'william', '', 'mckinley', 'william.mckinley@whitehouse.gov', true, true, true, true, 3);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (26, 'theodore.roosevelt', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'theodore', '', 'roosevelt', 'theodore.roosevelt@whitehouse.gov', true, true, true, true, 3);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (27, 'william.taft', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'william', '', 'taft', 'william.taft@whitehouse.gov', true, true, true, true, 3);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (28, 'woodrow.wilson', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'woodrow', '', 'wilson', 'woodrow.wilson@whitehouse.gov', true, true, true, true, 3);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (29, 'warren.harding', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'warren', '', 'harding', 'warren.harding@whitehouse.gov', true, true, true, true, 3);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (30, 'calvin.coolidge', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'calvin', '', 'coolidge', 'calvin.coolidge@whitehouse.gov', true, true, true, true, 3);

INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (31, 'herbert.hoover', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'herbert', '', 'hoover', 'herbert.hoover@whitehouse.gov', true, true, true, true, 4);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (32, 'franklin.roosevelt', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'franklin', '', 'roosevelt', 'franklin.roosevelt@whitehouse.gov', true, true, true, true, 4);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (33, 'harry.truman', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'harry', '', 'truman', 'harry.truman@whitehouse.gov', true, true, true, true, 4);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (34, 'dwight.eisenhower', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'dwight', '', 'eisenhower', 'dwight.eisenhower@whitehouse.gov', true, true, true, true, 4);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (35, 'john.kennedy', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'john', '', 'kennedy', 'john.kennedy@whitehouse.gov', true, true, true, true, 4);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (36, 'lyndon.johnson', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'lyndon', '', 'johnson', 'lyndon.johnson@whitehouse.gov', true, true, true, true, 4);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (37, 'richard.nixon', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'richard', '', 'nixon', 'richard.nixon@whitehouse.gov', true, true, true, true, 4);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (38, 'gerald.ford', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'gerald', '', 'ford', 'gerald.ford@whitehouse.gov', true, true, true, true, 4);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (39, 'jimmy.carter', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'jimmy', '', 'carter', 'jimmy.carter@whitehouse.gov', true, true, true, true, 4);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (40, 'ronald.reagan', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'ronald', '', 'reagan', 'ronald.reagan@whitehouse.gov', true, true, true, true, 4);

INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (41, 'george.h.bush', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'george', '', 'bush', 'george.h.bush@whitehouse.gov', true, true, true, true, 1);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (42, 'bill.clinton', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'bill', '', 'clinton', 'bill.clinton@whitehouse.gov', true, true, true, true, 1);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (43, 'george.w.bush', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'george', 'w', 'bush', 'george.w.bush@whitehouse.gov', true, true, true, true, 1);
INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (44, 'barack.obama', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'barack', '', 'obama', 'barack.obama@whitehouse.gov', true, true, true, true, 1);
INSERT INTO "public"."user"("id", "external_source", "external_id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (45, 'Purdue University', '0010308390', 'david.nichols', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'David', 'E', 'Nichols', 'david.nichols@purdue.edu', true, true, true, true, 1);
INSERT INTO "public"."user"("id", "external_source", "external_id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (46, 'Purdue University', '0010343470', 'mark.cushman', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'Mark', 'S', 'Cushman', 'mark.cushman@purdue.edu', true, true, true, true, 1);
INSERT INTO "public"."user"("id", "external_source", "external_id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (47, 'Purdue University', '0010045113', 'val.watts', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'Val', 'J', 'Watts', 'val.watts@purdue.edu', true, true, true, true, 1);
INSERT INTO "public"."user"("id", "external_source", "external_id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (48, 'Purdue University', '0014470658', 'shuang.liu', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'Shuang', '', 'Liu', 'shuang.liu@purdue.edu', true, true, true, true, 1);
INSERT INTO "public"."user"("id", "external_source", "external_id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (49, 'Purdue University', '0015091205', 'rusi.taleyarkhan', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'Rusi', 'P', 'Taleyarkhan', 'rusi.taleyarkhan@purdue.edu', true, true, true, true, 1);

INSERT INTO "public"."user"("id", "username", "password", "salt", "first_name", "middle_name", "last_name", "email", "account_non_expired", "account_non_locked", "credentials_non_expired", "enabled", "tenant_id") VALUES (50, 'george.washington', '2d81284cbaf25228c4ad58083a240afe26245eccccd4d97d763cdbe95e0712cb', '93dba907fe231a96cb0b6c8f347e78bd2f7c65119d5179bee49479be7688e9da', 'george', '', 'washington', 'george.washington@whitehouse.gov', true, true, true, true, 5);
ALTER SEQUENCE "public"."user_id_seq" RESTART WITH 51;

DELETE FROM "public"."group";
INSERT INTO "public"."group"("id", "name", "description", "owner_id", "classification_id", "tenant_id") VALUES (1000, 'Admin Group', '', 1, 1, 1);
INSERT INTO "public"."group"("id", "name", "description", "owner_id", "classification_id", "tenant_id") VALUES (1001, 'Mass Spectrometry Technician Group ', '', 3, 1, 1);
INSERT INTO "public"."group"("id", "name", "description", "owner_id", "classification_id", "tenant_id") VALUES (1002, 'Business Group', '', 5, 1, 1);
INSERT INTO "public"."group"("id", "name", "description", "owner_id", "classification_id", "tenant_id") VALUES (1003, 'PI: Mark S Cushman', '', 7, 1, 1);
INSERT INTO "public"."group"("id", "name", "description", "owner_id", "classification_id", "tenant_id") VALUES (1004, 'Acme', '', 9, 2, 1);

INSERT INTO "public"."group"("id", "name", "description", "owner_id", "classification_id", "tenant_id") VALUES (2000, 'Admin Group', '', 11, 1, 2);
INSERT INTO "public"."group"("id", "name", "description", "owner_id", "classification_id", "tenant_id") VALUES (2001, 'Mass Spectrometry Technician Group ', '', 13, 1, 2);
INSERT INTO "public"."group"("id", "name", "description", "owner_id", "classification_id", "tenant_id") VALUES (2002, 'Business Group', '', 15, 1, 2);
INSERT INTO "public"."group"("id", "name", "description", "owner_id", "classification_id", "tenant_id") VALUES (2003, 'PI: Mark S Cushman', '', 17, 1, 2);
INSERT INTO "public"."group"("id", "name", "description", "owner_id", "classification_id", "tenant_id") VALUES (2004, 'Acme', '', 19, 2, 2);

INSERT INTO "public"."group"("id", "name", "description", "owner_id", "classification_id", "tenant_id") VALUES (3000, 'Admin Group', '', 21, 1, 3);
INSERT INTO "public"."group"("id", "name", "description", "owner_id", "classification_id", "tenant_id") VALUES (3001, 'Mass Spectrometry Technician Group ', '', 23, 1, 3);
INSERT INTO "public"."group"("id", "name", "description", "owner_id", "classification_id", "tenant_id") VALUES (3002, 'Business Group', '', 25, 1, 3);
INSERT INTO "public"."group"("id", "name", "description", "owner_id", "classification_id", "tenant_id") VALUES (3003, 'PI: Mark S Cushman', '', 27, 1, 3);
INSERT INTO "public"."group"("id", "name", "description", "owner_id", "classification_id", "tenant_id") VALUES (3004, 'Acme', '', 29, 2, 3);

INSERT INTO "public"."group"("id", "name", "description", "owner_id", "classification_id", "tenant_id") VALUES (4000, 'Admin Group', '', 31, 1, 4);
INSERT INTO "public"."group"("id", "name", "description", "owner_id", "classification_id", "tenant_id") VALUES (4001, 'Mass Spectrometry Technician Group ', '', 33, 1, 4);
INSERT INTO "public"."group"("id", "name", "description", "owner_id", "classification_id", "tenant_id") VALUES (4002, 'Business Group', '', 35, 1, 4);
INSERT INTO "public"."group"("id", "name", "description", "owner_id", "classification_id", "tenant_id") VALUES (4003, 'PI: Mark S Cushman', '', 37, 1, 4);
INSERT INTO "public"."group"("id", "name", "description", "owner_id", "classification_id", "tenant_id") VALUES (4004, 'Acme', '', 39, 2, 4);

INSERT INTO "public"."group"("id", "name", "description", "owner_id", "classification_id", "tenant_id") VALUES (5000, 'Admin Group', '', 50, null, 5);
ALTER SEQUENCE "public"."group_id_seq" RESTART WITH 5100;

DELETE FROM "public"."group_user";
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (1000, 1, 1);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (1000, 2, 1);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (1001, 3, 1);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (1001, 4, 1);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (1002, 5, 1);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (1002, 6, 1);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (1003, 7, 1);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (1003, 8, 1);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (1004, 9, 1);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (1004, 10, 1);

INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (2000, 11, 2);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (2000, 12, 2);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (2001, 13, 2);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (2001, 14, 2);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (2002, 15, 2);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (2002, 16, 2);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (2003, 17, 2);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (2003, 18, 2);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (2004, 19, 2);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (2004, 20, 2);

INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (3000, 21, 3);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (3000, 22, 3);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (3001, 23, 3);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (3001, 24, 3);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (3002, 25, 3);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (3002, 26, 3);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (3003, 27, 3);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (3003, 28, 3);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (3004, 29, 3);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (3004, 30, 3);

INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (4000, 31, 4);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (4000, 32, 4);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (4001, 33, 4);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (4001, 34, 4);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (4002, 35, 4);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (4002, 36, 4);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (4003, 37, 4);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (4003, 38, 4);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (4004, 39, 4);
INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (4004, 40, 4);

INSERT INTO "public"."group_user"("group_id", "user_id", "tenant_id") VALUES (5000, 50, 5);

DELETE FROM "public"."project";
INSERT INTO "public"."project"("id", "asset_type_id", "status_id", "name", "description", "tenant_id") VALUES(5001, 5, 1, 'NSF Fund 12345678', 'Purdue Center for Cancer Research', 1);
INSERT INTO "public"."project"("id", "asset_type_id", "status_id", "name", "description", "tenant_id") VALUES(5002, 5, 1, 'NIH Fund ABCDEFGH', 'Purdue Center for Cancer Research', 1);
ALTER SEQUENCE "public"."project_id_seq" RESTART WITH 5003;

DELETE FROM "public"."experiment";
INSERT INTO "public"."experiment"("id", "asset_type_id", "project_id", "status_id", "name", "description", "tenant_id") VALUES(7001, 5, 5001, 1, 'NSF Fund ABCD1234 Experiment 1', 'Purdue Center for Cancer Research', 1);
INSERT INTO "public"."experiment"("id", "asset_type_id", "project_id", "status_id", "name", "description", "tenant_id") VALUES(7002, 5, 5001, 1, 'NSF Fund ABCD1234 Experiment 2', 'Purdue Center for Cancer Research', 1);
ALTER SEQUENCE "public"."experiment_id_seq" RESTART WITH 7003;

DELETE FROM "public"."resource";
INSERT INTO "public"."resource"("id", "asset_type_id", "status_id", "name", "description", "owner_id", "tenant_id") VALUES(6001, 6, 1, 'HPLC Resource', 'HPLC related', 1000, 1);
INSERT INTO "public"."resource"("id", "asset_type_id", "status_id", "name", "description", "owner_id", "tenant_id") VALUES(6002, 6, 1, 'MASS Spec', 'MASS spec related', 1000, 1);
ALTER SEQUENCE "public"."resource_id_seq" RESTART WITH 6003;

DELETE FROM "public"."tool";

DELETE FROM "public"."workflow";

DELETE FROM "public"."term";

DELETE FROM "public"."vocabulary";

DELETE FROM "public"."storage";

DELETE FROM "public"."storage_file";

DELETE FROM "public"."computational_node";

DELETE FROM "public"."role";

DELETE FROM "public"."role_operation";

DELETE FROM "public"."permission";
INSERT INTO "public"."permission"("group_id", "role_type_id", "tenant_id") VALUES(1000, 1, 1);
INSERT INTO "public"."permission"("group_id", "role_type_id", "tenant_id") VALUES(2000, 1, 2);
INSERT INTO "public"."permission"("group_id", "role_type_id", "tenant_id") VALUES(3000, 1, 3);
INSERT INTO "public"."permission"("group_id", "role_type_id", "tenant_id") VALUES(4000, 1, 4);
INSERT INTO "public"."permission"("group_id", "role_type_id", "tenant_id") VALUES(5000, 1, 5);

DELETE FROM "public"."job";

DELETE FROM "public"."job_context";
