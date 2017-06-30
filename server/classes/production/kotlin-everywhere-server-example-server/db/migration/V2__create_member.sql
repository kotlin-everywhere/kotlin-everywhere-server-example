CREATE TABLE member
(
  id             UUID PRIMARY KEY                       NOT NULL,
  login_id       VARCHAR(256)                           NOT NULL,
  login_password VARCHAR(256)                           NOT NULL,
  created_at     TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
  updated_at     TIMESTAMP WITH TIME ZONE,
  deleted_at     TIMESTAMP WITH TIME ZONE
);

CREATE UNIQUE INDEX member_login_id_uindex
  ON member (login_id)