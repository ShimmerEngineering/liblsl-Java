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

public class SendData extends BasicProcessWithCallBack {
	static ShimmerDevice shimmerDevice;
	static BasicShimmerBluetoothManagerPc btManager = new BasicShimmerBluetoothManagerPc();
	static LSL.StreamOutlet outlet;
	static String btComport = "Com3";
	
	public static void main(String[] args) throws IOException, InterruptedException  {
        System.out.println("Creating a new StreamInfo...");
        LSL.StreamInfo info = new LSL.StreamInfo("SendData","Accel",3,51.2,LSL.ChannelFormat.float32,"test");
        System.out.println("Creating an outlet...");
        outlet = new LSL.StreamOutlet(info);
        
        SendData s = new SendData();
        s.setWaitForData(btManager.callBackObject);
        btManager.connectShimmerThroughCommPort(btComport);
        
        //outlet.close();
        //info.destroy();
	}
	
	@Override
	protected void processMsgFromCallback(ShimmerMsg shimmerMSG) {
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
				shimmerDevice.startStreaming();
			}
		}
		else if (ind == ShimmerPC.MSG_IDENTIFIER_DATA_PACKET) {
			System.out.println("Shimmer MSG_IDENTIFIER_DATA_PACKET");
			ObjectCluster objc = (ObjectCluster) shimmerMSG.mB;
			double data = objc.getFormatClusterValue("Accel_LN_X", "CAL");
			if(data != Double.NaN) {
				float[] dataArray = new float[3];
				dataArray[0] = (float)objc.getFormatClusterValue("Accel_LN_X", "CAL");
				dataArray[1] = (float)objc.getFormatClusterValue("Accel_LN_Y", "CAL");
				dataArray[2] = (float)objc.getFormatClusterValue("Accel_LN_Z", "CAL");
				outlet.push_sample(dataArray);
			}
		}
		
	}
}
