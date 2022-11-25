# Showcase : Organise and manage your construction and landscaping portfolio
![](https://res.cloudinary.com/whodunya/image/upload/v1646082553/showcase/310-1-3D_View_1_ersrii.jpg)

## Overview

Showcase is a mobile app that allows vendors in the construction and landscaping industy to *showcase* their work.

## Getting Started

After downloading and building the project on Android Studio, it may be necessary to download/update necessary libraries within Gradle (e.g. in build.gradle), ensuring the project is then synced with the Gradle files.
It will also be necessary to set for incorporating Google maps (see here https://developer.android.com/training/maps/index.html), including establishing a Google maps API key (see here https://developers.google.com/maps/documentation/android-api/start#get-key).
Be sure to insert the API key (e.g. MAPS_API_KEY= AIzaSXXXXXXXXXXXXXXXXXXXXXXXX) in your local.properties file.

## Portfolio Creation

Upon starting the app, users can create Portfolios by entering:
- The Portfolio title
- The Portfolio description
- The Portfolio type

The list of Portfolios can be filtered based on different types/categories, including:
- Extensions
- Renovations
- New Builds
- Landscaping
- Commercial

A map can be accessed from the Portfolio list page (map menu icon) to show all of the Projects related to a selected Portfolio theme (also showing the related Portfolio of each displayed Project). The list of Portfolios provides brief details of each, however clicking into individual Portfolios allows updatign of details and access to Projects within the Portfolio.

## Project Creation

Once a Portfolio has been created, users are then able to add Projects to that Porfolio. Initial details collected for each Project include:
- The Project title
- The Project description
- The Project location (latitude and longitude co-ordinanates)*
- The Project completion date
- The Project budget range
- Up to 3 Project images

* When creating or updating a Project within a Portfolio, the location of the Project is set by dragging a marker on a map. These locations are reflected on the map of all Projects, accessed via the Home page.
  Projects within a Portfolio are listed with brief details, however clicking into individual Projects allows display of detailed information, including a display of images related to the Project. Here, Project details can also be edited and updated, including images.

