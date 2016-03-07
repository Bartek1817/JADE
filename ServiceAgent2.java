package jadeExample;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import java.net.*;
import java.io.*;

public class ServiceAgent2 extends Agent {
	protected void setup () {
		//rejestracja serwisu u agenta DF
		DFAgentDescription dfad = new DFAgentDescription();
		dfad.setName(getAID());
		//usluga 1
		ServiceDescription sd1 = new ServiceDescription();
		sd1.setType("answers");
		sd1.setName("eng2fra");
		//dodanie wszystkich uslug
		dfad.addServices(sd1);
		try {
			DFService.register(this,dfad);
		} catch (FIPAException ex) {
			ex.printStackTrace();
		}
		//dodanie zachowania
		addBehaviour(new Eng2FraCyclicBehaviour(this));
		//doDelete();
	}
	protected void takeDown() {
		//wyrejestrowanie uslug
		try {
			DFService.deregister(this);
		} catch (FIPAException ex) {
			ex.printStackTrace();
		}
	}
	public String makeRequest(String serviceName, String word)
	{
		StringBuffer response = new StringBuffer();
		try
		{
			URL url;
			URLConnection urlConn;
			DataOutputStream printout;
			DataInputStream input;
			url = new URL("http://www.dict.org/bin/Dict");
			urlConn = url.openConnection();
			urlConn.setDoInput(true);
			urlConn.setDoOutput(true);
			urlConn.setUseCaches(false);
			urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			String content = "Form=Dict1&Strategy=*&Database=" + URLEncoder.encode(serviceName) + "&Query=" + URLEncoder.encode(word);
			//zadanie
			printout = new DataOutputStream(urlConn.getOutputStream());
			printout.writeBytes(content);
			printout.flush();
			printout.close();
			//odpowiedz
			input = new DataInputStream(urlConn.getInputStream());
			String str;
			while (null != ((str = input.readLine())))
			{
				response.append(str);
			}
			input.close();
		}
		catch (Exception ex)
		{
			System.out.println(ex.getMessage());
		}
		//zwroc tylko interesujaca czesc odpowiedzi
		return response.substring(response.indexOf("<hr>")+4, response.lastIndexOf("<hr>"));
	}
}

class Eng2FraCyclicBehaviour extends CyclicBehaviour
{
	ServiceAgent2 agent;
	public Eng2FraCyclicBehaviour(ServiceAgent2 agent)
	{
		this.agent = agent;
	}
	public void action()
	{
		MessageTemplate template = MessageTemplate.MatchOntology("eng2fra");
		ACLMessage message = agent.receive(template);
		if (message == null)
		{
			block();
		}
		else
		{
			//przetwarzanie otrzymanej wiadomosci
			String content = message.getContent();
			ACLMessage reply = message.createReply();
			reply.setPerformative(ACLMessage.INFORM);
			String response = "";
			try
			{
				response = agent.makeRequest("fd-eng-fra", content);
			}
			catch (NumberFormatException ex)
			{
				response = ex.getMessage();
			}
			reply.setContent(response);
			agent.send(reply);
		}
	}
}
