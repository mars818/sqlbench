# sqlbench

#### Description
JDBC drived sqlbench tool, some oltp tests like sysbench, oracle/mysql/oceanbase/opengauss/vastbase/dameng are supported now. you can clone and extend more drivers and test mode for your test case.

#### Software Requirements
openjdk version 11+

#### Installation

1.  clone or download and unzip
2.  bash makejar.sh
3.  run as following steps.

#### Instructions
for oracle test:
1.  create database user<br>
    create user test identified by 'Ben_1234';<br>
    grant connect,resouce,unlimited tablespace to test;<br><br>
2.  prepare data for test<br>
    java -jar sqlbench.jar --db_driver=oracle --dbname=orcl --host=localhost --user=test --password=Ben_1234 --test_mode=read_only --threads=5 --tables=5 --table_size=10000 prepare<br><br>
3.  run test.<br>
    java -jar sqlbench.jar --db_driver=oracle --dbname=orcl --host=localhost --user=test --password=Ben_1234 --test_mode=read_only --threads=5 --tables=5 --table_size=10000 run<br><br>
4.  cleanup data.<br>
    java -jar sqlbench.jar --db_driver=oracle --dbname=orcl --host=localhost --user=test --password=Ben_1234 --test_mode=read_only --threads=5 --tables=5 --table_size=10000 cleanup<br>

