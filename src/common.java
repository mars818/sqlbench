import java.sql.*;

public class common {
    public static int point_selects=10;
    public static int range_size=100;
    public static int multi_cnt=3;
    public static int commit_size=1000;      // bulk insert commit count
    public static int record_count=0;
    public static boolean skip_trx=false;
    public static boolean pre_begin=true;
    public static boolean range_selects=true;
    public PreparedStatement ps=null;
    public ResultSet rs=null;
    public String sqlstr=null;
    
    //long latb=0;

    public void cmd_prepare(Connection conn,int tid)
    {
        for(int i=0;i<sqlbench.tables;i++)
        {
            if(i%sqlbench.threadcnt == tid) {
                System.out.println("Tread-"+tid+" create table obtest"+(i+1));
                create_table(conn,i+1);
                insert_table(conn,i+1,sqlbench.table_size);
            }
        }
    }

    public void create_table(Connection conn,int n)
    {
        try{
            String strKey=null;
            if(sqlbench.lang_syntax.equals("mysql")) strKey="PRIMARY KEY (id),KEY k_1(k))";
            if(sqlbench.lang_syntax.equals("oracle")) {
                if(sqlbench.dbdriver.equals("dameng")) 
                    strKey="PRIMARY KEY (id))";
                else
                    strKey="PRIMARY KEY (id) using index)";
            }
            ps = conn.prepareStatement("CREATE TABLE obtest"+n+"("+
            "id INTEGER DEFAULT '0' NOT NULL,"+
            "k INTEGER DEFAULT '0' NOT NULL,"+
            "c CHAR(120) DEFAULT '' NOT NULL,"+
            "pad CHAR(60) DEFAULT '' NOT NULL,"+strKey);
            ps.execute();
            ps.close();
            if(sqlbench.lang_syntax.equals("oracle"))
            {
                ps = conn.prepareStatement("CREATE INDEX k_1_"+n+" ON obtest"+n+"(k)");
                ps.execute();
                ps.close();
            }
        }catch (Exception e) {
            if(sqlbench.db_debug) e.printStackTrace();
        }
    }

    public void insert_table(Connection conn,int n,int cnt)
    {
        int l=cnt/commit_size;
        int m=cnt%commit_size;
        int blk=commit_size;
        try{
            ps = conn.prepareStatement("INSERT INTO obtest"+n+
                "(id, k, c, pad) VALUES(?,?,?,?)");
            for(int i=0;i<l+1;i++){
                if(i==l) blk=m;
                for(int j=0;j<blk;j++)
                {
                    ps.setInt(1, i*commit_size+j+1);
                    ps.setInt(2, (int)(Math.random()*(sqlbench.table_size))+1);
                    ps.setString(3, pubfunc.get_c_value());
                    ps.setString(4, pubfunc.get_pad_value());
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
            }
            ps.close();
            
        }catch (Exception e) {
            if(sqlbench.db_debug) e.printStackTrace();
        }
    }

    public void execute_bulk_insert(int tid)
    {
        int tn=tid%sqlbench.tables+1;
        try{
            ps = sqlbench.worker[tid].conn.prepareStatement("INSERT INTO obtest"+tn+
                "(id, k, c, pad) VALUES(?,?,?,?)");
            for(int i=0;i<commit_size;i++){
                //sqlbench.mxid[tid]++;
                ps.setInt(1, sqlbench.get_next_id(tn-1));
                ps.setInt(2, (int)(Math.random()*(sqlbench.table_size))+1);
                ps.setString(3, pubfunc.get_c_value());
                ps.setString(4, pubfunc.get_pad_value());
                //ps.executeUpdate();
                ps.addBatch();
                sqlbench.worker[tid].wrcnt++;
                sqlbench.worker[tid].qrcnt++;
            }
            ps.executeBatch();
            if(common.skip_trx) {
                sqlbench.worker[tid].otcnt++;
                sqlbench.worker[tid].conn.commit();
            }
            ps.close();
            
        }catch (Exception e) {
            exception_handle(tid,e);
        }
    }

    public void cmd_cleanup(Connection conn,int tid)
    {
        for(int i=0;i<sqlbench.tables;i++)
        {
            if(i%sqlbench.threadcnt == tid) 
            {
                System.out.println("Tread-"+tid+" drop table obtest"+(i+1));
                drop_table(conn,i+1);
            }
        }     
    }

    public void drop_table(Connection conn,int n)
    {
        try{
            //PreparedStatement ps=null;
            ps = conn.prepareStatement("DROP TABLE obtest"+n);
            ps.execute();
            ps.close();
            conn.commit(); //opengauss and vastbase need commit to drop
        }catch (Exception e) {
            if(sqlbench.db_debug) e.printStackTrace();
        }
    }

    public void exec_sql(Connection conn,String sql)
    {
        try{
            ps=conn.prepareStatement(sql);
            ps.execute();
            ps.close();
        }catch (Exception e) {
            if(sqlbench.db_debug) e.printStackTrace();
        }
    }

    public void execute_point_selects(int tid)
    {
        try{
            for(int i=0;i<point_selects;i++)
            {
                if(sqlbench.bBindVal){
                    ps = sqlbench.worker[tid].conn.prepareStatement("select c from obtest"+
                    pubfunc.get_table_num()+" where id=?");
                    ps.setInt(1,pubfunc.get_id());
                } else ps = sqlbench.worker[tid].conn.prepareStatement("select c from obtest"+
                    pubfunc.get_table_num()+" where id="+pubfunc.get_id());
                rs=ps.executeQuery();
                rs.next();
                rs.close();
                ps.close();
                sqlbench.worker[tid].rdcnt++;
                sqlbench.worker[tid].qrcnt++;
            }            
        }catch (Exception e) {
            exception_handle(tid,e);
        }
    }
    public void execute_simple_ranges(int tid)
    {
        int id=0;
        try{
            id=pubfunc.get_id();
            if(sqlbench.bBindVal){
                ps = sqlbench.worker[tid].conn.prepareStatement("select c from obtest"+
                pubfunc.get_table_num()+" where id between ? and ?");
                ps.setInt(1,id);
                ps.setInt(2,id+range_size);
            } else ps = sqlbench.worker[tid].conn.prepareStatement("select c from obtest"+
                pubfunc.get_table_num()+" where id between "+id+" and "+(id+range_size));
            ps.executeQuery();
            ps.close();
            sqlbench.worker[tid].rdcnt++;
            sqlbench.worker[tid].qrcnt++;
        }catch (Exception e) {
            exception_handle(tid,e);
        }
    }
    public void execute_sum_ranges(int tid)
    {
        int id=0;
        try{
            id=pubfunc.get_id();
            if(sqlbench.bBindVal){
                ps = sqlbench.worker[tid].conn.prepareStatement("select SUM(k) from obtest"+
                pubfunc.get_table_num()+" where id between ? and ?");
                ps.setInt(1,id);
                ps.setInt(2,id+range_size);
            } else ps = sqlbench.worker[tid].conn.prepareStatement("select SUM(k) from obtest"+
                pubfunc.get_table_num()+" where id between "+id+" and "+(id+range_size));
            ps.executeQuery();
            ps.close();
            sqlbench.worker[tid].rdcnt++;
            sqlbench.worker[tid].qrcnt++;
        }catch (Exception e) {
            exception_handle(tid,e);
        }
    }
    public void execute_order_ranges(int tid)
    {
        int id=0;
        try{
            id=pubfunc.get_id();
            if(sqlbench.bBindVal){
                ps = sqlbench.worker[tid].conn.prepareStatement("select c from obtest"+
                pubfunc.get_table_num()+" where id between ? and ? ORDER BY c");
                ps.setInt(1,id);
                ps.setInt(2,id+range_size);
            } else ps = sqlbench.worker[tid].conn.prepareStatement("select c from obtest"+
                pubfunc.get_table_num()+" where id between "+id+" and "+(id+range_size)+" ORDER BY c");
            ps.executeQuery();
            ps.close();
            sqlbench.worker[tid].rdcnt++;
            sqlbench.worker[tid].qrcnt++;
        }catch (Exception e) {
            exception_handle(tid,e);
        }
    }
    public void execute_distinct_ranges(int tid)
    {
        int id=0;
        try{
            id=pubfunc.get_id();
            if(sqlbench.bBindVal){
                ps = sqlbench.worker[tid].conn.prepareStatement("select DISTINCT c from obtest"+
                pubfunc.get_table_num()+" where id between ? and ? ORDER BY c");
                ps.setInt(1,id);
                ps.setInt(2,id+range_size);
            } else ps = sqlbench.worker[tid].conn.prepareStatement("select DISTINCT c from obtest"+
            pubfunc.get_table_num()+" where id between "+id+" and "+(id+range_size)+" ORDER BY c");
            ps.executeQuery();
            ps.close();
            sqlbench.worker[tid].rdcnt++;
            sqlbench.worker[tid].qrcnt++;
        }catch (Exception e) {
            exception_handle(tid,e);
        }
    }

    public void execute_index_updates(int tid)
    {
        int id=0;
        try{
            id=pubfunc.get_id();
            if(sqlbench.bBindVal){
                ps = sqlbench.worker[tid].conn.prepareStatement("UPDATE obtest"+
                pubfunc.get_table_num()+" set k=k+1 where id=?");
                ps.setInt(1,id);
            } else ps = sqlbench.worker[tid].conn.prepareStatement("UPDATE obtest"+
                pubfunc.get_table_num()+" set k=k+1 where id="+id);
            ps.executeUpdate();
            ps.close();
            sqlbench.worker[tid].wrcnt++;
            sqlbench.worker[tid].qrcnt++;
        }catch (Exception e) {
            exception_handle(tid,e);
        }
    }

    public void execute_non_index_updates(int tid)
    {
        int id=0;
        try{
            id=pubfunc.get_id();
            if(sqlbench.bBindVal){
                ps = sqlbench.worker[tid].conn.prepareStatement("UPDATE obtest"+
                pubfunc.get_table_num()+" set c=? where id=?");
                ps.setString(1,pubfunc.get_c_value());
                ps.setInt(2,id);
            } else ps = sqlbench.worker[tid].conn.prepareStatement("UPDATE obtest"+
                pubfunc.get_table_num()+" set c='"+pubfunc.get_c_value()+"' where id="+id);
            ps.executeUpdate();
            ps.close();
            sqlbench.worker[tid].wrcnt++;
            sqlbench.worker[tid].qrcnt++;
        }catch (Exception e) {
            exception_handle(tid,e);
        }
    }

    public void execute_delete_inserts(int tid)
    {
        int id=pubfunc.get_id();
        int k=(int)(Math.random()*(sqlbench.table_size))+1;
        int tno=pubfunc.get_table_num();
        String cval=pubfunc.get_c_value();
        String pval=pubfunc.get_pad_value();
        try{
            if(sqlbench.bBindVal){
                ps = sqlbench.worker[tid].conn.prepareStatement("DELETE from obtest"+tno+" where id=?");
                ps.setInt(1,id);
            } else ps = sqlbench.worker[tid].conn.prepareStatement("DELETE from obtest"+tno+" where id="+id);
            ps.executeUpdate();
            ps.close();
            sqlbench.worker[tid].wrcnt++;
            sqlbench.worker[tid].qrcnt++;
            if(sqlbench.bBindVal){
                ps = sqlbench.worker[tid].conn.prepareStatement("INSERT INTO obtest"+tno+
                " (id, k, c, pad) VALUES(?,?,?,?)");
                ps.setInt(1,id);
                ps.setInt(2,k);
                ps.setString(3,cval);
                ps.setString(4,pval);
            } else ps = sqlbench.worker[tid].conn.prepareStatement("INSERT INTO obtest"+tno+
                " (id, k, c, pad) VALUES("+id+","+k+",'"+cval+"','"+pval+"')");
            ps.executeUpdate();
            ps.close();
            sqlbench.worker[tid].wrcnt++;
            sqlbench.worker[tid].qrcnt++;
        }catch (Exception e) {
            exception_handle(tid,e);
        }
    }

    public void execute_begin(int tid)
    {
        try{
            if(pre_begin){
                if(!skip_trx){
                    ps=sqlbench.worker[tid].conn.prepareStatement("BEGIN");
                    ps.execute();
                    ps.close();
                    sqlbench.worker[tid].qrcnt++;
                    sqlbench.worker[tid].otcnt++;
                }
            }
        }catch (Exception e) {
            exception_handle(tid,e);
        }
    }
    public void execute_commit(int tid)
    {
        try{
            if(!skip_trx){
                ps=sqlbench.worker[tid].conn.prepareStatement("COMMIT");
                ps.execute();
                ps.close();
                sqlbench.worker[tid].qrcnt++;
                sqlbench.worker[tid].otcnt++;
            }    
        }catch (Exception e) {
            exception_handle(tid,e);
        }          
    }
    public void execute_prepare(int tid)
    {
        try{
            if(!skip_trx){
                if(pre_begin){
                    ps=sqlbench.worker[tid].conn.prepareStatement("BEGIN");
                    ps.execute();
                    ps.close();
                    sqlbench.worker[tid].qrcnt++;
                }
                ps=sqlbench.worker[tid].conn.prepareStatement("COMMIT");
                ps.execute();
                ps.close();
                sqlbench.worker[tid].qrcnt++;
            }
        }catch (Exception e) {
            exception_handle(tid,e);
        }          
    }
    public void exception_handle(int tid,Exception e)
    {
        if(sqlbench.db_debug) e.printStackTrace();        
        sqlbench.worker[tid].ercnt++;
        try{
            if(!sqlbench.worker[tid].conn.isValid(1000))
            {
                Class.forName(sqlbench.drv);
                sqlbench.worker[tid].conn = DriverManager.getConnection(sqlbench.url, sqlbench.username, sqlbench.password);
                sqlbench.worker[tid].conn.setAutoCommit(false);
            }
        }catch (Exception ex) {
            if(sqlbench.db_debug) ex.printStackTrace();
        } 
    }

    public void execute_join_selects(int tid)
    {
        int id=0;
        String tbstr=null;
        String tbcon=null;
        int atn[] = new int[12];
        for(int i=0;i<multi_cnt;i++) atn[i]=pubfunc.get_table_num();
        tbstr="obtest"+atn[0]+" t"+0;
        tbcon="";
        for(int i=1;i<multi_cnt;i++)
        {
            tbstr=tbstr+",obtest"+atn[i]+" t"+i;
            tbcon=tbcon+"t"+(i-1)+".id=t"+i+".id and ";
        }
        id=pubfunc.get_id();
        try{
            //dameng bind value issue
            if(sqlbench.IsDameng)
            {
                ps = sqlbench.worker[tid].conn.prepareStatement("select SUM(t"+(multi_cnt-1)+".k) from "+
                tbstr+" where "+tbcon+" t0.id between "+id+" and "+(id+range_size));
            }
            else
            {
                ps = sqlbench.worker[tid].conn.prepareStatement("select SUM(t"+(multi_cnt-1)+".k) from "+tbstr+" where "+tbcon+" t0.id between ? and ?");
                ps.setInt(1,id);
                ps.setInt(2,id+range_size);
            }
            ps.executeQuery();
            ps.close();
            sqlbench.worker[tid].rdcnt++;
            sqlbench.worker[tid].qrcnt++;
        }catch (Exception e) {
            System.out.println("Conn"+tid+":"+sqlbench.worker[tid].conn);
            exception_handle(tid,e);
        }
    }

    public void execute_insert(int tid)
    {
        try{
            int tn=tid%sqlbench.tables+1;
            ps = sqlbench.worker[tid].conn.prepareStatement("INSERT INTO obtest"+tn+
                "(id, k, c, pad) VALUES(?,?,?,?)");
            //sqlbench.mxid[tn-1]++;
            ps.setInt(1, sqlbench.get_next_id(tn-1));
            ps.setInt(2, (int)(Math.random()*(sqlbench.table_size))+1);
            ps.setString(3, pubfunc.get_c_value());
            ps.setString(4, pubfunc.get_pad_value());
            ps.execute();
            sqlbench.worker[tid].qrcnt++;
            sqlbench.worker[tid].wrcnt++;
            ps.close();
            
        }catch (Exception e) {
            exception_handle(tid,e);
        }
    }

    public void set_tab_mxid(int tid)
    {
        int tabid=tid%sqlbench.tables;
        if(sqlbench.db_debug) System.out.println("set table by table_id");
        try{
            ps=sqlbench.worker[tid].conn.prepareStatement("select max(id) from obtest"+(tabid+1));
            rs=ps.executeQuery();
            rs.next();
            sqlbench.mxid[tabid]=rs.getInt(1); //if null,jdbc return 0
            rs.close();
            ps.close();
        }catch (Exception e) {
            if(sqlbench.db_debug) e.printStackTrace();
        }
    }

    public void set_tn_id(int tid,int tn)
    {
        if(sqlbench.db_debug) System.out.println("set table by table_name");
        try{
            ps=sqlbench.worker[tid].conn.prepareStatement("select max(id) from obtest"+tn);
            rs=ps.executeQuery();
            rs.next();
            sqlbench.mxid[tn-1]=rs.getInt(1); //if null,jdbc return 0
            rs.close();
            ps.close();
        }catch (Exception e) {
            if(sqlbench.db_debug) e.printStackTrace();
        }
    }

    public boolean valid_conn(int tid)
    {
        for(int i=0;i<300;i++)
        {
            try{
                Thread.sleep(100);
                ps=sqlbench.worker[tid].conn.prepareStatement("commit");
                ps.execute();
                ps.close();
                return true;
            }catch (Exception e) {
                if(sqlbench.db_debug) e.printStackTrace();
            }                      
        }
        return false;  
    }

}
