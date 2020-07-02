CREATE TABLE IF NOT EXISTS installers (
    id SERIAL,
    client_id varchar(255),
    enterprise_id varchar(255),
    team_id varchar(255),
    data varchar(2000)
);

CREATE TABLE IF NOT EXISTS bots (
    id SERIAL,
    client_id varchar(255),
    enterprise_id varchar(255),
    team_id varchar(255),
    data varchar(2000)
);

CREATE TABLE IF NOT EXISTS states (
  id SERIAL,
  state varchar(255),
  data varchar(255)
);