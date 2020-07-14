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

  // Add a time range to Collection results
  private void addRangeToResults(int validRangeStart, int validRangeEnd,
      boolean inclusive, Collection<TimeRange> results, long duration) {
    TimeRange validRange = TimeRange.fromStartEnd(validRangeStart, validRangeEnd, inclusive);
    if (validRange.duration() >= duration) {
      results.add(validRange);
    }
  }

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<TimeRange> results = new ArrayList<TimeRange>();
    Collection<String> attendees = request.getAttendees();
    long duration = request.getDuration();

    // Not possible to have an event longer than a full day
    if (duration > TimeRange.WHOLE_DAY.duration()) {
      return results;
    }

    // If no one's attending, there are no conflicts
    if (attendees.isEmpty()) {
      results.add(TimeRange.WHOLE_DAY);
      return results;
    }

    ArrayList<TimeRange> invalidRanges = new ArrayList<TimeRange>();
    for (Event event : events) {
      // If there are attendees in common between the request and this event
      if (!Collections.disjoint(attendees, event.getAttendees())) {
        // Requested meeting can't be at same time as this event
        invalidRanges.add(event.getWhen());
      }
    }

    if (invalidRanges.isEmpty()) {
      results.add(TimeRange.WHOLE_DAY);
      return results;
    }

    Collections.sort(invalidRanges, TimeRange.ORDER_BY_START);
    
    int invalidRangeStart;
    int minInvalidRangeEnd;
    int validRangeStart;
    int validRangeEnd;

    // If first event doesn't start at start of day, add time range beginning
    // at start of day
    int firstInvalidRangeStart = invalidRanges.get(0).start();
    if (firstInvalidRangeStart != TimeRange.START_OF_DAY) {
      addRangeToResults(TimeRange.START_OF_DAY, firstInvalidRangeStart, false, results, duration);
    }

    invalidRangeStart = invalidRanges.get(0).start();
    minInvalidRangeEnd = invalidRanges.get(0).end();
    // Look for open time slots between invalid ranges
    for (int i = 1; i < invalidRanges.size(); i++) {
      TimeRange currInvalidRange = invalidRanges.get(i);
      if (currInvalidRange.start() > minInvalidRangeEnd) {
        // We've found an open time slot
        addRangeToResults(minInvalidRangeEnd, currInvalidRange.start(), false, results, duration);
        invalidRangeStart = currInvalidRange.start();
        minInvalidRangeEnd = currInvalidRange.end();
      } else {
        // We definitely won't find a valid range until at least the end of curr invalid range
        minInvalidRangeEnd = Math.max(currInvalidRange.end(), minInvalidRangeEnd);
      }
    }

    // Add last block of day to results (if long enough)
    if (minInvalidRangeEnd < TimeRange.END_OF_DAY) {
      addRangeToResults(minInvalidRangeEnd, TimeRange.END_OF_DAY, true, results, duration);
    }

    return results;
  }
}
