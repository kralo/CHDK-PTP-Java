package camera;

import javax.usb.UsbDisconnectedException;
import javax.usb.UsbEndpoint;
import javax.usb.UsbException;
import javax.usb.UsbIrp;
import javax.usb.UsbNotActiveException;
import javax.usb.UsbNotOpenException;
import javax.usb.UsbPipe;

import packet.PTPPacket;


public class PTPSession {
	private int Seq = 0;
    UsbPipe camInpipe = null;
    UsbPipe camOutpipe = null;
    Boolean isConnected = false;
    UsbIrp read,write;
    byte[] recbuf = new byte[300000];
    
    public PTPSession(String SerialNumber){
    	
    }
	public PTPSession(UsbEndpoint camIn,UsbEndpoint camOut) throws Exception{
		camInpipe = camIn.getUsbPipe();
		camOutpipe = camOut.getUsbPipe();
		
		camInpipe.open();
		camOutpipe.open();
		
		if (camInpipe.isActive()==false || camOutpipe.isActive()==false) throw new Exception("Pipes not active.. Balls");

		
		
		
	}
	
	public void sendPTPPacket(PTPPacket p) {
		try {
		//p.setTransaction(Seq);
		//Seq++;
		// Send init command
		long startTime = System.nanoTime();

		write = camOutpipe.createUsbIrp();
        write.setData(p.getPacket());
        write.setLength(p.getPacket().length);
        write.setOffset(0);
        write.setAcceptShortPacket(true);

		camOutpipe.syncSubmit(write);
		write.waitUntilComplete();
		long stopTime = System.nanoTime();
        System.out.println("TX Delta:\t"+((stopTime - startTime)/(float)10000000)+"ms");
        System.out.print(p);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public PTPPacket getResponse(){
		try {
			long startTime = System.nanoTime();
			read = camInpipe.createUsbIrp();
	        read.setData(recbuf);
	        read.setLength(recbuf.length);
			read.setOffset(0);
			read.setAcceptShortPacket(true);
	        
			camInpipe.syncSubmit(read);
	        PTPPacket response;
			response = new PTPPacket(recbuf);
			read.waitUntilComplete();
	        long stopTime = System.nanoTime();
	        
	        System.out.println("RX Delta:\t"+((stopTime - startTime)/(float)1000000)+"ms");
	        startTime = System.nanoTime();
	        
	        byte[] image = response.getData();
	        stopTime = System.nanoTime();
	        System.out.println("Copy:\t\t"+((stopTime - startTime)/(float)1000000)+"ms");
	        startTime = System.nanoTime();
	        System.out.print(response);
	        return response;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        

		return null;
	}
	public void close() throws UsbNotActiveException, UsbNotOpenException, UsbDisconnectedException, UsbException {
		// TODO Auto-generated method stub
		this.camInpipe.close();
		this.camOutpipe.close();
	}
	
	
}