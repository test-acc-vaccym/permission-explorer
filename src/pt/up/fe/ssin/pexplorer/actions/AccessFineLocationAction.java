package pt.up.fe.ssin.pexplorer.actions;

import java.util.Timer;
import java.util.TimerTask;

import pt.up.fe.ssin.pexplorer.R;
import pt.up.fe.ssin.pexplorer.app.PermissionAction;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

public class AccessFineLocationAction extends PermissionAction {

	LocationManager m_location_manager;
	Boolean gpsEnabled;
	Timer timer1;
    public LocationResult locationResult;
	
	public AccessFineLocationAction() {
		super(R.string.access_fine_location_label, R.string.access_fine_location_desc,
				PermissionAction.WARN);
	}

	@Override
	protected void doAction(final Context context) {
		  m_location_manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		  gpsEnabled = m_location_manager.isProviderEnabled(LocationManager.GPS_PROVIDER);

		  MyLocation myLocation = new MyLocation();
		  myLocation.getLocation(context, locationResult);
  
		  if(gpsEnabled){
			  Toast.makeText(context,"GPS is enabled. Getting you current location",Toast.LENGTH_SHORT).show();
			  locationResult = new LocationResult(){
				    @Override
				    public void gotLocation(final Location location){
				    	new AlertDialog.Builder(context)
				        .setTitle(R.string.read_contact_title)
				        .setMessage("Latitude\n" + location.getLatitude() +"\nLongitude\n" + location.getLongitude())
				        .setCancelable(true)
				        .setPositiveButton(R.string.continue_,new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {}
				        }).show();
				    };
			  };
		  }
		  else
			  Toast.makeText(context,"Your gps is not enabled! Please turn it on.",Toast.LENGTH_SHORT).show();
	}

	class MyLocation {
	    Timer timer1;
	    LocationManager lm;
	    boolean gps_enabled=false;
	    boolean network_enabled=false;
	
	    public boolean getLocation(Context context, LocationResult result)
	    {
	        locationResult=result;
	        if(lm==null)
	            lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	
	        try{gps_enabled=lm.isProviderEnabled(LocationManager.GPS_PROVIDER);}catch(Exception ex){}
	        try{network_enabled=lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);}catch(Exception ex){}
	
	        if(!gps_enabled && !network_enabled)
	            return false;
	
	        if(gps_enabled)
	            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
	        if(network_enabled)
	            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);
	        timer1=new Timer();
	        timer1.schedule(new GetLastLocation(), 20000);
	        return true;
	    }
	
	    LocationListener locationListenerGps = new LocationListener() {
	        public void onLocationChanged(Location location) {
	            timer1.cancel();
	            locationResult.gotLocation(location);
	            lm.removeUpdates(this);
	            lm.removeUpdates(locationListenerNetwork);
	        }
	        public void onProviderDisabled(String provider) {}
	        public void onProviderEnabled(String provider) {}
	        public void onStatusChanged(String provider, int status, Bundle extras) {}
	    };
	
	    LocationListener locationListenerNetwork = new LocationListener() {
	        public void onLocationChanged(Location location) {
	            timer1.cancel();
	            locationResult.gotLocation(location);
	            lm.removeUpdates(this);
	            lm.removeUpdates(locationListenerGps);
	        }
	        public void onProviderDisabled(String provider) {}
	        public void onProviderEnabled(String provider) {}
	        public void onStatusChanged(String provider, int status, Bundle extras) {}
	    };
	
	    class GetLastLocation extends TimerTask {
	        @Override
	        public void run() {
	             lm.removeUpdates(locationListenerGps);
	             lm.removeUpdates(locationListenerNetwork);
	
	             Location net_loc=null, gps_loc=null;
	             if(gps_enabled)
	                 gps_loc=lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	             if(network_enabled)
	                 net_loc=lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	
	             //if there are both values use the latest one
	             if(gps_loc!=null && net_loc!=null){
	                 if(gps_loc.getTime()>net_loc.getTime())
	                     locationResult.gotLocation(gps_loc);
	                 else
	                     locationResult.gotLocation(net_loc);
	                 return;
	             }
	
	             if(gps_loc!=null){
	                 locationResult.gotLocation(gps_loc);
	                 return;
	             }
	             if(net_loc!=null){
	                 locationResult.gotLocation(net_loc);
	                 return;
	             }
	             locationResult.gotLocation(null);
	        }
	    }
	}

	public static abstract class LocationResult{
	    public abstract void gotLocation(Location location);
	}
}




