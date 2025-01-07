-- SQL Dump for t2
CREATE TABLE t2 (id int, PRIMARY KEY (id));

INSERT INTO t2 VALUES ('1');

-- SQL Dump for t3
CREATE TABLE t3 (t3id int, id int, PRIMARY KEY (t3id), FOREIGN KEY (id) REFERENCES t2(id));

INSERT INTO t3 VALUES ('1', '1');
INSERT INTO t3 VALUES ('2', '1');

