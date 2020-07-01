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

let index = 0;
const jobs = [
  {
    "title": "UI Development Intern @ Accenture",
    "dates": "Sept. 2017 - Aug. 2018",
    "summary": [
      "Used ReactJS to build a web app that assists with scheduling meetings and greets meeting attendees by name through Amazon Alexa and Microsoft’s FaceID API",
      "Gained experience in NodeJS, Amazon Web Services, Git, and building Amazon Alexa Skills",
      "Built UI for a web app intended for Accenture clients to interact with during Accenture presentations",
      "Aided team working on a new computer aided dispatch system for the New York City Fire Department by doing research on technologies and writing code for proofs of concept",
      "Assisted in ideation and creation of innovative demos at Accenture Liquid Studio, which rapidly develops demos and software"
    ]
  },
  {
    "title": "STEP Intern @ Google",
    "dates": "Summer 2019",
    "summary": [
      "Wrote SQL scripts to query datasets to collect and analyze data on bandwidth usage in Google data centers",
      "Analyzed bandwidth usage data using Python to create a new guideline for bandwidth provisioning in Google data centers with the goal of optimizing resource utilization while minimizing cost",
      "Wrote design document for project methodology",
      "Committed code to Google’s codebase, which involved having my code reviewed by Google engineers and writing tests"
    ]
  },
  {
    "title": "STEP Intern @ Google",
    "dates": "Summer 2020",
    "summary": [
      "Will complete 4 starter projects and a capstone project utilizing Google APIs"
    ]
  }
];
const jobCount = jobs.length;

/**
 * Set innerHTML text in work experience section
 */
function setContent() {
  const job = document.getElementById('job');
  const date = document.getElementById('date');
  const summary = document.getElementById('summary');
  const carouselContent = document.getElementById('carousel-content');

  carouselContent.style.opacity = 0;

  /* Use setTimeout to make opacity change from 0 to 1
  after 600ms as a transition */
  setTimeout(function() {
    job.innerHTML = jobs[index].title;
    date.innerHTML = jobs[index].dates;
    summary.innerHTML = '<ul>' + jobs[index].summary.map(function(point) {
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
  if (index === jobCount) index = 0;
  setContent();
}

/**
 * Decrement index or set to 2 if reached beginning of jobs array
 */
function jobsBack() {
  index = index - 1;
  if (index === -1) index = 2;
  setContent();
}

/**
 * Change accent color to selected color
 */
function changeAccentColor(selectedButton) {
  const selectedIcon = selectedButton.firstElementChild;
  const id = document.getElementById(selectedIcon.id);
  const newAccentColor = getComputedStyle(id).color;
  document.documentElement.style.setProperty('--accent-color', newAccentColor);
}

/**
 * Show/hide the back to top button depending
 * on how far user has scrolled from the top
 */
document.addEventListener('DOMContentLoaded', function() {
  const backToTopBtn = document.getElementById('back-to-top-btn');

  function toggleBackToTopBtn() {
    if (window.scrollY > 300) {
      backToTopBtn.style.opacity = '.5';
    }
    else {
      backToTopBtn.style.opacity = '0';
    }
  }

  window.addEventListener('scroll', toggleBackToTopBtn);
});

/**
 * Fade in animation for intro section.
 */
function fadeInIntro() {
  const intro = document.getElementById('intro');
  intro.style.opacity = '1';
}

/**
 * Fetch comments from server and add to DOM.
 */
function getComments() {
  const maxComments = document.getElementById('max-comments').value;
  if (maxComments === null) maxComments = 5; /* Show at most 5 comments by default. */

  fetch(`/comments?max-comments=${maxComments}`)
  .then(response => response.json())
  .then((response) => {
    const comments = response.comments;
    const commentsContainer = document.getElementById('comments-container');
    while (commentsContainer.firstChild) { /* Remove old comments */
      commentsContainer.removeChild(commentsContainer.firstChild);
    }
    comments.forEach((comment) => {
      commentsContainer.appendChild(createCommentElement(comment));
    });
  });
}

/**
 * Create element that represents a comment.
 */
function createCommentElement(comment) {
  const commentElement = document.createElement('div');

  const nameElement = document.createElement('div');
  nameElement.innerText = comment.name;

  const textElement = document.createElement('div');
  textElement.innerText = comment.text;

  const timeElement = document.createElement('div');
  const timestampString = new Date(comment.timestamp).toString();
  timeElement.innerText = timestampString;

  commentElement.appendChild(nameElement);
  commentElement.appendChild(textElement);
  commentElement.appendChild(timeElement);

  return commentElement;
}

/**
 * Delete all comments from datastore.
 */
function deleteAllComments() {
  fetch('/delete-data', {
    method: 'POST',
  }).then(getComments());
}
