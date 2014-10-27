package project1.VM;

import java.net.URL;

import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.VirtualMachineRuntimeInfo;
import com.vmware.vim25.VirtualMachineSnapshotTree;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;
import com.vmware.vim25.mo.VirtualMachineSnapshot;

public class VmThread extends Thread {
	
	HostSystem host;
	public VmThread(){}
	public VmThread(HostSystem h){
		
		this.host =h;
		
	}
	public void  run(){
		try{
		
			URL url = new URL("https://"+host.getName()+"/sdk");
			ServiceInstance si = new ServiceInstance(url, "root", "12!@qwQW", true);
			VirtualMachine vm[]=host.getVms();
			for(int i= 0;i<vm.length;i++){
				boolean pingStatus = functions_vm.pingVirtualMachine(vm[i]);
				VirtualMachineRuntimeInfo vmri=vm[i].getRuntime();
				String state1= vmri.getPowerState().toString();
				System.out.println("power state of the VM::"+vm[i].getName()+"::"+state1+":IP:"+vm[i].getGuest().getIpAddress());
				if(pingStatus==false){	
					System.out.println("ping of VM "+ vm[i].getName()+"failed");
					System.out.println("checking if host is live");
					if (functions_vm.pingHost(host)){
						System.out.println(host.getName()+" is live and functional");	
					}
					else{
						host_Threads hthread = new host_Threads(host);
						hthread.start();
					}
					if(state1== "poweredOff"){
						System.out.println("VM is off. Checking for alarms..");
						boolean al =functions_vm.getalarm(si,vm[i]);
						if(al==true) // alarm set
						{
							System.out.println("User might have turned off VM,"+vm[i].getName()
									+ " Cannot ping. Exiting Thread");
						}
						else //alarm not set
						{
						functions_vm.setalarm(vm[i],si);
						System.out.println("Setting Alarm for: "+vm[i].getName());
						}						
							System.out.println("trying to power on VM:"+vm[i].getName());
							functions_vm.powerOnVM(vm[i]);
							System.out.println("waiting for changes to take effect...");
							sleep(2500000);
							
						}
						 pingStatus = functions_vm.pingVirtualMachine(vm[i]);
						if(pingStatus== false)
						{
									System.out.println("====================================");
									System.out.println("Reverting VM"+vm[i].getName()+" to prev snapshot");
									System.out.println("====================================");
									boolean rev= functions_vm.revertToSnapshot(vm[i]);									
									sleep(2500000);
									URL urladmin = new URL("https://130.65.132.113/sdk");
									ServiceInstance siadmin = new ServiceInstance(urladmin, "administrator", "12!@qwQW", true);
									Folder rootFolderadmin = siadmin.getRootFolder();
							    	ManagedEntity [] mesadmin = new InventoryNavigator(rootFolderadmin).searchManagedEntities("HostSystem");
									if(rev == true)// revert success
									{						
										System.out.println("====================================");
										System.out.println("revert to previous snapshot of "+vm[i].getName()+" success");
										System.out.println("====================================");
										if(VirtualMachinePowerState.poweredOff == vmri.getPowerState())
										{
											functions_vm.powerOnVM(vm[i]);
											System.out.println("waiting for changes to take effect...");
											sleep(2500000);
										}
										System.out.println("====================================");
										System.out.println("pinging(2) VM "+vm[i].getName()+" again after trying to power it on");
										System.out.println("====================================");
										 pingStatus =functions_vm.pingVirtualMachine(vm[i]);
										
										if(pingStatus==false)
										{
											System.out.println("====================================");
											System.out.println("pinging(2)failed now the VM"+vm[i].getName()+" will be migrated");
											System.out.println("====================================");
											for(int k= 0;k<mesadmin.length;k++){
												HostSystem newhost=(HostSystem) mesadmin[k];												
												VirtualMachine vm2[]=newhost.getVms();
												if(newhost!= host){
													if(vm2.length<2){
														System.out.println("calling migrate");
														functions_vm.MigrateVM(siadmin, vm[i], rootFolderadmin,newhost);
														break;
														}	
													}
												else{
													System.out.println("sorry no available host to migrate");
												}
											}//end of for
										}//end of if
									}//end of if
										else
										{
											System.out.println("No snapshots available to revert for vm:"+vm[i].getName()+"Migrating the VM to new host ");
											for(int k= 0;k<mesadmin.length;k++){
												HostSystem newhost=(HostSystem) mesadmin[k];
												VirtualMachine vm2[]=newhost.getVms();
												if(newhost!= host && functions_vm.pingHost(newhost)){
													if(vm2.length<2){
														System.out.println("====================================");
														System.out.println("unable to resolve error migrating VM");
														System.out.println("====================================");
														functions_vm.MigrateVM(siadmin, vm[i], rootFolderadmin,newhost);
														break;
														}	
													}
												else{
													System.out.println("sorry no avialble hosts to migrate");
												}
												}//end of for
										}//end of else 2
									}//end of if
										
							
						
						
						}//end of if
					else if (pingStatus == true) {
							System.out.println("taking snapshot of the VM:"+vm[i].getName());
							functions_vm.takeSnapshot(vm[i]);
							System.out.println("ping success for Vm:"+vm[i].getName());
					}//end of else
								
					}//end of main for loop 
					
					
		}//end of try
							
					
		catch (Exception e){
    		e.printStackTrace();
    	}
			
	}//end of run block


}//end of the class
 