package pl.edu.uj.cluster;

import org.jgroups.*;
import org.jgroups.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.edu.uj.ApplicationInitializedEvent;
import pl.edu.uj.ApplicationShutdownEvent;
import pl.edu.uj.OptionsDispatchedEvent;
import pl.edu.uj.engine.NodeIdFactory;
import pl.edu.uj.options.NodeIdOptionEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Component
@Primary
public class JGroups extends ReceiverAdapter implements MessageGateway, NodeIdFactory {
    private static final String DEFAULT_JNODE_CHANNEL = "DefaultJNodeChannel";
    private Logger logger = LoggerFactory.getLogger(JGroups.class);
    private JChannel channel;
    private ExecutorService executorService;
    @Autowired
    private Distributor distributor;
    private List<String> membersInCurrentView = new ArrayList<>();
    private String nodeId;

    public JGroups() {
        executorService = Executors.newSingleThreadExecutor();
    }

    @EventListener
    public void on(NodeIdOptionEvent event) {
        this.nodeId = event.getNodeId();
        if (channel != null) {
            throw new IllegalStateException("channel initialized before getting node id");
        }
    }

    @EventListener
    public void on(OptionsDispatchedEvent event) {
        init();
    }

    private void init() {
        if (channel != null)
            return;
        synchronized (this) {
            if (channel != null)
                return;
            try {
                channel = new JChannel();
                if (nodeId != null)
                    channel.setName(nodeId);
                channel.setReceiver(this);
                channel.connect(DEFAULT_JNODE_CHANNEL);

                channel.setDiscardOwnMessages(true);
                logger.trace("Address type " + channel.getView().getMembers().get(0).getClass().getCanonicalName());
                logger.trace("Channel properties" + channel.getProperties());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @EventListener
    public void on(ApplicationInitializedEvent event) {
        init();
    }

    @EventListener
    public void on(ApplicationShutdownEvent event) {
        if (channel != null)
            channel.close();
    }

    @Override
    public void receive(Message msg) {
        executorService.execute(() -> handleOnReceive(msg));
    }

    private void handleOnReceive(Message msg) {
        String sourceNodeId = msg.src().toString();
        Optional<String> destinationNodeId = ofNullable(msg.getDest()).map(adr -> adr.toString());
        Object messageBody = msg.getObject();

        logger.debug(" received " + messageBody + " from " + sourceNodeId);
        // , "Headers: " + msg.printHeaders()
        // , "Flags: " + msg.getFlags()));

        if (messageBody instanceof Distributable) {
            Distributable distributable = (Distributable) messageBody;
            distributable.distribute(distributor, sourceNodeId, destinationNodeId);
        } else {
            logger.warn("Unexpected message body type: " + messageBody.getClass().getSimpleName());
        }
    }

    @Override
    public void viewAccepted(View view) {
        executorService.execute(() -> handleViewAccepted(view));
    }

    private void handleViewAccepted(View view) {
        logger.debug(String.join(", "
                , "View creator:" + view.getCreator()
                , "View id:" + view.getViewId()
                , "Current nodeId:" + getCurrentNodeId()
                , "jNodes number:" + view.size()
                , "changed view" + view.getMembers()));

        synchronized (JGroups.this) {
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

    private void distributeNewNodeForEachNewNode(List<String> membersInLastView) {
        membersInCurrentView.stream()
                .filter(nodeIdInNewView -> !membersInLastView.contains(nodeIdInNewView))
                .forEach(newNodeId -> {
                    if (!newNodeId.equals(getCurrentNodeId()))
                        distributor.onNewNode(newNodeId);
                });
    }

    private void distributeNodeGoneForEachGoneNode(List<String> membersInLastView) {
        membersInLastView.stream()
                .filter(nodeIdInLastView -> !membersInCurrentView.contains(nodeIdInLastView))
                .forEach(goneNodeId -> distributor.onNodeGone(goneNodeId));
    }

    @Override
    public void send(Serializable obj, String destinationNodeId) {
        init();
        try {
            logger.debug("Sending message " + obj + " to " + (destinationNodeId == null ? "all" : destinationNodeId));
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
        if (nodeId == null) {
            init();
            nodeId = channel.getAddressAsString();
        }
        return nodeId;
    }


    private Address getAddressByNodeId(String destinationNodeId) {
        //Uwaga!! nie jestem pewny tej linijki i póki co nie mam jak spr.
        return destinationNodeId != null ? UUID.getByName(destinationNodeId) : null;
    }


}
