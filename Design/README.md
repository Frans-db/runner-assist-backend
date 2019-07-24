# Group 06 TRIMM Project

## Things we couldn't get done in time

Our backend is almost completely implemented. Functions exist for virtually every feature we planned to implement. However, due to one of our main frontend developers having to travel to America for personal circumstances, we were not able to completely hook the frontend up to the backend. As such, some of the following may not work in your testing, however they WILL be complete in time for the final presentation.
 - While the data is received in the frontend to display charts, it isn't properly passed into the charts component and as such causes an error when trying to view a chart.
 - Some user data isn't persistent across reloads. As such, if you reload, you will have to sign in again.
 - Clicking on an element on the calendar may not direct you to the correct detailed run page.
 - At the moment you are able to upload GPS and run data, however we don't currently display a polyline over the map to show your path for the run, which is what we have planned.
 - We have planned a premium feature for displaying feedback to runners based off of a fatigue value we calculate in the backend per runner based on their performance. However, this is not yet implemented.
 - Our account settings pages are functional, however currently we don't update the user state on the frontend when a change is made, so for it to reflect you have to log out and log back in. 

## Where to preview our app

Our app is currently hosted on my AWS server, which you can find here: [trimm.farooq.xyz](https://trimm.farooq.xyz/)

***The user that has data assigned to it:***
 - Email: `email@email.com`
 - Password: `Password1!`

***PLEASE NOTE***: To use the frontend, you need to download an extension to enable CORS. We couldn't find a solution in time
to solve this permanently, but for now, 
 - on Firefox, you can use [this extension](https://addons.mozilla.org/en-US/firefox/addon/access-control-allow-origin/)
 - on Chrome, you can use [this extension](https://chrome.google.com/webstore/detail/allow-cors-access-control/lhobafahddgcelffkeicbaginigeejlf?hl=en)

Please be sure to uninstall/disable the extension when you are done, as keeping it enabled may cause you issues loading on other websites, aside from the huge security risk!

The API is also hosted on the same server, which you can find at [tapi.farooq.xyz/app/api](https://tai.farooq.xyz/app/api)

We would have loved to host this on the University's farm servers, however we found out much too late that we could not 
run our API over HTTPS on the farm servers, so Klaas advised us to just run the API and backend using our own solution.

## Self hosting the frontend and backend
If you would like to set up the frontend and backend yourself, please see the respective README's in their own repositories.

## Technical accomplishments we're proud of
For the frontend the technical part we are most proud of implementing material design, using react instead of JSP and being able to automatically generate functions based on the index the backend gives us when first loading the dashboard login page.

For the backend, the security implementation is a point of pride mainly the ability to properly convert the already generated RSA key pair into the KeyPair object and properly signing the JWTs and app_key. The wrapper function is also very important and something we worked very hard to get running and convert the existing servlets to.

For the database the complex queries for generating timestamps on TRIMMs data since it they were not there.