package pl.edu.uj.jnode.cluster;

import org.jgroups.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.edu.uj.jnode.engine.NodeIdFactory;
import pl.edu.uj.jnode.main.*;
import pl.edu.uj.jnode.main.options.*;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;

import static java.util.Optional.ofNullable;

@Component
@Primary
public class JGroups extends ReceiverAdapter implements MessageGateway, NodeIdFactory {
    private static final String DEFAULT_JNODE_CHANNEL = "DefaultJNodeChannel";
    private Logger logger = LoggerFactory.getLogger(JGroups.class);
    private ExecutorService executorService;
    @Autowired
    private Distributor distributor;
    @Autowired
    private JNodeApplication jNodeApplication;
    private List<Address> membersInCurrentView = new ArrayList<>();
    private String nodeId;
    private String bindAddress;
    private String port;
    private String initialHosts;
    private volatile JChannel channel;

    public JGroups() {
        executorService = Executors.newSingleThreadExecutor();
    }

    @EventListener
    public void on(BindAddressOptionEvent event) {
        bindAddress = event.getBindAddress();
    }

    @EventListener
    public void on(BindPortOptionEvent event) {
        port = event.getPort();
    }

    @EventListener
    public void on(InitialHostsOptionEvent event) {
        initialHosts = event.getInitialHosts();
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
        if (channel != null) {
            return;
        }
        synchronized (this) {
            if (channel != null) {
                return;
            }
            if (jNodeApplication.isShutDown()) {
                return;
            }
            try {
                if (bindAddress == null && initialHosts == null) {
                    channel = new JChannel();
                } else if (bindAddress == null || initialHosts == null) {
                    logger.error("both bind address and initial hosts must be given or none. starting as udp.");
                    channel = new JChannel();
                } else {
                    System.setProperty("jgroups.bind_addr", bindAddress);
                    System.setProperty("jgroups.tcpping.initial_hosts", initialHosts);
                    if (port != null) {
                        System.setProperty("jgroups.bind_port", port);
                    }
                    channel = new JChannel("tcp.xml");
                }

                if (nodeId != null) {
                    channel.setName(nodeId);
                }
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
        if (channel != null) {
            channel.close();
        }
        executorService.shutdownNow();
    }

    @Override
    public void receive(Message msg) {
        executorService.execute(() -> handleOnReceive(msg));
    }

    private void handleOnReceive(Message msg) {
        String sourceNodeId = msg.src().toString();
        Optional<String> destinationNodeId = ofNullable(msg.getDest()).map(adr -> adr.toString());
        Object messageBody = msg.getObject();

        logger.debug("Received " + messageBody + " from " + sourceNodeId);
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
        logger.debug(String.join(", ", "View creator:" + view.getCreator(), "View id:" + view.getViewId(), "Current nodeId:" + getCurrentNodeId(),
                                 "jNodes number:" + view.size(), "changed view" + view.getMembers()));

        synchronized (JGroups.this) {
            List<Address> membersInLastView = membersInCurrentView;
            membersInCurrentView = getCurrentViewAsString(view);
            distributeNewNodeForEachNewNode(membersInLastView);
            distributeNodeGoneForEachGoneNode(membersInLastView);
        }
    }

    private List<Address> getCurrentViewAsString(View view) {
        return view.getMembers();
    }

    private void distributeNewNodeForEachNewNode(List<Address> membersInLastView) {
        membersInCurrentView.stream().filter(nodeIdInNewView -> !membersInLastView.contains(nodeIdInNewView)).forEach(newNode -> {
            View view = channel.getView();
            String newNodeId = newNode.toString();
            if (!newNodeId.equals(getCurrentNodeId())) {
                distributor.onNewNode(newNodeId);
            }
        });
    }

    private void distributeNodeGoneForEachGoneNode(List<Address> membersInLastView) {
        membersInLastView.stream().filter(nodeIdInLastView -> !membersInCurrentView.contains(nodeIdInLastView))
                         .forEach(goneNode -> distributor.onNodeGone(goneNode.toString()));
    }

    //@Scheduled(fixedDelay = 2000, initialDelay = 2000)
    public void asd() {

        View view = channel.getView();
        System.out.println("view" + view);
        System.out.println("view.getMembers" + view.getMembers());
        System.out.println("view.getMembersRaw" + Arrays.deepToString(view.getMembersRaw()));
        System.out.println("view.getViewId" + view.getViewId());
    }

    @Override
    public void send(Serializable obj, String destinationNodeId) {
        init();
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

    @Override
    public String getCurrentNodeId() {
        if (nodeId == null) {
            init();
            nodeId = channel.getAddressAsString();
        }
        return nodeId;
    }

    private Address getAddressByNodeId(String destinationNodeId) {
        if (destinationNodeId == null) {
            return null;
        }
        for (Address address : membersInCurrentView) {
            if (address.toString().equals(destinationNodeId)) {
                return address;
            }
        }
        throw new IllegalStateException("Couldn't find addres in current view for node " + destinationNodeId);
    }
}
