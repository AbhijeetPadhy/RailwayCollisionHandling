/*
Arguments to create a TrainAgent
Name
track
dir
station
time
*/
package TrainManagement;

import jade.core.Agent;
//import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
//import jade.core.behaviours.WakerBehaviour;
import java.util.Scanner;
import java.io.*;

public class TrainAgent extends Agent {
    
    String Msg;
    Scanner in = new Scanner(System.in);
    static int count = 0;
    
    protected void setup() {
        // receiving messages from First
        ACLMessage msg=new ACLMessage(ACLMessage.REQUEST);
        //Object[] args = getArguments();
        String args[]=null;
        
        try{
            File dir = new File(".");
            File fin = new File(dir.getCanonicalPath() + File.separator + "dataset.txt");		
            FileInputStream fis = new FileInputStream(fin);
            //Construct BufferedReader from InputStreamReader
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line;
            int i=0;
            
            while ((line = br.readLine()) != null && i<=count) {
                System.out.println(line);
                args = line.split(",");
                i++;
            }
        }catch(IOException e){
            System.out.println("Train Agent cannot be created!! "+e);
        }
        String abc = args[0]+","+args[1]+","+args[2]+","+args[4];
        count++;
        //System.out.println(abc);
        msg.setContent(abc);
        msg.addReceiver(new AID(args[3],AID.ISLOCALNAME));
        send(msg);
    
        addBehaviour(new CyclicBehaviour(this) {
            
            MessageTemplate mt=MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            @Override
            public void action() {
                
                ACLMessage msg1=receive(mt);
                
                if(msg1!=null) {
                    msg1.clearAllReceiver();
                    Msg=msg1.getContent();
                    
                   System.out.println(getAID().getLocalName()+": "+Msg);
                    
                }else{
                    //System.out.println("No got!!");
                    //block();
                }
            }
        });
    }
}
