public class insert {
    long latb=0;
    public void event(int tid){
        if(sqlbench.mxid[tid%sqlbench.tables]==0) sqlbench.worker[tid].comm.set_tab_mxid(tid);
        while(!sqlbench.tmend)
        {
            latb=System.currentTimeMillis();
            if(!common.skip_trx) sqlbench.worker[tid].comm.execute_begin(tid);
            sqlbench.worker[tid].comm.execute_insert(tid);
            pubfunc.trx_sleep();
            if(!common.skip_trx) sqlbench.worker[tid].comm.execute_commit(tid);      
            sqlbench.worker[tid].txcnt++;
            sqlbench.worker[tid].latms+=System.currentTimeMillis()-latb;
        } 
    }
}
