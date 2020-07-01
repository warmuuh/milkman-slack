CREATE TABLE IF NOT EXISTS installers (
    id int NOT NULL PRIMARY KEY,
    client_id varchar(255),
    enterprise_id varchar(255),
    team_id varchar(255),
    data varchar(2000)
);

CREATE TABLE IF NOT EXISTS bots (
    id int NOT NULL PRIMARY KEY,
    client_id varchar(255),
    enterprise_id varchar(255),
    team_id varchar(255),
    data varchar(2000)
);

CREATE TABLE IF NOT EXISTS states (
  id int NOT NULL PRIMARY KEY,
  state varchar(255),
  data varchar(255)
);