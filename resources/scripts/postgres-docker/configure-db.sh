#!/usr/bin/env bash

set -euo pipefail

DB_APP_DB=valihuutobot
DB_APP_USER=valihuuto_user
DB_APP_PASSWORD=valihuuto

echo "Creating database \"$DB_APP_DB\", creating role \"$DB_APP_USER\" with database owner privileges…"

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-END
create role "${DB_APP_USER}" with password '${DB_APP_PASSWORD}' login;
create database "${DB_APP_DB}" encoding 'UTF-8';
grant all privileges on database "${DB_APP_DB}" to "${DB_APP_USER}";
END
