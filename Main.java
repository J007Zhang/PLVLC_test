package test1;

import java.awt.BorderLayout;
import java.awt.LayoutManager;
import java.util.Hashtable;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import com.fazecast.jSerialComm.SerialPort;
 


public class Main {		
	static int comm1state = 1, comm2state = 1;
	static int sender_id, led_id, receiver_id, pd_id;
	static boolean mess_complete = false;
	
		public static void main(String[] args) {
        	

                // create a window with a slider
                JFrame window = new JFrame();
                
        		window.setTitle("VLC Passive Localization");        	
        		window.setLayout(new BorderLayout());
        		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        		
                JSlider slider = new JSlider();
                slider.setMaximum(5000);             
                
                slider.setMajorTickSpacing(100);
                slider.setMinorTickSpacing(1);
                slider.setPaintTicks(true);
                
              //Create the label table
                Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
                labelTable.put( 1000, new JLabel("A") );
                labelTable.put( 2000, new JLabel("Mid") );
                labelTable.put( 3000, new JLabel("B") );
                slider.setLabelTable( labelTable );
                slider.setPaintLabels(true);
                slider.setBorder(
                        BorderFactory.createEmptyBorder(0,0,10,0));
                
		
                window.add(slider);
                window.pack();
                window.setVisible(true);
               
                // determine which serial port to use
                SerialPort ports[] = SerialPort.getCommPorts();
                System.out.println("Select a port:");
                int i = 1;
                for(SerialPort port : ports) {
                        System.out.println(i++ + ". " + port.getSystemPortName());
                }

				Scanner s1 = new Scanner(System.in);
                int chosenPort1 = s1.nextInt();
                s1.close();
                // open and configure the port
                SerialPort port1 = ports[chosenPort1 - 1];
                if(port1.openPort()) {
                        System.out.println("Successfully opened the first port.");
                } else {
                        System.out.println("Unable to open the first port.");
                        return;
                }
                port1.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
                
                Scanner s11 = new Scanner(System.in);
                int chosenPort2 = s11.nextInt();
                s11.close();
                
                // open the second port
                SerialPort port2 = ports[chosenPort2 - 1];
                if(port2.openPort()) {
                		System.out.println("Successfully opened the second port.");
                } else {
                		System.out.println("Unable to open the second port.");
                		return;
                }
                port2.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
               
                // enter into an infinite loop that reads from the port and updates the GUI
                @SuppressWarnings("resource")
				Scanner data = new Scanner(port1.getInputStream());
                @SuppressWarnings("resource")
				Scanner data2 = new Scanner(port2.getInputStream());
                
                while(data.hasNextLine()|| data2.hasNextLine()) {
                        int number = 0;
                        int databyte = 0;
                        int nextstatecomm1 = 1;
                        try
                        {
                        		number = Integer.parseInt(data.nextLine());
                        		databyte = Integer.parseInt(data.nextLine());


                        		switch(comm1state)
                        		{
                        			case 1://	The start of the state machine
                        				if(databyte == 0xC0)
                        				{
                        					nextstatecomm1 = 2;
                        				}
                        				else
                        				{
                        					nextstatecomm1 = 1;
                        				}
                        			break;
                        			case 2://	Take the sender id from the serial communication
                        				nextstatecomm1 = 3;
                        				sender_id = databyte;
                        				break;
                        			case 3://	Take the led id from the serial communication
                        				nextstatecomm1 = 4;
                        				led_id = databyte;
                        				break;
                        			case 4://	Take the receiver id
                        				nextstatecomm1 = 5;
                        				receiver_id = databyte;
                        				break;
                        			case 5://	Take the PD id
                        				nextstatecomm1 = 6;
                        				pd_id = databyte;
                        				break;
                        			case 6://	Judge the ending of the complete message
                        				if(databyte == 0xC0)
                        				{
                        					nextstatecomm1 = 1;
                        					mess_complete = true;
                        				}
                        				else
                        				{
                        					nextstatecomm1 = 6;
                        				}
                        				break;
                        			default:
                        				nextstatecomm1 = 1;
                        		}
                        		comm1state = nextstatecomm1;
                        		
                        	}
                        catch(Exception e){}
                        while(mess_complete)
                        {
                        	// TODO, update the position of the mirror according to the complete message
                        	if((sender_id == 1)&&(led_id == 1)&&(receiver_id == 1)&&(pd_id == 1))
                        	{
                        		number = 1500;
                        	}
                        	else if((sender_id == 2)&&(led_id == 1)&&(receiver_id == 2)&&(pd_id == 1))
                        	{
                        		number = 3500;
                        	}
                        	mess_complete = false;
                        }
                        slider.setValue(number);
                }
        }
		
		class MyThread implements Runnable{
			  Scanner data; // name of thread

			  Thread t;

			  MyThread(Scanner datain) {
			    data = datain;
			    t = new Thread(this);
			    t.start();
			  }

			  public void run() {

			  }
		}

}