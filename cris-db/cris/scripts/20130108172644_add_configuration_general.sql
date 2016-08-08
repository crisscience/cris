--// add configuration general
-- Migration SQL that makes the change goes here.
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('externalSource', 'text', 'Purdue University', null);
INSERT INTO "public"."configuration"("name", "type", "value_text", "tenant_id") VALUES('searchEngineUrl', 'text', 'http://localhost:9200/', null);
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


--//@UNDO
-- SQL to undo the change goes here.
DELETE FROM "public"."configuration" WHERE "tenant_id" IS NULL AND "name" = 'wsEmailAccountProblem';
DELETE FROM "public"."configuration" WHERE "tenant_id" IS NULL AND "name" = 'wsEmailGeneral';
DELETE FROM "public"."configuration" WHERE "tenant_id" IS NULL AND "name" = 'wsAuthReset';
DELETE FROM "public"."configuration" WHERE "tenant_id" IS NULL AND "name" = 'wsAuthProblem';
DELETE FROM "public"."configuration" WHERE "tenant_id" IS NULL AND "name" = 'wsSignupInstruction';
DELETE FROM "public"."configuration" WHERE "tenant_id" IS NULL AND "name" = 'wsSigninInstruction';
DELETE FROM "public"."configuration" WHERE "tenant_id" IS NULL AND "name" = 'wsDescription';
DELETE FROM "public"."configuration" WHERE "tenant_id" IS NULL AND "name" = 'wsName';
DELETE FROM "public"."configuration" WHERE "tenant_id" IS NULL AND "name" = 'wsAuthBackgroundImage';
DELETE FROM "public"."configuration" WHERE "tenant_id" IS NULL AND "name" = 'wsBannerImage';
DELETE FROM "public"."configuration" WHERE "tenant_id" IS NULL AND "name" = 'wsFavicon';
DELETE FROM "public"."configuration" WHERE "tenant_id" IS NULL AND "name" = 'CopyrightYear';
DELETE FROM "public"."configuration" WHERE "tenant_id" IS NULL AND "name" = 'externalSource';
