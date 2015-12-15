package pl.edu.uj.cluster;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.edu.uj.ApplicationShutdownEvent;

import javax.annotation.PostConstruct;

@Component
public class JGroups extends ReceiverAdapter {
    Logger logger = LoggerFactory.getLogger(JGroups.class);

    public static final String DEFAULT_JNODE_CHANNEL = "DefaultJNodeChannel";
    private JChannel channel;

    public JGroups() {
    }

    @PostConstruct
    public void init() throws Exception {
        channel = new JChannel();
        channel.setReceiver(this);
        channel.connect(DEFAULT_JNODE_CHANNEL);
        channel.setDiscardOwnMessages(true);
        channel.send(new Message(null, "hello world from " + channel.getAddress()));

        logger.trace("Channel properties"  + channel.getProperties());
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
    }

    @Override
    public void receive(Message msg) {
        logger.debug("Received message, src " + msg.src() + " dst " + msg.dest());
        logger.debug("Message: " + msg.getObject());
        logger.debug("Headers: " + msg.printHeaders());
        logger.debug("Flags: " + msg.getFlags());
    }
}
