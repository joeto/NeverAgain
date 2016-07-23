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
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;

public class Plans extends API.Response {
    public class Plan {
        @SerializedName("CORES")
        private int cores;
        @SerializedName("PRICE")
        private double price;
        @SerializedName("RAM")
        private int ram;
        @SerializedName("XFER")
        private int xfer;
        @SerializedName("PLANID")
        private int planid;
        @SerializedName("LABEL")
        private String label;
        @SerializedName("AVAIL")
        private Map<String, Integer> avail;
        @SerializedName("DISK")
        private int disk;
        @SerializedName("HOURLY")
        private double hourly;

        public int getCores() {
            return cores;
        }

        public double getPrice() {
            return price;
        }

        public int getRAM() {
            return ram;
        }

        public int getXfer() {
            return xfer;
        }

        public int getPlanID() {
            return planid;
        }

        @Nonnull
        public String getLabel() {
            return label;
        }

        @Nonnull
        public Map<String, Integer> getAvailableByDatacenter() {
            return avail;
        }

        public int getDisk() {
            return disk;
        }

        public double getHourly() {
            return hourly;
        }

        @Nonnull
        @Override
        public String toString() {
            return API.toString(this);
        }
    }

    @SerializedName("DATA")
    private Plan[] data;

    @Nonnull
    public List<Plan> getPlans() {
        return ImmutableList.copyOf(this.data);
    }

    @Nonnull
    public Optional<Plan> getBestPlanBelowHourlyAtDatacenter(double hourly, int datacenterID) {
        return this.getPlanByPredicate((current, test) ->
                (test.getAvailableByDatacenter().get(String.valueOf(datacenterID)) > 0) &&
                        (test.getHourly() <= hourly) &&
                        (current == null || test.getHourly() > current.getHourly()));
    }

    @Nonnull
    public Optional<Plan> getBestPlanBelowPrice(double price) {
        return this.getPlanByPredicate((current, test) ->
                (test.getPrice() <= price) &&
                        (current == null || test.getPrice() > current.getPrice()));
    }

    @Nonnull
    public Optional<Plan> getBestPlanBelowPriceAtDatacenter(double price, int datacenterID) {
        return this.getPlanByPredicate((current, test) ->
                (test.getAvailableByDatacenter().get(String.valueOf(datacenterID)) > 0) &&
                        (test.getPrice() <= price) &&
                        (current == null || test.getPrice() > current.getPrice()));
    }

    @Nonnull
    public Optional<Plan> getCheapestPlanAtDatacenter(int datacenterID) {
        return this.getPlanByPredicate((current, test) ->
                (test.getAvailableByDatacenter().get(String.valueOf(datacenterID)) > 0) &&
                        (current == null || test.getPrice() < current.getPrice()));
    }

    @Nonnull
    public Optional<Plan> getPlanByPredicate(@Nonnull BiPredicate<Plan, Plan> predicate) {
        Plan current = null;
        for (Plan plan : this.data) {
            if (predicate.test(current, plan)) {
                current = plan;
            }
        }
        return Optional.ofNullable(current);
    }
}
