create table automatisch_proces (
    dtype varchar(255) not null,
    id  bigserial not null,
    cron_expressie varchar(255),
    lastrun timestamp,
    logfile text,
    samenvatting text,
    status varchar(255),
    primary key (id)
);

create table automatisch_proces_config (
    proces_id int8 not null,
    value text,
    config_key varchar(255),
    primary key (proces_id, config_key)
);

create table bericht (
    id  bigserial not null,
    br_orgineel_xml text,
    br_xml text,
    datum timestamp,
    db_xml text,
    job_id varchar(255),
    object_ref varchar(255),
    opmerking text,
    soort varchar(255),
    status varchar(255),
    status_datum timestamp,
    volgordenummer int4,
    xsl_version varchar(255),
    laadprocesid int8,
    primary key (id)
);

create table gebruiker_ (
    gebruikersnaam varchar(255) not null,
    wachtwoord varchar(255),
    primary key (gebruikersnaam)
);

create table gebruiker_groepen (
    gebruikersnaam varchar(255) not null,
    groep_ varchar(255) not null,
    primary key (gebruikersnaam, groep_)
);

create table groep_ (
    naam varchar(255) not null,
    beschrijving text,
    primary key (naam)
);

create table laadproces (
    id  bigserial not null,
    bestand_datum timestamp,
    bestand_naam varchar(255),
    contact_email varchar(255),
    gebied varchar(255),
    opmerking text,
    soort varchar(255),
    status varchar(255),
    status_datum timestamp,
    automatisch_proces int8,
    primary key (id)
);

alter table automatisch_proces_config
    add constraint FK39F3573E561B9F9B
    foreign key (proces_id)
    references automatisch_proces;

alter table bericht
    add constraint bericht_laadprocesid_fkey
    foreign key (laadprocesid)
    references laadproces;

alter table gebruiker_groepen
    add constraint FKD875A48FD741C965
    foreign key (groep_)
    references groep_;

alter table gebruiker_groepen
    add constraint FKD875A48F49E041F8
    foreign key (gebruikersnaam)
    references gebruiker_;

alter table laadproces
    add constraint FK8C420DCE3DA16A8
    foreign key (automatisch_proces)
    references automatisch_proces;

CREATE TABLE job(
    jid bigserial NOT NULL,
    id BIGINT,
    br_xml TEXT,
    datum TIMESTAMP(6) WITHOUT TIME ZONE,
    object_ref CHARACTER VARYING(255),
    soort CHARACTER VARYING(255),
    volgordenummer INTEGER,
    PRIMARY KEY (jid)
);

create table geometries (
    id bigserial not null,
    datum timestamp,
    naam varchar(255),
    geom geometry(MultiPolygon,28992),
    primary key (id)
);

CREATE SEQUENCE testing_seq MINVALUE 1;

create table booleantable (
    id bigserial not null,
    ishetwaar boolean not null
);