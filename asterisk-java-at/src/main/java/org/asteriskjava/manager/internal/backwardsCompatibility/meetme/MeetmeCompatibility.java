package org.asteriskjava.manager.internal.backwardsCompatibility.meetme;

import org.asteriskjava.manager.event.*;

/**
 * Backwards compatibility for bridge events. <br>
 * <br>
 * Asterisk 13 uses BridgeCreate, BridgeEnter, BridgeLeave and BridgeDestroy
 * events. <br>
 * <br>
 * Here we track active bridges and simulate BridgeEvent's for them allowing
 * legacy code to still work with BridgeEvent's
 *
 * @author rsutton
 */
public class MeetmeCompatibility {

    public ManagerEvent handleEvent(ManagerEvent event) {
        if (event instanceof ConfbridgeStartEvent) {
        } else if (event instanceof ConfbridgeEndEvent) {
            MeetMeEndEvent endEvent = new MeetMeEndEvent(this);
            endEvent.setDateReceived(event.getDateReceived());
            endEvent.setMeetMe(((ConfbridgeEndEvent) event).getConference());
            return endEvent;
        } else if (event instanceof ConfbridgeJoinEvent) {
            MeetMeJoinEvent joinEvent = new MeetMeJoinEvent(this);
            joinEvent.setCallerIdNum(event.getCallerIdNum());
            joinEvent.setCallerIdName(event.getCallerIdName());
            joinEvent.setUniqueId(((ConfbridgeJoinEvent) event).getUniqueId());
            joinEvent.setChannel(((ConfbridgeJoinEvent) event).getChannel());
            joinEvent.setMeetMe(((ConfbridgeJoinEvent) event).getBridgeName());
            joinEvent.setDateReceived(event.getDateReceived());

            return joinEvent;
        } else if (event instanceof ConfbridgeLeaveEvent) {
            MeetMeLeaveEvent leaveEvent = new MeetMeLeaveEvent(this);
            leaveEvent.setCallerIdNum(event.getCallerIdNum());
            leaveEvent.setCallerIdName(event.getCallerIdName());
            leaveEvent.setUniqueId(((ConfbridgeLeaveEvent) event).getUniqueId());
            leaveEvent.setChannel(((ConfbridgeLeaveEvent) event).getChannel());
            leaveEvent.setMeetMe(((ConfbridgeLeaveEvent) event).getConference());
            leaveEvent.setDateReceived(event.getDateReceived());

            return leaveEvent;
        }
        return null;

    }

}
