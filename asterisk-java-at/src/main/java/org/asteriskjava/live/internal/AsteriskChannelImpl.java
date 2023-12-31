/*
 *  Copyright 2004-2006 Stefan Reuter
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.asteriskjava.live.internal;

import org.asteriskjava.live.*;
import org.asteriskjava.lock.LockableList;
import org.asteriskjava.lock.LockableMap;
import org.asteriskjava.lock.Locker.LockCloser;
import org.asteriskjava.manager.action.*;
import org.asteriskjava.manager.response.ManagerError;
import org.asteriskjava.manager.response.ManagerResponse;
import org.asteriskjava.util.MixMonitorDirection;

import java.util.*;

/**
 * Default implementation of the AsteriskChannel interface.
 *
 * @author srt
 * @version $Id$
 */
class AsteriskChannelImpl extends AbstractLiveObject implements AsteriskChannel {
    private static final String CAUSE_VARIABLE_NAME = "PRI_CAUSE";
    /**
     * Date this channel has been created.
     */
    private final Date dateOfCreation;
    private final LockableList<ExtensionHistoryEntry> extensionHistory;
    private final LockableList<ChannelStateHistoryEntry> stateHistory;
    private final LockableList<LinkedChannelHistoryEntry> linkedChannelHistory;
    private final LockableList<DialedChannelHistoryEntry> dialedChannelHistory;
    private final LockableList<AsteriskChannel> dialedChannels;
    private final LockableList<AsteriskChannel> dialingChannels;
    /**
     * If this channel is bridged to another channel, the linkedChannels
     * contains the channel this channel is bridged with.
     */
    private final LockableList<AsteriskChannel> linkedChannels;
    private final LockableMap<String, String> variables;
    /**
     * Unique id of this channel.
     */
    private String id;
    /**
     * The traceId is used to trace originated channels.
     */
    private String traceId;
    /**
     * Date this channel has left the Asterisk server.
     */
    private Date dateOfRemoval;
    /**
     * Name of this channel.
     */
    private String name;
    /**
     * Caller*ID of this channel.
     */
    private CallerId callerId;
    /**
     * State of this channel.
     */
    private ChannelState state;
    /**
     * Account code used to bill this channel.
     */
    private String account;
    /**
     * Indicates if this channel was linked to another channel at least once.
     */
    private boolean wasLinked;
    private HangupCause hangupCause;
    private String hangupCauseText;
    private CallDetailRecordImpl callDetailRecord;
    /**
     * MeetMe room user associated with this channel if any, <code>null</code>
     * otherwise.
     */
    private MeetMeUserImpl meetMeUserImpl;
    /**
     * Queue entry associated with this channel if any, <code>null</code>
     * otherwise.
     */
    private AsteriskQueueEntryImpl queueEntryImpl;
    /**
     * Extension where the call is parked if it is parked, <code>null</code>
     * otherwise.
     */
    private Extension parkedAt;
    /**
     * Parkinglot where the call is parked if it is parked, <code>null</code>
     * otherwise.
     */
    private String parkingLot;
    /**
     * Last dtmf digit recieved on this channel if any, <code>null</code>
     * otherwise.
     */
    private Character dtmfReceived;
    /**
     * Last dtmf digit sent on this channel if any, <code>null</code> otherwise.
     */
    private Character dtmfSent;
    /**
     * Actual monitor state
     */
    private boolean isMonitored;

    /**
     * Creates a new Channel.
     *
     * @param server         server this channel belongs to.
     * @param name           name of this channel, for example "SIP/1310-20da".
     * @param id             unique id of this channel, for example "1099015093.165".
     * @param dateOfCreation date this channel has been created.
     * @throws IllegalArgumentException if any of the parameters are null.
     */
    AsteriskChannelImpl(final AsteriskServerImpl server, final String name, final String id, final Date dateOfCreation)
            throws IllegalArgumentException {
        super(server);

        if (server == null) {
            throw new IllegalArgumentException("Parameter 'server' passed to AsteriskChannelImpl() must not be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter 'name' passed to AsteriskChannelImpl() must not be null.");
        }
        if (id == null) {
            throw new IllegalArgumentException("Parameter 'id' passed to AsteriskChannelImpl() must not be null.");
        }
        if (dateOfCreation == null) {
            throw new IllegalArgumentException(
                    "Parameter 'dateOfCreation' passed to AsteriskChannelImpl() must not be null.");
        }

        this.name = name;
        this.id = id;
        this.dateOfCreation = dateOfCreation;
        this.extensionHistory = new LockableList<>(new ArrayList<>());
        this.stateHistory = new LockableList<>(new ArrayList<>());
        this.linkedChannelHistory = new LockableList<>(new ArrayList<>());
        this.dialedChannelHistory = new LockableList<>(new ArrayList<>());
        this.variables = new LockableMap<>(new HashMap<>());
        this.dialedChannels = new LockableList<>(new ArrayList<>());
        this.dialingChannels = new LockableList<>(new ArrayList<>());
        this.linkedChannels = new LockableList<>(new ArrayList<>());
    }

    public String getId() {
        return id;
    }

    /**
     * Changes the id of this channel.
     *
     * @param date date of the name change.
     * @param id   the new unique id of this channel.
     */
    void idChanged(Date date, String id) {
        final String oldId = this.id;

        if (oldId != null && oldId.equals(id)) {
            return;
        }

        this.id = id;
        firePropertyChange(PROPERTY_ID, oldId, id);
    }

    String getTraceId() {
        return traceId;
    }

    void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getName() {
        return name;
    }

    /**
     * Changes the name of this channel.
     *
     * @param date date of the name change.
     * @param name the new name of this channel.
     */
    void nameChanged(Date date, String name) {
        final String oldName = this.name;

        if (oldName != null && oldName.equals(name)) {
            return;
        }

        this.name = name;
        firePropertyChange(PROPERTY_NAME, oldName, name);
    }

    public CallerId getCallerId() {
        return callerId;
    }

    /**
     * Sets the caller id of this channel.
     *
     * @param callerId the caller id of this channel.
     */
    void setCallerId(final CallerId callerId) {
        final CallerId oldCallerId = this.callerId;

        this.callerId = callerId;
        firePropertyChange(PROPERTY_CALLER_ID, oldCallerId, callerId);
    }

    public ChannelState getState() {
        return state;
    }

    public boolean wasInState(ChannelState state) {
        try (LockCloser closer = stateHistory.withLock()) {
            for (ChannelStateHistoryEntry historyEntry : stateHistory) {
                if (historyEntry.getState() == state) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean wasBusy() {
        return wasInState(ChannelState.BUSY) || hangupCause == HangupCause.AST_CAUSE_BUSY
                || hangupCause == HangupCause.AST_CAUSE_USER_BUSY;
    }

    /**
     * Changes the state of this channel.
     *
     * @param date  when the state change occurred.
     * @param state the new state of this channel.
     */
    void stateChanged(Date date, ChannelState state) {
        try (LockCloser closer = this.withLock()) {
            final ChannelStateHistoryEntry historyEntry;
            final ChannelState oldState = this.state;

            if (oldState == state) {
                return;
            }

            // System.err.println(id + " state change: " + oldState + " => " +
            // state
            // + " (" + name + ")");
            historyEntry = new ChannelStateHistoryEntry(date, state);
            try (LockCloser closer2 = stateHistory.withLock()) {
                stateHistory.add(historyEntry);
            }

            this.state = state;
            firePropertyChange(PROPERTY_STATE, oldState, state);
        }
    }

    public String getAccount() {
        return account;
    }

    /**
     * Sets the account code used to bill this channel.
     *
     * @param account the account code used to bill this channel.
     */
    void setAccount(String account) {
        final String oldAccount = this.account;

        this.account = account;
        firePropertyChange(PROPERTY_ACCOUNT, oldAccount, account);
    }

    public Extension getCurrentExtension() {
        final Extension extension;

        try (LockCloser closer = extensionHistory.withLock()) {
            if (extensionHistory.isEmpty()) {
                extension = null;
            } else {
                extension = extensionHistory.get(extensionHistory.size() - 1).getExtension();
            }
        }

        return extension;
    }

    public Extension getFirstExtension() {
        final Extension extension;

        try (LockCloser closer = extensionHistory.withLock()) {
            if (extensionHistory.isEmpty()) {
                extension = null;
            } else {
                extension = extensionHistory.get(0).getExtension();
            }
        }

        return extension;
    }

    public List<ExtensionHistoryEntry> getExtensionHistory() {
        final List<ExtensionHistoryEntry> copy;

        try (LockCloser closer = extensionHistory.withLock()) {
            copy = new ArrayList<>(extensionHistory);
        }

        return copy;
    }

    /**
     * Adds a visted dialplan entry to the history.
     *
     * @param date      the date the extension has been visited.
     * @param extension the visted dialplan entry to add.
     */
    void extensionVisited(Date date, Extension extension) {
        final Extension oldCurrentExtension = getCurrentExtension();
        final ExtensionHistoryEntry historyEntry;

        historyEntry = new ExtensionHistoryEntry(date, extension);

        try (LockCloser closer = extensionHistory.withLock()) {
            extensionHistory.add(historyEntry);
        }

        firePropertyChange(PROPERTY_CURRENT_EXTENSION, oldCurrentExtension, extension);
    }

    public Date getDateOfCreation() {
        return dateOfCreation;
    }

    public Date getDateOfRemoval() {
        return dateOfRemoval;
    }

    public HangupCause getHangupCause() {
        return hangupCause;
    }

    public String getHangupCauseText() {
        return hangupCauseText;
    }

    public CallDetailRecord getCallDetailRecord() {
        return callDetailRecord;
    }

    void callDetailRecordReceived(Date date, CallDetailRecordImpl callDetailRecord) {
        final CallDetailRecordImpl oldCallDetailRecord = this.callDetailRecord;

        this.callDetailRecord = callDetailRecord;
        firePropertyChange(PROPERTY_CALL_DETAIL_RECORD, oldCallDetailRecord, callDetailRecord);
    }

    /**
     * Sets dateOfRemoval, hangupCause and hangupCauseText and changes state to
     * {@link ChannelState#HUNGUP}. Fires a PropertyChangeEvent for state.
     *
     * @param dateOfRemoval   date the channel was hung up
     * @param hangupCause     cause for hangup
     * @param hangupCauseText textual representation of hangup cause
     */
    void hungup(Date dateOfRemoval, HangupCause hangupCause, String hangupCauseText) {
        try (LockCloser closer = this.withLock()) {
            this.dateOfRemoval = dateOfRemoval;
            this.hangupCause = hangupCause;
            this.hangupCauseText = hangupCauseText;
            // update state and fire PropertyChangeEvent
            stateChanged(dateOfRemoval, ChannelState.HUNGUP);
        }
    }

    /**
     * Retrives the conplete List of all dialed channels associated to ths calls
     *
     * @return List of all dialed channels
     */
    public List<AsteriskChannel> getDialedChannels() {
        final List<AsteriskChannel> copy;

        try (LockCloser closer = dialedChannels.withLock()) {
            copy = new ArrayList<>(dialedChannels);
        }

        return copy;
    }

    /* dialed channels */

    public AsteriskChannel getDialedChannel() {
        try (LockCloser closer = dialedChannels.withLock()) {
            for (AsteriskChannel channel : dialedChannels) {
                if (channel != null)
                    return channel;
            }
        }
        return null;
    }

    public List<DialedChannelHistoryEntry> getDialedChannelHistory() {
        final List<DialedChannelHistoryEntry> copy;

        try (LockCloser closer = dialedChannelHistory.withLock()) {
            copy = new ArrayList<>(dialedChannelHistory);
        }

        return copy;
    }

    void channelDialed(Date date, AsteriskChannel dialedChannel) {
        try (LockCloser closer = this.withLock()) {
            final AsteriskChannel oldDialedChannel;
            try (LockCloser closer2 = dialedChannels.withLock()) {
                if (dialedChannels.isEmpty())
                    oldDialedChannel = null;
                else
                    oldDialedChannel = dialedChannels.get(dialedChannels.size() - 1);
                dialedChannels.add(dialedChannel);
            }
            final DialedChannelHistoryEntry historyEntry;

            historyEntry = new DialedChannelHistoryEntry(date, dialedChannel);
            try (LockCloser closer3 = dialedChannelHistory.withLock()) {
                dialedChannelHistory.add(historyEntry);
            }

            firePropertyChange(PROPERTY_DIALED_CHANNEL, oldDialedChannel, dialedChannel);
        }
    }

    /* dialed channels */

    public AsteriskChannel getDialingChannel() {
        try (LockCloser closer = dialingChannels.withLock()) {
            if (dialingChannels.isEmpty())
                return null;
            return dialingChannels.get(0);
        }
    }

    void channelDialing(Date date, AsteriskChannel dialingChannel) {
        try (LockCloser closer = this.withLock()) {
            final AsteriskChannel oldDialingChannel;
            try (LockCloser closer2 = this.dialingChannels.withLock()) {
                if (this.dialingChannels.isEmpty()) {
                    oldDialingChannel = null;
                    this.dialingChannels.add(dialingChannel);
                } else {
                    oldDialingChannel = this.dialingChannels.get(0);
                    this.dialingChannels.set(0, dialingChannel);
                }
            }

            firePropertyChange(PROPERTY_DIALING_CHANNEL, oldDialingChannel, dialingChannel);
        }
    }

    /* linked channels */

    public AsteriskChannel getLinkedChannel() {
        try (LockCloser closer = linkedChannels.withLock()) {
            if (linkedChannels.isEmpty())
                return null;
            return linkedChannels.get(0);
        }
    }

    public List<LinkedChannelHistoryEntry> getLinkedChannelHistory() {
        final List<LinkedChannelHistoryEntry> copy;

        try (LockCloser closer = linkedChannelHistory.withLock()) {
            copy = new ArrayList<>(linkedChannelHistory);
        }

        return copy;
    }

    public boolean wasLinked() {
        return wasLinked;
    }

    /**
     * Sets the channel this channel is bridged with.
     *
     * @param date          the date this channel was linked.
     * @param linkedChannel the channel this channel is bridged with.
     */
    void channelLinked(Date date, AsteriskChannel linkedChannel) {
        try (LockCloser closer = this.withLock()) {
            final AsteriskChannel oldLinkedChannel;
            try (LockCloser closer2 = this.linkedChannels.withLock()) {
                if (this.linkedChannels.isEmpty()) {
                    oldLinkedChannel = null;
                    this.linkedChannels.add(linkedChannel);
                } else {
                    oldLinkedChannel = this.linkedChannels.get(0);
                    this.linkedChannels.set(0, linkedChannel);
                }
            }

            final LinkedChannelHistoryEntry historyEntry;

            historyEntry = new LinkedChannelHistoryEntry(date, linkedChannel);
            try (LockCloser closer3 = linkedChannelHistory.withLock()) {
                linkedChannelHistory.add(historyEntry);
            }
            this.wasLinked = true;
            firePropertyChange(PROPERTY_LINKED_CHANNEL, oldLinkedChannel, linkedChannel);
        }
    }

    void channelUnlinked(Date date) {
        try (LockCloser closer = this.withLock()) {
            final AsteriskChannel oldLinkedChannel;

            try (LockCloser closer2 = this.linkedChannels.withLock()) {
                if (this.linkedChannels.isEmpty()) {
                    oldLinkedChannel = null;
                } else {
                    oldLinkedChannel = this.linkedChannels.get(0);
                }
                linkedChannels.clear();
            }

            final LinkedChannelHistoryEntry historyEntry;

            try (LockCloser closer3 = linkedChannelHistory.withLock()) {
                if (linkedChannelHistory.isEmpty()) {
                    historyEntry = null;
                } else {
                    historyEntry = linkedChannelHistory.get(linkedChannelHistory.size() - 1);
                }
            }

            if (historyEntry != null) {
                historyEntry.setDateUnlinked(date);
            }

            firePropertyChange(PROPERTY_LINKED_CHANNEL, oldLinkedChannel, null);
        }
    }

    /* MeetMe user */

    public MeetMeUserImpl getMeetMeUser() {
        return meetMeUserImpl;
    }

    void setMeetMeUserImpl(MeetMeUserImpl meetMeUserImpl) {
        final MeetMeUserImpl oldMeetMeUserImpl = this.meetMeUserImpl;
        this.meetMeUserImpl = meetMeUserImpl;
        firePropertyChange(PROPERTY_MEET_ME_USER, oldMeetMeUserImpl, meetMeUserImpl);
    }

    // action methods

    public void hangup() throws ManagerCommunicationException, NoSuchChannelException {
        hangup(null);
    }

    public void hangup(HangupCause cause) throws ManagerCommunicationException, NoSuchChannelException {
        final HangupAction action;
        final ManagerResponse response;

        if (cause != null) {
            setVariable(CAUSE_VARIABLE_NAME, Integer.toString(cause.getCode()));
            action = new HangupAction(name, cause.getCode());
        } else {
            action = new HangupAction(name);
        }

        response = server.sendAction(action);
        if (response instanceof ManagerError) {
            throw new NoSuchChannelException("Channel '" + name + "' is not available: " + response.getMessage());
        }
    }

    public void setAbsoluteTimeout(int seconds) throws ManagerCommunicationException, NoSuchChannelException {
        ManagerResponse response;

        response = server.sendAction(new AbsoluteTimeoutAction(name, seconds));
        if (response instanceof ManagerError) {
            throw new NoSuchChannelException("Channel '" + name + "' is not available: " + response.getMessage());
        }
    }

    public void redirect(String context, String exten, int priority)
            throws ManagerCommunicationException, NoSuchChannelException {
        ManagerResponse response;

        response = server.sendAction(new RedirectAction(name, context, exten, priority));
        if (response instanceof ManagerError) {
            throw new NoSuchChannelException("Channel '" + name + "' is not available: " + response.getMessage());
        }
    }

    public void redirectBothLegs(String context, String exten, int priority)
            throws ManagerCommunicationException, NoSuchChannelException {
        ManagerResponse response;

        try (LockCloser closer = linkedChannels.withLock()) {
            if (linkedChannels.isEmpty()) {
                response = server.sendAction(new RedirectAction(name, context, exten, priority));
            } else {
                response = server.sendAction(new RedirectAction(name, linkedChannels.get(0).getName(), context, exten,
                        priority, context, exten, priority));
            }
        }

        if (response instanceof ManagerError) {
            throw new NoSuchChannelException("Channel '" + name + "' is not available: " + response.getMessage());
        }
    }

    public String getVariable(String variable) throws ManagerCommunicationException, NoSuchChannelException {
        ManagerResponse response;
        String value;

        try (LockCloser closer = variables.withLock()) {

            value = variables.get(variable);
            if (value != null) {
                return value;
            }

            response = server.sendAction(new GetVarAction(name, variable));
            if (response instanceof ManagerError) {
                throw new NoSuchChannelException("Channel '" + name + "' is not available: " + response.getMessage());
            }
            value = response.getAttribute("Value");
            if (value == null) {
                value = response.getAttribute(variable); // for Asterisk 1.0.x
            }

            variables.put(variable, value);
        }
        return value;
    }

    public void setVariable(String variable, String value) throws ManagerCommunicationException, NoSuchChannelException {
        ManagerResponse response;

        response = server.sendAction(new SetVarAction(name, variable, value));
        if (response instanceof ManagerError) {
            throw new NoSuchChannelException("Channel '" + name + "' is not available: " + response.getMessage());
        }
        try (LockCloser closer = variables.withLock()) {
            variables.put(variable, value);
        }
    }

    public void playDtmf(String digit) throws ManagerCommunicationException, NoSuchChannelException, IllegalArgumentException {
        ManagerResponse response;

        if (digit == null) {
            throw new IllegalArgumentException("DTMF digit to send must not be null");
        }

        response = server.sendAction(new PlayDtmfAction(name, digit));
        if (response instanceof ManagerError) {
            throw new NoSuchChannelException("Channel '" + name + "' is not available: " + response.getMessage());
        }
    }

    public void startMonitoring(String filename) throws ManagerCommunicationException, NoSuchChannelException {
        startMonitoring(filename, null, false);
    }

    public void startMonitoring(String filename, String format) throws ManagerCommunicationException, NoSuchChannelException {
        startMonitoring(filename, format, false);
    }

    public void startMonitoring(String filename, String format, boolean mix)
            throws ManagerCommunicationException, NoSuchChannelException {
        ManagerResponse response;

        response = server.sendAction(new MonitorAction(name, filename, format, mix));
        if (response instanceof ManagerError) {
            throw new NoSuchChannelException("Channel '" + name + "' is not available: " + response.getMessage());
        }
    }

    public void changeMonitoring(String filename)
            throws ManagerCommunicationException, NoSuchChannelException, IllegalArgumentException {
        ManagerResponse response;

        if (filename == null) {
            throw new IllegalArgumentException("New filename must not be null");
        }

        response = server.sendAction(new ChangeMonitorAction(name, filename));
        if (response instanceof ManagerError) {
            throw new NoSuchChannelException("Channel '" + name + "' is not available: " + response.getMessage());
        }
    }

    public void stopMonitoring() throws ManagerCommunicationException, NoSuchChannelException {
        ManagerResponse response;

        response = server.sendAction(new StopMonitorAction(name));
        if (response instanceof ManagerError) {
            throw new NoSuchChannelException("Channel '" + name + "' is not available: " + response.getMessage());
        }
    }

    public void pauseMonitoring() throws ManagerCommunicationException, NoSuchChannelException {
        ManagerResponse response;

        response = server.sendAction(new PauseMonitorAction(name));
        if (response instanceof ManagerError) {
            throw new NoSuchChannelException("Channel '" + name + "' is not available: " + response.getMessage());
        }
    }

    public void unpauseMonitoring() throws ManagerCommunicationException, NoSuchChannelException {
        ManagerResponse response;

        response = server.sendAction(new UnpauseMonitorAction(name));
        if (response instanceof ManagerError) {
            throw new NoSuchChannelException("Channel '" + name + "' is not available: " + response.getMessage());
        }
    }

    public void pauseMixMonitor(MixMonitorDirection direction)
            throws ManagerCommunicationException, NoSuchChannelException, RecordingException {
        ManagerResponse response;
        response = server.sendAction(new PauseMixMonitorAction(this.name, 1, direction.getStateName()));
        if (response instanceof ManagerError) {
            if (response.getMessage().equals("Cannot set mute flag")) {
                throw new RecordingException(response.getMessage() + " on channel: '" + name);
            }
            throw new NoSuchChannelException("Channel '" + name + "' is not available: " + response.getMessage());
        }
    }

    public void unPauseMixMonitor(MixMonitorDirection direction)
            throws ManagerCommunicationException, NoSuchChannelException, RecordingException {
        ManagerResponse response;
        response = server.sendAction(new PauseMixMonitorAction(this.name, 0, direction.getStateName()));
        if (response instanceof ManagerError) {
            if (response.getMessage().equals("Cannot set mute flag")) {
                throw new RecordingException(response.getMessage() + " on channel: '" + name);
            }
            throw new NoSuchChannelException("Channel '" + name + "' is not available: " + response.getMessage());
        }
    }

    public Extension getParkedAt() {
        // warning: the context of this extension will be null until we get the
        // context property from
        // the parked call event!
        return parkedAt;
    }

    public String getParkingLot() {
        return this.parkingLot;
    }

    void setParkedAt(Extension parkedAt, String parkingLot) {
        final Extension oldParkedAt = this.parkedAt;
        final String oldParkingLot = this.parkingLot;

        this.parkedAt = parkedAt;
        this.parkingLot = parkingLot;
        firePropertyChange(PROPERTY_PARKED_AT, oldParkedAt, parkedAt);
        firePropertyChange(PROPERTY_PARKING_LOT, oldParkingLot, parkingLot);
    }

    void updateVariable(String name, String value) {
        try (LockCloser closer = variables.withLock()) {
            // final String oldValue = variables.get(name);
            variables.put(name, value);
            // TODO add notification for updated channel variables
        }
    }

    public Map<String, String> getVariables() {
        try (LockCloser closer = variables.withLock()) {
            return new HashMap<>(variables);
        }
    }

    public Character getDtmfReceived() {
        return this.dtmfReceived;
    }

    public Character getDtmfSent() {
        return this.dtmfSent;
    }

    void dtmfReceived(Character digit) {
        final Character oldDtmfReceived = this.dtmfReceived;

        this.dtmfReceived = digit;
        firePropertyChange(PROPERTY_DTMF_RECEIVED, oldDtmfReceived, digit);
    }

    void dtmfSent(Character digit) {
        final Character oldDtmfSent = this.dtmfSent;

        this.dtmfSent = digit;
        firePropertyChange(PROPERTY_DTMF_SENT, oldDtmfSent, digit);
    }

    public AsteriskQueueEntryImpl getQueueEntry() {
        return queueEntryImpl;
    }

    void setQueueEntry(AsteriskQueueEntryImpl queueEntry) {
        final AsteriskQueueEntry oldQueueEntry = this.queueEntryImpl;

        this.queueEntryImpl = queueEntry;
        firePropertyChange(PROPERTY_QUEUE_ENTRY, oldQueueEntry, queueEntry);
    }

    public boolean isMonitored() {
        return this.isMonitored;
    }

    void setMonitored(boolean monitored) {
        final boolean oldMonitored = this.isMonitored;

        this.isMonitored = monitored;
        firePropertyChange(PROPERTY_MONITORED, oldMonitored, monitored);
    }

    @Override
    public String toString() {
        final StringBuilder sb;
        final LockableList<AsteriskChannel> dialingChannel;
        final LockableList<AsteriskChannel> linkedChannel;

        sb = new StringBuilder("AsteriskChannel[");

        try (LockCloser closer = this.withLock()) {
            sb.append("id='").append(getId()).append("',");
            sb.append("name='").append(getName()).append("',");
            sb.append("callerId='").append(getCallerId()).append("',");
            sb.append("state='").append(getState()).append("',");
            sb.append("account='").append(getAccount()).append("',");
            sb.append("dateOfCreation=").append(getDateOfCreation()).append(",");
            dialingChannel = this.dialingChannels;
            linkedChannel = this.linkedChannels;
        }
        if (dialedChannels.isEmpty()) {
            sb.append("dialedChannel=null,");
        } else {
            sb.append("dialedChannel=AsteriskChannel[");
            try (LockCloser closer = dialedChannels.withLock()) {
                for (AsteriskChannel dialedChannel : dialedChannels) {
                    sb.append("[id='").append(dialedChannel.getId()).append("',");
                    sb.append("name='").append(dialedChannel.getName()).append("'],");
                }
                sb.append("],");
            }
        }
        if (dialingChannel.isEmpty()) {
            sb.append("dialingChannel=null,");
        } else {
            sb.append("dialingChannel=AsteriskChannel[");
            sb.append("id='").append(dialingChannel.get(0).getId()).append("',");
            sb.append("name='").append(dialingChannel.get(0).getName()).append("'],");
        }
        try (LockCloser closer = linkedChannel.withLock()) {
            if (linkedChannel.isEmpty()) {
                sb.append("linkedChannel=null");
            } else {
                sb.append("linkedChannel=AsteriskChannel[");
                {
                    sb.append("id='").append(linkedChannel.get(0).getId()).append("',");
                    sb.append("name='").append(linkedChannel.get(0).getName()).append("']");
                }
            }
        }
        sb.append("]");

        return sb.toString();
    }
}
