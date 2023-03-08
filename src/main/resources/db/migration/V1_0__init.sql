create table reviewer (
    id              BIGINT          NOT NULL    AUTO_INCREMENT,
    job             VARCHAR(255),
    career          VARCHAR(255),
    introduction    VARCHAR(255),
    PRIMARY KEY (id),
)

create table member (
    id              BIGINT          NOT NULL    AUTO_INCREMENT,
    reviewer_id     BIGINT          NOT NULL,
    github_id       VARCHAR(255)    NOT NULL    UNIQUE,
    username        VARCHAR(255)    NOT NULL    UNIQUE,
    email           VARCHAR(255)    UNIQUE,
    image_url       VARCHAR(255),
    profile_url     VARCHAR(255),
    is_reviewer     BOOLEAN         DEFAULT 0,
    PRIMARY KEY (id),
    FOREIGN KEY (reviewer_id) REFERENCES reviewer (id)
)

create table category (
    id              BIGINT          NOT NULL    AUTO_INCREMENT,
    name            VARCHAR(255)    NOT NULL,
    PRIMARY KEY (id)
)

create table tag (
    id              BIGINT          NOT NULL    AUTO_INCREMENT,
    category_id     BIGINT          NOT NULL,
    name            VARCHAR(255)    NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (category_id) REFERENCES category (id)
)

create table reviewer_tag (
    reviewer_id     BIGINT          NOT NULL,
    tag_id          BIGINT          NOT NULL,
    PRIMARY KEY (reviewer_id, tag_id),
    FOREIGN KEY (reviewer_id) REFERENCES reviewer (id),
    FOREIGN KEY (tag_id) REFERENCES tag (id)
)