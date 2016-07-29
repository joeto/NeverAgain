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

public class DomainList extends API.Response {
    public class Domain {
        @SerializedName("DOMAINID")
        private int domainID;
        @SerializedName("DESCRIPTION")
        private String description;
        @SerializedName("TYPE")
        private String type;
        @SerializedName("STATUS")
        private int status;
        @SerializedName("SOA_EMAIL")
        private String soaEmail;
        @SerializedName("DOMAIN")
        private String domain;
        @SerializedName("RETRY_SEC")
        private int retrySeconds;
        @SerializedName("MASTER_IPS")
        private String masterIPs;
        @SerializedName("EXPIRE_SEC")
        private int expireSeconds;
        @SerializedName("REFRESH_SEC")
        private int refreshSeconds;
        @SerializedName("TTL_SEC")
        private int ttlSeconds;

        public int getDomainID() {
            return domainID;
        }

        @Nullable
        public String getDescription() {
            return description;
        }

        @Nullable
        public String getType() {
            return type;
        }

        public int getStatus() {
            return status;
        }

        @Nullable
        public String getSOAEmail() {
            return soaEmail;
        }

        @Nullable
        public String getDomain() {
            return domain;
        }

        public int getRetrySeconds() {
            return retrySeconds;
        }

        @Nullable
        public String getMasterIPs() {
            return masterIPs;
        }

        public int getExpireSeconds() {
            return expireSeconds;
        }

        public int getRefreshSeconds() {
            return refreshSeconds;
        }

        public int getTTLSeconds() {
            return ttlSeconds;
        }

        @Nonnull
        @Override
        public String toString() {
            return API.toString(this);
        }
    }

    @SerializedName("DATA")
    private Domain[] data;

    @Nonnull
    public List<Domain> getDomains() {
        return ImmutableList.copyOf(this.data);
    }

    @Nonnull
    public Optional<Domain> getByDomainName(@Nonnull String domainName) {
        for (Domain domain : this.data) {
            if (domainName.equals(domain.getDomain())) {
                return Optional.of(domain);
            }
        }
        return Optional.empty();
    }
}
