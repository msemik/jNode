package pl.edu.uj.jnode.main.options;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.springframework.stereotype.Component;

/**
 * Created by michal on 22.10.15.
 */
@Component
public class JNodeOptions {
    Options options;

    public JNodeOptions() {

        options = new Options();

        options.addOption(Option.builder("h").longOpt("help").desc("Shows this info").build());

        options.addOption(Option.builder("j").hasArgs().desc("paths to jar files to execute").longOpt("jar").build());

        options.addOption(Option.builder("n").hasArgs().desc("node identifier").longOpt("nodeId").build());

        options.addOption(Option.builder("s").hasArgs().type(Integer.class).desc("size of worker pool used by this node").longOpt("pool-size").build());

        options.addOption(Option.builder("z").hasArgs().desc("path to jar path (by default $JNODE_HOME/jarpath").longOpt("jar-path").build());

        options.addOption(Option.builder("b").hasArgs().desc("tcp address to bind").longOpt("bind_addr").build());

        options.addOption(Option.builder("p").hasArgs().desc("tcp port to bind").longOpt("bind_port").build());

        options.addOption(Option.builder("i").hasArgs().desc("comma separated ips and ports. each pair in format:192.168.5.2[7800]").longOpt("initial_hosts").build());

    }

    public void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("jNode", options);
    }

    public Options getOptions() {
        return options;
    }
}
