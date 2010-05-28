#!/bin/bash
#Temporary test script for triggers and unnormalized data.
#Don't use this on the real database.

if [ $# != 2 ]; then
  echo "usage: $0 dbname dbuser"
  exit
fi
DB=$1
USER=$2

/usr/local/pgsql/bin/psql -A -t -q $DB $USER <<EOF
begin;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

CREATE TABLE descriptor (
    descriptor character(40) NOT NULL,
    address character varying(15) NOT NULL,
    orport integer NOT NULL,
    dirport integer NOT NULL,
    bandwidthavg bigint NOT NULL,
    bandwidthburst bigint NOT NULL,
    bandwidthobserved bigint NOT NULL,
    platform character varying(256),
    published timestamp without time zone NOT NULL,
    uptime bigint
);

CREATE TABLE statusentry (
    validafter timestamp without time zone NOT NULL,
    descriptor character(40) NOT NULL,
    isauthority boolean DEFAULT false NOT NULL,
    isbadexit boolean DEFAULT false NOT NULL,
    isbaddirectory boolean DEFAULT false NOT NULL,
    isexit boolean DEFAULT false NOT NULL,
    isfast boolean DEFAULT false NOT NULL,
    isguard boolean DEFAULT false NOT NULL,
    ishsdir boolean DEFAULT false NOT NULL,
    isnamed boolean DEFAULT false NOT NULL,
    isstable boolean DEFAULT false NOT NULL,
    isrunning boolean DEFAULT false NOT NULL,
    isunnamed boolean DEFAULT false NOT NULL,
    isvalid boolean DEFAULT false NOT NULL,
    isv2dir boolean DEFAULT false NOT NULL,
    isv3dir boolean DEFAULT false NOT NULL
);

--
--  descriptor-status: Unnormalized table containing both descriptors and
--  status entries in one big table.
--

CREATE TABLE descriptor_statusentry (
    descriptor character(40) NOT NULL,
    address character varying(15) NOT NULL,
    orport integer NOT NULL,
    dirport integer NOT NULL,
    bandwidthavg bigint NOT NULL,
    bandwidthburst bigint NOT NULL,
    bandwidthobserved bigint NOT NULL,
    platform character varying(256),
    published timestamp without time zone NOT NULL,
    uptime bigint,
    validafter timestamp without time zone NOT NULL,
    isauthority boolean DEFAULT false NOT NULL,
    isbadexit boolean DEFAULT false NOT NULL,
    isbaddirectory boolean DEFAULT false NOT NULL,
    isexit boolean DEFAULT false NOT NULL,
    isfast boolean DEFAULT false NOT NULL,
    isguard boolean DEFAULT false NOT NULL,
    ishsdir boolean DEFAULT false NOT NULL,
    isnamed boolean DEFAULT false NOT NULL,
    isstable boolean DEFAULT false NOT NULL,
    isrunning boolean DEFAULT false NOT NULL,
    isunnamed boolean DEFAULT false NOT NULL,
    isvalid boolean DEFAULT false NOT NULL,
    isv2dir boolean DEFAULT false NOT NULL,
    isv3dir boolean DEFAULT false NOT NULL
);

ALTER TABLE ONLY descriptor
    ADD CONSTRAINT descriptor_pkey PRIMARY KEY (descriptor);

ALTER TABLE ONLY statusentry
    ADD CONSTRAINT statusentry_pkey PRIMARY KEY (validafter, descriptor);

ALTER TABLE ONLY descriptor_statusentry
    ADD CONSTRAINT descriptor_statusentry_pkey PRIMARY KEY (validafter, descriptor);

CREATE INDEX descriptorid ON descriptor USING btree (descriptor);
CREATE INDEX statusvalidafter ON statusentry USING btree (validafter);

CREATE LANGUAGE plpgsql;

--TRIGGER mirror_statusentry()
--We want the unnormalized table 'descriptor_status' to have any
--inserts to statusentry.

CREATE FUNCTION mirror_statusentry() RETURNS TRIGGER AS \$mirror_statusentry\$
    DECLARE
        rd descriptor%ROWTYPE;
        dcount INTEGER;
    BEGIN
        IF (TG_OP = 'INSERT') THEN
            SELECT count(*) INTO dcount
            FROM descriptor
            WHERE descriptor=NEW.descriptor;

            IF (dcount = 0) THEN
                RAISE EXCEPTION 'There is no record with descriptor=\'% \' in descriptor', NEW.descriptor;
            END IF;

            SELECT * INTO rd FROM descriptor WHERE descriptor=NEW.descriptor;
            INSERT INTO descriptor_statusentry
            VALUES (rd.descriptor, rd.address, rd.orport, rd.dirport,
                    rd.bandwidthavg, rd.bandwidthburst, rd.bandwidthobserved,
                    rd.platform, rd.published, rd.uptime, new.validafter,
                    new.isauthority, new.isbadexit, new.isbaddirectory,
                    new.isexit, new.isfast, new.isguard, new.ishsdir,
                    new.isnamed, new.isstable, new.isrunning, new.isunnamed,
                    new.isvalid, new.isv2dir, new.isv3dir);
        ELSIF (TG_OP = 'UPDATE') THEN
            UPDATE descriptor_statusentry
            SET isauthority=NEW.isauthority,
                isbadexit=NEW.isbadexit, isbaddirectory=NEW.isbaddirectory,
                isexit=NEW.isexit, isfast=NEW.isfast, isguard=NEW.isguard,
                ishsdir=NEW.ishsdir, isnamed=NEW.isnamed, isstable=NEW.isstable,
                isrunning=NEW.isrunning, isunnamed=NEW.isunnamed,
                isvalid=NEW.isvalid, isv2dir=NEW.isv2dir, isv3dir=NEW.isv3dir
            WHERE descriptor=NEW.descriptor AND validafter=NEW.validafter;
        ELSIF (TG_OP = 'DELETE') THEN
            DELETE FROM descriptor_statusentry
            WHERE validafter=OLD.validafter AND descriptor=OLD.descriptor;
        END IF;
    RETURN NEW;
END;
\$mirror_statusentry\$ LANGUAGE plpgsql;

--
--FUNCTION mirror_descriptor
--Reflect changes in descriptor_statusentry when changes made to descriptor table
--

CREATE FUNCTION mirror_descriptor() RETURNS TRIGGER AS \$mirror_descriptor\$
    DECLARE
    BEGIN
        IF (TG_OP = 'UPDATE') THEN
            UPDATE descriptor_statusentry
            SET address=NEW.address, orport=NEW.orport, dirport=NEW.dirport,
                bandwidthavg=NEW.bandwidthavg, bandwidthburst=NEW.bandwidthburst,
                bandwidthobserved=NEW.bandwidthobserved, platform=NEW.platform,
                published=NEW.published, uptime=NEW.uptime
            WHERE descriptor=NEW.descriptor;
        ELSIF (TG_OP = 'DELETE') THEN
            DELETE FROM descriptor_statusentry
            WHERE descriptor=OLD.descriptor;
            DELETE FROM statusentry
            WHERE descriptor=OLD.descriptor;
        END IF;
    RETURN NEW;
END;
\$mirror_descriptor\$ LANGUAGE plpgsql;

CREATE TRIGGER mirror_statusentry AFTER INSERT OR UPDATE OR DELETE ON statusentry
    FOR EACH ROW EXECUTE PROCEDURE mirror_statusentry();

CREATE TRIGGER mirror_descriptor AFTER UPDATE OR DELETE ON descriptor
    FOR EACH ROW EXECUTE PROCEDURE mirror_descriptor();

--
--TEST QUERIES - To make sure data stays consistent.
--

insert into descriptor values ('ff0613a644c1406cc2ea42ef46a32ed572ed9386', '119.42.144.18',
                                9001, 0, 20480, 40960, 0, 'Tor 0.2.1.19 on Linux i686',
                                '2010-03-16 07:11:14', 10);
insert into descriptor values ('ab0313a644c1406cc2ea42ef46a32ed572ed9386', '119.42.144.18',
                                9001, 0, 20480, 40960, 0, 'Tor 0.2.1.19 on Linux i686',
                                '2010-03-16 07:11:14', 10);
insert into statusentry values ('2010-03-19 15:00:00',
                                'ab0313a644c1406cc2ea42ef46a32ed572ed9386', 't', 'f', 'f',
                                'f', 'f', 'f', 'f', 'f', 'f', 'f', 'f', 'f', 'f', 'f');
insert into statusentry values ('2010-03-19 15:00:00',
                                'ff0613a644c1406cc2ea42ef46a32ed572ed9386', 't', 'f', 'f',
                                'f', 'f', 'f', 'f', 'f', 'f', 'f', 'f', 'f', 'f', 'f');
insert into statusentry values ('2010-03-20 16:00:00',
                                'ff0613a644c1406cc2ea42ef46a32ed572ed9386', 't', 'f', 'f',
                                'f', 'f', 'f', 'f', 'f', 'f', 'f', 'f', 'f', 'f', 'f');

--Test update and delete triggers on statusentry
--update statusentry set ishsdir='t' where descriptor='ff0613a644c1406cc2ea42ef46a32ed572ed9386';
--delete from statusentry where descriptor='ff0613a644c1406cc2ea42ef46a32ed572ed9386';

--Test update and delete triggers on descriptor
--update descriptor set orport='10000' where descriptor='ff0613a644c1406cc2ea42ef46a32ed572ed9386';
--delete from descriptor where descriptor='ff0613a644c1406cc2ea42ef46a32ed572ed9386';

--Test insert when there is no corresponding descriptor
--insert into statusentry values ('2010-03-19 15:00:00',
--                                'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', 't', 'f', 'f',
--                                'f', 'f', 'f', 'f', 'f', 'f', 'f', 'f', 'f', 'f', 'f');

\\echo -----descriptor-----
select * from descriptor;
\\echo -----statusentry-----
select * from statusentry;
\\echo -----descriptor_statusentry-----
select * from descriptor_statusentry;
rollback;
EOF
