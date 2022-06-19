
public class sqlbench {
    public static int tid = 0;
    public static int tables = 1;
    public static int table_size = 1;
    public static int threadcnt = 1;
    public static int duration = 10;
    public static int rptinterval=1;
    public static String test_mode="read_only";
    public static String lang_syntax="oracle";
    public static String jdbc_props=null;
    public static boolean db_debug=false;
    public static String drv = null;
    public static String url = null;
    public static String username = "test";
    public static String password = "Tst_1234"; 
    public static String oper = null;
    public static String dbdriver="oracle";
    public static long minsleep=0; //transaction min sleep once,ms
    public static long maxsleep=0; //transaction max sleep once,ms
    public static long starttm=0;  // start time
    public static boolean tmend=false;
    public static int inc_threads=0;
    public static int max_threads=Runtime.getRuntime().availableProcessors()*2;
    public static int inc_interval=60;
    public static int txcnt=0;    // transaction count
    public static int qrcnt=0;
    public static int rdcnt=0;
    public static int wrcnt=0;
    public static int otcnt=0;
    public static int ercnt=0;
    public static long latms=0; //latency,ms
    public static int rccnt=0;  //reconnect
    public static thread worker[] = new thread[1024];
    public static int mxid[]=null; //table max id value
    public static Object lck[] = new Object[1024];
    public static boolean IsDameng=false;
    public static boolean InitInsIdx=false;
    public static boolean bBindVal=true;

    public static void main (String[] args) throws Exception {
        int i=0;
        int portno = 0;
        String hoststr=null;
        String dbstr=null;
        int args_pcnt[]=null;
        int run_cnt=0;
        
        args_pcnt=new int[args.length];
        //parse program arguments
        for(i=0;i<args.length;i++)
        {
            args_pcnt[i]=0;
            if(args[i].contains("--db_driver=")){
                args_pcnt[i]++;
                dbdriver=args[i].replace("--db_driver=","");
            }
            if(args[i].contains("--host=")){
                args_pcnt[i]++;
                hoststr=args[i].replace("--host=","");
            }      
            if(args[i].contains("--port=")){
                args_pcnt[i]++;
                portno =Integer.parseInt(args[i].replace("--port=",""));
            }      
            if(args[i].contains("--dbname=")){
                args_pcnt[i]++;
                dbstr  =args[i].replace("--dbname=","");
            }
            if(args[i].contains("--user=")){
                args_pcnt[i]++;
                username=args[i].replace("--user=","");
            }
            if(args[i].contains("--password=")){
                args_pcnt[i]++;
                password=args[i].replace("--password=","");
            }
            if(args[i].contains("--jdbc_props=")){
                args_pcnt[i]++;
                jdbc_props=args[i].replace("--jdbc_props=","");
            }
            if(args[i].contains("--tables=")){
                args_pcnt[i]++;
                tables=Integer.parseInt(args[i].replace("--tables=",""));
            }
            if(args[i].contains("--table_size=")){
                args_pcnt[i]++;
                table_size=Integer.parseInt(args[i].replace("--table_size=",""));
            }
            if(args[i].contains("--test_mode=")){
                args_pcnt[i]++;
                test_mode=args[i].replace("--test_mode=","");
            }
            if(args[i].contains("--threads=")){
                args_pcnt[i]++;
                threadcnt=Integer.parseInt(args[i].replace("--threads=",""));
            }
            if(args[i].contains("--time=")){
                args_pcnt[i]++;
                duration=Integer.parseInt(args[i].replace("--time=",""));
            }
            if(args[i].contains("--report_interval=")){
                args_pcnt[i]++;
                rptinterval=Integer.parseInt(args[i].replace("--report_interval=",""));
                if(rptinterval==0) rptinterval=3600;
            }
            if(args[i].contains("--skip_trx=on")){
                args_pcnt[i]++;
                common.skip_trx=true;
            }
            if(args[i].contains("--record_count=")){
                args_pcnt[i]++;
                common.record_count=Integer.parseInt(args[i].replace("--record_count=",""));
            }
            if(args[i].contains("--commit_size=")){
                args_pcnt[i]++;
                common.commit_size=Integer.parseInt(args[i].replace("--commit_size=",""));
            }
            if(args[i].contains("--db_debug=on")){
                args_pcnt[i]++;
                db_debug=true;
            }
            if(args[i].contains("--bind_val=off")){
                args_pcnt[i]++;
                bBindVal=false;
            }
            if(args[i].equals("prepare") || args[i].equals("run") || args[i].equals("cleanup") ){
                args_pcnt[i]++;
                oper = args[i];
            }
            

            if(args[i].contains("--inc_threads=")){
                args_pcnt[i]++;
                inc_threads=Integer.parseInt(args[i].replace("--inc_threads=",""));
            }
            if(args[i].contains("--max_threads=")){
                args_pcnt[i]++;
                max_threads=Integer.parseInt(args[i].replace("--max_threads=",""));
            }
            if(args[i].contains("--inc_interval=")){
                args_pcnt[i]++;
                inc_interval=Integer.parseInt(args[i].replace("--inc_interval=",""));
            }
            if(args[i].contains("--min_sleep=")){
                args_pcnt[i]++;
                minsleep=Long.parseLong(args[i].replace("--min_sleep=",""));
            }
            if(args[i].contains("--max_sleep=")){
                args_pcnt[i]++;
                maxsleep=Long.parseLong(args[i].replace("--max_sleep=",""));
            }

            if(args[i].contains("--help")){
                args_pcnt[i]++;
                pubfunc.usage();
                return;
            }
        }

        int iUnkownParam=0;
        for(i=0;i<args.length;i++)
        {
            if(args_pcnt[i]==0){
                System.out.println("Unknown parameter : "+args[i]);
                iUnkownParam=1;
                return;
            }
        }
        if(iUnkownParam>0) pubfunc.usage();
        
        if(maxsleep<minsleep) maxsleep=minsleep;

        //Set JDBC string
        if(dbdriver.equals("mysql"))
        {
            lang_syntax="mysql";
            if(portno==0)     portno=3306;
            if(hoststr==null) hoststr="localhost";
            if(dbstr==null)   dbstr="test";
            drv = "com.mysql.jdbc.Driver";
            //url = "jdbc:mysql://localhost:3306/test?useServerPrepStmts=true&cachePrepStmts=true&useLocalSessionState=true";
            url = "jdbc:mysql://"+hoststr+":"+portno+"/"+dbstr;
        }
        if(dbdriver.equals("oracle"))
        {
            if(portno==0)     portno=1521;
            if(hoststr==null) hoststr="localhost";
            if(dbstr==null)   dbstr="orcl";
            drv = "oracle.jdbc.driver.OracleDriver";
            url = "jdbc:oracle:thin:@"+hoststr+":"+portno+":"+dbstr;
        }
        if(dbdriver.equals("oceanbase"))
        {
            if(portno==0)     portno=2881;
            if(hoststr==null) hoststr="localhost";
            if(dbstr==null)   dbstr="SYS";
            drv = "com.alipay.oceanbase.obproxy.mysql.jdbc.Driver";
            url = "jdbc:oceanbase://"+hoststr+":"+portno+"/"+dbstr;
            //Get oceanbase tenant mode
            pubfunc.get_oceanbase_mode();
        }
        if(dbdriver.equals("opengauss") || dbdriver.equals("vastbase"))
        {
            if(portno==0)     portno=5432;
            if(hoststr==null) hoststr="localhost";
            if(dbstr==null)   dbstr="postgres";
            drv = "org.postgresql.Driver";
            url = "jdbc:postgresql://"+hoststr+":"+portno+"/"+dbstr;
        }
        if(dbdriver.equals("dameng"))
        {
            if(portno==0)     portno=5236;
            if(hoststr==null) hoststr="localhost";
            if(dbstr==null)   dbstr="DAMENG";
            drv = "dm.jdbc.driver.DmDriver";
            url = "jdbc:dm://"+hoststr+":"+portno+"/"+dbstr;
            IsDameng=true;
        }
        if(jdbc_props!=null) url=url+"?"+jdbc_props;
        
        if(drv==null) System.out.println("\nUnknown database driver!\n");
        if(oper==null) System.out.println("\nUnknown sqlbench operation!\n");
        if(drv==null || oper==null) {
            pubfunc.usage();
            return;
        }
        System.out.println("Database Driver: "+dbdriver);
        System.out.println("Database Language: "+lang_syntax);

        if(lang_syntax.equals("oracle"))
        {
            common.pre_begin=false;   
        }
        if(test_mode.equals("bulk_insert") && max_threads>tables && oper.equals("run"))
        {
            max_threads=tables;
            System.out.println("Mode bulk_insert set max_threads to tables!");
        }
        //mxid=new int[max_threads>tables?max_threads:tables];
        mxid=new int[tables];
        for(i=0;i<tables;i++)
        {
            lck[i]=new Object();
        }
        
        starttm=System.currentTimeMillis();
        
        for(i=0;i<threadcnt;i++)
        {
            worker[i]=new thread();
            worker[i].start();
        }
        //InitInsIdx=true;
        
        while(oper.equals("run"))
        {
            if((System.currentTimeMillis()-starttm)/1000>duration)
            {
                tmend=true;
                break;
            }
            Thread.sleep(1000);
            if(inc_threads>0 && threadcnt<max_threads){
                if(((System.currentTimeMillis()-starttm)/1000)%inc_interval == 0){
                    for(i=0;i<inc_threads;i++)
                    {
                        worker[threadcnt]=new thread();
                        worker[threadcnt].start();
                        threadcnt++;
                    }
                }
            }
            txcnt=0;
            qrcnt=0;
            rdcnt=0;
            wrcnt=0;
            otcnt=0;
            ercnt=0;
            latms=0;
            if(((System.currentTimeMillis()-starttm)/1000)%rptinterval == 0){
                run_cnt=0;
                for(i=0;i<threadcnt;i++)
                {
                    if(worker[i].run) run_cnt++;
                    txcnt+=worker[i].txcnt;worker[i].txcnt=0;
                    qrcnt+=worker[i].qrcnt;worker[i].qrcnt=0;
                    rdcnt+=worker[i].rdcnt;worker[i].rdcnt=0;
                    wrcnt+=worker[i].wrcnt;worker[i].wrcnt=0;
                    otcnt+=worker[i].otcnt;worker[i].otcnt=0;
                    ercnt+=worker[i].ercnt;worker[i].ercnt=0;
                    latms+=worker[i].latms;worker[i].latms=0;
                    rccnt+=worker[i].rccnt;worker[i].rccnt=0;
                }
                System.out.println("[ "+((System.currentTimeMillis()-starttm)/1000)+
                "s ] thds: "+run_cnt+" tps: "+txcnt+" qps: "+qrcnt+" (r/w/o: "+rdcnt+"/"+
                wrcnt+"/"+otcnt+") lat(ms): "+String.format("%.2f", (float)latms/txcnt)+
                " err/s: "+ercnt+" reconn/s: "+rccnt);
                if(run_cnt==0) break;
            }
        }
    }
    //public static Object lock = new Object();
    public synchronized static int get_next_id(int tabid) {        
        //synchronized(lck[tabid]){
            mxid[tabid]++;
            return mxid[tabid];
        //}
    }
}
