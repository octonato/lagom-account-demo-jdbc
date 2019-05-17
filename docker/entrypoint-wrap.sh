#!/bin/bash

exec /docker-entrypoint.sh "$@" 

read -r -d '' CQL <<'EOF'
CREATE KEYSPACE IF NOT EXISTS account
WITH REPLICATION = { 'class' : 'SimpleStrategy','replication_factor':1 };


CREATE TABLE IF NOT EXISTS account.accountReports (
    accountNumber text PRIMARY KEY,
    txCount int
);

CREATE TABLE IF NOT EXISTS account.offsetstore (
    eventprocessorid text,
    tag text,
    sequenceoffset bigint,
    timeuuidoffset timeuuid,
    PRIMARY KEY (eventprocessorid, tag)
);


CREATE TABLE IF NOT EXISTS account.config (
  property text primary key, 
  value text
);

CREATE TABLE IF NOT EXISTS account.messages (
    used boolean static,
    persistence_id text,
    partition_nr bigint,
    sequence_nr bigint,
    timestamp timeuuid,
    timebucket text,
    writer_uuid text,
    ser_id int,
    ser_manifest text,
    event_manifest text,
    event blob,
    meta_ser_id int,
    meta_ser_manifest text,
    meta blob,
    tag1 text,
    tag2 text,
    tag3 text,
    message blob,
    PRIMARY KEY ((persistence_id, partition_nr), sequence_nr, timestamp, timebucket))
    WITH gc_grace_seconds = 864000
    AND compaction =  {'bucket_high': '1.5', 'bucket_low': '0.5', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'enabled': 'true', 'max_threshold': '32', 'min_sstable_size': '50', 'min_threshold': '4', 'tombstone_compaction_interval': '86400', 'tombstone_threshold': '0.2', 'unchecked_tombstone_compaction': 'false'};

CREATE TABLE IF NOT EXISTS account.metadata (
  persistence_id text PRIMARY KEY,
  deleted_to bigint,
  properties map<text,text>
);

CREATE MATERIALIZED VIEW IF NOT EXISTS account.eventsbytag1 AS
  SELECT tag1, timebucket, timestamp, persistence_id, partition_nr, sequence_nr, writer_uuid, ser_id, ser_manifest, event_manifest, event,
    meta_ser_id, meta_ser_manifest, meta, message
  FROM account.messages
  WHERE persistence_id IS NOT NULL AND partition_nr IS NOT NULL AND sequence_nr IS NOT NULL
    AND tag1 IS NOT NULL AND timestamp IS NOT NULL AND timebucket IS NOT NULL
  PRIMARY KEY ((tag1, timebucket), timestamp, persistence_id, partition_nr, sequence_nr)
  WITH CLUSTERING ORDER BY (timestamp ASC);

CREATE TABLE account.snapshots (
    persistence_id text,
    sequence_nr bigint,
    meta blob,
    meta_ser_id int,
    meta_ser_manifest text,
    ser_id int,
    ser_manifest text,
    snapshot blob,
    snapshot_data blob,
    timestamp bigint,
    PRIMARY KEY (persistence_id, sequence_nr)
) WITH CLUSTERING ORDER BY (sequence_nr DESC)
    AND gc_grace_seconds = 864000
    AND compaction = {'bucket_high': '1.5', 'bucket_low': '0.5', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'enabled': 'true', 'max_threshold': '32', 'min_sstable_size': '50', 'min_threshold': '4', 'tombstone_compaction_interval': '86400', 'tombstone_threshold': '0.2', 'unchecked_tombstone_compaction': 'false'};  
EOF
  
  
until echo $CQL | cqlsh; do
  echo "cqlsh: Cassandra is unavailable - retry later"
  sleep 2
done 
