# Runner Assist Server

### Welcome to the backend module of the runner assist application

### For documentation please refrence our main repository [Runner Assist Dashboard](https://git.snt.utwente.nl/kindadysfunctional/runner-assist-dashboard)
#### Documentation Refrence:
+ [Wiki](https://git.snt.utwente.nl/kindadysfunctional/runner-assist-dashboard/wikis/home)
+ [Readme](https://git.snt.utwente.nl/kindadysfunctional/runner-assist-dashboard/blob/master/README.md)

## Running and Editing Backend
+ Our backend team uses inteliJ
    - make sure that the `src` folder is th inteliJ project source
    - run `mvn package` to build the `war` file
    - deploy to tomcat
+ If you do not run inteliJ and **do** run linux
    - run `sh run_mvn.sh` to package the progeck and move it to the tomcat folder
    - only works on linux & tomcat8
    - careful: if you have a different file path for tomcat this **will not** work