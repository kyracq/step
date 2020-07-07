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
  if (maxComments === null) maxComments = 5; /* Show at most 5 comments */

  fetch(`/comments?max-comments=${maxComments}`)
    .then(response => response.json().then(data =>
      ({ data: data, success: response.ok })))
    .then((response) => {
      const commentsContainer = document.getElementById('comments-container');
      while (commentsContainer.firstChild) { /* Remove old comments */
        commentsContainer.removeChild(commentsContainer.firstChild);
      }
      if (response.success === true) {
        const comments = response.data.comments;
        if (comments != null) {
          comments.forEach((comment) => {
            const commentHtml = getCommentHtml(comment);
            const commentElement = document.createElement('div');
            commentElement.innerHTML = commentHtml;
            commentsContainer.appendChild(commentElement);
          });
        }
      }
      else {
        commentsContainer.innerHTML = response.errorMessage;
      }
    });
}

/**
 * Format timestamp for display in comment section.
 */
function parseTimestamp(timestamp) {
  const date = new Date(timestamp);
  return date.toLocaleString();
}

/**
 * Get name of icon that represents sentiment score.
 */
function getIcon(sentimentScore) {
  /* Conditions are ordered. */
  if (sentimentScore >= 0.5) return 'sentiment_very_satisfied';
  if (sentimentScore >= 0) return 'sentiment_satisfied';
  if (sentimentScore >= -0.5) return 'sentiment_dissatisfied';
  return 'sentiment_very_dissatisfied';
}

/**
 * Create element that represents a comment.
 */
function getCommentHtml(comment) {
  const tooltipUrl = 'https://cloud.google.com/natural-language/docs/basics' +
      '#interpreting_sentiment_analysis_values';

  const html =
      `<li>
        <div class="comment">
          <div>
            <div class="comment-name">${comment.name}</div>
            <div class="comment-text">${comment.text}</div>
            <div class="comment-time">${parseTimestamp(comment.timestamp)}</div>
          </div>
          <div class="sentiment-icon">
            <span class="material-icons"
            title="Sentiment score: ${comment.sentimentScore.toFixed(2)}">
            ${getIcon(comment.sentimentScore)}
            </span>
            <a href="${tooltipUrl}" target="_blank" class="more-info">?</a>
          </div>
        </div>
      </li>`;

  return html;
}

/**
 * Delete all comments from datastore.
 */
function deleteAllComments() {
  if (confirm('Are you sure you want to delete all comments, ' +
    'including those from other users?')) {
    fetch('/delete-data', {
      method: 'POST',
    }).then(getComments());
  }
}

function getUserInfo() {
  fetch('/login')
    .then(response => response.json())
    .then((response) => {
      const loginStatusElement = document.getElementById('login-status');
      const commentForm = document.getElementById('comment-form');
      const commentMessage = document.getElementById('comment-msg');

      if (response.loggedIn) {
        loginStatusElement.innerHTML =
          `<div>Hello ${response.nickname}!</p>
          <a href=${response.logoutUrl}>Log out</a>`;
          commentForm.style.display = 'block';
          commentMessage.innerHTML = '';
      }
      else {
        loginStatusElement.innerHTML =
          `<a href=${response.loginUrl}>Log in</a>`;
        commentMessage.innerHTML = 'Click "Login" in the ' +
            'top right to get the option to leave a comment.<br><br>';
        
      }
    });
}

function setUserNickname() {
  /* Check if user has already set a nickname */
  fetch('/login')
    .then(response => response.json())
    .then((response) => {
      if (response.nickname != "") {
        window.location.replace("/");
      }
      /* If no nickname set, prompt for a nickname and save to datastore */
      else {
        let nickname = prompt('Enter a nickname to be displayed when you ' +
          'leave a comment.');
        /* TODO: don't allow user to cancel nickname input */
        if(nickname == null) nickname = 'Anonymous';
        fetch(`/nickname?nickname=${nickname}`, {
          method: 'POST',
        }).then(window.location.replace("/"));
      }
    });
}
