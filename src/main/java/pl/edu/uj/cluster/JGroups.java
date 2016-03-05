package pl.edu.uj.cluster;

import org.jgroups.*;
import org.jgroups.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.edu.uj.ApplicationInitializedEvent;
import pl.edu.uj.ApplicationShutdownEvent;
import pl.edu.uj.cluster.messages.Distributable;
import pl.edu.uj.cluster.messages.Sry;
import pl.edu.uj.options.NodeIdOptionEvent;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

@Component
public class JGroups extends ReceiverAdapter implements MessageGateway {
    private Logger logger = LoggerFactory.getLogger(JGroups.class);

    private static final String DEFAULT_JNODE_CHANNEL = "DefaultJNodeChannel";
    private JChannel channel;

    @Autowired
    private Distributor distributor;
    private List<String> membersInCurrentView = new ArrayList<>();
    private String nodeId;

    public JGroups() {
    }

    @EventListener
    public void on(NodeIdOptionEvent event) {
        this.nodeId = event.getNodeId();
    }

    @EventListener
    public void on(ApplicationInitializedEvent event) throws Exception {
        channel = new JChannel();
        if (nodeId != null)
            channel.setName(nodeId);
        channel.setReceiver(this);
        channel.connect(DEFAULT_JNODE_CHANNEL);
        if (nodeId == null)
            nodeId = channel.getAddressAsString();

        channel.setDiscardOwnMessages(true);
        logger.trace("Address type " + channel.getView().getMembers().get(0).getClass().getCanonicalName());
        logger.trace("Channel properties" + channel.getProperties());
    }

    @EventListener
    public void on(ApplicationShutdownEvent event) {
        if (channel != null)
            channel.close();
    }

    @Override
    public void send(Serializable obj, String destinationNodeId) {
        try {
            logger.debug("Sending message " + obj + " to " + destinationNodeId == null ? "all" : destinationNodeId);
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

    @Override
    public String getCurrentNodeId() {
        return nodeId;
    }

    private Address getAddressByNodeId(String destinationNodeId) {
        //Uwaga!! nie jestem pewny tej linijki i póki co nie mam jak spr.
        return destinationNodeId != null ? UUID.getByName(destinationNodeId) : null;
    }

    @Override
    public void viewAccepted(View view) {
        logger.debug(String.join(", "
                , "View creator:" + view.getCreator()
                , "View id:" + view.getViewId()
                , "Current nodeId:" + getCurrentNodeId()
                , "jNodes number:" + view.size()
                , "changed view" + view.getMembers()));

        synchronized (this) {
            List<String> membersInLastView = membersInCurrentView;
            membersInCurrentView = getCurrentViewAsString(view);
            distributeNewNodeForEachNewNode(membersInLastView);
            distributeNodeGoneForEachGoneNode(membersInLastView);
        }
    }

    private List<String> getCurrentViewAsString(View view) {
        return view.getMembers()
                .stream()
                .map(Address::toString)
                .collect(Collectors.toList());
    }

    private void distributeNodeGoneForEachGoneNode(List<String> membersInLastView) {
        membersInLastView.stream()
                .filter(nodeIdInLastView -> !membersInCurrentView.contains(nodeIdInLastView))
                .forEach(goneNodeId -> distributor.onNodeGone(goneNodeId));
    }

    private void distributeNewNodeForEachNewNode(List<String> membersInLastView) {
        membersInCurrentView.stream()
                .filter(nodeIdInNewView -> !membersInLastView.contains(nodeIdInNewView))
                .forEach(newNodeId -> {
                    if (!newNodeId.equals(getCurrentNodeId()))
                        distributor.onNewNode(newNodeId);
                });
    }

    @Override
    public void receive(Message msg) {
        String sourceNodeId = msg.src().toString();
        String destinationNodeId = msg.dest().toString();
        Object messageBody = msg.getObject();

        logger.debug(" received " + messageBody + " from " + sourceNodeId);
        // , "Headers: " + msg.printHeaders()
        // , "Flags: " + msg.getFlags()));

        if (messageBody instanceof Distributable) {
            Distributable distributable = (Distributable) messageBody;
            distributable.distribute(distributor, sourceNodeId, ofNullable(destinationNodeId));
        } else {
            logger.warn("Unexpected message body type: " + messageBody.getClass().getSimpleName());
        }
    }
}
