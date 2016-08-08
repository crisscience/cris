/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * Author:  xu222
 * Created: Oct 27, 2015
 */

CREATE TABLE "public"."vocabulary_term" (
    "vocabulary_id"     int4 NOT NULL,
    "term_id"           int4 NOT NULL,
    PRIMARY KEY("vocabulary_id", "term_id")
);

CREATE INDEX "vocabulary_term_vocabulary_id_index" ON "public"."vocabulary_term"("vocabulary_id");
CREATE INDEX "vocabulary_term_term_id_index" ON "public"."vocabulary_term"("term_id");

ALTER TABLE "public"."vocabulary_term" ADD FOREIGN KEY("vocabulary_id") REFERENCES "public"."vocabulary"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."vocabulary_term" ADD FOREIGN KEY("term_id") REFERENCES "public"."term"("id") ON DELETE CASCADE ON UPDATE NO ACTION;

INSERT INTO vocabulary_term(vocabulary_id, term_id) SELECT DISTINCT MAX(vocabulary_id) OVER (PARTITION BY vocabulary_uuid, tenant_id) AS vocabulary_id, MAX(id) OVER (PARTITION BY uuid, tenant_id) AS term_id FROM term WHERE vocabulary_id IS NOT NULL AND status_id = 1;

--//@UNDO
-- SQL to undo the change goes here.
DROP TABLE IF EXISTS "public"."vocabulary_term_aud";
DROP TABLE "public"."vocabulary_term";
