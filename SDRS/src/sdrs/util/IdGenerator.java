package sdrs.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class IdGenerator implements Serializable {

    private static final long serialVersionUID = 1L;
    private final Map<String, Integer> counters = new HashMap<String, Integer>();

    public String next(String prefix) {
        int value = 1;
        Integer current = counters.get(prefix);
        if (current != null) {
            value = current.intValue() + 1;
        }
        counters.put(prefix, Integer.valueOf(value));
        return prefix + "-" + value;
    }

    public Map<String, Integer> getCounters() {
        return counters;
    }

    public void setCounters(Map<String, Integer> counters) {
        this.counters.clear();
        this.counters.putAll(counters);
    }
}
