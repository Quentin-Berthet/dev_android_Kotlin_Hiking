CREATE TABLE IF NOT EXISTS "position" (
	"id"	INTEGER NOT NULL,
	"latitude"	REAL NOT NULL,
	"longitude"	REAL NOT NULL,
	PRIMARY KEY("id" AUTOINCREMENT)
);

CREATE TABLE IF NOT EXISTS "marker" (
	"id"	INTEGER NOT NULL,
	"latitude"	REAL NOT NULL,
	"longitude"	REAL NOT NULL,
	PRIMARY KEY("id" AUTOINCREMENT)
);

CREATE TABLE IF NOT EXISTS "user" (
	"id"	INTEGER NOT NULL,
	"pseudo"	TEXT NOT NULL UNIQUE,
	"password"	TEXT NOT NULL,
	PRIMARY KEY("id" AUTOINCREMENT)
);

CREATE TABLE IF NOT EXISTS "hike" (
	"id"	INTEGER NOT NULL,
	"name"	TEXT NOT NULL,
	"difficulty"	INTEGER NOT NULL,
	"distance_km"	REAL NOT NULL,
	"time_sec"	LONG NOT NULL,
	"comment"	TEXT NOT NULL,
	"created_by"	INTEGER NOT NULL,
	PRIMARY KEY("id" AUTOINCREMENT),
	FOREIGN KEY("created_by") REFERENCES "user"("id")
);

CREATE TABLE IF NOT EXISTS "path_hike" (
	"hike_id"	INTEGER NOT NULL,
	"position_id"	INTEGER NOT NULL,
	PRIMARY KEY('hike_id','position_id'),
	FOREIGN KEY("hike_id") REFERENCES "hike"("id"),
	FOREIGN KEY("position_id") REFERENCES "position"("id")
);

CREATE TABLE IF NOT EXISTS "marker_hike" (
	"hike_id"	INTEGER NOT NULL,
	"marker_id"	INTEGER NOT NULL,
	PRIMARY KEY('hike_id','marker_id'),
	FOREIGN KEY("hike_id") REFERENCES "hike"("id"),
	FOREIGN KEY("marker_id") REFERENCES "marker"("id")
);

CREATE TABLE IF NOT EXISTS "user_hike" (
	"id"	INTEGER NOT NULL,
	"hike_id"	INTEGER NOT NULL,
	"user_id"	INTEGER NOT NULL,
	"note"	INTEGER,
	"comment"	TEXT,
	PRIMARY KEY("id" AUTOINCREMENT),
	FOREIGN KEY("hike_id") REFERENCES "hike"("id"),
	FOREIGN KEY("user_id") REFERENCES "user"("id")
);
