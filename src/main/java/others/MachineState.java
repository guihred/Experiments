package others;

import static com.google.common.collect.ImmutableMap.of;

import java.util.Collection;
import java.util.Map;
import utils.SupplierEx;

public enum MachineState {
    ERROR(of()),
	CLOSED(null),
    LAST_ACK(of("RCV_ACK", MachineState.CLOSED)),
    CLOSE_WAIT(of("APP_CLOSE", LAST_ACK)),
	TIME_WAIT(of("APP_TIMEOUT", CLOSED)),
    CLOSING(of("RCV_ACK", TIME_WAIT)),
    FIN_WAIT_2(of("RCV_FIN", TIME_WAIT)),
    FIN_WAIT_1(of("RCV_FIN", CLOSING, "RCV_FIN_ACK", TIME_WAIT, "RCV_ACK", FIN_WAIT_2)),
    ESTABLISHED(of("APP_CLOSE", FIN_WAIT_1, "RCV_FIN", CLOSE_WAIT)),
    SYN_RCVD(of("APP_CLOSE", MachineState.FIN_WAIT_1, "RCV_ACK", ESTABLISHED)),
    SYN_SENT(of("RCV_SYN", SYN_RCVD, "RCV_SYN_ACK", ESTABLISHED, "APP_CLOSE", CLOSED)),
    LISTEN(of("RCV_SYN", MachineState.SYN_RCVD, "APP_SEND", SYN_SENT, "APP_CLOSE", MachineState.CLOSED));
    private Map<String, MachineState> map;

	MachineState(Map<String, MachineState> map) {
		this.map = map;
	}

	public Map<String, MachineState> getMap() {
        if (map == null && this == CLOSED) {
            map = of("APP_PASSIVE_OPEN", MachineState.LISTEN, "APP_ACTIVE_OPEN", MachineState.SYN_SENT);
        }
		return map;
	}

    public static MachineState getStateMachine(MachineState estado, Collection<String> eventList) {
        return eventList.stream().sequential().reduce(estado, (e, s) -> e.getMap().getOrDefault(s, ERROR),
            SupplierEx::nonNull);
    }

}