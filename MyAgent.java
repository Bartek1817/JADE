package jadeExample;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MyAgent extends Agent {
	protected void setup () {
		displayResponse("Witam, jestem " + getAID().getLocalName());
		addBehaviour(new MyCyclicBehaviour(this));
		//doDelete();
	}
	protected void takeDown() {
		displayResponse("Do widzenia");
	}
	public void displayResponse(String message) {
		JOptionPane.showMessageDialog(null,message,"Komunikat",JOptionPane.PLAIN_MESSAGE);
	}
	public void displayHtmlResponse(String html) {
		JTextPane tp = new JTextPane();
		JScrollPane js = new JScrollPane();
		js.getViewport().add(tp);
		JFrame jf = new JFrame();
		jf.getContentPane().add(js);
		jf.pack();
		jf.setSize(400,500);
		jf.setVisible(true);
		tp.setContentType("text/html");
		tp.setEditable(false);
		tp.setText(html);
	}
}

class MyCyclicBehaviour extends CyclicBehaviour {
	MyAgent myAgent;
	public MyCyclicBehaviour(MyAgent myAgent) {
		this.myAgent = myAgent;
	}
	public void action() {
		ACLMessage message = myAgent.receive();
		if (message == null) {
			block();
		} else {
			String ontology = message.getOntology();
			String content = message.getContent();
			int performative = message.getPerformative();
			if (performative == ACLMessage.REQUEST)
			{
				//ja nie potrafie odpowiedziec ale poszukam kogos kto potrafi
				DFAgentDescription dfad = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setName(ontology);
				dfad.addServices(sd);
				try
				{
					DFAgentDescription[] result = DFService.search(myAgent, dfad);
					if (result.length == 0) myAgent.displayResponse("Nie znaleziono us³ugi ...");
					else
					{
						String foundAgent = result[0].getName().getLocalName();
						myAgent.displayResponse("Agent " + foundAgent + " dostarcza ¿¹danej us³ugi. Kliknij aby przes³aæ do niego wiadomoœæ ... ");
						ACLMessage forward = new ACLMessage(ACLMessage.REQUEST);
						forward.addReceiver(new AID(foundAgent, AID.ISLOCALNAME));
						forward.setContent(content);
						forward.setOntology(ontology);
						myAgent.send(forward);
					}
				}
				catch (FIPAException ex)
				{
					ex.printStackTrace();
					myAgent.displayResponse("Wyst¹pi³ problem przy wyszukiwaniu us³ugi ...");
				}
			}
			else
			{	//jesli to nie zapytanie (request)
				myAgent.displayHtmlResponse(content);
			}
		}
	}
}
