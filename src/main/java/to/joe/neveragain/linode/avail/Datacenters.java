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
package to.joe.neveragain.linode.avail;

import com.google.common.collect.ImmutableList;
import com.google.gson.annotations.SerializedName;
import to.joe.neveragain.linode.API;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

public class Datacenters extends API.Response {
    public class Datacenter {
        @SerializedName("DATACENTERID")
        private int datacenterID;
        @SerializedName("LOCATION")
        private String location;
        @SerializedName("ABBR")
        private String abbr;

        @Nonnull
        public int getDatacenterID() {
            return this.datacenterID;
        }

        @Nonnull
        public String getLocation() {
            return this.location;
        }

        @Nonnull
        public String getAbbreviation() {
            return this.abbr;
        }

        @Nonnull
        @Override
        public String toString() {
            return API.toString(this);
        }
    }

    @SerializedName("DATA")
    private Datacenter[] data;

    @Nonnull
    public List<Datacenter> getDatacenters() {
        return ImmutableList.copyOf(this.data);
    }

    @Nonnull
    public Optional<Datacenter> getDatacenterByAbbreviation(@Nonnull String abbreviation) {
        for (Datacenter datacenter : this.data) {
            if (abbreviation.equals(datacenter.getAbbreviation())) {
                return Optional.of(datacenter);
            }
        }
        return Optional.empty();
    }
}
