public class read_multi {
    long latb=0;
    public void event(int tid){
        while(!sqlbench.tmend)
        {
            latb=System.currentTimeMillis();
            if(!common.skip_trx) sqlbench.worker[tid].comm.execute_begin(tid);
            sqlbench.worker[tid].comm.execute_join_selects(tid);
            pubfunc.trx_sleep();
            if(!common.skip_trx) sqlbench.worker[tid].comm.execute_commit(tid);      
            sqlbench.worker[tid].txcnt++;
            sqlbench.worker[tid].latms+=System.currentTimeMillis()-latb;
        } 
    }
}
