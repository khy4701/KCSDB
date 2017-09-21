

import java.util.Calendar;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class ClientSim extends Thread
{
	private static ClientSim[] clientSim;
    
    public ClientSim() {}
    
    private static String ipAddress;
    private static String operator;
    private static String []params;
    private static String param1;
    private static String param2;
    private static String param3;
    
    private static int tps;
    private static int threadNum;
    public static int sendLimit = 0;
    public static int totalSendNum = 0;
    public static int totalRecvNum = 0;
    public static int sndTotal   = 0;
    public static int rcvTotal   = 0;    
    public static int res200 = 0;
    public static int res403 = 0;
    public static int res503 = 0;
    public static int res400 = 0;
    public static int stopFlag = 0;
    public static long startSubs = 0;
    public static long endSubs = 0;
    
    /***
     * @param args
     * args[0] : Thread Num
     * args[1] : IP Address
     * args[2] : MDN
     * args[3] : Date
     */
    public static void main(String[] args) {
        if(args.length < 6) {
            System.out.println("Parameter1 : Thread Num");
            System.out.println("Parameter2 : IP Address");
            System.out.println("Parameter3 : QUERY_CCSINFO/UPT_DELFLAG");
            System.out.println("Parameter4 : Param1");
            System.out.println("Parameter5 : Param2");
            System.out.println("Parameter6 : TPS");
            System.out.println("Parameter7 : SEND LIMIT");
        }
        
//        threadNum = Integer.parseInt(args[0]);
//        ipAddress = args[1];
//        operator = args[2];
//        param1 = args[3];
//        param2 = args[4];
//        
//        clientSim = new ClientSim[threadNum];
//        tps = Integer.parseInt(args[5]);
//        sendLimit = Integer.parseInt(args[6]);
//        
//        if(operator.equals("QUERY_CCSINFO")){
//            param3 = args[7];
//
//        	startSubs = Long.parseLong(param1);        	
//        	endSubs = Long.parseLong(param3);
//        }else
//        	endSubs = 1099999999;
        
        
//        for(int i=0; i<clientSim.length; i++) {
//            clientSim[i] = new ClientSim();
//            clientSim[i].start();
//        }
        
        System.out.println("Working Directory = " +         System.getProperty("user.dir"));
        
        threadNum = Integer.parseInt(SimProperty.getPropPath("THREADNUM"));
        ipAddress = SimProperty.getPropPath("IP");
        tps		  = Integer.parseInt(SimProperty.getPropPath("TPS"));
        sendLimit = Integer.parseInt(SimProperty.getPropPath("MAX_SNDCNT"));
        
        startSubs = Long.parseLong(SimProperty.getPropPath("START_SUBS"));
        endSubs   = Long.parseLong(SimProperty.getPropPath("END_SUBS"));
        operator  = SimProperty.getPropPath("OPERATOR");
        
        params    = SimProperty.getPropPath("PARAMS").split(" ");
        param1    = params[0];
        param2    = params[1];
        
        
        System.out.println("---------------Start Client Simulator---------------");
        System.out.println("[Thread Num] : " + threadNum);
        System.out.println("[IP Address] : " + ipAddress);
        System.out.println("[Operator  ] : " + operator);
        System.out.println("[Param1    ] : " + params);
        System.out.println("[Param1    ] : " + param1);
        System.out.println("[Param2    ] : " + param2);
        System.out.println("[Tps       ] : " + tps);
        System.out.println("[SendLimit ] : " + sendLimit);
        

        int now, old, ret;
        now = Calendar.getInstance().get(Calendar.SECOND);
        old = now;

        
        
//        while(true){
//        	
//        	now = Calendar.getInstance().get(Calendar.SECOND);
//        	//if( old != now ){
//        	if( now != old){
//        		old = now ; 
//        		
//        		ret = plusTps(0);        
//            	if (ClientSim.stopFlag == 1){
//            		
//            		for(int i = 0; i < clientSim.length ; i++){
//            			clientSim[i].interrupt();
//            		}
//            	}            		
//        	}
//        	
//        	try {
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//        }
    }

    @Override
    public void run() {
    	int ret;
        Client client = Client.create();
        WebResource webResource = null;
        ClientResponse response = null;
        
        String url ;
        if(operator.equals("QUERY_CCSINFO"))
        	url = "http://"+ipAddress+"/kcsdbAPI/v1/query";
        else
        	url = "http://"+ipAddress+"/kcsdbAPI/v1/updateDeleteFlag";
                       
        int i = 0 ;        
        while(true) {
        	              
        	try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				break;
			}
        	
        	if( ClientSim.endSubs < ClientSim.startSubs ){
        		ClientSim.startSubs = Long.parseLong(param1);
        	}
        	
    		if ( ClientSim.totalSendNum >= ClientSim.sendLimit){    			
    			ClientSim.stopFlag = 1;
    			break;
    		}
    		
        	if( ClientSim.sndTotal >= tps  ){              		
        		//System.out.println("Send TPS TOTAL["+ClientSim.sndTotal+"]");
        		        		
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					break;
				}

        		continue;
        	}
        	
            ret = plusTps(1);         
            if ( ret == -1 )
            	continue;

            webResource = client.resource(url);

            webResource.header("Content-Type", "application/json;charset=UTF-8");
            
            if(operator.equals("QUERY_CCSINFO")){
        		String startStr = new String("010"+startSubs);
				String input = "{\"CDMDN\":\"" + startStr + "\",\"CCM_TIME\":\"" + param2 + "\"}";
				response = webResource.type("application/json").post(ClientResponse.class, input);
            }else{
				String input = "{\"CCM_REFERID\":\"" + param1 + "\",\"CCM_TIME\":\"" + param2 + "\"}";
				response = webResource.type("application/json").put(ClientResponse.class, input);
            }
            
            plusTps(2);

            int resCode = response.getStatus();
            
            if(resCode == 200)
            	res200++;
            else if (resCode == 403)
            	res403++;
            else if (resCode == 503)
            	res503++;
            else
            	res400++;     
            
         }
    }
    
    public static synchronized int plusTps(int flag){
    	    	
    	if (flag == 1){    		
    		    		
    		if( ClientSim.sndTotal >= tps )
    			return -1;
    		
    		ClientSim.sndTotal++;
    		ClientSim.totalSendNum++;
    		
    	}    	
    	else if (flag == 2){
    		ClientSim.startSubs++;
            ClientSim.totalRecvNum++;
            ClientSim.rcvTotal++;

    	}
    	else if (flag == 0){    		
    		
    		if(operator.equals("QUERY_CCSINFO"))
    			System.out.print(String.format("TPS[S:%-4d | R:%-4d] TOT[S:%-4d | R:%-4d]  = 200[%-4d] 400[%-4d] 403[%-4d] 503[%-4d] SUBS[%-4d]\n",
    				ClientSim.sndTotal, ClientSim.rcvTotal, ClientSim.totalSendNum, ClientSim.totalRecvNum, res200, res400 ,res403, res503, ClientSim.startSubs ));
    		else
    			System.out.print(String.format("TPS[S:%-4d | R:%-4d] TOT[S:%-4d | R:%-4d]  = 200[%-4d] 400[%-4d] 403[%-4d] 503[%-4d] \n",
    					ClientSim.sndTotal, ClientSim.rcvTotal, ClientSim.totalSendNum, ClientSim.totalRecvNum, res200, res400 ,res403, res503));	
    		
    		ClientSim.sndTotal = 0;
    		ClientSim.rcvTotal = 0;
    		res200 = 0;
    		res400 = 0;
    		res403 = 0;
    		res503 = 0;    		
    	}
    	return 1;
    }    	 	
    
}

