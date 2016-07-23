/*
 * * Copyright (C) 2016 Matt Baxter http://joe.to
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package to.joe.neveragain;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.event.channel.ChannelMessageEvent;
import org.kitteh.irc.client.library.event.client.ClientConnectedEvent;
import org.kitteh.irc.client.library.feature.auth.NickServ;
import org.kitteh.irc.client.library.util.AcceptingTrustManagerFactory;
import org.kitteh.irc.client.library.util.CISet;
import org.kitteh.irc.lib.net.engio.mbassy.listener.Handler;
import org.kitteh.irc.lib.net.engio.mbassy.listener.Invoke;
import to.joe.neveragain.linode.API;
import to.joe.neveragain.linode.avail.Datacenters;
import to.joe.neveragain.linode.avail.Plans;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.Set;

public class NeverAgain extends Thread {
    private Client client;
    private Config config;

    @Override
    public void run() {
        this.setName("NeverAgain Main Thread");

        // Config first!
        try {
            this.config = new Config();
        } catch (Config.NoneSuchConfigFileException e) {
            if (e.getCause() == null) {
                System.out.println("Default config copied.");
            } else {
                System.out.println("Failed to copy default config:");
                e.getCause().printStackTrace();
            }
            return;
        } catch (IOException e) {
            System.out.println("Failed to load config at start:");
            e.printStackTrace();
            return;
        }

        API.API_KEY = this.config.getData().getLinode().getAPIKey();

        this.startIRC();

        final BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        while (!this.isInterrupted()) {
            final String line;
            try {
                line = input.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                // TODO shut down everything.
                break;
            }
            if (line != null) {
                this.client.sendRawLine(line);
                // TODO commands
            }
        }
    }

    private Set<String> allowed;
    private String channel;

    private void startIRC() {
        if (this.client != null) {
            this.client.shutdown("Reloading!");
        }
        Client.Builder builder = Client.builder();
        builder.name("NeverAgainBot");
        Config.Data.IRC irc = this.config.getData().getIRC();
        builder.nick(irc.getSelf().getNick());
        builder.realName(irc.getSelf().getRealname());
        builder.user(irc.getSelf().getUser());
        builder.serverHost(irc.getServer().getHost());
        builder.serverPort(irc.getServer().getPort());
        builder.secure(irc.getServer().isSecure());
        if (irc.getServer().isAcceptUntrusted()) {
            builder.secureTrustManagerFactory(new AcceptingTrustManagerFactory());
        }
        builder.listenException(Exception::printStackTrace);
        if (irc.getAuth().getUsername() != null) {
            builder.afterBuildConsumer(client -> client.getAuthManager().addProtocol(new NickServ(client, irc.getAuth().getUsername(), irc.getAuth().getPassword())));
        }

        builder.afterBuildConsumer(client -> {
            if (irc.getAuth().getUsername() != null) {
                client.getAuthManager().addProtocol(new NickServ(client, irc.getAuth().getUsername(), irc.getAuth().getPassword()));
            }
            client.getEventManager().registerEventListener(this);
            this.allowed = new CISet(client);
            irc.getAuthorizedUsers().getList().forEach(this.allowed::add);
        });

        this.channel = this.config.getData().getIRC().getChannel().getName();

        this.client = builder.build();
    }

    @Handler(delivery = Invoke.Asynchronously)
    public void ready(ClientConnectedEvent event) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {
        }
        if (this.config.getData().getIRC().getChannel().getKey() == null) {
            this.client.addChannel(this.channel);
        } else {
            this.client.addChannel(this.channel, this.config.getData().getIRC().getChannel().getKey());
        }
    }

    @Handler
    public void msg(ChannelMessageEvent event) {
        if (event.getMessage().startsWith("!cheapest") && event.getMessage().length() > ("!cheapest ".length())) {
            try {
                Datacenters dcs = API.getAvailableDatacenters();
                Optional<Datacenters.Datacenter> dc = dcs.getDatacenterByAbbreviation(event.getMessage().substring("!cheapest ".length()));
                if (dc.isPresent()) {
                    Plans plans = API.getPlans();
                    Optional<Plans.Plan> plan = plans.getCheapestPlanAtDatacenter(dc.get().getDatacenterID());
                    if (plan.isPresent()) {
                        Plans.Plan p = plan.get();
                        event.sendReply("Plan ID " + p.getPlanID() + ": " + p.getLabel() + " with " + p.getCores() + " cores at $" + p.getHourly() + " per hour");
                    } else {
                        event.sendReply("No plans exist at this datacenter");
                    }
                } else {
                    event.sendReply("No datacenter exists with that name");
                    return;
                }
            } catch (IOException e) {
                event.sendReply("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        if (event.getMessage().startsWith("!under") && event.getMessage().length() > ("!under ".length())) {
            try {
                String[] args = event.getMessage().split(" ");
                if (args.length < 3) {
                    event.sendReply("!under <hourly> <datacenter>");
                    return;
                }
                Datacenters dcs = API.getAvailableDatacenters();
                Optional<Datacenters.Datacenter> dc = dcs.getDatacenterByAbbreviation(args[2]);
                if (dc.isPresent()) {
                    Plans plans = API.getPlans();
                    Optional<Plans.Plan> plan = plans.getBestPlanBelowHourlyAtDatacenter(Double.parseDouble(args[1]), dc.get().getDatacenterID());
                    if (plan.isPresent()) {
                        Plans.Plan p = plan.get();
                        event.sendReply("Plan ID " + p.getPlanID() + ": " + p.getLabel() + " with " + p.getCores() + " cores at $" + p.getHourly() + " per hour");
                    } else {
                        event.sendReply("No plans exist at this datacenter");
                    }
                } else {
                    event.sendReply("No datacenter exists with that name");
                    return;
                }
            } catch (Exception e) {
                event.sendReply("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
