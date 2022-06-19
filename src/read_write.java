public class read_write {
    long latb=0;
    public void event(int tid){
        while(!sqlbench.tmend)
        {
            latb=System.currentTimeMillis();
            //keep sysbench prepare
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
            sqlbench.worker[tid].comm.execute_index_updates(tid);
            sqlbench.worker[tid].comm.execute_non_index_updates(tid);
            sqlbench.worker[tid].comm.execute_delete_inserts(tid);
            pubfunc.trx_sleep();
            if(!common.skip_trx) sqlbench.worker[tid].comm.execute_commit(tid);      
            sqlbench.worker[tid].txcnt++;
            sqlbench.worker[tid].latms+=System.currentTimeMillis()-latb;
        } 
    }
}
