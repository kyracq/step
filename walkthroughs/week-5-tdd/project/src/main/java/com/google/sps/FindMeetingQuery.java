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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class FindMeetingQuery {
  /**
   * Given the currently booked events (time ranges and attendees) and
   * a meeting request (duration and attendees), return a collection
   * the of time ranges within which the meeting can be booked.
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<TimeRange> results = new ArrayList<TimeRange>();
    Collection<String> attendees = request.getAttendees();
    long duration = request.getDuration();

    if (duration > TimeRange.WHOLE_DAY.duration()) {
      return results;
    }
    if (attendees.isEmpty()) {
      results.add(TimeRange.WHOLE_DAY);
      return results;
    }

    ArrayList<TimeRange> invalidRanges = new ArrayList<TimeRange>();
    for (Event event : events) {
      Set<String> intersection = new HashSet<String>(attendees);
      
      if(!Collections.disjoint(attendees, event.getAttendees())) {
        // Requested meeting can't be at same time as event
        invalidRanges.add(event.getWhen());
      }
    }
    Collections.sort(invalidRanges, TimeRange.ORDER_BY_START);
    int validRangeStart;
    // First time range
    if(invalidRanges.get(0).start() != TimeRange.START_OF_DAY) {
      validRangeStart = TimeRange.START_OF_DAY;
    } else {
      validRangeStart = invalidRanges.get(0).start();
    }
    int validRangeEnd;
    for (TimeRange range : invalidRanges) {
      validRangeEnd = range.start();
      results.add(TimeRange.fromStartEnd(validRangeStart, validRangeEnd, false));
    }
    validRangeEnd = invalidRanges.get(invalidRanges.size() - 1).end(); 
    results.add(TimeRange.fromStartEnd(validRangeEnd, TimeRange.END_OF_DAY, true));
    return results;
  }
}
