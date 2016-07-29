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
package to.joe.neveragain.linode.dns;

import com.google.common.collect.ImmutableList;
import com.google.gson.annotations.SerializedName;
import to.joe.neveragain.linode.API;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class DomainResourceList extends API.Response {
    public class Resource {
        @SerializedName("DOMAINID")
        private int domainID;
        @SerializedName("PROTOCOL")
        private String protocol;
        @SerializedName("TTL_SEC")
        private int ttlSeconds;
        @SerializedName("PRIORITY")
        private int priority;
        @SerializedName("TYPE")
        private String type;
        @SerializedName("TARGET")
        private String target;
        @SerializedName("WEIGHT")
        private String weight;
        @SerializedName("RESOURCEID")
        private int resourceID;
        @SerializedName("PORT")
        private int port;
        @SerializedName("NAME")
        private String name;

        public int getDomainID() {
            return domainID;
        }

        @Nullable
        public String getProtocol() {
            return protocol;
        }

        public int getTtlSeconds() {
            return ttlSeconds;
        }

        public int getPriority() {
            return priority;
        }

        @Nullable
        public String getType() {
            return type;
        }

        @Nullable
        public String getTarget() {
            return target;
        }

        @Nullable
        public String getWeight() {
            return weight;
        }

        public int getResourceID() {
            return resourceID;
        }

        public int getPort() {
            return port;
        }

        @Nullable
        public String getName() {
            return name;
        }

        @Nonnull
        @Override
        public String toString() {
            return API.toString(this);
        }
    }

    @SerializedName("DATA")
    private Resource[] data;

    @Nonnull
    public List<Resource> getDomainResources() {
        return ImmutableList.copyOf(this.data);
    }

    @Nonnull
    public Optional<Resource> getByName(@Nonnull String name) {
        for (Resource resource : this.data) {
            if (name.equals(resource.getName())) {
                return Optional.of(resource);
            }
        }
        return Optional.empty();
    }
}
