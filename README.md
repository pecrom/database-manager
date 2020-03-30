**Description of the task**

  - class called ConnectionManager with 2 database servers configured (you can choose any database server)  
  - use pool for database connections  
  - implement failover access to database  
  - if one database dies the ConnectionManager should automatically switch to second database server  
  - during failover mode checks if master DB server is again ready to use and establish all new connections to master DB server afterwards
  - do not use any open source library for ConnectionManager implementation
  

**Testing of ConnectionManager**

There is a docker compose file, which starts two postgres databases - master and slave.

`cd docker && docker-compose up`

Now the two databases are running. So now it's possible to test the application by running Main class. 
