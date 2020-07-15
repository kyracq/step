// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public final class FindMeetingQuery {

  // Add a time range to Collection validRanges
  private void addRange(int validRangeStart, int validRangeEnd,
      boolean inclusive, Collection<TimeRange> validRanges, long duration) {
    TimeRange validRange = TimeRange.fromStartEnd(validRangeStart, validRangeEnd, inclusive);
    if (validRange.duration() >= duration) {
      validRanges.add(validRange);
    }
  }

  // Return time ranges that don't conflict with invalidRanges
  private Collection<TimeRange> getValidRanges(
      ArrayList<TimeRange> invalidRanges, long duration) {
    Collection<TimeRange> validRanges = new ArrayList<TimeRange>();
    int invalidRangeStart;
    int minInvalidRangeEnd;
    int validRangeStart;
    int validRangeEnd;

    // If first event doesn't start at start of day, add time range beginning
    // at start of day
    int firstInvalidRangeStart = invalidRanges.get(0).start();
    if (firstInvalidRangeStart != TimeRange.START_OF_DAY) {
      addRange(TimeRange.START_OF_DAY, firstInvalidRangeStart, false, validRanges, duration);
    }

    invalidRangeStart = invalidRanges.get(0).start();
    minInvalidRangeEnd = invalidRanges.get(0).end();
    // Look for open time slots between invalid ranges
    for (int i = 1; i < invalidRanges.size(); i++) {
      TimeRange currInvalidRange = invalidRanges.get(i);
      if (currInvalidRange.start() > minInvalidRangeEnd) {
        // We've found an open time slot
        addRange(minInvalidRangeEnd, currInvalidRange.start(), false, validRanges, duration);
        invalidRangeStart = currInvalidRange.start();
        minInvalidRangeEnd = currInvalidRange.end();
      } else {
        // We definitely won't find a valid range until at least the end of curr invalid range
        minInvalidRangeEnd = Math.max(currInvalidRange.end(), minInvalidRangeEnd);
      }
    }

    // Add last block of day to validRanges (if long enough)
    if (minInvalidRangeEnd < TimeRange.END_OF_DAY) {
      addRange(minInvalidRangeEnd, TimeRange.END_OF_DAY, true, validRanges, duration);
    }

    return validRanges;
  }

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<TimeRange> validRanges = new ArrayList<TimeRange>();
    Collection<String> attendees = request.getAttendees();
    Collection<String> optionalAttendees = request.getOptionalAttendees();
    long duration = request.getDuration();

    // Not possible to have an event longer than a full day
    if (duration > TimeRange.WHOLE_DAY.duration()) {
      return validRanges;
    }

    if (attendees.isEmpty()) {
      if (optionalAttendees.isEmpty()) {
        // If no one's attending, there are no conflicts
        validRanges.add(TimeRange.WHOLE_DAY);
        return validRanges; 
      } else { // All attendees optional
        attendees = optionalAttendees;
        optionalAttendees = attendees; // empty set
      }
    }

    ArrayList<TimeRange> invalidRanges = new ArrayList<TimeRange>();
    ArrayList<TimeRange> optionalInvalidRanges = new ArrayList<TimeRange>();
    for (Event event : events) {
      // If there are attendees in common between the request and this event
      if (!Collections.disjoint(attendees, event.getAttendees())) {
        // Requested meeting can't be at same time as this event
        invalidRanges.add(event.getWhen());
      }
      if (!Collections.disjoint(optionalAttendees, event.getAttendees())) {
        optionalInvalidRanges.add(event.getWhen());
      }
    }

    if (invalidRanges.isEmpty()) {
      if (optionalInvalidRanges.isEmpty()) {
        validRanges.add(TimeRange.WHOLE_DAY);
        return validRanges;
      } else {
        invalidRanges = optionalInvalidRanges;
        optionalInvalidRanges = invalidRanges; // empty
      }
    }

    Collections.sort(invalidRanges, TimeRange.ORDER_BY_START);
    validRanges = getValidRanges(invalidRanges, duration);

    invalidRanges.addAll(optionalInvalidRanges);    
    Collections.sort(invalidRanges, TimeRange.ORDER_BY_START);
    Collection<TimeRange> optionalValidRanges = getValidRanges(invalidRanges, duration);

    // If all valid ranges had conflicts with optional attendees,
    // don't consider optional attendees
    if (optionalValidRanges.isEmpty()) return validRanges;
    return optionalValidRanges;
  }
}
