DROP TABLE IF EXISTS host;
CREATE TABLE host(
id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
name varchar(42) UNIQUE NOT NULL,
MAC char(12) UNIQUE NOT NULL,
validated INTEGER NOT NULL DEFAULT 0
);

DROP TABLE IF EXISTS "group";
CREATE TABLE "group"(
id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
name varchar(42) UNIQUE NOT NULL,
description TEXT
);

DROP TABLE IF EXISTS storage;
CREATE TABLE storage(
id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
name varchar(42) UNIQUE NOT NULL,
type varchar(8) NOT NULL,
baseURL varchar(256) NOT NULL,
directory varchar(128) UNIQUE NOT NULL
);

DROP TABLE IF EXISTS image;
CREATE TABLE image(
id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
name varchar(42) UNIQUE NOT NULL,
type varchar(8) NOT NULL,
description TEXT,
script TEXT NOT NULL,
storageID INTEGER NOT NULL,
directory varchar(128),
UNIQUE (storageID, directory),
FOREIGN KEY(storageID) REFERENCES storage(id)
);

DROP TABLE IF EXISTS HostGroup;
CREATE TABLE HostGroup(
hostID INTEGER NOT NULL,
groupID INTEGER NOT NULL,
PRIMARY KEY(hostID, groupID),
FOREIGN KEY(hostID) REFERENCES host(id) ON DELETE CASCADE,
FOREIGN KEY(groupID) REFERENCES "group"(id) ON DELETE CASCADE
);

DROP TABLE IF EXISTS HostImage;
CREATE TABLE HostImage(
id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
hostID INTEGER NOT NULL,
imageID INTEGER NOT NULL,
timePeriod INTEGER NOT NULL DEFAULT FALSE,
bootParameter TEXT NOT NULL DEFAULT "",
priority INTEGER NOT NULL DEFAULT 0 CHECK (priority >= 0),
UNIQUE(hostID, imageID, timePeriod, bootParameter, priority),
FOREIGN KEY(hostID) REFERENCES host(id) ON DELETE CASCADE,
FOREIGN KEY(imageID) REFERENCES image(id)
);

DROP TABLE IF EXISTS GroupImage;
CREATE TABLE GroupImage(
id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
groupID INTEGER NOT NULL,
imageID INTEGER NOT NULL,
timePeriod INTEGER NOT NULL DEFAULT FALSE,
bootParameter TEXT NOT NULL DEFAULT "",
priority INTEGER NOT NULL DEFAULT 0 CHECK (priority >=0),
UNIQUE(groupID, imageID, timePeriod, bootParameter, priority),
FOREIGN KEY(groupID) REFERENCES "group"(id) ON DELETE CASCADE,
FOREIGN KEY(imageID) REFERENCES image(id)
);

DROP TABLE IF EXISTS HostTime;
CREATE TABLE HostTime(
id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
hostImageID INTEGER NOT NULL,
"minute" INTEGER NOT NULL DEFAULT 0 CHECK("minute" >= 0 AND "minute" < 60),
"hour" INTEGER NOT NULL DEFAULT 0 CHECK ("hour" >= 0 AND "hour" < 24),
dom INTEGER DEFAULT NULL CHECK (dom > 0 AND dom < 32),
"month" INTEGER DEFAULT NULL CHECK ("month" > 0 AND "month" < 13),
dow INTEGER DEFAULT NULL CHECK (dow >= 0 AND dow <= 6), -- Sunday == 0
validMinutes INTEGER NOT NULL CHECK (validMinutes > 0 AND validMinutes <= (1440-"minute"-60*"hour")), --Only minutes on a specific day are allowed. If you want two transitioning days, define two rows.
FOREIGN KEY(hostImageID) REFERENCES HostImage(id) ON DELETE CASCADE
);

DROP TABLE IF EXISTS GroupTime;
CREATE TABLE GroupTime(
id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
groupImageID INTEGER NOT NULL,
"minute" INTEGER NOT NULL DEFAULT 0 CHECK("minute" >= 0 AND "minute" < 60),
"hour" INTEGER NOT NULL DEFAULT 0 CHECK ("hour" >= 0 AND "hour" < 24),
dom INTEGER DEFAULT NULL CHECK (dom > 0 AND dom < 32),
"month" INTEGER DEFAULT NULL CHECK ("month" > 0 AND "month" < 13),
dow INTEGER DEFAULT NULL CHECK (dow >= 0 AND dow <= 6),
validMinutes INTEGER NOT NULL CHECK (validMinutes > 0 AND validMinutes <= (1440-"minute"-60*"hour")),
FOREIGN KEY(GroupImageID) REFERENCES "GroupImage"(id) ON DELETE CASCADE
);