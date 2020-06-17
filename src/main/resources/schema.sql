
CREATE TABLE USER (
    ID      BIGINT GENERATED BY DEFAULT AS IDENTITY,
    NAME    VARCHAR(50)  NOT NULL,
    EMAIL   VARCHAR(80)  NOT NULL,
    ROLE    VARCHAR(250) NOT NULL
);

CREATE UNIQUE INDEX UK_USER_EMAIL ON USER (EMAIL);