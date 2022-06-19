# sqlbench

#### Description
JDBC drived sqlbench tool, some oltp tests like sysbench, oracle/mysql/oceanbase/opengauss/vastbase/dameng are supported now. you can clone and extend more drivers and test mode for your test case.

#### Software Architecture
java

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


#### Contribution

1.  Fork the repository
2.  Create Feat_xxx branch
3.  Commit your code
4.  Create Pull Request


#### Gitee Feature

1.  You can use Readme\_XXX.md to support different languages, such as Readme\_en.md, Readme\_zh.md
2.  Gitee blog [blog.gitee.com](https://blog.gitee.com)
3.  Explore open source project [https://gitee.com/explore](https://gitee.com/explore)
4.  The most valuable open source project [GVP](https://gitee.com/gvp)
5.  The manual of Gitee [https://gitee.com/help](https://gitee.com/help)
6.  The most popular members  [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)

