## Running and Editing Backend
+ Our backend team uses inteliJ
    - make sure that the `src` folder is th inteliJ project source
    - run `mvn package` to build the `war` file
    - deploy to tomcat
+ If you do not run inteliJ and **do** run linux
    - run `sh run_mvn.sh` to package the progeck and move it to the tomcat folder
    - only works on linux & tomcat8
    - careful: if you have a different file path for tomcat this **will not** work

Some example images of the frontend:

Login page
![Login page](/Pictures/login.png)

Main page
![Main page](/Pictures/main.png)

Statistics page
![Statistics page](/Pictures/stats.png)
