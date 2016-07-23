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

import com.google.common.collect.ImmutableList;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Config {
    public class NoneSuchConfigFileException extends Exception {
        public NoneSuchConfigFileException() {
            super();
        }

        public NoneSuchConfigFileException(Exception e) {
            super(e);
        }
    }

    public class Data {
        public class IRC {
            public class Server {
                private final String host;
                private final int port;
                private final boolean secure;
                private final boolean acceptUntrusted;

                private Server(ConfigurationNode server) {
                    this.host = server.getNode("host").getString("irc.esper.net");
                    this.port = server.getNode("port").getInt(6697);
                    this.secure = server.getNode("secure").getBoolean(true);
                    this.acceptUntrusted = server.getNode("accept-untrusted-cert").getBoolean(false);
                }

                public String getHost() {
                    return this.host;
                }

                public int getPort() {
                    return this.port;
                }

                public boolean isSecure() {
                    return this.secure;
                }

                public boolean isAcceptUntrusted() {
                    return this.acceptUntrusted;
                }
            }

            public class Self {
                private final String nick;
                private final String user;
                private final String realname;

                private Self(ConfigurationNode self) {
                    this.nick = self.getNode("nick").getString("NeverAgain");
                    this.user = self.getNode("user").getString("j2GameBot");
                    this.realname = self.getNode("realname").getString("Shots 4 Shots was a horrible idea");
                }

                public String getNick() {
                    return this.nick;
                }

                public String getUser() {
                    return this.user;
                }

                public String getRealname() {
                    return this.realname;
                }
            }

            public class Channel {
                private final String name;
                private final String key;

                private Channel(ConfigurationNode channel) {
                    this.name = channel.getNode("name").getString("#neveragain");
                    this.key = channel.getNode("key").getString(null);
                }

                public String getName() {
                    return this.name;
                }

                public String getKey() {
                    return this.key;
                }
            }

            public class Auth {
                private final String username;
                private final String password;

                private Auth(ConfigurationNode auth) {
                    this.username = auth.getNode("username").getString(null);
                    this.password = auth.getNode("password").getString(null);
                }

                public String getUsername() {
                    return this.username;
                }

                public String getPassword() {
                    return this.password;
                }
            }

            public class AuthorizedUsers {
                private final List<String> list;

                private AuthorizedUsers(ConfigurationNode authorizedUsers) {
                    this.list = authorizedUsers.getList(o -> (o instanceof String) ? ((String) o) : null, ImmutableList.of("mbaxter"));
                }

                public List<String> getList() {
                    return this.list;
                }
            }

            private final Server server;
            private final Self self;
            private final Channel channel;
            private final Auth auth;
            private final AuthorizedUsers authorizedUsers;

            private IRC(ConfigurationNode irc) {
                this.server = new Server(irc.getNode("server"));
                this.self = new Self(irc.getNode("self"));
                this.channel = new Channel(irc.getNode("channel"));
                this.auth = new Auth(irc.getNode("auth"));
                this.authorizedUsers = new AuthorizedUsers(irc.getNode("authorized-users"));
            }

            @Nonnull
            public Server getServer() {
                return this.server;
            }

            @Nonnull
            public Self getSelf() {
                return this.self;
            }

            @Nonnull
            public Channel getChannel() {
                return this.channel;
            }

            @Nonnull
            public Auth getAuth() {
                return this.auth;
            }

            @Nonnull
            public AuthorizedUsers getAuthorizedUsers() {
                return this.authorizedUsers;
            }
        }

        public class Linode {
            private final String api_key;

            public Linode(ConfigurationNode linode) {
                this.api_key = linode.getNode("api_key").getString("NeverGonnaGiveYouUp");
            }

            public String getAPIKey() {
                return this.api_key;
            }
        }

        private final IRC irc;
        private final Linode linode;

        private Data(ConfigurationNode data) {
            this.irc = new IRC(data.getNode("irc"));
            this.linode = new Linode(data.getNode("linode"));
        }

        @Nonnull
        public IRC getIRC() {
            return this.irc;
        }

        @Nonnull
        public Linode getLinode() {
            return this.linode;
        }
    }

    private Data data;
    private final YAMLConfigurationLoader yamlConfigurationLoader;

    public Config() throws IOException, NoneSuchConfigFileException {
        Path path = FileSystems.getDefault().getPath("config.yml");
        if (!Files.isRegularFile(path)) {
            try {
                final String[] array = Main.class.getResource("/config.yml").toURI().toString().split("!");
                try (final FileSystem fs = FileSystems.newFileSystem(URI.create(array[0]), new HashMap<>())) {
                    final Path inJar = fs.getPath(array[1]);
                    Files.copy(inJar, path, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException | URISyntaxException e) {
                throw new NoneSuchConfigFileException(e);
            }
            throw new NoneSuchConfigFileException();
        }
        this.yamlConfigurationLoader = YAMLConfigurationLoader.builder().setPath(path).build();
        this.loadConfig();
    }

    @Nonnull
    public Data getData() {
        return this.data;
    }

    public void loadConfig() throws IOException {
        this.data = new Data(this.yamlConfigurationLoader.load());
    }
}
