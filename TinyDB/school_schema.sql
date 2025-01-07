-- SQL Dump for student
CREATE TABLE student (studentid int, studentname varchar(20), PRIMARY KEY (studentid));

INSERT INTO student VALUES ('1', 'manish jadhav');
INSERT INTO student VALUES ('2', 'jay patel');

-- SQL Dump for subject
CREATE TABLE subject (subjectid int, subjectname varchar(20), studentid int, PRIMARY KEY (subjectid), FOREIGN KEY (studentid) REFERENCES student(studentid));

INSERT INTO subject VALUES ('1', 'dwmt', '1');
INSERT INTO subject VALUES ('2', 'cloud', '2');

