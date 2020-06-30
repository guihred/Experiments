package others;

import static com.google.common.collect.ImmutableMap.of;
import static others.Values.*;

import java.util.Collection;
import java.util.Map;
import utils.SupplierEx;

public enum MachineState {
    ERROR(of()),
	CLOSED(null),
    LAST_ACK(of(RCV_ACK, CLOSED)),
    CLOSE_WAIT(of(APP_CLOSE, LAST_ACK)),
	TIME_WAIT(of(APP_TIMEOUT, CLOSED)),
    CLOSING(of(RCV_ACK, TIME_WAIT)),
    FIN_WAIT_2(of(RCV_FIN, TIME_WAIT)),
    FIN_WAIT_1(of(RCV_FIN, CLOSING, RCV_FIN_ACK, TIME_WAIT, RCV_ACK, FIN_WAIT_2)),
    ESTABLISHED(of(APP_CLOSE, FIN_WAIT_1, RCV_FIN, CLOSE_WAIT)),
    SYN_RCVD(of(APP_CLOSE, FIN_WAIT_1, RCV_ACK, ESTABLISHED)),
    SYN_SENT(of(RCV_SYN, SYN_RCVD, RCV_SYN_ACK, ESTABLISHED, APP_CLOSE, CLOSED)),
    LISTEN(of(RCV_SYN, SYN_RCVD, APP_SEND, SYN_SENT, APP_CLOSE, CLOSED));
    private Map<String, MachineState> map;

	MachineState(Map<String, MachineState> map) {
		this.map = map;
	}

	public Map<String, MachineState> getMap() {
        if (map == null && this == CLOSED) {
            map = of(APP_PASSIVE_OPEN, LISTEN, APP_ACTIVE_OPEN, SYN_SENT);
        }
		return map;
	}

    public static MachineState getStateMachine(MachineState estado, Collection<String> eventList) {
        return eventList.stream().sequential().reduce(estado, (e, s) -> e.getMap().getOrDefault(s, ERROR),
            SupplierEx::nonNull);
    }

}

final class Values {
    static final String APP_ACTIVE_OPEN = "APP_ACTIVE_OPEN";
    static final String APP_CLOSE = "APP_CLOSE";
    static final String APP_TIMEOUT = "APP_TIMEOUT";
    static final String RCV_ACK = "RCV_ACK";
    static final String RCV_FIN = "RCV_FIN";
    static final String RCV_FIN_ACK = "RCV_FIN_ACK";
    static final String APP_SEND = "APP_SEND";
    static final String RCV_SYN = "RCV_SYN";
    static final String RCV_SYN_ACK = "RCV_SYN_ACK";
    static final String APP_PASSIVE_OPEN = "APP_PASSIVE_OPEN";
    private Values() {
    }
}