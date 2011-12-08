package com.ning.atlas.spi.protocols;

import com.ning.atlas.spi.space.Space;

import java.util.concurrent.TimeUnit;

public class EC2SecurityGroup
{
    public static void publishAvailability(String name, Space space) {
        space.store(AWS.ID.createChild("ec2-security-group", "availability"), name, name);
    }

    public static void waitForAvailabilityOf(String name, Space space, long time, TimeUnit unit) {
        long give_up_at = unit.toMillis(time) + System.currentTimeMillis();
        while (give_up_at > System.currentTimeMillis()) {
            if (space.get(AWS.ID.createChild("ec2-security-group", "availablity"), name).isKnown()) {
                return;
            }
            else {
                try {
                    Thread.sleep(100);
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("interrupted", e);
                }
            }
        }
    }
}
