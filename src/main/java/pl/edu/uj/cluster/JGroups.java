package pl.edu.uj.cluster;

import org.jgroups.*;
import org.jgroups.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.edu.uj.ApplicationShutdownEvent;
import pl.edu.uj.cluster.messages.Distributable;
import pl.edu.uj.cluster.messages.Sry;

import javax.annotation.PostConstruct;
import java.io.Serializable;

import static java.util.Optional.ofNullable;

@Component
public class JGroups extends ReceiverAdapter implements MessageGateway {
    private Logger logger = LoggerFactory.getLogger(JGroups.class);

    private static final String DEFAULT_JNODE_CHANNEL = "DefaultJNodeChannel";
    private JChannel channel;

    @Autowired
    private Distributor distributor;

    public JGroups() {
    }

    @PostConstruct
    public void init() throws Exception {
        channel = new JChannel();
        channel.setReceiver(this);
        channel.connect(DEFAULT_JNODE_CHANNEL);
        channel.setDiscardOwnMessages(true);
        logger.info("Address type " + channel.getView().getMembers().get(0).getClass().getCanonicalName());
        logger.trace("Channel properties" + channel.getProperties());

        send(new Sry(69));
    }

    @Override
    public void send(Serializable obj, String destinationNodeId) {
        try {
            channel.send(new Message(getAddressByNodeId(destinationNodeId), obj));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void send(Serializable obj) {
        send(obj, null);
    }

    private Address getAddressByNodeId(String destinationNodeId) {
        //Uwaga!! nie jestem pewny tej linijki i póki co nie mam jak spr.
        return destinationNodeId != null ? UUID.getByName(destinationNodeId) : null;
    }

    @EventListener
    public void on(ApplicationShutdownEvent event) {
        channel.close();
    }

    @Override
    public void viewAccepted(View view) {
        logger.debug("View creator:" + view.getCreator());
        logger.debug("View id:" + view.getViewId());
        logger.debug("Current node address:" + channel.getAddress());
        logger.debug("JNodes number:" + view.size());
        logger.debug("View changed" + view.getMembers());

        //TODO: distributor.onNodeGone();


    }

    @Override
    public void receive(Message msg) {
        String sourceNodeId = msg.src().toString();
        String destinationNodeId = msg.dest().toString();
        Object messageBody = msg.getObject();

        logger.debug("Received message, src " + sourceNodeId + " dst " + destinationNodeId);
        logger.debug("Message: " + messageBody);
        logger.debug("Message class: " + messageBody.getClass().getSimpleName());
        logger.debug("Headers: " + msg.printHeaders());
        logger.debug("Flags: " + msg.getFlags());

        if (messageBody instanceof Distributable) {
            Distributable distributable = (Distributable) messageBody;
            distributable.distribute(distributor, sourceNodeId, ofNullable(destinationNodeId));
        } else {
            logger.warn("Unexpected message body type: " + messageBody.getClass().getSimpleName());
        }
    }
}
