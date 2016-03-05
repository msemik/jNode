package pl.edu.uj.options;

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

        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("Shows this info")
                .build());

        options.addOption(Option.builder("j")
                .hasArgs()
                .desc("paths to jar files to execute")
                .longOpt("jar")
                .build());

        options.addOption(Option.builder("i")
                .hasArgs()
                .desc("node identifier")
                .longOpt("nodeId")
                .build());

        options.addOption(Option.builder("p")
                .hasArgs()
                .type(Integer.class)
                .desc("size of worker pool used by this node")
                .longOpt("pool-size")
                .build());
    }

    public void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("jNode", options);
    }

    public Options getOptions() {
        return options;
    }
}
