DROP TABLE IF EXISTS users, categories, events, requests, compilations, compilations_events;

CREATE TABLE IF NOT EXISTS users (
    user_id integer GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    email varchar(254) UNIQUE NOT NULL,
    name varchar(250) NOT NULL
);

CREATE TABLE IF NOT EXISTS categories (
    category_id integer GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    name varchar(50) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS events (
    event_id integer GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    title varchar(120) NOT NULL,
    annotation varchar(2000) NOT NULL,
    description varchar(7000) NOT NULL,
    category_id integer NOT NULL REFERENCES categories(category_id) ON DELETE CASCADE,
    initiator_id integer NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    created_on TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    published_on TIMESTAMP WITH TIME ZONE,
    event_date TIMESTAMP WITH TIME ZONE NOT NULL,
    location_lat float NOT NULL,
    location_lon float NOT NULL,
    state integer NOT NULL,
    participant_limit integer NOT NULL,
    request_moderation boolean NOT NULL,
    paid boolean NOT NULL
);

CREATE TABLE IF NOT EXISTS requests (
    request_id integer GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    requester_id integer NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    event_id integer NOT NULL REFERENCES events(event_id) ON DELETE CASCADE,
    created TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    status integer NOT NULL
);

CREATE TABLE IF NOT EXISTS compilations (
    compilation_id integer GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    title varchar(50) NOT NULL,
    pinned boolean NOT NULL
);

CREATE TABLE IF NOT EXISTS compilations_events (
    compilation_id integer NOT NULL REFERENCES compilations (compilation_id) ON DELETE CASCADE,
    event_id integer NOT NULL REFERENCES events (event_id) ON DELETE CASCADE,
    PRIMARY KEY (compilation_id, event_id)
);