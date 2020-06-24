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

var index = 0;
const jobCount = 3;

/**
 * Set innerHTML text in work experience section
 */
function setContent() {
  const jobs = ['UI Development Intern @ Accenture', 'STEP Intern @ Google', 'STEP Intern @ Google'];
  const dates = ['Sept. 2017 - Aug. 2018', 'Summer 2019', 'Summer 2020'];
  const bulletPoints = [
    [
      'Used ReactJS to build a web app that assists with scheduling meetings and greets meeting attendees by name through Amazon Alexa and Microsoft’s FaceID API',
      'Gained experience in NodeJS, Amazon Web Services, Git, and building Amazon Alexa Skills',
      'Built UI for a web app intended for Accenture clients to interact with during Accenture presentations',
      'Aided team working on a new computer aided dispatch system for the New York City Fire Department by doing research on technologies and writing code for proofs of concept',
      'Assisted in ideation and creation of innovative demos at Accenture Liquid Studio, which rapidly develops demos and software'
    ],
    [
      'Wrote SQL scripts to query datasets to collect and analyze data on bandwidth usage in Google data centers',
      'Analyzed bandwidth usage data using Python to create a new guideline for bandwidth provisioning in Google data centers with the goal of optimizing resource utilization while minimizing cost',
      'Wrote design document for project methodology',
      'Committed code to Google’s codebase, which involved having my code reviewed by Google engineers and writing tests'
    ],
    [
      'Will complete 4 starter projects and a capstone project utilizing Google APIs'
    ]
  ];

  const job = document.getElementById('job');
  const date = document.getElementById('date');
  const summary = document.getElementById('summary');
  const carouselContent = document.getElementById('carousel-content');

  carouselContent.style.opacity = 0;

  /* Use setTimeout to make opacity change from 0 to 1
  after 600ms as a transition */ 
  setTimeout(function() {
    job.innerHTML = jobs[index];
    date.innerHTML = dates[index];
    summary.innerHTML = '<ul>' + bulletPoints[index].map(function(point) {
      return '<li>' + point + '</li>';
    }).join('') + '</ul>';
  }, 600);

  setTimeout(function() {
    carouselContent.style.opacity = 1;
  }, 600);
}

/**
 * Increment index or set to zero if reached end of jobs array
 */
function jobsNext() {
  index = index + 1;
  if (index === jobCount) {
    index = 0;
  }

  setContent();
}

/**
 * Decrement index or set to 2 if reached beginning of jobs array
 */
function jobsBack() {
  index = index - 1;
  if (index === -1) {
    index = 2;
  }

  setContent();
}

