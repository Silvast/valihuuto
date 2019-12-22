#!/usr/bin/env bash

set -euo pipefail

echo "Configuring PostgreSQL to log database modification statements…"

cat <<END >> /var/lib/postgresql/data/postgresql.conf
log_destination = 'stderr'
log_line_prefix = '%t %u '
log_statement = 'mod'
END
