package project1.VM;
import java.net.URL;

import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;

public class disasterRecovery {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
			
		try{
				URL url = new URL("https://130.65.132.113/sdk");
				ServiceInstance si = new ServiceInstance(url, "administrator", "12!@qwQW", true);
				Folder rootFolder = si.getRootFolder();
				String name = rootFolder.getName();
				
				ManagedEntity[] mes = new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
				
				
				for(int i=0;i<mes.length;i++)
				{
					
					HostSystem host=(HostSystem) mes[i];
					System.out.println("Host: "+host.getName().toString());
					host_Threads hostThread = new host_Threads(host);
					hostThread.start();
					
					
				}
				if(mes.length==0){
					// create a vHost, since there are no hosts in the vCenter
					 System.out.println("No Hosts");
				}

			}
			catch ( Exception e ) 
	        { System.out.println( e.toString() ) ; }
	}
}				
			
				




	


