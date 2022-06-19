public class bulk_insert {
    long latb=0;
    public void event(int tid){
        int blk=1;
        if(common.record_count>0)
            blk=common.record_count/common.commit_size+(common.record_count%common.commit_size>0?1:0);
        if(!sqlbench.InitInsIdx) sqlbench.worker[tid].comm.set_tab_mxid(tid);
        while(!sqlbench.tmend && blk>0)
        {
            //System.out.println("blk: "+blk);
            if(common.record_count>0) blk--;
            latb=System.currentTimeMillis();
            if(!common.skip_trx) sqlbench.worker[tid].comm.execute_begin(tid);
            sqlbench.worker[tid].comm.execute_bulk_insert(tid);
            pubfunc.trx_sleep();
            if(!common.skip_trx) sqlbench.worker[tid].comm.execute_commit(tid);
            sqlbench.worker[tid].txcnt++;
            sqlbench.worker[tid].latms+=System.currentTimeMillis()-latb;
        } 
    }
}
