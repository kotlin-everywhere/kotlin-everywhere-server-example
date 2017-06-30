CREATE TABLE access_token
(
  id         UUID PRIMARY KEY                       NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE,
  deleted_at TIMESTAMP WITH TIME ZONE,
  member_id  UUID                                   NOT NULL,
  CONSTRAINT access_token_member_id_fk FOREIGN KEY (member_id) REFERENCES public.member (id)
)
