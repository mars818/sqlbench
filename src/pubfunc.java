import java.sql.*;

//import java.util.Random;

public class pubfunc {
    //public static Random random=new Random();
    public static String get_c_value()
    {
        String Val = null;
        Val = String.format("%011d",Math.round(Math.random()*100000000000D));
        for(int i=0;i<9;i++)
        {
            Val = Val + "-" + String.format("%011d", Math.round(Math.random()*100000000000D));
        }
        return Val;
    }
    public static String get_pad_value()
    {
        String Val = null;
        Val = String.format("%011d", Math.round(Math.random()*100000000000D));
        for(int i=0;i<4;i++)
        {
            Val = Val + "-" + String.format("%011d", Math.round(Math.random()*100000000000D));
        }
        return Val;
    }
    public static int get_table_num()
    {
        return (int)(Math.random()*(sqlbench.tables))+1;
        //return random.nextInt(obench.tables)+1;
    }
    public static int get_id()
    {
        return (int)(Math.random()*(sqlbench.table_size))+1;
        //return random.nextInt(obench.table_size)+1;
    }
    public static void trx_sleep()
    {
        if(sqlbench.maxsleep==0) return;
        try{
            Thread.sleep(sqlbench.minsleep+(long)(Math.random()*(sqlbench.maxsleep-sqlbench.minsleep)));
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static void get_oceanbase_mode()
    {
        Connection conn = null;
        PreparedStatement ps=null;
        ResultSet rs=null;
        try{
            Class.forName(sqlbench.drv);
            conn = DriverManager.getConnection(sqlbench.url, sqlbench.username, sqlbench.password);
            ps = conn.prepareStatement("SHOW variables like 'ob_compatibility_mode'");
            rs = ps.executeQuery();
            rs.next();
            sqlbench.lang_syntax=rs.getString(2).toLowerCase();
            rs.close();
            ps.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != conn) {
                try{
                    conn.close();
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void usage()
    {
        System.out.print("Usage:\n"+
        "  sysbench [options]... [command]\n"+
        "\n"+
        "Commands implemented by most tests: prepare run cleanup\n"+
        "\n"+
        "General options:\n"+
        "  --test_mode=STRING              read_only,read_multi,read_write,bulk_insert,insert [read_only]\n"+
        "  --tables=N                      number of tables [1]\n"+
        "  --table_size=N                  number of table records\n"+
        "  --threads=N                     number of threads to use [1]\n"+
        "  --inc_threads=N                 number of threads for auto increment [0]\n"+
        "  --max_threads=N                 number of max threads for auto increment [CPU*2]\n"+
        "  --inc_interval=N                interval seconds for auto increment [10]\n"+
        "  --min_sleep=N                   min sleep time(ms) between two transactions. [0]\n"+
        "  --max_sleep=N                   max sleep time(ms) between two transactions. [0]\n"+
        "  --time=N                        limit for total execution time in seconds [10]\n"+
        "  --report_interval=N             periodically report intermediate statistics with a specified interval in seconds.[1]\n"+
        "  --help                          print help and exit\n"+
        "\n"+
        "General database options:\n"+
        "\n"+
        "  --db_driver=STRING    specifies database driver to use (mysql,oracle,oceanbase,opengauss,vastbase,dameng) [mysql]\n"+
        "  --host=STRING         specifies database host IP [localhost]\n"+
        "  --port=N              number of database service\n"+
        "  --dbname=STRING       database(schema name for oceanbase)\n"+
        "  --user=STRING         database user(user@tenant for oceanbase)\n"+
        "  --password=STRING     password of connect user\n"+
        "  --commit_size=N       bulk insert commit size [1000]\n"+
        "  --jdbc_props='STRING' jdbc url connect properties(eg:'useServerPrepStmts=true&cachePrepStmts=true')\n"+
        "  --bind_val[=on|off]   print database-specific debug information [on]\n"+
        "  --db_debug[=on|off]   print database-specific debug information [off]\n"+
        "\n"+
        "for example:\n"+
        "java -jar sqlbench.jar --db_driver=oracle --dbname=orcl --host=localhost --user=test --password=Ben_1234 --test_mode=read_only --threads=3 --tables=3 --table_size=1000 prepare\n"+
        "java -jar sqlbench.jar --db_driver=oracle --dbname=orcl --host=localhost --user=test --password=Ben_1234 --test_mode=read_only --threads=3 --tables=3 --table_size=1000 run\n"+
        "java -jar sqlbench.jar --db_driver=oracle --dbname=orcl --host=localhost --user=test --password=Ben_1234 --test_mode=read_only --threads=3 --tables=3 --table_size=1000 cleanup\n"+
        "\n"+
        "See source code(https://github.com/mars818/sqlbench) for full options for each test.\n\n");
        System.exit(0);
    }
}
