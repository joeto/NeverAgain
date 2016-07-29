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
package to.joe.neveragain.linode;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.kitteh.irc.client.library.util.Pair;
import to.joe.neveragain.linode.avail.Datacenters;
import to.joe.neveragain.linode.avail.Plans;
import to.joe.neveragain.linode.dns.DomainList;
import to.joe.neveragain.linode.dns.DomainResourceList;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class API {
    public static class Response {
        @SerializedName("ACTION")
        private String action;

        @Nonnull
        public String getAction() {
            return this.action;
        }

        @Nonnull
        @Override
        public String toString() {
            return API.toString(this);
        }
    }

    private static final List<Pair<String, String>> EMPTY = ImmutableList.of();
    private static final Gson GSON = new Gson();
    public static String API_KEY;

    @Nonnull
    public static <R extends Response> R get(@Nonnull Class<R> clazz, @Nonnull String action, @Nonnull List<Pair<String, String>> parameters) throws IOException {
        Form form = Form.form()
                .add("api_key", API_KEY)
                .add("api_action", action);
        parameters.forEach(pair -> form.add(pair.getLeft(), pair.getRight()));
        String reply = Request.Post("https://api.linode.com/")
                .bodyForm(form.build())
                .execute()
                .returnContent().asString(Charset.defaultCharset());
        return GSON.fromJson(reply, clazz);
    }

    @Nonnull
    public static Datacenters getAvailableDatacenters() throws IOException {
        return API.get(Datacenters.class, "avail.datacenters", EMPTY);
    }

    @Nonnull
    public static DomainList getDomains() throws IOException {
        return API.get(DomainList.class, "domain.list", EMPTY);
    }

    @Nonnull
    public static DomainResourceList getDomainResources(int domainID) throws IOException {
        return API.get(DomainResourceList.class, "domain.resource.list", ImmutableList.of(Pair.of("DOMAINID", String.valueOf(domainID))));
    }

    @Nonnull
    public static Plans getPlans() throws IOException {
        return API.get(Plans.class, "avail.linodeplans", EMPTY);
    }

    @Nonnull
    public static String toString(@Nonnull Object object) {
        return object.getClass().getSimpleName() + ':' + GSON.toJson(object);
    }
}
