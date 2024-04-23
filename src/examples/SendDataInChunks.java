package examples;
import edu.ucsd.sccn.LSL;
import java.io.IOException;

import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.driver.BasicProcessWithCallBack;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.pcDriver.ShimmerPC;
import com.shimmerresearch.tools.bluetooth.BasicShimmerBluetoothManagerPc;
import com.shimmerresearch.exceptions.ShimmerException;

public class SendDataInChunks extends BasicProcessWithCallBack {
	static ShimmerDevice shimmerDevice;
	static BasicShimmerBluetoothManagerPc btManager = new BasicShimmerBluetoothManagerPc();
	static LSL.StreamOutlet outlet;
	static String btComport = "Com8";
	
    public static void main(String[] args) throws IOException, InterruptedException  {
    	
    	NativeLibraryLoader.loadLibrary(); //to use the lib/liblsl64.dll
        System.out.println("Creating a new StreamInfo...");
        LSL.StreamInfo info = new LSL.StreamInfo("SendData","Accel",3,51.2,LSL.ChannelFormat.float32,"test");

        System.out.println("Creating an outlet...");
        outlet = new LSL.StreamOutlet(info);
        
        SendDataInChunks s = new SendDataInChunks();
        s.setWaitForData(btManager.callBackObject);
        btManager.connectShimmerThroughCommPort(btComport);
        
        /*
        
        System.out.println("Sending data...");
        float[] chunk = new float[10*8];
        for (int t=0;t<100000;t++) {
            // the chunk array contains first all values for the first sample, then the second, and so on
            for (int k=0;k<chunk.length;k++)
                chunk[k] = (float)Math.random()*50-25;
            outlet.push_chunk(chunk); // note: it is also possible to pass in time stamps
            Thread.sleep(100);
        }
        
        outlet.close();
        info.destroy();
        
        */
    }

	@Override
	protected void processMsgFromCallback(ShimmerMsg shimmerMSG) {
		// TODO Auto-generated method stub
		int ind = shimmerMSG.mIdentifier;

		Object object = (Object) shimmerMSG.mB;

		if (ind == ShimmerPC.MSG_IDENTIFIER_STATE_CHANGE) {
			CallbackObject callbackObject = (CallbackObject) object;
			if (callbackObject.mState == BT_STATE.CONNECTED) {
				shimmerDevice = btManager.getShimmerDeviceBtConnected(btComport);
				System.out.println("Sending data...");
			}
		}
		else if (ind == ShimmerPC.MSG_IDENTIFIER_NOTIFICATION_MESSAGE) {
			CallbackObject callbackObject = (CallbackObject)object;
			int msg = callbackObject.mIndicator;
			if (msg== ShimmerPC.NOTIFICATION_SHIMMER_FULLY_INITIALIZED){
				try {
			        shimmerDevice.startStreaming();
			    } catch (ShimmerException e) {
			        e.printStackTrace();
			    }
			}
		}
		else if (ind == ShimmerPC.MSG_IDENTIFIER_DATA_PACKET) {
			System.out.println("Shimmer MSG_IDENTIFIER_DATA_PACKET");
			ObjectCluster objc = (ObjectCluster) shimmerMSG.mB;
			double data = objc.getFormatClusterValue("Accel_LN_X", "CAL");
			if(data != Double.NaN) {
				int sample = 10;
				float[] dataArray = new float[sample * 3];
				for (int i = 0; i < sample; i++) {
					dataArray[i * 3] = (float)objc.getFormatClusterValue("Accel_LN_X", "CAL");
					dataArray[i * 3 + 1] = (float)objc.getFormatClusterValue("Accel_LN_Y", "CAL");
					dataArray[i * 3 + 2] = (float)objc.getFormatClusterValue("Accel_LN_Z", "CAL");
				}
				outlet.push_sample(dataArray);
			}
		}
		
	}
}
