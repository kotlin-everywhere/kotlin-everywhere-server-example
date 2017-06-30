-- This is dummy login data
INSERT INTO member (id, login_id, login_password)
VALUES ('28c24cc0-47fd-45eb-877a-51d7e8783c09', 'dummy', '28c24cc0-47fd-45eb-877a-51d7e8783c09');

ALTER TABLE public.todo
  ADD member_id UUID NOT NULL DEFAULT '28c24cc0-47fd-45eb-877a-51d7e8783c09';

ALTER TABLE public.todo
  ALTER member_id DROP DEFAULT;

ALTER TABLE public.todo
  ADD CONSTRAINT todo_member_id_fk
FOREIGN KEY (member_id) REFERENCES member (id);