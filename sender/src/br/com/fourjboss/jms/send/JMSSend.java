package br.com.fourjboss.jms.send;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import br.com.fourjboss.utils.LoadBundle;

/**
 * 
 * Classe responsável por enviar mensagens a fila JMS configurada no servidor de aplicação JBoss EAP 6.x.
 * Necessária a criação da fila no servidor de aplicação.
 * Para executar esta classe, é necessário informar corretamente os dados no arquivo de properties.
 * @author Marco Simões
 *
 */

public class JMSSend {
	
	public final static String JNDI_FACTORY=LoadBundle.getValue("JNDI_FACTORY");
	public final static String JMS_FACTORY=LoadBundle.getValue("JMS_FACTORY");
	public final static String QUEUE=LoadBundle.getValue("NOME_FILA");
	public final static String USERNAME=LoadBundle.getValue("USUARIO");
	public final static String PASSWORD=LoadBundle.getValue("SENHA");
	public final static String URL=LoadBundle.getValue("URL");
	
	private QueueConnectionFactory qconFactory;
	private QueueConnection qcon;
	private QueueSession qsession;
	private QueueSender qsender;
	private Queue queue;
	private TextMessage msg;

	public void createConnection(Context ctx, String queueName)throws NamingException, JMSException
	{
		qconFactory = (QueueConnectionFactory) ctx.lookup(JMS_FACTORY);

		//*************** Creating Queue Connection using the UserName & Password *************************
		qcon = qconFactory.createQueueConnection(USERNAME,PASSWORD);           

		qsession = qcon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		queue = (Queue) ctx.lookup(queueName);
		qsender = qsession.createSender(queue);
		msg = qsession.createTextMessage();
		qcon.start();
	}

	public void sendMessage(String message) throws JMSException {
		msg.setText(message);
		qsender.send(msg);
	}

	public void closeConnection() throws JMSException {
		qsender.close();
		qsession.close();
		qcon.close();
	}

	public static void main(String[] args) throws Exception {
		
		InitialContext initialContext = getInitialContext(URL);
		JMSSend queueSender = new JMSSend();
		queueSender.createConnection(initialContext, QUEUE);
		readMessage(queueSender);
		queueSender.closeConnection();
	}

	private static void readMessage(JMSSend qs) throws IOException, JMSException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
		boolean readFlag=true;
		System.out.println("\n\tIniciando o envio de Mensagens para a fila \""+QUEUE+"\". Para finalizar digite: SAIR\n");
		while(readFlag)
		{
			System.out.print("<Digite a mensagem:> ");
			String msg=br.readLine();
			if(msg.equals("SAIR") || msg.equals("sair"))
			{
				qs.sendMessage(msg);
				System.exit(0);
			}
			qs.sendMessage(msg);
			System.out.println();
		}
		br.close();
	}

	private static InitialContext getInitialContext(String url) throws NamingException
	{
		Hashtable env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
		env.put(Context.PROVIDER_URL, url);

		//*************** UserName & Password for the Initial Context for JNDI lookup *************************
		env.put(Context.SECURITY_PRINCIPAL, USERNAME);
		env.put(Context.SECURITY_CREDENTIALS, PASSWORD);

		return new InitialContext(env);
	}
}



