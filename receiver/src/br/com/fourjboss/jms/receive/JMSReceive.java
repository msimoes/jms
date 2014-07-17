package br.com.fourjboss.jms.receive;

import java.util.Hashtable;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import br.com.fourjboss.utils.LoadBundle;

/**
 * 
 * Classe responsável por receber mensagens da fila JMS configurada no servidor de aplicação JBoss EAP 6.x.
 * Necessária a criação da fila no servidor de aplicação.
 * Para executar esta classe, é necessário informar corretamente os dados no arquivo de properties.
 * @author Marco Simões
 *
 */

public class JMSReceive implements MessageListener {
	

	public final static String JNDI_FACTORY=LoadBundle.getValue("JNDI_FACTORY");
	public final static String JMS_FACTORY=LoadBundle.getValue("JMS_FACTORY");
	public final static String QUEUE=LoadBundle.getValue("NOME_FILA");
	public final static String USERNAME=LoadBundle.getValue("USUARIO");
	public final static String PASSWORD=LoadBundle.getValue("SENHA");
	public final static String URL=LoadBundle.getValue("URL");
	
	private QueueConnectionFactory qconFactory;
	private QueueConnection qcon;
	private QueueSession qsession;
	private QueueReceiver qreceiver;
	private Queue queue;
	private boolean sair = false;

		public void onMessage(Message msg){
			try {
				String msgText;
				if (msg instanceof TextMessage){
					msgText = ((TextMessage)msg).getText();
				}
				else{
					msgText = msg.toString();
				}
				
				System.out.println("\n\t "+ msgText );
				if (msgText.equalsIgnoreCase("sair")){
					synchronized(this){
						sair = true;
						this.notifyAll(); 
					}
				}
			}
			catch (JMSException jmse){
				jmse.printStackTrace();
			}
		}
		public void createConnection(Context ctx, String queueName) throws NamingException, JMSException{
			qconFactory = (QueueConnectionFactory) ctx.lookup(JMS_FACTORY);

			//*************** Creating Queue Connection using the UserName & Password *************************
			qcon = qconFactory.createQueueConnection(USERNAME,PASSWORD);           
			qsession = qcon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			queue = (Queue) ctx.lookup(queueName);
			qreceiver = qsession.createReceiver(queue);
			qreceiver.setMessageListener(this);
			qcon.start();
		}

		public void closeConnection()throws JMSException{
			qreceiver.close();
			qsession.close();
			qcon.close();
		}
		public static void main(String[] args) throws Exception{
			InitialContext initialContext = getInitialContext(URL);
			JMSReceive qr = new JMSReceive();
			qr.createConnection(initialContext, QUEUE);
			System.out.println("JMS pronto para receber Mensagens da fila \""+QUEUE+"\". Para finalizar digite: SAIR");
			synchronized(qr){
				while (! qr.sair){
					try{
						qr.wait();
					}
					catch (InterruptedException ie)
					{}
				}
			}
			qr.closeConnection();
		}

		private static InitialContext getInitialContext(String url) throws NamingException{
			Hashtable env = new Hashtable();
			env.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
			env.put(Context.PROVIDER_URL, url);

			//*************** UserName & Password for the Initial Context for JNDI lookup *************************
			env.put(Context.SECURITY_PRINCIPAL, USERNAME);
			env.put(Context.SECURITY_CREDENTIALS, PASSWORD);

			return new InitialContext(env);
		}
	}