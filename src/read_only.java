public class read_only {
    long latb=0;
    public static void prepare(int tid){
        //common.cmd_prepare(thread.conn,thread.tid);
    }
    public void event(int tid){
        
        //if(obench.drv.contains("oracle")) common.skip_trx=true;
        while(!sqlbench.tmend)
        {
            //keep sysbench prepare
            latb=System.currentTimeMillis();
            sqlbench.worker[tid].comm.execute_prepare(tid);

            if(!common.skip_trx) sqlbench.worker[tid].comm.execute_begin(tid);
            sqlbench.worker[tid].comm.execute_point_selects(tid);
            if(common.range_selects)
            {
                sqlbench.worker[tid].comm.execute_simple_ranges(tid);
                sqlbench.worker[tid].comm.execute_sum_ranges(tid);
                sqlbench.worker[tid].comm.execute_order_ranges(tid);
                sqlbench.worker[tid].comm.execute_distinct_ranges(tid);
            }
            pubfunc.trx_sleep();
            if(!common.skip_trx) sqlbench.worker[tid].comm.execute_commit(tid);      
            sqlbench.worker[tid].txcnt++;
            sqlbench.worker[tid].latms+=System.currentTimeMillis()-latb;
        } 
    }
}
