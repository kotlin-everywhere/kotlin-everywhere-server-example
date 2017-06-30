CREATE TABLE todo
(
  id           UUID PRIMARY KEY                       NOT NULL,
  title        VARCHAR(256)                           NOT NULL,
  created_at   TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
  updated_at   TIMESTAMP WITH TIME ZONE,
  deleted_at   TIMESTAMP WITH TIME ZONE,
  completed_at TIMESTAMP WITH TIME ZONE
)