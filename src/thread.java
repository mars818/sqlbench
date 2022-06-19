import java.sql.*;

       
public class thread extends Thread {
    public Connection conn = null;
    public int tid=0;
    public boolean run=true;

    public int txcnt=0;    // transaction count
    public int qrcnt=0;
    public int rdcnt=0;
    public int wrcnt=0;
    public int otcnt=0;
    public int ercnt=0;
    public long latms=0;
    public int rccnt=0;

    public common comm=new common();
    public read_only rdonly=new read_only();
    public read_write rdwrt=new read_write();
    public read_multi rdmul=new read_multi();
    public insert inst=new insert();
    public bulk_insert blkist=new bulk_insert();

    @Override
    public void run() 
    {
        //run=true;
        tid=sqlbench.tid;
        sqlbench.tid++;
        //System.out.println("tread id:"+tid);
        try {
            Class.forName(sqlbench.drv);
            conn = DriverManager.getConnection(sqlbench.url, sqlbench.username, sqlbench.password);
            conn.setAutoCommit(false);

            if(!sqlbench.worker[tid].comm.valid_conn(tid))
            {
                System.out.println("Can not get database connection within 30s !");
                return;
            }

            if(sqlbench.db_debug) System.out.println("InitConn"+tid+" stime:"+System.currentTimeMillis()+" conn:"+conn);

            if(sqlbench.oper.equals("prepare"))
            {
                comm.cmd_prepare(conn,tid);
            }
            if(sqlbench.oper.equals("cleanup"))
            {
                comm.cmd_cleanup(conn,tid);
            }
            if(sqlbench.oper.equals("run"))
            {
                if(sqlbench.test_mode.equals("read_only"))  rdonly.event(tid);
                if(sqlbench.test_mode.equals("read_write")) rdwrt.event(tid);
                if(sqlbench.test_mode.equals("read_multi")) rdmul.event(tid);
                if(sqlbench.test_mode.equals("insert"))     inst.event(tid);
                if(sqlbench.test_mode.equals("bulk_insert")) blkist.event(tid);
            }
            
            if(sqlbench.db_debug) System.out.println("Thread-"+tid+" etime:"+System.currentTimeMillis());
            conn.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != conn) {
                try{
                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        run=false;
    }
    
}
