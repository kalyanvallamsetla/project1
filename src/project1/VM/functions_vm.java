package project1.VM;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;



















import com.vmware.vim25.Action;
import com.vmware.vim25.AlarmAction;
import com.vmware.vim25.AlarmSetting;
import com.vmware.vim25.AlarmSpec;
import com.vmware.vim25.AlarmTriggeringAction;
import com.vmware.vim25.GroupAlarmAction;
import com.vmware.vim25.HostRuntimeInfo;
import com.vmware.vim25.HostVMotionCompatibility;
import com.vmware.vim25.MethodAction;
import com.vmware.vim25.MethodActionArgument;
import com.vmware.vim25.SendEmailAction;
import com.vmware.vim25.StateAlarmExpression;
import com.vmware.vim25.StateAlarmOperator;
import com.vmware.vim25.TaskInfo;
import com.vmware.vim25.VirtualMachineCloneSpec;
import com.vmware.vim25.VirtualMachineMovePriority;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.VirtualMachineRelocateSpec;
import com.vmware.vim25.mo.Alarm;
import com.vmware.vim25.mo.AlarmManager;
import com.vmware.vim25.mo.ComputeResource;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;
import com.vmware.vim25.HostSystemPowerState;



public class functions_vm {

	private String vmname ;
    private ServiceInstance si ;
    private static VirtualMachine vm ;
    private Datacenter dc;
    private String cloneName ;
	private String vmPath ;
	private Folder rootFolder;
	private static HostSystem host;
	//private String hostName;
	Scanner s =  new Scanner(System.in);  
	        
	    
	    public static void powerOnHost( ManagedEntity mes[], HostSystem host) throws Exception
	    {
	    	String str = host.getName().toString();
	    	String str1 = str.substring(7);
	    	//System.out.println(str1);
	    	for (int i =0;i < mes.length; i ++)
	    	{
	    		//System.out.println("test in ponh");
	    	VirtualMachine vm= (VirtualMachine)mes[i];
	    	
	    		String vmName = vm.getName(); 
	    		if (vmName.contains(str1))
	    		{
	    			powerOnVM(vm);
	    			return;
	    				
	    		}
    			    		
	    	}
		    	
	    }
	    public static void powerOnVM(VirtualMachine vm) throws Exception
	    {
	    	try{
	    	Task task = vm.powerOnVM_Task(null);
	    	String status = task.waitForMe();
	        if(status==Task.SUCCESS)
	        {
	          System.out.println(vm.getName() + " VM Powered ON Successfully.");
	        }
	        else
	        {
	          System.out.println("Failure to power ON");
	        }
	    	}catch (Exception e)
	    	{
	    		e.printStackTrace();
	    	}
	    	
	    }

//=================================check host power state=====================
	    public static boolean checkHostPowerState(HostSystem host)
		{
			boolean flag = true;
			HostRuntimeInfo hri=host.getRuntime();
			String state=hri.getPowerState().toString();
			
			
			if(hri.getPowerState() == HostSystemPowerState.poweredOff)
			{
				System.out.println("host::"+host.getName()+"::state::"+state);
				flag = false;
			}
			if(hri.getPowerState()== HostSystemPowerState.unknown)
			{
				System.out.println("host::"+host.getName()+"::state::"+state);
		    	flag = false;
			}
			return flag;
		}

		
	//========PING HOST=====================================================    
	    public static boolean pingHost(HostSystem host) throws Exception{
	    	boolean pingResult=false;
	    	String consoleResult="";
	 		System.out.println("ping started for: "+host.getName());
	 		String pingCmd = "ping " + host.getName();

 			
	 			Runtime r = Runtime.getRuntime();
	 			Process p = r.exec(pingCmd);

	 			BufferedReader in = new BufferedReader(new
	 			InputStreamReader(p.getInputStream()));
	 			String inputLine;
	 			
	 			while ((inputLine = in.readLine()) != null) {
	 			System.out.println(inputLine);
	 			consoleResult+=inputLine;
	 			}
	 			if(consoleResult.contains("Request timed out"))
	 			{
	 				System.out.println("Packets Dropped");
	 				pingResult=false;
	 				
	 			}
	 			else
	 			{
	 				//ping successful
	 				System.out.println("ping success in vhost");
	 				pingResult=true;
	 				
	 			}
	 			return pingResult;
	 	}
	   //========================VM STATUS===================================
	    
	    public static void vmStatus(VirtualMachine vm) throws Exception{
			System.out.println("*****************************************");
			System.out.println(vm.getName()+" is Alive");
			System.out.println("CPU Usage of Virtual Machine "+vm.getSummary().getQuickStats().overallCpuUsage);
			System.out.println("Memory Usage of Virtual Machine "+vm.getSummary().getQuickStats().getGuestMemoryUsage());
			System.out.println(vm.getNetworks());
			System.out.println("*****************************************");
		}
		
		//====================Take a snapshot===================================
		public static void takeSnapshot(VirtualMachine vm) throws Exception
		{
				if (vm != null)
				{
					String Vm_Name = vm.getName();
					System.out.println("------------------------------------------");
					System.out.println("Snapshot for " + Vm_Name);

					try 
					{
						vm.removeAllSnapshots_Task();						
						Task task = vm.createSnapshot_Task("Snapshot Name: "+Vm_Name,"Snapshot for " + Vm_Name, false, false);
						
						String status = null;
						status = task.waitForTask();
						System.out.println(task.getServerConnection());
						if (status.equalsIgnoreCase(Task.SUCCESS)) 
						{
							System.out.println("VM cloned");
							System.out.println("Snapshot taken successfully -> " + Vm_Name);
							System.out.println("**********************************************");
							System.out.println("Current snapshot updated for " + Vm_Name);
						}
						else 
						{
							System.out.println("Error, VM not cloned!"+vm.getName());
							TaskInfo info = task.getTaskInfo();
							System.out.println(info.getError().getFault());
							throw new RuntimeException("Error while cloning VM");
						}

					} 
					
					catch (Exception e) 
					{
						e.printStackTrace();
					}

					
				}
			}
		//=========================PING VM=========================================
		public static boolean pingVirtualMachine(VirtualMachine vm) throws Exception{
			   boolean pingResult=false;
	  	       String consoleResult="";
			   System.out.println("Thread started for: "+vm.getName());
			   System.out.println("*** "+vm.getName()+" its ip address is: "+vm.getGuest().getIpAddress()+" ***");
			   
			   //before pinging check if IP address is available
			   if(vm.getGuest().getIpAddress()==null){
				   System.out.println("CHECKING IF NULL IP IS RETURNED AS FALSE::"+vm.getName());
				   pingResult=false;
				   return pingResult;
			   }
			   else{
			   
			   String pingCmd = "ping " + vm.getGuest().getIpAddress();

				Runtime r = Runtime.getRuntime();
				Process p = r.exec(pingCmd);

				BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String inputLine;
				
				while ((inputLine = in.readLine()) != null) {
				//System.out.println(inputLine);
				consoleResult+=inputLine;
				}
				if(consoleResult.contains("Request timed out"))
				{
					System.out.println("Packets Dropped::"+vm.getName());
					pingResult=false;
					//flag=false;
				}
				else
				{
					//ping successful
					System.out.println("ping success in vm:"+vm.getName());
					pingResult=true;
					
				}
			   }
				return pingResult;
		}
					
	///// ALARM MANAGER CODE ///
		public static void setalarm(VirtualMachine vm, ServiceInstance si) throws Exception
		{
			System.out.println("setting alarm for "+ vm.getName());
			  AlarmManager alarmMgr = si.getAlarmManager();
			      
			    //This will remove alarm if it exists...
			  try{ 
			  Alarm alarms[]=alarmMgr.getAlarm(vm);
			    for(int i=0;i<alarms.length;i++)
			    {
			    		alarms[i].removeAlarm();
			    }
			  } 
			  catch(Exception e)
		    	{
		    		System.out.println(e.toString());
				    AlarmSpec spec = new AlarmSpec();
				    
				    StateAlarmExpression expression = createStateAlarmExpression();
				    AlarmAction methodAction = createAlarmTriggerAction(createPowerOnAction());
				    GroupAlarmAction gaa = new GroupAlarmAction();

				    //gaa.setAction(new AlarmAction[]{emailAction, methodAction});
				    spec.setAction(gaa);
				    spec.setExpression(expression);
				    //Date date = new Date();
				    spec.setName(vm.getName().toString()+":"+new Date().toString());
				    spec.setDescription("Monitor VM state and send email " + "and power it on if VM powers off");
				    spec.setEnabled(true);    
				    //spec.setAction(emailAction);
				   
				  		    
				    AlarmSetting as = new AlarmSetting();
				    as.setReportingFrequency(0); //as often as possible
				    as.setToleranceRange(0);
				    
				    spec.setSetting(as);
				    try{
				    alarmMgr.createAlarm(vm, spec);
				    }catch (Exception x)
				    {
				    	
				    	System.out.println("Duplicate alarm name error for: "+vm.getName());
				    	System.out.println(e.getClass().getName());
				    	//e.printStackTrace();
				    	if(x instanceof com.vmware.vim25.DuplicateName )
				    	{
				    		System.out.println("Alarm already exists for: "+vm.getName());
				    		
				    	}
				    }
		    	}
			    AlarmSpec spec = new AlarmSpec();
			    
			    StateAlarmExpression expression = createStateAlarmExpression();
			    AlarmAction methodAction = createAlarmTriggerAction(createPowerOnAction());
			    GroupAlarmAction gaa = new GroupAlarmAction();

			    //gaa.setAction(new AlarmAction[]{emailAction, methodAction});
			    spec.setAction(gaa);
			    spec.setExpression(expression);
			    //Date date = new Date();
			    spec.setName(vm.getName().toString()+":"+new Date().toString());
			    spec.setDescription("Monitor VM state and send email " + "and power it on if VM powers off");
			    spec.setEnabled(true);    
			    //spec.setAction(emailAction);
			   
			  		    
			    AlarmSetting as = new AlarmSetting();
			    as.setReportingFrequency(0); //as often as possible
			    as.setToleranceRange(0);
			    
			    spec.setSetting(as);
			    try{
			    alarmMgr.createAlarm(vm, spec);
			    }catch (Exception e)
			    {
			    	
			    	System.out.println("Duplicate alarm name error for: "+vm.getName());
			    	System.out.println(e.getClass().getName());
			    	//e.printStackTrace();
			    	if(e instanceof com.vmware.vim25.DuplicateName )
			    	{
			    		System.out.println("Alarm already exists for: "+vm.getName());
			    		
			    	}
			    }
			  }

			 public static StateAlarmExpression createStateAlarmExpression()
			  {
			    StateAlarmExpression expression = 
			      new StateAlarmExpression();
			    expression.setType("VirtualMachine");
			    expression.setStatePath("runtime.powerState");
			    expression.setOperator(StateAlarmOperator.isEqual);
			    expression.setRed("poweredOff");
			    return expression;
			  }

			  public static MethodAction createPowerOnAction() 
			  {
			    MethodAction action = new MethodAction();
			    action.setName("PowerOffVM_Task");
			    MethodActionArgument argument = new MethodActionArgument();
			    argument.setValue(null);
			    action.setArgument(new MethodActionArgument[] { argument });
			    return action;
			  }
			  
			  

			  public static AlarmTriggeringAction createAlarmTriggerAction(
			      Action action) 
			  {
			    AlarmTriggeringAction alarmAction = 
			      new AlarmTriggeringAction();
			    alarmAction.setYellow2red(true);
			    alarmAction.setAction(action);
			    return alarmAction;
			  }
		
		
		////////////////////// GET ALARM STATUS ////
		public static boolean getalarm(ServiceInstance si,VirtualMachine vm)
	    {
	    	boolean res=false;
	    	AlarmManager a = si.getAlarmManager();

	    	try
	    	{
	    		//System.out.println("CP1 for "+vm.getName());
	    		Alarm [] alarms= a.getAlarm(vm);
	    		//System.out.println("CP2 for "+vm.getName());
	    		//System.out.println("Number of alarms set for this vm is : "+ alarms.length);
	    		if(alarms.length>0)
	    		{
	    			System.out.println("CP3 for "+vm.getName());
	    			boolean al= false;
	    			for(int i=0;i<alarms.length;i++)
	    			{
	    				String name=alarms[i].getAlarmInfo().getName();
	    				if(name.equals("PoweredOff"))
	    				{
	    					System.out.println("Required Alarm Found");
	    					
	    					al=true;
	    					res=true;
	    					//System.out.println("Res = " + res);
	    					return res;
	    				}
	      			}
	    			
	    			if(al==false)
	    			{
	    				//set alarm
	    				System.out.println("no alarms match the required one.. setting new alarm..");
	    				return res=false;
	    				//setalarm();
	    			}
	    			
	    		}
	    		else
	    		{
	    			
	    			System.out.println("No alarms set. Setting new alarm");
	    			return res=false;
	    			//call set alarm
	    			//setalarm();
	    			//getalarm();
	    		}
	    	}
	    	catch(Exception e)
	    	{
	    		System.out.println(e.toString());
	    	}
	  
	    	return res;
	    }
	    		//=========================REVERT SNAPSHOT===============================
		public static boolean revertToSnapshot(VirtualMachine vm)
	    {
	    	System.out.println("reverting VM "+vm.getName());
	    	try
	    	{
	    		System.out.println("Revert CP1");
	    		Task t1 = vm.getCurrentSnapShot().revertToSnapshot_Task(null);
	    		System.out.println("Revert CP2");
	    		System.out.println("reverting VM "+vm.getName());
	    		vm.getCurrentSnapShot().toString();
	    		if(t1.waitForTask()==Task.SUCCESS)
	    			return true;
	    		else return false;
	    	
	    	}
	    	catch(Exception e)
	    	{
	    		System.out.println(e.toString());
	    	}
	    	return false;
	    }
		//=================================revert host===================================
		public static boolean revertHostSnapshot(ManagedEntity[] mes,HostSystem host)
	    {	boolean result=false;
	    	System.out.println("in revert");
	    	try
	    	{
	   
	    		String str = host.getName().toString();
	    		String str1 = str.substring(7);
	    		//System.out.println("String to be found"+str1);
	    		
//	    		System.out.println("Get all the Host System");
	    		for (int i =0;i < mes.length; i ++)
	    		{
	    		VirtualMachine vm= (VirtualMachine)mes[i];
	    		//	System.out.println(vm.getName());
	    			String vmName = vm.getName();
	    			if (vmName.contains(str1))
	    			{
	    				
	    				//System.out.println("Host in vm"+host.getName());
	    				Task t1 = vm.getCurrentSnapShot().revertToSnapshot_Task(null);
	    	    		vm.getCurrentSnapShot().toString();
	    				System.out.println("Reverting Host Snapshot for: "+vm.getName());
	    				if(t1.waitForTask()==t1.SUCCESS)
	    	    			result=true;
	    	    		else
	    	    			result=false;
	    				
	    			}
	    		}
	    		
	    	}
	    	catch(Exception e)
	    	{
	    		System.out.println(e.toString());
	    		result=false;
	    		return result;
	    	}
	    	return result;
	    }
	    
		//============================MIGRATE VM====================================
		public static void MigrateVM(ServiceInstance si,VirtualMachine vmName,Folder rootFolder,HostSystem host) throws Exception
		{
//			ComputeResource cr = (ComputeResource) host.getParent();			    
//		    Task task = vmName.migrateVM_Task(cr.getResourcePool(), host,
//		        VirtualMachineMovePriority.highPriority, 
//		        VirtualMachinePowerState.poweredOff);
//		  
//			try{				
//				System.out.println("TO HOST: "+host.getName());			    
//			    if(task.waitForMe()==Task.SUCCESS)
//			    {
//			      System.out.println("Cold Migration successful!");
//			    }
//			    
//			} catch ( Exception e ) 
//	        { 
//				
//				  System.out.println("Something went wrong!");
//			      TaskInfo info = task.getTaskInfo();
//			      System.out.println(info.getError().getFault());
//			      System.out.println( e.toString() ) ; 
//			      System.out.println("inside catch of vm migrate");
//	        }
		}
//==============================take host snapshot===============================
		public static boolean takeHostSnapshot(ManagedEntity[] mes,HostSystem host)
	    {
	    	try
	    	{	    		
	    		String str = host.getName().toString();
	    		String str1 = str.substring(7);
	    		//System.out.println("String to be found"+str1);
	    		
//	    		System.out.println("Get all the Host System");
	    		for (int i =0;i < mes.length; i ++)
	    		{
	    		VirtualMachine vm= (VirtualMachine)mes[i];
	    		//	System.out.println(vm.getName());
	    			String vmName = vm.getName();
	    			if (vmName.contains(str1))
	    			{
	    				
	    				//System.out.println("Host in vm"+host.getName());
	    				vm.removeAllSnapshots_Task();
	    				vm.createSnapshot_Task("Snapshot Name: "+vm.getName(),"Snapshot for " + vm.getName(), false, false);
	    				System.out.println("Host Snapshot taken for: "+vm.getName());
	    				return true;
	    					
	    			}
	    		}
	    		System.out.println("Sorry could not take snapshot for host: "+host.getName());
	    		return false;
	    	}
	    	catch(Exception e)
	    	{
	    		System.out.println(e.toString());
	    	}
			return false;
	    }
}
