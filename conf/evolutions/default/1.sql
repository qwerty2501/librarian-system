# --- !Ups

create function SET_CREATE_TIME() returns opaque as $$
  begin
    new."CREATED_AT" := now();;
    new."UPDATED_AT" := now();;
    return new;;
  end;;
$$ language 'plpgsql';



create function SET_UPDATE_TIME() returns opaque as $$
  begin
    new."UPDATED_AT" := now();;
    return new;;
  end;;
$$ language 'plpgsql';

# --- !Downs

DROP FUNCTION IF EXISTS SET_CREATE_TIME();
DROP FUNCTION IF EXISTS SET_UPDATE_TIME();
