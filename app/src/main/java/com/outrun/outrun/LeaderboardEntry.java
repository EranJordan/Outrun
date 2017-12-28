package com.outrun.outrun;

import android.support.annotation.NonNull;

import java.util.concurrent.TimeUnit;

public class LeaderboardEntry {
    public int time;
    public String name; //name will be derived from current user's Uid at time of submission

        public LeaderboardEntry() {
            time = 0;
            name = null;
        }
        
    public String timeToString() {
        long millis = this.time;
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(64);
        sb.append(hours);
        sb.append("h:");
        sb.append(minutes);
        sb.append("m:");
        sb.append(seconds);
        sb.append("s");
        return(sb.toString());
    }
}
