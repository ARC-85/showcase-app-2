# Showcase : Organise and manage your construction and landscaping portfolio
![](https://res.cloudinary.com/whodunya/image/upload/v1646082553/showcase/310-1-3D_View_1_ersrii.jpg)

## Getting Started

After downloading and building the project on Android Studio, it may be necessary to download/update necessary libraries within Gradle (e.g. in build.gradle), ensuring the project is then synced with the Gradle files.
It will also be necessary to set for incorporating Google maps (see here https://developer.android.com/training/maps/index.html), including establishing a Google maps API key (see here https://developers.google.com/maps/documentation/android-api/start#get-key).
Be sure to insert the API key (e.g. MAPS_API_KEY= AIzaSXXXXXXXXXXXXXXXXXXXXXXX) in your local.properties file.
It will also be necessary to set up a Firebase project, including real-time database, authentication, and cloud-storage. The Firebase project can be connected to the app via Android Studio, and approproriate documentation (e.g. google-services.json file) will need to be updated within the app folder. 

## 1   Overview
Showcase is a mobile app that allows vendors in the construction and landscaping industry to *showcase* their work. It allows users (vendors) to create Portfolios under different defined categories, including: 
- New Builds
- Renovations
- Interiors
- Landscaping
- Commercial
Within a created Portfolio, the user can then create a series of Projects. Each Project captures information on the:
- Name
- Description
- Location
- Date of completion 
- Budget range
There is also an option for the user to upload up to three images for each project. 
Once saved, Projects can be viewed within a Portfolio, or on an interactive map. Using a toggle button in both the Project list and the interactive map, the user can view either their own Projects, or those of all users. Users can also filter lists of Projects based on budget range, or list Portfolios based on type. 
Finally, there is also an option for users to add their own Projects, or those of other users, to a list of Favourites. Their own favourite projects are denoted by a star within the folders, while all Favourites (including other user Projects), can be viewed on a map. 

## 2   Showcase Functionalities
### 2.1	Backend Storage
Firebase is used to manage the backend requirements of the Showcase app, including real-time database data storage for text data and Google cloud storage for images. After creating a Firebase account, a new project (Showcase) was started in Firebase and the console was used to set up the real-time database. This included a statement of rules.
It also included generating an updated google-services.json file for the project. The app was then connected to the DB via the Firebase tools in Android Studio. Similarly, storage was setup in Firebase, including associated rules.
Connection to the Firebase storage was again configured through Firebase tools on Android Studio, with an update in the google-services.json file. New images are stored according to their file name, unless they are profile pictures, in which case they are stored according to their user ID. Each time a new image is stored, a check is performed to determine if the file name already exists. If the file does not exist it is added, but if the file does exist (e.g. it was used previously) then there is no upload and the existing file path is used for displaying the image. In this way, storage duplication of images is avoided. 
 
### 2.2	Splash Page
A splash page has been created to great users when first opening the app.
 
### 2.3	Create Account/Sign-in
In addition to the real-time database and cloud storage, Firebase has also been used to handle authentication of users. Users are able to initially create an account using Google authentication within Firebase, or alternatively, they can also use any email/password combination. 
  
### 2.4	Nav Drawer/MVVM Framework and Navigation
The framework used is model–view–viewmodel (MVVM), with nav drawer menu functionality for navigating to different pages, accessing map, switching themes, or signing out. 
 
For navigation of pages outside the nav drawer (e.g. Project List, Portfolio Details, etc) the top menu includes Home and Reverse buttons, where Home brings the user back to the Portfolio List page, while Revers brings the user back to the previous screen in the defined navigation path (Portfolio List > Portfolio Detail > Project List > New Project/Project Detail).
 
### 2.5	User Profile Pictures
Users signing in with a Google account (via Google authorisation) are automatically assigned a profile picture from their user account. Users are also able to click on their profile feature in the nav drawer and change their profile picture
 
### 2.6	Creating a New Portfolio
Users can create a new Portfolio, including details on features such as:
•	Title
•	Description
•	Type (New Builds, Renovations, Interiors, Landscaping, Commercial)
They can also add a representative image. Once saved, the new Portfolio can be viewed in the Portfolio List (accessible via the nav drawer). 
 
### 2.7	Updating or Deleting a Portfolio
To update a Portfolio, the user clicks on the specific Portfolio in the Portfolio List, which takes them to the Portfolio Detail page. Here, the user can update and save any details (using the disk icon in the top menu), or delete the Portfolio (using the trash icon in the top menu). The Portfolio Details page also allow the user to progress the navigation to the specific Projects within that Portfolio. 
 
### 2.8	Creating a New Project
When the user first navigates to Projects within a Portfolio, the list will include the title of the relevant Portfolio, but be shown to be empty. The user can click the blue Plus Button in the bottom righthand corner to add a new project. 
 
In the New Project view, the user will be able to type the title and description of the Project, and select a budget range from a drop down menu. Co-ordinates and a map will also show the new Project’s location. The app is designed to generate co-ordinates reflecting the last location of the phone, which is achieved using the Location Service API via Google Play Services. To avail of these services, the user must first provide permission to the Showcase app, so an initial dialogue box prompts the user to provide a location. If permission is not provided, the location reverts to a default location (SETU campus in Waterford), otherwise the last location of the device is used (in the case of the Emulator, this is Googleplex in Palo Alto, California). It should be noted that the app stores the permission settings for the device, so permission is only sought once for a device and then persisted for follow-up sign-ins. 

     
To change location, the user can scroll down and click the Set Location button, which takes them to a separate map where they can drag the marker to a desired location. As the marker is dragged, the latitude and longitude co-ordinates are adjusted accordingly. Once the desired location has been achieved, the blue Save Button can be clicked in the bottom righthand corner to return to the New Project page. This is then reflected in the updated map on the New Project page as well. 
  
The Project completion date can be adjusted using the Date Picker (i.e. by scrolling), and images of the Project can be sequentially added. Note that each time an image is added, the option for a new additional image is presented (up to 3 images for now). 

   
Once all the details have been entered and the location has been set, the Save Button (top righthand corner of menu) can be clicked to then show the Project in the Project List. 
 
### 2.9	Updating or Deleting a Project
Clicking into the Project on the Project List takes the user to the Project Details view and allows the user to update details or delete the project altogether, similar to the process for updating/deleting a Portfolio. 
 
### 2.10	Using Swipe to Edit/Delete Projects and Portfolios from Lists
Both Portfolios and Projects can be edited (updated) and deleted by using swipe functions, with a left swipe deleting the specific Portfolio/Project, and a right swipe taking the user to the respective Portfolio/Project Details page. A list can also be refreshed by dragging down on the list. 
     
### 2.11	Toggling Portfolio and Project Lists to Display Other User Lists
In addition to displaying the Portfolios/Projects of the signed-in user, users can click the grey Toggle Button in the top righthand corner of the respective list views to see Portfolios/Projects from other users. 
In the case of Portfolios, if the Toggle Button is switched to show Portfolios from all users, the user is unable to enter or alter the Portfolios (e.g. using swipes for Delete or Update). 

In the case of Projects, if the Toggle Button is switched to show Projects from all users, the user is able to remove another user’s project from the list by swiping left (this only removes the project from the list, not from the DB). The user can also enter another user’s Project by swiping right or clicking on it. However, if the Project does not belong to the logged-in user, an altered view of the Project Detail screen is presented, where the details of the Project are all locked and cannot be altered. In this way, users can still view details of projects from other users, without altering them. 

### 2.12	Projects Map
Google Services has again been used to support the generation of maps showing the locations of various Projects. The Projects Map, which can be accessed via the nav drawer menu, allows users to see all of their projects within the one map. Furthermore, by clicking on the individual markers on the map, details of each Project are shown in the card at the bottom of the screen, including the title, description, and main image for the project, as well as the email of the vendor. 
   
Similar to the Projects List, the user can click the Toggle Button at the top righthand corner to show all projects belonging to other users as well as their own, this changes the sub-title from My Projects to All Projects. Now, by having the email address displayed for different projects, a user could contact another user to make enquiries. In order to see more details about a particular project, the user can click on the marker, and then click on the information card at the bottom of the screen, which will then navigate to the Project Details page for that specific Project. Again, if the Project does not belong to the user, the fields are all locked, however if it does belong to the user, the fields can be varied as normal. 

### 2.13	Adding/Removing Favourites
A key feature of the Showcase app is the ability of users to compile a list of favourite Projects. The Favourites model is kept separate to the Portfolios model in the Firebase DB to allow the user to include Projects other than their own in their list of favourites. For example, it is not possible to include a list of users that have favourited a Project within the Portfolio model, as users are not authorised/authenticated to change Projects/Portfolios that are not their own (as per the rules of the DB during setup).  
 
To add a Project as a favourite, the user clicks on the Project in the Projects List, and within the Project Detail view, clicks the Add To Favourites Button at the bottom of the screen. This then takes the user back to the Project List, but their Project is now including a star to signal it has been favourited. To remove a project from the list of favourites, the user re-enters the Project Details view and clicks the Remove From Favourites Button at the bottom of the screen (not the Add To Favourites Button is no longer visible). 
     
It should be noted that it is also possible for users to favourite other users’ Projects, by using the Toggle Button to show all Projects within the list, navigating to the Project Details of the other user’s Project, and clicking the Add To Favourites Button. They can subsequently click Remove From Favourites to remove another user’s Project from their favourites.
     
It should be noted that initially, the Showcase app was designed for a second set of users to be involved, i.e. the clients of the vendors, and the favourites feature would be better served in a separate set of interfaces directed to face the client. Because of this, there are a couple small “bugs”/limitations with the favourites. For example, in the Project List, the stars denote those Projects that have been favourited by their own users. If a user favourites another user’s Project, this will not be shown in Project List. Also, when a user clicks into a Project that they don’t own and favourite it, when they re-enter the Project Details view for this Project, the Add To Favourites Button will still be visible. In this scenario, to remove the Project from their list of favourites, they must click the Add To Favourites Button and then click the Remove From Favourites Button once it subsequently appears. In the Favourites DB, this will temporaily add a new favourite for that user (i.e. a duplicate), but they model is programmed to remove both the orignial and any duplicates when the Remove From Favourites Button is clicked. 
In order to compensate for the inability for a user to identify their favourites for Projects that don’t belong to them in the Project List, the Favourites Map can be used to display a user’s favourites related to both their own projects, and those of others. 

### 2.14	Favourites Map
Similar to the Projects Map, the Favourites Map can be accessed via the nav drawer menu and the Toggle Button can be used to switch between projects that have been favourited by the user and belong to them or belong to everyone (it does not show projects favourited by other users but not the logged-in user). Once again, users can click the information card at the bottom of the screen to navigate to the Project Details for a specific Project, which also allows them to remove any Projects from their favourites list. 
   
### 2.15	Filtering Functions
To make it easier to identify Portfolios or Projects of interest, filtering function have been provided in both the Portfolio and Project List views, as well as the Projects and Favourites Map views. 
In the top lefthand corners of the Portfolio List, Projects Map, and Favourites Map views, the dropdown menu allows the user to filter by Portfolio/Project type. 
   
Show All or the correct type will show the associated Portfolios/Projects, while nothing appears for non-related Portfolios/Projects. 
   
Alternatively, the Project List allows users to filter based on the budget range for Projects. It should be noted, Projects or Portfolios can be filtered even when the Toggle Button is switched to include all users. 
   
### 2.16	Light/Dark Theme Switching
The theme of the app, in terms of appearance, can be switched from Light Mode to Dark Mode by using the relevant buttons in the nav drawer menu. 
        
It should be noted that although the main display item for the spinner dropdown menus (e.g. Show All) did not change colour (i.e. text didn’t turn white), the contents of the menus did and so did the arrow directing to the dropdown menu. 

### 2.17	About Us and Sign-Out
The final features of the Showcase app displayed on the nav drawer menu include the About Us tab and the Sign-Out button. The About Us button takes the user to a very basic overview of the Showcase app, but serves as a placeholder for a link to a supporting website in the future, the current website listed relates to the Showcase website created in Full Stack Web Development. 
The Sign-Out button signs the user out and brings them to the Sign-In/Register view once again. 
