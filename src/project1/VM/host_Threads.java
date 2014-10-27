package project1.VM;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import com.vmware.vim25.HostRuntimeInfo;
import com.vmware.vim25.HostSystemPowerState;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.VirtualMachineRuntimeInfo;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;
public class host_Threads extends Thread{
		HostSystem host;
		ServiceInstance si,siadmin;
		URL url,urladmin;
		ManagedEntity[] mes,mesadmin;
		Folder rootFolderadmin,rootFolder;
		
		public host_Threads(){}
		public host_Threads(HostSystem h)throws Exception
		{
			this.host=h;
		}
		public void run()
		{
			   //System.out.println("checking all host");
			   try {
				   URL urladmin1 = new URL("https://130.65.132.14/sdk");
					ServiceInstance siadmin1 = new ServiceInstance(urladmin1, "administrator", "12!@qwQW", true);
					Folder rootFolderadmin1 = siadmin1.getRootFolder();
			    	ManagedEntity [] mesadmin1 = new InventoryNavigator(rootFolderadmin1).searchManagedEntities("VirtualMachine");
				  
				   System.out.println("====================================");
				   	boolean pingResult=functions_vm.pingHost(host);
					System.out.println(host.getName()+" :host ping result: "+ pingResult);
					System.out.println("====================================");
					if(pingResult==true){
											   
							boolean take_host_snap=functions_vm.takeHostSnapshot(mesadmin1,host);
						   	if(take_host_snap==false)
							{
					    		System.out.println("Something went wrong. Couldn't take snapshot for host: "+host.getName());
							} else
							{
								System.out.println("Snapshot taken for host: "+host.getName());
							}
							
							//Threads for each VM starts
							takeSnapShot snapshots=new takeSnapShot(host);
							VmThread pingVm = new VmThread(host);
							
							//snapshots.start();
							//System.out.println("test1");
							pingVm.start();		
							//	System.out.println("test2");
							//sleep(720000);
							
					   }	
				
					 if(pingResult==false)
					{						
						System.out.println("====================================");
						System.out.println("ping failed for host::"+host.getName()+": trying to power on");
						System.out.println("====================================");						
						
				    	int i=0;
				    	
				    	while(i<3 && pingResult==false)
				    	{
				    		
				    		HostRuntimeInfo hri=host.getRuntime();
							String state=hri.getPowerState().toString();
							System.out.println(state);
							
							if(functions_vm.checkHostPowerState(host)==false){
				    			//System.out.println("test2");	
				    		System.out.println("====================================");
				    		System.out.println("Error with::" +this.host.getName()+":powering on host failed" );   		
				    		System.out.println("====================================");
				    		System.out.println("====================================");
				    		System.out.println("trying to power on host:"+host.getName()+i);
				    		System.out.println("====================================");
				    		functions_vm.powerOnHost(mesadmin1, host);
				    		i++;
				    		sleep(4000000);
				    		pingResult =	functions_vm.pingHost(host);
				    		}
				    		
				    	}
				    	if(pingResult==false){
				    			System.out.println("==================================================");
				    			System.out.println("ping host:"+ host.getName()+"failed:poweing on host failed");
				    			System.out.println("reverting the host snapshot" );
				    			System.out.println("===================================================");
				    			boolean revert_snapshot=functions_vm.revertHostSnapshot(mesadmin1,host);
				    			System.out.println("waiting for changes to take effect!!!");
				    			sleep(25000);
				    			if(revert_snapshot==true)
								{
						    		System.out.println("Reverting to older snapshot for host: "+host.getName());
						    	} else
								{
						    	System.out.println("reverting to previous host snapshot failed");	
								System.out.println("now migrating all VMs");

								URL urladmin = new URL("https://130.65.132.113/sdk");
								ServiceInstance si = new ServiceInstance(urladmin, "administrator", "12!@qwQW", true);
								Folder rootFolder = siadmin.getRootFolder();
						    	ManagedEntity [] mes = new InventoryNavigator(rootFolderadmin).searchManagedEntities("HostSystem");
						    	VirtualMachine vm[]=host.getVms();
						    	for(int k= 0;k<mes.length;k++){
									HostSystem newhost=(HostSystem) mes[k];
									VirtualMachine vm2[]=newhost.getVms();
									if(newhost!= host && functions_vm.pingHost(newhost)== true){
										if(vm2.length<2){
											System.out.println("calling migrate");
											for(int l =0;l<vm.length;l++){
											functions_vm.MigrateVM(si, vm[l], rootFolder,newhost);
											}
											break;
											}	
										}
									else{
										System.out.println("sorry no host available to migrate");
									}
								}
								}
				    		}
				    	
						}
			   	}
								   
			    catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			   
			   }
			   
		}
	


}
