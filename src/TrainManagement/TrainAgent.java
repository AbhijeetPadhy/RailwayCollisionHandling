/*
Name
track
dir
x,y
speed
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

public class TrainAgent extends Agent {
    String Msg;
    Scanner in = new Scanner(System.in);
    protected void setup() {
        // receiving messages from First
        ACLMessage msg=new ACLMessage(ACLMessage.REQUEST);
        Object[] args = getArguments();
        String abc = args[0].toString()+","+args[1].toString()+","+args[2].toString()+","+args[4].toString();
        //System.out.println(abc);
        msg.setContent(abc);
        msg.addReceiver(new AID(args[3].toString(),AID.ISLOCALNAME));
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
