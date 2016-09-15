package chess180;

import java.awt.event.InputEvent;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.media.sse.EventInput;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;


public class ButtonInput implements Runnable {

	ButtonInputEventListener bel;
	EventInput eventInput;
	
	public ButtonInput(ButtonInputEventListener bel) {
		this.bel = bel;
		Client client = ClientBuilder.newBuilder()
		        .register(SseFeature.class).build();
		WebTarget target = client.target("https://api.particle.io/v1/devices/340037000647343138333038/events/mikkibutton?access_token=" + Config.accessToken);
		 
		eventInput = target.request().get(EventInput.class);
		Thread thread = new Thread(this);
		thread.setDaemon(true);
		thread.start();
	}
	
	@Override
	public void run() {
		//while (!eventInput.isClosed()) {
		while (true) {
		    final InboundEvent inboundEvent = eventInput.read();
		    if (inboundEvent == null) {
		        // connection has been closed
		        break;
		    }
		    if (inboundEvent.getName() == null)
		    	continue;
		    //String[] buttons = {"mikkibutton1", "mikkibutton2", "mikkibutton3", "mikkibutton4"};
		    //for (String button : buttons) {
		    //	if (button.equals(inboundEvent.getName()))
		    //}
		    //System.out.println(inboundEvent.getName());
		    if ("mikkibuttonall".equals(inboundEvent.getName())) 
	    		bel.allClick();
		    else
	    		bel.click();
		}
	}
}
