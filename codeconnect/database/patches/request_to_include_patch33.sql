CREATE TABLE parcel (
	id INTEGER NOT NULL,
	parcelid INTEGER,
	createdts TIMESTAMP WITHOUT TIME ZONE,
	PRIMARY KEY (id)
);

CREATE TABLE externaldatawprdc (
	id INTEGER NOT NULL,
	parcel_id INTEGER,
	creationts TIMESTAMP WITHOUT TIME ZONE,
	lastupdatedts TIMESTAMP WITHOUT TIME ZONE,
	"PARID" VARCHAR(30),
	"PROPERTYHOUSENUM" INTEGER,
	"PROPERTYFRACTION" VARCHAR(6),
	"PROPERTYADDRESS" VARCHAR(80),
	"PROPERTYCITY" VARCHAR(50),
	"PROPERTYSTATE" VARCHAR(50),
	"PROPERTYUNIT" VARCHAR(30),
	"PROPERTYZIP" VARCHAR(10),
	"MUNICODE" VARCHAR(5),
	"MUNIDESC" VARCHAR(50),
	"SCHOOLCODE" VARCHAR(30),
	"SCHOOLDESC" VARCHAR(50),
	"LEGAL1" VARCHAR(60),
	"LEGAL2" VARCHAR(60),
	"LEGAL3" VARCHAR(60),
	"NEIGHCODE" VARCHAR(8),
	"NEIGHDESC" VARCHAR(50),
	"TAXCODE" VARCHAR(1),
	"TAXDESC" VARCHAR(50),
	"TAXSUBCODE" VARCHAR(1),
	"TAXSUBCODE_DESC" VARCHAR(50),
	"OWNERCODE" VARCHAR(3),
	"OWNERDESC" VARCHAR(50),
	"CLASS" VARCHAR(2),
	"CLASSDESC" VARCHAR(50),
	"USECODE" VARCHAR(4),
	"USEDESC" VARCHAR(50),
	"LOTAREA" FLOAT,
	"HOMESTEADFLAG" VARCHAR(6),
	"CLEANGREEN" VARCHAR(3),
	"FARMSTEADFLAG" VARCHAR(6),
	"ABATEMENTFLAG" VARCHAR(6),
	"RECORDDATE" VARCHAR(10),
	"SALEDATE" VARCHAR(10),
	"SALEPRICE" FLOAT,
	"SALECODE" VARCHAR(2),
	"SALEDESC" VARCHAR(50),
	"DEEDBOOK" VARCHAR(8),
	"DEEDPAGE" VARCHAR(8),
	"PREVSALEDATE" VARCHAR(10),
	"PREVSALEPRICE" FLOAT,
	"PREVSALEDATE2" VARCHAR(10),
	"PREVSALEPRICE2" FLOAT,
	"CHANGENOTICEADDRESS1" VARCHAR(100),
	"CHANGENOTICEADDRESS2" VARCHAR(100),
	"CHANGENOTICEADDRESS3" VARCHAR(100),
	"CHANGENOTICEADDRESS4" VARCHAR(100),
	"COUNTYBUILDING" FLOAT,
	"COUNTYLAND" FLOAT,
	"COUNTYTOTAL" FLOAT,
	"COUNTYEXEMPTBLDG" FLOAT,
	"LOCALBUILDING" FLOAT,
	"LOCALLAND" FLOAT,
	"LOCALTOTAL" FLOAT,
	"FAIRMARKETBUILDING" FLOAT,
	"FAIRMARKETLAND" FLOAT,
	"FAIRMARKETTOTAL" FLOAT,
	"STYLE" VARCHAR(2),
	"STYLEDESC" VARCHAR(50),
	"STORIES" VARCHAR(3),
	"YEARBLT" FLOAT,
	"EXTERIORFINISH" VARCHAR(2),
	"EXTFINISH_DESC" VARCHAR(50),
	"ROOF" VARCHAR(20),
	"ROOFDESC" VARCHAR(50),
	"BASEMENT" VARCHAR(1),
	"BASEMENTDESC" VARCHAR(50),
	"GRADE" VARCHAR(3),
	"GRADEDESC" VARCHAR(50),
	"CONDITION" VARCHAR(2),
	"CONDITIONDESC" VARCHAR(50),
	"CDU" VARCHAR(2),
	"CDUDESC" VARCHAR(50),
	"TOTALROOMS" FLOAT,
	"BEDROOMS" FLOAT,
	"FULLBATHS" FLOAT,
	"HALFBATHS" FLOAT,
	"HEATINGCOOLING" VARCHAR(1),
	"HEATINGCOOLINGDESC" VARCHAR(50),
	"FIREPLACES" FLOAT,
	"BSMTGARAGE" VARCHAR(1),
	"FINISHEDLIVINGAREA" FLOAT,
	"CARDNUMBER" FLOAT,
	"ALT_ID" VARCHAR(30),
	"TAXYEAR" FLOAT,
	"ASOFDATE" TEXT,
	PRIMARY KEY (id),
	FOREIGN KEY(parcel_id) REFERENCES parcel (id)
);

CREATE TABLE externaldatarealestateportal (
	id INTEGER NOT NULL,
	parcel_id INTEGER,
	creationts TIMESTAMP WITHOUT TIME ZONE,
	lastupdatedts TIMESTAMP WITHOUT TIME ZONE,
	parcelid TEXT,
	propertyaddress TEXT,
	municipality TEXT,
	ownername TEXT,
	PRIMARY KEY (id),
	FOREIGN KEY(parcel_id) REFERENCES parcel (id)
);

CREATE TABLE externaldatataxstatus (
	id INTEGER NOT NULL,
	creationts TIMESTAMP WITHOUT TIME ZONE,
	lastupdatedts TIMESTAMP WITHOUT TIME ZONE,
	externaldatarealestateportalid INTEGER,
	year SMALLINT,
	paidstatus TEXT,
	tax MONEY,
	penalty MONEY,
	interest MONEY,
	total MONEY,
	datepaid DATE,
	PRIMARY KEY (id),
	FOREIGN KEY(externaldatarealestateportalid) REFERENCES externaldatarealestateportal (id)
);
